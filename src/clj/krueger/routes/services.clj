(ns krueger.routes.services
  (:require
    [krueger.db.core :as db]
    [krueger.db.posts :as posts-db]
    [krueger.routes.services.attachments :as attachments]
    [krueger.routes.services.auth :as auth]
    [krueger.routes.services.comments :as comments]
    [krueger.routes.services.common :as common]
    [krueger.routes.services.posts :as posts]
    [buddy.auth.accessrules :refer [restrict]]
    [buddy.auth :refer [authenticated?]]
    [compojure.api.meta :refer [restructure-param]]
    [compojure.api.sweet :refer :all]
    [compojure.api.upload :refer [TempFileUpload wrap-multipart-params]]
    [ring.util.http-response :refer :all]
    [schema.core :as s]))

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

  (POST "/api/login" req
    :return auth/LoginResponse
    :body-params [email :- s/Str
                  pass :- s/Str]
    :summary "user login handler"
    (auth/login email pass req))

  (POST "/api/register" req
    :return auth/LoginResponse
    :body-params [screnname :- s/Str
                  email :- s/Str
                  pass :- s/Str]
    :summary "user registration handler"
    (auth/register!
      {:screnname screnname
       :email     email
       :pass      pass}
      req)
    (ok {:result :ok}))

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
    :auth-rules authenticated?
    :tags ["private"]

    (POST "/logout" []
      :return auth/LogoutResponse
      :summary "remove the user from the session"
      (auth/logout))

    (GET "/page" []
      :return posts/PostPreviews
      :query-params [category :- String page :- Long]
      :summary "return posts with the given page offset"
      (ok (posts-db/get-post-previews category page)))

    (GET "/post" []
      :return posts/Post
      :query-params [id :- String]
      :summary "return post with the given id"
      (ok (posts-db/get-post-by-slug id)))


    (POST "/post" req
      :return common/Success
      :body-params [post :- posts/PostSubmission]
      :summary "new post submission"
      (do
        (posts-db/save-post!
          (assoc post :author (common/user-id req)))
        (ok {:result :ok})))

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
    (POST "/add-comment" []
      :return common/Success
      :body-params [comment :- comments/CommentSubmission]
      :summary "adds a comment to the post"
      (do
        (posts-db/add-post-comment! comment)
        (ok {:result :ok})))

    ;;attachments
    (POST "/media" []
      :multipart-params [file :- TempFileUpload]
      :middleware [wrap-multipart-params]
      :current-user user
      :summary "handles media upload"
      :return attachments/AttachmentResult
      (attachments/upload-media! {:user-id (:user-id user)} file))

    (GET "/media/:id" []
      :summary "load media attachment from the database matching the filename"
      :path-params [id :- s/Str]
      :current-user user
      (attachments/load-file-data {:user-id (:user-id user)
                                   :name    id}))

    (DELETE "/media/:name" []
      :summary "delete media from the database"
      :path-params [name :- s/Str]
      :current-user user
      :return attachments/AttachmentResult
      (attachments/remove-media! {:user-id (:user-id user)
                                  :name    name}))))
