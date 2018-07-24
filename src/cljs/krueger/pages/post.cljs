(ns krueger.pages.post
  (:require
    [cljsjs.semantic-ui-react :as ui]
    [re-frame.core :as rf]
    [kee-frame.core :as kf]
    [krueger.common :refer [match-route]]
    [krueger.components.widgets :refer [spinner]]
    [krueger.terminology :refer [term]]
    [krueger.time :refer [ago]]
    [krueger.components.widgets :as widgets]
    [reagent.core :as r]))

(rf/reg-sub
  ::post
  (fn [db _]
    (::post db)))

(rf/reg-sub
  ::comments
  :<- [::post]
  (fn [post _]
    (:comments post)))

(rf/reg-event-db
  ::add-edit
  (fn [db [_ {:keys [post parent] :as comment}]]
    (update db :edit/comment assoc [post parent] comment)))

(rf/reg-event-db
  ::stop-edit
  (fn [db [_ k]]
    (update db :edit/comment dissoc k)))

(kf/reg-event-fx
  ::submit-comment
  (fn [{db :db} [k]]
    {:http {:method        :post
            :url           "/api/restricted/comment"
            :resource-id   :submit-comment
            :params        {:comment (get-in db [:edit/comment k])}
            :success-event [::stop-edit k]}}))

(defn submit-action [post-id parent-comment-id]
  (if @(rf/subscribe [:auth/user])
    (rf/dispatch [::submit-comment [post-id parent-comment-id]])
    (rf/dispatch [:auth/login-modal-shown true])))

(defn submit-form [post-id parent-comment-id]
  [:> ui/Form
   [:> ui/Form.Field
    [:textarea
     {:placeholder (term :post/comment)
      :on-blur     #(rf/dispatch [::add-edit
                                  {:post    post-id
                                   :parent  parent-comment-id
                                   :content (-> % .-target .-value)}])}]]
   [:> ui/Form.Field
    [widgets/ajax-button
     {:primary     true
      :resource-id :submit-comment
      :on-click    #(submit-action post-id parent-comment-id)}
     (term :submit)]]])

(defn comment-reply [post-id comment-id]
  (r/with-let [expanded? (r/atom false)]
    (if @expanded?
      [submit-form post-id comment-id]
      [:> ui/Button
       {:primary  true
        :on-click #(reset! expanded? true)}
       (term :post/comment)])))

(defn comment-content [post-id {:keys [id content]}]
  [:div.comment
   [:p content]
   (when @(rf/subscribe [:auth/user])
     [comment-reply post-id id])])

(defn comments-list [post-id]
  [:> ui/List
   (for [comment-data @(rf/subscribe [::comments])]
     ^{:key comment-data}
     [:> ui/List.Item
      [:> ui/List.Content
       [comment-content post-id comment-data]]])])

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
   [submit-form (:id @(rf/subscribe [::post])) nil]
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
