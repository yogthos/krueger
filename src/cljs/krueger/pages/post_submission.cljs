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
            :params      {:post (update (::post db) :tags not-empty)}
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
     ;;todo searchable typeahead tags should have lable, description, id (number)
     [:> ui/Form.Field
      [widgets/tag-dropdown {:path [::post :tags] :options @(rf/subscribe [:tags/list])}]
      #_[:> ui/Container {:fluid true}]
      #_[widgets/text-input {:label (term :post/tags) :path [::post :tags]}]]
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