(ns krueger.pages.messages
  (:require
    [cljsjs.semantic-ui-react :as ui]
    [re-frame.core :as rf]
    [kee-frame.core :as kf]
    [krueger.common :refer [match-route]]))

(rf/reg-sub
  ::messages
  (fn [db _]
    (::messages db)))

(defn message [{:keys [id author content unread?]}]
  [:div
   [:p content]
   [:p [:span "from " author]]])

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
    {:db (assoc db ::messages [{:id 1
                                :content "some message"
                                :author "yogthos"}])}
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


