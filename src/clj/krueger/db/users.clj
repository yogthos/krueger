(ns krueger.db.users
  (:require
    [krueger.db.core :refer [*db*]]
    [clojure.java.jdbc :as jdbc]
    [conman.core :as conman]))

(conman/bind-connection *db* "sql/users.sql")

(defn activate-user! [token]
  (jdbc/with-db-transaction [t-conn *db*]
    (if-let [user (get-user-by-token t-conn {:token token})]
      (do
        (finish-registration t-conn {:token token})
        user)
      (throw (IllegalArgumentException. "invalid registration token")))))
