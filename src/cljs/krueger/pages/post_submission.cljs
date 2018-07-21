(ns krueger.pages.post-submission
  (:require
    [cljsjs.semantic-ui-react :as ui]
    [re-frame.core :as rf]
    [kee-frame.core :as kf]
    [krueger.common :refer [match-route]]
    [krueger.components.widgets :as widgets]))

(kf/reg-chain
  ::submit-post
  (fn [{db :db} _]
    {:db   (dissoc db ::post)
     :http {:method      :post
            :url         "/api/restricted/post"
            :params      {:post (update (::post db) :tags not-empty)}
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
    [:h2 "Submit a story"]
    [:> ui/Form
     [:> ui/Form.Field
      [widgets/text-input {:label "URL:" :path [::post :url]}]]
     [:> ui/Form.Field
      [widgets/text-input {:label "Title:" :path [::post :title]}]]
     ;;todo searchable typeahead tags should have lable, description, id (number)
     [:> ui/Form.Field
      [widgets/text-input {:label "Tags:" :path [::post :tags]}]]
     [:> ui/Form.Field
      [widgets/textarea {:label "Text:" :path [::post :text]}]]
     [widgets/error-notification ::post-error]
     [:div
      [:> ui/Button
       {:basic   true
        :color   "red"
        :floated "left"
        ;;todo
        :onClick #(rf/dispatch [:nav/back])}
       "cancel"]
      [:> ui/Button
       {:primary  true
        :floated  "right"
        :on-click #(rf/dispatch [::submit-post])}
       "submit"]]]]])

(rf/reg-event-fx
  ::init-submit-post-page
  (fn [{:keys [db]} _]
    (println "loading submission page" (:auth/user db))
    (merge
      {:db (dissoc db ::post)}
      (when-not (:auth/user db)
        {:dispatch [:auth/close-login-modal true]}))))

(kf/reg-controller
  ::post-submission-controller
  {:params (fn [route] (match-route route :submit-post))
   :start  (fn [_] [::init-submit-post-page])})