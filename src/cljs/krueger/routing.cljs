(ns krueger.routing
  (:require
    [krueger.client-routes :refer [routes]]
    [kee-frame.api :as api]
    [re-frame.core :as rf]
    [reitit.core :as reitit]
    [clojure.string :as string]
    [goog.events :as events]
    [goog.history.EventType :as HistoryEventType])
  (:import goog.History))

(def router (reitit/router routes))

(defrecord ReititRouter [routes]
  api/Router

  (data->url [_ [route-name path-params]]
    (str (:path (reitit/match-by-name routes route-name path-params))
         (when-some [q (:query-string path-params)] (str "?" q))
         (when-some [h (:hash path-params)] (str "#" h))))

  (url->data [_ url]
    (let [[path+query fragment] (string/split url #"#" 2)
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
    (assoc db ::route route)))

(rf/reg-event-fx
  :nav/back
  (fn [_ _]
    (.back (.-history js/window))))

(rf/reg-event-fx
  :nav/forward
  (fn [_ _]
    (.forward (.-history js/window))))

(rf/reg-event-fx
  :nav/by-route-path
  (fn [_ [_ path]]
    (let [route (match-route path)]
      {:navigate-to [(-> route :data :name) (:path-params route)]})))

(rf/reg-event-fx
  :nav/by-route-name
  (fn [_ [_ route-name]]
    (let [route (reitit/match-by-name router route-name)]
      {:navigate-to [(-> route :data :name) (:path-params route)]})))

(rf/reg-sub
  :nav/route
  (fn [db _]
    (-> db ::route)))

(rf/reg-sub
  :nav/page
  :<- [:nav/route]
  (fn [route _]
    (-> route :data :name)))
