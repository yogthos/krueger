(ns krueger.routes.services
  (:require
    [krueger.db.core :as db]
    [krueger.db.messages :as messages-db]
    [krueger.db.posts :as posts-db]
    [krueger.routes.services.attachments :as attachments]
    [krueger.routes.services.auth :as auth]
    [krueger.routes.services.common :as common]
    [krueger.routes.services.messages :as messages]
    [krueger.routes.services.posts :as posts]
    [krueger.routes.services.terminology :as terminology]
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
      :parameters {:body {:id           s/Str
                          :email        s/Str
                          :pass         s/Str
                          :pass-confirm s/Str}}
      :responses  {200 {:body s/Any}}
      :handler    (fn [{{params :body} :parameters session :session :as req}]
                    (let [user (auth/register! params req)]
                      (assoc (ok {:result :ok})
                        :session (assoc session :identity user))))}}]

   ["/terminology"
    {:get
     {:summary   "fetch terminology"
      :responses {200 {:body {:terminology terminology/Terminology}}}
      :handler   (fn [_]
                   (ok {:terminology (terminology/terminology)}))}}]
   ["/tags"
    {:get
     {:summary   "returns currently available tags"
      :responses {200 {:body {:tags [posts/Tag]}}}
      :handler   (fn [_]
                   (ok {:tags (posts-db/tags {})}))}}]

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
       :parameters {:body {:id                            s/Str
                           (s/optional-key :pass)         (s/maybe s/Str)
                           (s/optional-key :pass-confirm) (s/maybe s/Str)
                           :admin                         s/Bool
                           :active                        s/Bool}}
       :handler    (fn [{{params :body} :parameters}]
                     (auth/update-user! params))}}]
    ["/tag"
     {:post
      {:summary    "create a new tag"
       :parameters {:body {:label       s/Str
                           :value       s/Str
                           :description s/Str}}
       :responses  {200 {:body {:status                   s/Keyword
                                (s/optional-key :message) s/Str}}}
       :handler    (fn [{{params :body} :parameters}]
                     (try
                       (posts-db/create-tag params)
                       (ok {:status :ok})
                       (catch Exception e
                         {:status  :error
                          :message (.getMessage e)})))}}]]

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
                     (-> (assoc post :author (common/user-id req))
                         (posts-db/save-post!)
                         (ok)))}}]
    ;;todo: add Mastodon style boosts for posts

    ["/up-vote-comment"
     {:post
      {:summary    "up-vote the comment with the given id"
       :parameters {:body {:id s/Num}}
       :responses  {200 {:body common/Success}}
       :handler    (fn [{{{:keys [id]} :body} :parameters :as req}]
                     (do
                       (posts-db/upvote-comment! (common/user-id req) id)
                       (ok {:result :ok})))}}]
    ["/down-vote-comment"
     {:post
      {:summary    "down-vote the post with the given id"
       :parameters {:body {:id s/Num}}
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
                     (-> (assoc comment :author (common/user-id req))
                         (posts-db/add-post-comment!)
                         (ok)))}}]
    ["/message"
     {:post
      {:summary    "send a message to a user"
       :parameters {:body {:message messages/Message}}
       :responses  {200 {:body {:id s/Num}}}
       :handler    (fn [{{{:keys [message]} :body} :parameters :as req}]
                     (-> (assoc message :author (common/user-id req))
                         (messages-db/add-message!)
                         (ok)))}}]
    ["/messages"
     {:get
      {:summary    "retrieve messages for the user"
       :parameters {:query {:all s/Bool
                            :page s/Num}}
       :responses  {200 {:body [messages/Message]}}
       :handler    (fn [{{{:keys [all page]} :query} :parameters :as req}]
                     (ok ((if all messages-db/get-messages messages-db/get-unread-messages)
                           {:recipient (common/user-id req)
                            :limit 100
                            :page page})))}}]
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
