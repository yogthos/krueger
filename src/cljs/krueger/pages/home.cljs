(ns krueger.pages.home
  (:require
    [re-frame.core :as rf]
    [kee-frame.core :as kf]))

(defn home-page []
  [:div.container
   [:div.row>div.col-sm-12
    [:h2.alert.alert-info "Tip: try pressing CTRL+H to open re-frame tracing menu"]]
   [:p (str @(rf/subscribe [::posts]))]])

(kf/reg-chain
  ::fetch-posts
  (fn [_ _]
    {:http {:method      :get
            :url         "/api/page"
            :params      {:category :all
                          :offset   0}
            :error-event [:common/set-error]}})
  (fn [{:keys [db]} [_ posts]]
    {:db (assoc db ::posts posts)}))

(kf/reg-controller
  ::home
  {:params (fn [route] (-> route :path-params))
   :start  [::fetch-posts]})
