(ns krueger.db.messages
  (:require
    [krueger.db.core :refer [*db*]]
    [conman.core :as conman]
    [krueger.config :refer [env]]
    [mount.core :refer [defstate]]))

(conman/bind-connection *db* "sql/messages.sql")
