(ns krueger.routes.services.feeds
  (:require
    [mount.core :refer [defstate]]
    [taoensso.sente :as sente]
    [taoensso.sente.server-adapters.immutant
     :refer [get-sch-adapter]]
    [clojure.tools.logging :as log]
    [krueger.db.posts :as posts-db]
    [krueger.routes.services.common :as common]))

(defstate connection
  :start
  (sente/make-channel-socket!
    (get-sch-adapter)
    {:user-id-fn
     (fn [ring-req] (get-in ring-req [:session :identity]))}))

(defmulti handle-message :post/create)

(defmethod handle-message :post/create [{:keys [client-id ?data]}]
  (try
    (let [response (posts-db/save-post!
                     (assoc ?data :author (common/user-id (:id client-id))))]
      (doseq [uid (:any (:connected-uids connection))]
        ((:send-fn connection) uid [:post/created response])))
    (catch Throwable t
      (log/error t "error creating post")
      ((:send-fn connection) client-id [:post/error "failed to create a post"]))))

(defn stop-router! [stop-fn]
  (when stop-fn (stop-fn)))

(defn start-router! []
  (log/debug "\n\n+++++++ STARTING ROUTER! +++++++\n\n")
  (sente/start-chsk-router! (:ch-recv connection) handle-message))

(defstate router
  :start (start-router!)
  :stop (stop-router! router))

(defn feed-routes []
  ["/ws"
   {:get  (fn [req] ((:ajax-get-or-ws-handshake-fn connection) req))
    :post (fn [req] ((:ajax-post-fn connection) req))}])