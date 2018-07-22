(ns krueger.routes.services
  (:require
    [krueger.db.core :as db]
    [krueger.db.posts :as posts-db]
    [krueger.routes.services.attachments :as attachments]
    [krueger.routes.services.auth :as auth]
    [krueger.routes.services.common :as common]
    [krueger.routes.services.posts :as posts]
    [muuntaja.middleware :as muuntaja]
    [reitit.ring.coercion :as rrc]
    [reitit.coercion.schema :as schema]
    [reitit.swagger :as swagger]
    [ring.util.http-response :refer :all]
    [ring.middleware.params :as params]
    [schema.core :as s]))

(defn admin? [request]
  (-> request :session :identity :admin))

(defn user? [request]
  (-> request :session :identity))

(defn wrap-restricted [rule handler]
  (fn [request]
    (if (rule request)
      (handler request)
      (unauthorized {:error "unauthorized"}))))

(defn service-routes []
  ["/api"
   {:coercion   schema/coercion
    :middleware [params/wrap-params
                 muuntaja/wrap-format
                 swagger/swagger-feature
                 rrc/coerce-exceptions-middleware
                 rrc/coerce-request-middleware
                 rrc/coerce-response-middleware]
    :swagger    {:id       ::api
                 :info     {:title       "forms"
                            :description "form management API"}
                 :produces #{"application/json"
                             "application/edn"
                             "application/transit+json"}
                 :consumes #{"application/json"
                             "application/edn"
                             "application/transit+json"}}}
   ["/swagger.json"
    {:get {:no-doc  true
           :handler (swagger/create-swagger-handler)}}]

   ;;public
   ["/login"
    {:post
     {:summary    "handles user login"
      :parameters {:body {:email s/Str
                          :pass  s/Str}}
      :responses  {200 {:body s/Any}}
      :handler    (fn [{{{:keys [email pass]} :body} :parameters :as req}]
                    (auth/login email pass req))}}]

   ["/register"
    {:post
     {:summary    "handles new user registration"
      :parameters {:body {:screenname   s/Str
                          :email        s/Str
                          :pass         s/Str
                          :pass-confirm s/Str}}
      :responses  {200 {:body s/Any}}
      :handler    (fn [{{params :body} :parameters :as req}]
                    (ok {:result (auth/register! params req)}))}}]

   ["/tags"
    {:get
     {:summary "fetch aall tags"
      :responses  {200 {:body {:tags [posts/Tag]}}}
      :handler (fn [_]
                 (ok {:tags (posts-db/tags)}))}}]

   ["/page"
    {:get
     {:summary    "return posts with the given page offset"
      :parameters {:query {:category s/Str
                           :page     Long}}
      :responses  {200 {:body posts/PostPreviews}}
      ;;todo add comment count to posts
      :handler    (fn [{{{:keys [category page]} :query} :parameters}]
                    (ok (posts-db/get-post-previews category page)))}}]

   ["/post/:id"
    {:get
     {:summary    "return post with the given id"
      :parameters {:path {:id s/Str}}
      :responses  {200 {:body posts/Post}}
      :handler    (fn [{{{:keys [id]} :path} :parameters}]
                    (ok (posts-db/get-post-by-slug id)))}}]

   ["/media/:id"
    {:get
     {:summary    "load media attachment from the database matching the filename"
      :parameters {:path {:id s/Str}}
      :responses  {200 {:body s/Any}}
      :handler    (fn [{{{:keys [id]} :path} :parameters
                        {:keys [identity]}   :session}]
                    (attachments/load-file-data {:user-id (:user-id identity)
                                                 :name    id}))}}]

   ["/admin"
    {:middleware [(partial wrap-restricted admin?)]}
    ["/user"
     {:put
      {:summary    "update user account"
       :parameters {:body {:user-id                       s/Int
                           :screenname                    s/Str
                           (s/optional-key :pass)         (s/maybe s/Str)
                           (s/optional-key :pass-confirm) (s/maybe s/Str)
                           :admin                         s/Bool
                           :active                        s/Bool}}
       :handler    (fn [{{params :body} :parameters}]
                     (auth/update-user! params))}}]

    ["/tag"
     {:post
      {:summary "create a new tag"
       :parameters {:body {:label s/Str
                           :description s/Str}}
       :responses  {200 {:body {:id s/Keyword}}}
       :handler (fn [{{params :body} :parameters}]
                  (posts-db/create-tag params))}}]]

   ;; private
   ["/restricted"
    {:middleware [(partial wrap-restricted user?)]}
    ["/logout"
     {:post
      {:summary   "remove the user from the session"
       :responses {200 {:body auth/LogoutResponse}}
       :handler   (fn [_]
                    (auth/logout))}}]
    ["/post"
     {:post
      {:summary    "new post submission"
       :parameters {:body {:post posts/PostSubmission}}
       :responses  {200 {:body posts/SubmissionResult}}
       :handler    (fn [{{{:keys [post]} :body} :parameters :as req}]
                     (ok (posts-db/save-post! (assoc post :author (common/user-id req)))))}}]
    ;;todo: add Mastodon style boosts for posts

    ["/up-vote-comment"
     {:post
      {:summary    "up-vote the comment with the given id"
       :parameters {:body {:id s/Str}}
       :responses  {200 {:body common/Success}}
       :handler    (fn [{{{:keys [id]} :body} :parameters :as req}]
                     (do
                       (posts-db/upvote-comment! (common/user-id req) id)
                       (ok {:result :ok})))}}]
    ["/down-vote-comment"
     {:post
      {:summary    "down-vote the post with the given id"
       :parameters {:body {:id s/Str}}
       :responses  {200 {:body common/Success}}
       :handler    (fn [{{{:keys [id]} :body} :parameters :as req}]
                     (do
                       (posts-db/downvote-comment! (common/user-id req) id)
                       (ok {:result :ok})))}}]
    ["/comment"
     {:post
      {:summary    "adds a comment to the post"
       :parameters {:body {:comment posts/CommentSubmission}}
       :responses  {200 {:body posts/SubmissionResult}}
       :handler    (fn [{{{:keys [comment]} :body} :parameters :as req}]
                     (ok (posts-db/add-post-comment! (assoc comment :author (common/user-id req)))))}}]
    ["/media"
     {:post
      {:summary    "add a media attachment"
       :parameters {:body {:file s/Any #_TempFileUpload}}
       :responses  {200 {:body attachments/AttachmentResult}}
       :handler    (fn [{{{:keys [file]} :body} :parameters
                         {:keys [identity]}     :session}]
                     (attachments/upload-media! {:user-id (:user-id identity)} file))}}]
    ["/media/:name"
     {:post
      {:summary    "delete media from the database"
       :parameters {:path {:name s/Str}}
       :responses  {200 {:body attachments/AttachmentResult}}
       :handler    (fn [{{{:keys [name]} :body} :parameters
                         {:keys [identity]}     :session}]
                     (attachments/remove-media! {:user-id (:user-id identity)
                                                 :name    name}))}}]]])
