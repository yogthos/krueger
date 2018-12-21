(ns krueger.pages.messages
  (:require
    [cljsjs.semantic-ui-react :as ui]
    [reagent.core :as r]
    [re-frame.core :as rf]
    [kee-frame.core :as kf]
    [krueger.common :refer [match-route]]
    [krueger.terminology :refer [term]]
    [krueger.components.widgets :as widgets]))

(rf/reg-sub
  ::messages
  (fn [db _]
    (::messages db)))

(kf/reg-chain
  ::submit-message
  (fn [{db :db} _]
    {:db   (dissoc db ::reply)
     :http {:method      :post
            :url         "/api/restricted/message"
            :params      {:post (::post db)}
            :resource-id :submit-post
            :error-event [::post-error]}})
  (fn [{:keys [db]} [{:keys [id]}]]
    {:db       (-> db (dissoc ::post))
     ;;todo figure out for dynamic routes (kf/path-for [:post])
     :dispatch [:nav/by-route-path (str "/post/" id)]}))

(rf/reg-event-db
  ::cancel-edit
  (fn [db [_ message-id]]
    (update db ::reply dissoc message-id)))

(rf/reg-sub
  ::reply-text
  (fn [db [_ message-id]]
    (get-in db [::reply message-id :text])))

(defn reply-dialog [message-id]
  (r/with-let [expanded? (r/atom false)]
    [:> ui/Container
     {:fuluid true}
     (if @expanded?
       [:div
        [widgets/textarea {:label (term :message/comment) :path [::reply message-id :text]}]
        [:> ui/Button
         {:basic    true
          :size     "tiny"
          :color    "red"
          :floated  "left"
          :on-click (fn []
                      (reset! expanded? false)
                      (rf/dispatch [::cancel-edit message-id]))}
         (term :cancel)]
        [widgets/ajax-button
         {:primary     true
          :floated     "right"
          :disabled    (empty? @(rf/subscribe [::reply-text]))
          :resource-id :submit-message
          :on-click    #(rf/dispatch [::submit-message])}
         (term :submit)]]
       [:> ui/Button
        {:on-click #(reset! expanded? true)}
        "reply"])]))

(defn message [{:keys [id author content unread?]}]
  [:div
   [:p content]
   [:p [:span (term :message/from) " " author]]
   [reply-dialog]])

(defn messages-page []
  [:> ui/Container
   {:fluid true}
   [:div
    (for [{:keys [id] :as m} @(rf/subscribe [::messages])]
      ^{:key id}
      [message m])]])

(kf/reg-chain
  ::fetch-messages
  (fn [{db :db} _]
    {:db (assoc db ::messages [{:id      1
                                :content "some message"
                                :author  "yogthos"}])}
    #_{:http {:method      :get
              :url         "/api/messages"
              :params      {:category :all
                            :offset   0}
              :error-event [:common/set-error]}})
  #_(fn [{:keys [db]} [_ posts]]
      {:db (assoc db ::posts posts)}))

(kf/reg-controller
  ::messages-controller
  {:params (fn [route] (match-route route :messages))
   :start  (fn [_] [::fetch-messages])})


