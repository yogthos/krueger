(ns krueger.routes.services.auth
  (:require
    [krueger.config :refer [env]]
    [krueger.db.users :as db]
    [krueger.email :as email]
    [krueger.validation :as v]
    [krueger.routes.services.common :refer [handler]]
    [buddy.hashers :as hashers]
    [mount.core :refer [defstate]]
    [clojure.tools.logging :as log]
    [clojure.set :refer [rename-keys]]
    [ring.util.http-response :refer :all]
    [schema.core :as s])
  (:import java.util.Date
           (java.util UUID)))

(def User
  {:screenname                      s/Str
   :email                           s/Str
   :admin                           (s/maybe s/Bool)
   :moderator                       (s/maybe s/Bool)
   :token                           (s/maybe s/Str)
   :bio                             (s/maybe s/Str)
   :active                          (s/maybe s/Bool)
   :last-login                      (s/maybe Date)
   (s/optional-key :client-ip)      s/Str
   (s/optional-key :source-address) s/Str})

(def SearchResponse
  {(s/optional-key :users) [User]
   (s/optional-key :error) s/Str})

(def LoginResponse
  {(s/optional-key :user)  User
   (s/optional-key :error) s/Str})

(def LogoutResponse
  {:result s/Str})

(defmacro timed [exp timeout]
  `(let [start-time# (System/currentTimeMillis)
         result#     (try ~exp (catch Exception e# e#))
         end-time#   (System/currentTimeMillis)
         wait-time#  (- ~timeout (- end-time# start-time#))]
     (when (pos? wait-time#)
       (Thread/sleep wait-time#))
     (if (instance? Exception result#)
       (throw result#)
       result#)))

(defn authenticate [email pass]
  (timed
    (when-let [user (db/get-user {:email email})]
      (when (hashers/check pass (:pass user))
        (dissoc user :pass)))
    500))

(handler register! [user {:keys [remote-addr]}]
  (log/info "registration attempt for" (dissoc user :pass) "from" remote-addr)
  (if-let [errors (v/validate-create-user user)]
    (do
      (log/error "error creating user:" errors)
      (bad-request {:error "invalid user"}))
    (if (:email-registration? env)
      (let [token (.toString (UUID/randomUUID))]
        (email/send-registration (:email user) token)
        (db/create-user!
          (-> user
              (dissoc :pass-confirm)
              (update-in [:pass] hashers/encrypt)
              (assoc :active false
                     :token token))))
      (db/create-user!
        (-> user
            (dissoc :pass-confirm)
            (update-in [:pass] hashers/encrypt)
            (assoc :active true
                   :token nil))))))

(handler update-user! [{:keys [pass] :as user}]
  (if-let [errors (v/validate-update-user user)]
    (do
      (log/error "error updating user:" errors)
      (bad-request {:error "invalid user"}))
    (ok
      {:user nil
       #_(db/update-or-insert-user-with-belongs-to!
           (cond-> user
                   pass (update :pass hashers/encrypt)
                   pass (assoc :update-password? true)))})))


(defn login [email pass {:keys [remote-addr server-name session]}]
  (if-let [user (some-> (authenticate email pass)
                        (dissoc :pass)
                        (merge
                          {:client-ip      remote-addr
                           :source-address server-name}))]
    (do
      (log/info "user:" email "successfully logged in from" remote-addr server-name)
      (-> {:user user}
          (ok)
          (assoc :session (assoc session :identity user))))
    (do
      (log/info "login failed for" email remote-addr server-name)
      (unauthorized {:error "The username or password was incorrect."}))))

(handler logout []
  (assoc (ok {:result "ok"}) :session nil))

#_(db/create-user!
    (-> {:screenname "bob"
         :email      "bob@bob.com"
         :pass       "pass"}
        (dissoc :pass-confirm)
        (update-in [:pass] hashers/encrypt)))

#_(authenticate "bob@bob.com" "pass")
