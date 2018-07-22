(ns krueger.terminology
  (:require
    [kee-frame.core :as kf]
    [re-frame.core :as rf]
    [tongue.core :as tongue]))

(kf/reg-chain
  ::fetch-terminology
  (fn [_ _]
    {:http {:method :get
            :url    "/api/terminology"}})
  (fn [{:keys [db]} [_ {:keys [terminology]}]]
    {:db (assoc db ::terminology
                   (update terminology :dictionary tongue/build-translate))}))

(rf/reg-sub
  ::terminology
  (fn [db _]
    (::terminology db)))

(rf/reg-sub
  :dict/translate
  (fn [db _]
    (-> db ::terminology :dictionary)))

(rf/reg-sub
  :dict/value
  :<- [:dict/translate]
  (fn [dictionary [_ k]]
    (when dictionary
      (dictionary (or js/locale :en) k))))

(defn term [k]
  @(rf/subscribe [:dict/value k]))

;;tags
(rf/reg-sub
  :tags/list
  (fn [db _]
    (-> db ::terminology :tags)))

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
  ::terminology-controller
  {:params (fn [_]
             (empty? @(rf/subscribe [::terminology])))
   :start  [::fetch-terminology]})