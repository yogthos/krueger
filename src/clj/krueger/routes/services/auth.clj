(ns krueger.routes.services.auth
  (:require
    [krueger.config :refer [env]]
    [krueger.db.core :as db]
    [krueger.validation :as v]
    [krueger.routes.services.common :refer [handler]]
    [buddy.hashers :as hashers]
    [mount.core :refer [defstate]]
    [clojure.tools.logging :as log]
    [clojure.set :refer [rename-keys]]
    [ring.util.http-response :refer :all]
    [schema.core :as s])
  (:import java.util.Date))

(def User
  {:user-id                         s/Int
   :screenname                      (s/maybe s/Str)
   :admin                           s/Bool
   :is-active                       s/Bool
   :last-login                      Date
   (s/optional-key :account-name)   (s/maybe s/Str)
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

(defn authenticate [userid pass]
  (when-let [user (db/get-user {:id userid})]
    (when (hashers/check pass (:pass user))
      (dissoc user :pass))))

(handler register! [user]
  (if-let [errors (v/validate-create-user user)]
    (do
      (log/error "error creating user:" errors)
      (bad-request {:error "invalid user"}))
    #_(db/insert-user-with-belongs-to!
      (-> user
          (dissoc :pass-confirm)
          (update-in [:pass] hashers/encrypt)))))

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


(defn login [userid pass {:keys [remote-addr server-name session]}]
  (if-let [user (when-let [user (authenticate userid pass)]
                  (-> user
                      (merge {:member-of    []
                              :account-name userid})))]
    (let [user (-> user
                   (dissoc :pass)
                   (merge
                     {:client-ip      remote-addr
                      :source-address server-name}))]
      (log/info "user:" userid "successfully logged in from" remote-addr server-name)
      (-> {:user user}
          (ok)
          (assoc :session (assoc session :identity user))))
    (do
      (log/info "login failed for" userid remote-addr server-name)
      (unauthorized {:error "The username or password was incorrect."}))))

(handler logout []
  (assoc (ok {:result "ok"}) :session nil))
