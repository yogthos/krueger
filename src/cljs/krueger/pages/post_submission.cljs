(ns krueger.pages.post-submission
  (:require
    [cljsjs.semantic-ui-react :as ui]
    [re-frame.core :as rf]
    [kee-frame.core :as kf]
    [krueger.common :refer [match-route]]
    [krueger.components.widgets :as widgets]
    [krueger.terminology :refer [term]]))

(kf/reg-chain
  ::submit-post
  (fn [{db :db} _]
    {:db   (dissoc db ::post)
     :http {:method      :post
            :url         "/api/restricted/post"
            :params      {:post (::post db)}
            :resource-id :submit-post
            :error-event [::post-error]}})
  (fn [{:keys [db]} [{:keys [id]}]]
    {:db       (-> db (dissoc ::post))
     ;;todo figure out for dynamic routes (kf/path-for [:post])
     :dispatch [:nav/by-route-path (str "/post/" id)]}))

(rf/reg-event-db
  ::post-error
  (fn [db [_ error]]
    (assoc db ::post-error (-> error :response :error))))

(rf/reg-sub
  ::post-error
  (fn [db _]
    (::post-error db)))

(rf/reg-sub
  ::post-ready?
  (fn [db _]
    (let [{:keys [url title tags text]} (::post db)]
      (and (not-empty title)
           (not-empty tags)
           (or (not-empty url) (not-empty text))))))

(defn submit-post-page []
  [:> ui/Grid
   {:centered true}
   [:> ui/Grid.Column
    {:width 10}
    [:h2 (term :post/submit)]
    [:> ui/Form
     [:> ui/Form.Field
      [widgets/text-input {:label (term :post/url) :path [::post :url]}]]
     [:> ui/Form.Field
      [widgets/text-input {:label (term :post/title) :path [::post :title]}]]
     [:> ui/Form.Field
      [widgets/tag-dropdown {:path [::post :tags] :options @(rf/subscribe [:tags/list])}]]
     [:> ui/Form.Field
      [widgets/textarea {:label (term :post/text) :path [::post :text]}]]
     [widgets/error-notification ::post-error]
     [:div
      [:> ui/Button
       {:basic   true
        :color   "red"
        :floated "left"
        :onClick #(rf/dispatch [:nav/back])}
       (term :cancel)]
      [widgets/ajax-button
       {:primary     true
        :floated     "right"
        :disabled     (not @(rf/subscribe [::post-ready?]))
        :resource-id :submit-post
        :on-click    #(rf/dispatch [::submit-post])}
       (term :submit)]]]]])

(rf/reg-event-fx
  ::init-submit-post-page
  (fn [{:keys [db]} _]
    (merge
      {:db (dissoc db ::post)}
      (when-not (:auth/user db)
        {:dispatch [:auth/login-modal-shown true]}))))

(kf/reg-controller
  ::post-submission-controller
  {:params (fn [route] (match-route route :submit-post))
   :start  (fn [_] [::init-submit-post-page])})