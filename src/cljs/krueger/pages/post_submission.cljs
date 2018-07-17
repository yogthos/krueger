(ns krueger.pages.post-submission
  (:require
    [cljsjs.semantic-ui-react :as ui]
    [re-frame.core :as rf]
    [kee-frame.core :as kf]
    [krueger.components.widgets :as widgets]))

(kf/reg-chain
  ::submit-post
  (fn [{db :db} _]
    {:db   (dissoc db ::post)
     :http {:method      :post
            :url         "/api/register"
            :params      (::post db)
            :error-event [::post]}})
  (fn [{:keys [db]} [{:keys [id]}]]
    {:db (-> db (dissoc ::post))
     :dispatch [:navigate-by-route-path id]}))

(defn submit-post-page []
  [:> ui/Grid
   {:centered true}
   [:> ui/Grid.Column
    {:width 10}
    [:h2 "Submit a story"]
    [:> ui/Form
     [:> ui/Form.Field
      [widgets/text-input {:label "URL:" :path [::post :url]}]]
     [:> ui/Form.Field
      [widgets/text-input {:label "Title:" :path [::post :title]}]]
     [:> ui/Form.Field
      [widgets/text-input {:label "Tags:" :path [::post :tags]}]]
     [:> ui/Form.Field
      [widgets/textarea {:label "Text:" :path [::post :text]}]]
     [:div
      [:> ui/Button
       {:basic   true
        :color   "red"
        :floated "left"
        ;;todo
        :onClick #(rf/dispatch [:nav/back])}
       "cancel"]
      [:> ui/Button
       {:primary true
        :onClick #(rf/dispatch [::submit-post])}
       "submit"]]]]])

(rf/reg-event-fx
  ::init-submit-post-page
  (fn [{:keys [db]} _]
    (dissoc db ::post)))

(kf/reg-controller
  ::post-submission-controller
  {:params (constantly true)
   :start  (fn [_] [::init-submit-post-page])})