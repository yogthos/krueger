(ns krueger.pages.home
  (:require
    [cljsjs.semantic-ui-react :as ui]
    [re-frame.core :as rf]
    [kee-frame.core :as kf]))

(rf/reg-sub
  ::posts
  (fn [db _]
    (::posts db)))

(defn home-page []
  [:div
   (into [:> ui/List]
         [[:> ui/List.Item
           [:> ui/Image {:src "/img/warning_clojure.png"}]
           [:> ui/List.Content
            [:> ui/List.Header {:as "header"} [:a {:href "reddit.com"} "Story Title"]]
            [:> ui/List.Description "description"]]]])])

(kf/reg-chain
  ::fetch-posts
  (fn [_ _]
    #_{:http {:method      :get
            :url         "/api/page"
            :params      {:category :all
                          :offset   0}
            :error-event [:common/set-error]}})
  #_(fn [{:keys [db]} [_ posts]]
    {:db (assoc db ::posts posts)}))

(kf/reg-controller
  ::home
  {:params (fn [route] (-> route :path-params))
   :start  [::fetch-posts]})
