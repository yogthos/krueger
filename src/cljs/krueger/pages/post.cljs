(ns krueger.pages.post
  (:require
    [cljsjs.semantic-ui-react :as ui]
    [re-frame.core :as rf]
    [kee-frame.core :as kf]
    [krueger.common :refer [match-route]]
    [krueger.components.widgets :refer [spinner]]
    [krueger.terminology :refer [term]]
    [krueger.time :refer [ago]]
    [krueger.components.widgets :as widgets]))

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

(kf/reg-chain
  ::submit-comment
  (fn [{db :db} _]
    {:http {:method        :post
            :url           "/api/restricted/comment"
            :resource-id   :submit-comment
            :params        (::comment-text db)
            :success-event [::set-comment-text nil]}})
  (fn [{db :db} [_ result]]))

(rf/reg-sub
  ::comment-text
  (fn [db _]
    (::comment-text db)))

(defn submit-form []
  [:> ui/Form
   [:> ui/Form.Field
    [:textarea
     {:placeholder (term :post/comment)
      :on-change   #(rf/dispatch [::set-comment-text (-> % .-target .-value)])
      :value       @(rf/subscribe [::comment-text])}]]
   [:> ui/Form.Field
    [widgets/ajax-button
     {:primary     true
      :resource-id :submit-comment
      :on-click    #(rf/dispatch [::submit-comment])}
     (term :submit)]]])

(defn comments-list []
  [:> ui/List
   (for [comment-data @(rf/subscribe [::comments])]
     ^{:key comment-data}
     [post-comment comment-data])])

(defn post-content []
  (if-let [{:keys [tags title author url text timestamp]} @(rf/subscribe [::post])]
    [:div
     [:h3 (if url [:a {:href url} title] title)
      (for [id tags]
        ^{:key id}
        [:> ui/Label @(rf/subscribe [:tag/label id])])]
     (when text [:p text])
     [:p (term :post/submitted-by)
      " " author
      " " (ago timestamp)
      " " (term :post/ago)]]
    [spinner]))

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
