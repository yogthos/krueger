(ns krueger.pages.post
  (:require
    [cljsjs.semantic-ui-react :as ui]
    [re-frame.core :as rf]
    [kee-frame.core :as kf]
    [krueger.common :refer [match-route]]
    [krueger.components.widgets :refer [spinner]]))

(rf/reg-sub
  ::post
  (fn [db _]
    (::post db)))

(rf/reg-sub
  ::comments
  (fn [db _]
    (::comments db)))

(defn post-comment [{:keys [id text]}]
  [:> ui/List.Item [:> ui/List.Content [:p.comment text]]])

(rf/reg-event-db
  ::set-comment-text
  (fn [db [_ text]]
    (assoc db ::comment-text text)))

;;todo disable submit button while posting
(rf/reg-event-fx
  ::submit-comment
  (fn [{db :db} _]
    #_{:http {:params        (::comment-text db)
              :success-event [::set-comment-text nil]}}))

(rf/reg-sub
  ::comment-text
  (fn [db _]
    (::comment-text db)))

(defn submit-form []
  [:> ui/Form
   [:> ui/Form.Field
    [:textarea
     {:placeholder "leave a comment"
      :on-change   #(rf/dispatch [::set-comment-text (-> % .-target .-value)])
      :value       @(rf/subscribe [::comment-text])}]]
   [:> ui/Form.Field
    [:> ui/Button
     {:primary  true
      :on-click #(rf/dispatch [::submit-comment])}
     "submit"]]])

(defn comments-list []
  [:> ui/List
   (for [comment-data @(rf/subscribe [::comments])]
     ^{:key comment-data}
     [post-comment comment-data])])

;;todo display author id instead of the email
(defn post-content []
  (if-let [{:keys [tags title author url text timestamp]} @(rf/subscribe [::post])]
    [:div
     [:h3 (if url [:a {:href url} title] title)]
     (when text [:p text])
     [:p "submitted by " author " at " (str timestamp)]]
    spinner))

(defn post-page []
  [:> ui/Container
   {:fluid true}
   [post-content]
   [:p @(rf/subscribe [::comment-text])]
   [submit-form]
   [comments-list]])

(kf/reg-chain
  ::fetch-post
  (fn [_ [post-id]]
    {:http {:method      :get
            :url         (str "/api/post/" post-id)
            :error-event [:common/set-error]}})
  (fn [{:keys [db]} [_ post]]
    {:db (assoc db ::post post)}))

(kf/reg-controller
  ::post-controller
  {:params (fn [route] (when (match-route route :post)
                         (-> route :path-params :id)))
   :start  (fn [_ post-id] [::fetch-post post-id])})
