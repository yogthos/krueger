(ns krueger.pages.profile
  (:require
    [cljsjs.semantic-ui-react :as ui]
    [re-frame.core :as rf]
    [kee-frame.core :as kf]
    [krueger.common :refer [match-route]]
    [krueger.terminology :refer [term]]))

(rf/reg-sub
  ::profile
  (fn [db _]
    (::profile db)))

(defn profile-page []
  [:> ui/Container
   {:fluid true}
   [:p (term :profile/title)]
   [:p @(rf/subscribe [::profile])]])

(kf/reg-chain
  ::fetch-profile
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
  ::profile-controller
  {:params (fn [route] (match-route route :profile))
   :start  (fn [_] [::fetch-profile])})



