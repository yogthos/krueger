(ns krueger.routing
  (:require
    [kee-frame.api :as api]
    [re-frame.core :as rf]
    [reitit.core :as reitit]
    [clojure.string :as string]
    [goog.events :as events]
    [goog.history.EventType :as HistoryEventType])
  (:import goog.History))

(def routes
  [["/" :home]])

(def router (reitit/router routes))


(defrecord ReititRouter [routes]
  api/Router
  (data->url [_ [route-name path-params]] (str (:path (reitit/match-by-name routes route-name path-params))
                                               (when-some [q (:query-string path-params)] (str "?" q))
                                               (when-some [h (:hash path-params)] (str "#" h))))
  (url->data [_ url] (let [[path+query fragment] (string/split url #"#" 2)
                           [path query] (string/split path+query #"\?" 2)]
                       (some-> (reitit/match-by-path routes path)
                               (assoc :query-string query :hash fragment)))))

(defn match-route [uri]
  (->> (or (not-empty (string/replace uri #"^.*#" "")) "/")
       (reitit/match-by-path router)))

(defn hook-browser-navigation! []
  (doto (History.)
    (events/listen
      HistoryEventType/NAVIGATE
      (fn [event]
        (rf/dispatch [:nav/go (match-route (.-token event))])))
    (.setEnabled true)))

(rf/reg-event-db
  :nav/go
  (fn [db [_ route]]
    (assoc db :route route)))

(rf/reg-event-db
  :navigate-by-route-name
  (fn [db [_ route-name]]
    (let [route (reitit/match-by-name router route-name)]
      #_(println "navigating to:" route-name route)
      ;;TODO hack, need to fix to work properly
      (.assign js/location (:path route))
      (assoc db :route route))))

(rf/reg-sub
  :nav/route
  (fn [db _]
    (-> db :route)))

(rf/reg-sub
  :nav/page
  :<- [:nav/route]
  (fn [route _]
    (-> route :data :name)))