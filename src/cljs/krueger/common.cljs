(ns krueger.common
  (:require
    [cljsjs.semantic-ui-react :as ui]
    [re-frame.core :as rf]))

(def chain-links
  [{;; Is the effect in the map?
    :effect-present? (fn [effects] (:http effects))
    ;;  The dispatch set for this effect in the map returned from the event handler
    :get-dispatch    (fn [effects]
                       (get-in effects [:http :success-event]))
    ;; Framework will call this function to insert inferred dispatch to next handler in chain
    :set-dispatch    (fn [effects dispatch]
                       (assoc-in effects [:http :success-event] dispatch))}])

(rf/reg-event-db
  :common/set-error
  (fn [db [_ error]]
    (assoc db :common/error error)))

(rf/reg-sub
  :common/error
  (fn [db _]
    (:common/error db)))

(defn error-modal []
  (when-let [{:keys [error status-text]} @(rf/subscribe [:common/error])]
    [:> ui/Modal
     {:closeOnDimmerClick false
      :open               true
      :size               "tiny"}
     [:> ui/Modal.Header "Error"]
     [:> ui/Modal.Content
      [:> ui/Modal.Description
       "Error: " (or error status-text)]]
     [:> ui/Modal.Actions
      [:> ui/Button
       {:basic   true
        :color   "red"
        :onClick #(rf/dispatch [:common/set-error nil])}
       "OK"]]]))

(defn match-route [route route-name]
  (or (= (-> route :data :name) route-name) nil))