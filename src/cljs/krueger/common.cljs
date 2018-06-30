(ns krueger.common
  (:require
    [reagent.core :as r]
    [re-frame.core :as rf]
    [clojure.string :as string]))

(rf/reg-event-db
  :common/set-error
  (fn [db [_ error]]
    (assoc db :common/error error)))

(rf/reg-sub
  :common/error
  (fn [db _]
    (:common/error db)))

(defn modal-hiccup
  ([header body footer]
   [modal-hiccup {} header body footer])
  ([{:keys [size]} header body footer]
   [:div.modal.fade
    [:div.modal-dialog {:class (string/join
                                 " "
                                 (filter
                                   not-empty
                                   [(case size
                                      :large "modal-lg"
                                      :small "modal-sm"
                                      nil)]))}
     [:div.modal-content
      [:div.modal-header header]
      [:div.modal-body body]
      [:div.modal-footer footer]]]
    [:div.modal-backdrop.fade.in]]))

(defn show-modal [this]
  (.modal (js/$ (r/dom-node this)) #js {:backdrop "static" :keyboard false}))

(defn remove-backdrop []
  (.removeClass (js/$ "body") "modal-open")
  (.removeAttr (js/$ "body") "style")
  (.removeAttr (js/$ "nav") "style")
  (.remove (js/$ ".modal-backdrop")))

(defn modal [_ _ _]
  (r/create-class
    {:component-did-mount
     (fn [this]
       (show-modal this))
     :component-did-update
     (fn [this]
       (show-modal this))
     :component-will-unmount
     (fn [this]
       (remove-backdrop))
     :reagent-render
     modal-hiccup}))

(defn error-modal []
  (when-let [{:keys [error status-text]} @(rf/subscribe [:common/error])]
    [modal "Error" (or error status-text)
     [:button.btn.btn-danger
      {:on-click #((rf/dispatch [:common/set-error nil]))}
      "OK"]]))