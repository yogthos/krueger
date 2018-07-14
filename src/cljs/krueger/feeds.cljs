(ns krueger.feeds
  (:require [taoensso.sente :as sente]))

(def connection (sente/make-channel-socket! "/ws" {:type :auto}))

(defn state-handler [{:keys [?data]}]
  (println "state changed: " ?data))

(defn handshake-handler [{:keys [?data]}]
  (println "connection established: " ?data))

(defn default-event-handler [ev-msg]
  (println "Unhandled event: " (:event ev-msg)))

(defmulti handle-message :id)


(defn event-msg-handler [& [{:keys [state handshake]
                             :or {state state-handler
                                  handshake handshake-handler}}]]
  (fn [ev-msg]
    (case (:id ev-msg)
      :chsk/handshake (handshake ev-msg)
      :chsk/state (state ev-msg)
      :chsk/recv (handle-message ev-msg)
      (default-event-handler ev-msg))))

(defn send-message [event-id message]
  ((:send-fn connection) [event-id message]))

(def router (atom nil))

(defn stop-router! []
  (when-let [stop-f @router] (stop-f)))

(defn start-router! []
  (stop-router!)
  (reset! router (sente/start-chsk-router!
                   (:ch-recv connection)
                   (event-msg-handler
                     {:state     handshake-handler
                      :handshake state-handler}))))
