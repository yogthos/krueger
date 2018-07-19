(ns krueger.common
  (:require
    [cljsjs.semantic-ui-react :as ui]
    [reagent.core :as r]
    [re-frame.core :as rf]))

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