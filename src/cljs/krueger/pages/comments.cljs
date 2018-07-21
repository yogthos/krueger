(ns krueger.pages.comments
  (:require
    [cljsjs.semantic-ui-react :as ui]
    [re-frame.core :as rf]
    [kee-frame.core :as kf]
    [krueger.common :refer [match-route]]))

(rf/reg-sub
  ::comments
  (fn [db _]
    (::comments db)))

(defn comments-page []
  [:> ui/Container
   {:fluid true}
   [:p "a comment"]
   [:p @(rf/subscribe [::comments])]])

(kf/reg-chain
  ::fetch-comments
  (fn [{db :db} _]
    {:db (assoc db ::messages [{:id 1 :text "some comment"}])}
    #_{:http {:method      :get
              :url         "/api/page"
              :params      {:category :all
                            :offset   0}
              :error-event [:common/set-error]}})
  #_(fn [{:keys [db]} [_ posts]]
      {:db (assoc db ::posts posts)}))

(kf/reg-controller
  ::comments-controller
  {:params (fn [route] (match-route route :comments))
   :start  (fn [_] [::fetch-comments])})



