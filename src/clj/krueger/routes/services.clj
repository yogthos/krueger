(ns krueger.routes.services
  (:require
    [buddy.auth.accessrules :refer [restrict]]
    [buddy.auth :refer [authenticated?]]
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

   ["/admin/user"
    {:put
     {:parameters {:body {:user-id      s/Int
                          :screenname   s/Str
                          :pass         (s/maybe s/Str)
                          :pass-confirm (s/maybe s/Str)
                          :admin        s/Bool
                          :belongs-to   [s/Str]
                          :is-active    s/Bool}}
      :responses  {200 auth/LoginResponse}
      :handler    (fn [{{params :body} :parameters}]
                    (auth/update-user! params))}}]

   ;; todo add context
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
      :parameters {:body {:screenname s/Str
                          :email      s/Str
                          :pass       s/Str}}
      :responses  {200 {:body s/Any}}
      :handler    (fn [{{params :body} :parameters :as req}]
                    (ok {:result (auth/register! params req)}))}}]
   ["/page"
    {:get
     {:summary    "return posts with the given page offset"
      :parameters {:query {:category s/Str
                           :page     Long}}
      :responses  {200 {:body s/Any}}
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

   ;; private
   ;; todo add context, auth
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

   ["/up-vote-post"
    {:post
     {:summary    "up-vote the post with the given id"
      :parameters {:body {:id s/Str}}
      :responses  {200 {:body common/Success}}
      :handler    (fn [{{{:keys [id]} :body} :parameters :as req}]
                    (do
                      (posts-db/upvote-post! (common/user-id req) id)
                      (ok {:result :ok})))}}]

   ["/down-vote-post"
    {:post
     {:summary    "down-vote the post with the given id"
      :parameters {:body {:id s/Str}}
      :responses  {200 {:body common/Success}}
      :handler    (fn [{{{:keys [id]} :body} :parameters :as req}]
                    (do
                      (posts-db/downvote-post! (common/user-id req) id)
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
                                                :name    name}))}}]])


(comment


  (defn admin?
    [request]
    (:admin (:identity request)))

  (defn access-error [_ _]
    (unauthorized {:error "unauthorized"}))

  (defn wrap-restricted [handler rule]
    (restrict handler {:handler  rule
                       :on-error access-error}))

  (defmethod restructure-param :auth-rules
    [_ rule acc]
    (update-in acc [:middleware] conj [wrap-restricted rule]))

  (defmethod restructure-param :current-user
    [_ binding acc]
    (update-in acc [:letks] into [binding `(:identity ~'+compojure-api-request+)]))

  (defapi service-routes
          {:swagger {:ui   "/swagger-ui"
                     :spec "/swagger.json"
                     :data {:info {:version     "1.0.0"
                                   :title       "Sample API"
                                   :description "Sample Services"}}}}

          (context "/admin" []
                   :auth-rules admin?
                   :tags ["admin"]

                   (PUT "/user" []
                        :body-params [user-id :- s/Int
                                      screenname :- s/Str
                                      pass :- (s/maybe s/Str)
                                      pass-confirm :- (s/maybe s/Str)
                                      admin :- s/Bool
                                      belongs-to :- [s/Str]
                                      is-active :- s/Bool]
                        :return auth/LoginResponse
                        (auth/update-user! {:user-id      user-id
                                            :screenname   screenname
                                            :belongs-to   belongs-to
                                            :pass         pass
                                            :pass-confirm pass-confirm
                                            :admin        admin
                                            :is-active    is-active})))
          (context "/api" []
                   :tags ["public"]

                   (POST "/login" req
                         :return auth/LoginResponse
                         :body-params [email :- s/Str
                                       pass :- s/Str]
                         :summary "user login handler"
                         (auth/login email pass req))

                   (POST "/register" req
                         :return common/Success
                         :body-params [screenname :- s/Str
                                       email :- s/Str
                                       pass :- s/Str]
                         :summary "user registration handler"
                         ; REVIEW: I'm not sure this gives the correct result if `register!` 500s
                         (ok {:result (auth/register! {:screenname screenname
                                                       :email      email
                                                       :pass       pass}
                                                      req)}))

                   (GET "/page" []
                        :return posts/PostPreviews
                        :query-params [category :- String
                                       page :- Long]
                        :summary "return posts with the given page offset"
                        (ok (posts-db/get-post-previews category page)))

                   (GET "/post" []
                        :return posts/Post
                        :query-params [id :- String]
                        :summary "return post with the given id"
                        (ok (posts-db/get-post-by-slug id)))

                   (GET "/media/:id" []
                        :summary "load media attachment from the database matching the filename"
                        :path-params [id :- s/Str]
                        :current-user user
                        (attachments/load-file-data {:user-id (:user-id user)
                                                     :name    id}))

                   (context "/restricted" []
                            :auth-rules authenticated?
                            :tags ["restricted"]

                            (POST "/logout" []
                                  :return auth/LogoutResponse
                                  :summary "remove the user from the session"
                                  (auth/logout))

                            (POST "/post" req
                                  :return posts/SubmissionResult
                                  :body-params [post :- posts/PostSubmission]
                                  :summary "new post submission"
                                  (ok (posts-db/save-post! (assoc post :author (common/user-id req)))))

                            (POST "/up-vote-post" req
                                  :return common/Success
                                  :body-params [id :- String]
                                  :summary "up-vote the post with the given id"
                                  (do
                                    (posts-db/upvote-post! (common/user-id req) id)
                                    (ok {:result :ok})))

                            (POST "/down-vote-post" req
                                  :return common/Success
                                  :body-params [id :- String]
                                  :summary "down-vote the post with the given id"
                                  (do
                                    (posts-db/downvote-post! (common/user-id req) id)
                                    (ok {:result :ok})))

                            (POST "/add-comment" req
                                  :return posts/SubmissionResult
                                  :body-params [comment :- posts/CommentSubmission]
                                  :summary "adds a comment to the post"
                                  (ok (posts-db/add-post-comment! (assoc comment :author (common/user-id req)))))

                            ;;attachments
                            (POST "/media" []
                                  :multipart-params [file :- TempFileUpload]
                                  :middleware [wrap-multipart-params]
                                  :current-user user
                                  :summary "handles media upload"
                                  :return attachments/AttachmentResult
                                  (attachments/upload-media! {:user-id (:user-id user)} file))

                            (DELETE "/media/:name" []
                                    :summary "delete media from the database"
                                    :path-params [name :- s/Str]
                                    :current-user user
                                    :return attachments/AttachmentResult
                                    (attachments/remove-media! {:user-id (:user-id user)
                                                                :name    name})))))
  )