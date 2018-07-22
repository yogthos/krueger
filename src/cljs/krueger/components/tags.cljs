(ns krueger.components.tags
  (:require
    [krueger.components.widgets :as widgets]
    [cljsjs.semantic-ui-react :as ui]
    [kee-frame.core :as kf]
    [reagent.core :as r]
    [re-frame.core :as rf]))

(kf/reg-chain
  ::fetch-tags
  (fn [_ _]
    {:http {:method :get
            :url    "/api/tags"}})
  (fn [{:keys [db]} [_ {:keys [tags]}]]
    {:db (assoc db ::tags (reduce (fn [m tag] (assoc m (:id tag) tag)) {} tags))}))

(rf/reg-sub
  :tags/list
  (fn [db _]
    (::tags db)))

(rf/reg-sub
  :tag/details
  :<- [:tags/list]
  (fn [tags [_ id]]
    (get tags id)))

(rf/reg-sub
  :tag/label
  :<- [:tags/list]
  (fn [tags [_ id]]
    (get-in tags [id :label])))

(rf/reg-sub
  :tag/description
  :<- [:tags/list]
  (fn [tags [_ id]]
    (get-in tags [id :description])))

(kf/reg-controller
  ::tags-controller
  {:params (fn [route]
             (empty? @(rf/subscribe [:tags/list])))
   :start  [::fetch-tags]})

