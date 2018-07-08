(ns krueger.pages.post
  (:require
    [cljsjs.semantic-ui-react :as ui]
    [re-frame.core :as rf]
    [kee-frame.core :as kf]))

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

(rf/reg-event-fx
  ::submit-comment
  (fn [{db :db} _]
    #_{:http {:params (::comment-text db)
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
     {:primary true
      :onClick #(rf/dispatch [::submit-comment])}
     "submit"]]])

(defn comments-list []
  [:> ui/List
   (for [comment-data @(rf/subscribe [::comments])]
     ^{:key comment-data}
     [post-comment comment-data])])

(defn post-page []
  [:> ui/Container
   {:fluid true}
   [:p "post content"]
   [:p @(rf/subscribe [::comment-text])]
   [submit-form]
   [comments-list]])

(kf/reg-chain
  ::fetch-post
  (fn [{db :db} _]
    {:db (assoc db ::comments [{:id 1 :text "some comment"}])}
    #_{:http {:method      :get
              :url         "/api/page"
              :params      {:category :all
                            :offset   0}
              :error-event [:common/set-error]}})
  #_(fn [{:keys [db]} [_ posts]]
      {:db (assoc db ::posts posts)}))

(kf/reg-controller
  ::post-controller
  {:params (fn [route] (-> route :path-params))
   :start  (fn [_ post-id] [::fetch-post post-id])})
