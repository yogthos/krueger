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

(defn messages-page []
  [:> ui/Container
   {:fluid true}
   [:p "post content"]
   [:p @(rf/subscribe [::messages])]])

(kf/reg-chain
  ::fetch-messages
  (fn [{db :db} _]
    {:db (assoc db ::messages [{:id 1 :text "some message"}])}
    #_{:http {:method      :get
              :url         "/api/page"
              :params      {:category :all
                            :offset   0}
              :error-event [:common/set-error]}})
  #_(fn [{:keys [db]} [_ posts]]
      {:db (assoc db ::posts posts)}))

(kf/reg-controller
  ::messages-controller
  {:params (fn [route] (match-route route :messages))
   :start  (fn [_] [::fetch-messages])})


