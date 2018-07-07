(ns krueger.pages.home
  (:require
    [cljsjs.semantic-ui-react :as ui]
    [re-frame.core :as rf]
    [kee-frame.core :as kf]))

(rf/reg-sub
  ::posts
  (fn [db _]
    (::posts db)))

(defn post [{:keys [id href thumb title description comments]}]
  [:> ui/List.Item
   [:> ui/Image {:src thumb}]
   [:> ui/List.Content
    [:> ui/List.Header {:as "header"} [:a {:href href} title]]
    [:> ui/List.Description (str description " " comments)]]])

(defn home-page []
  [:> ui/Container
   {:fluid true}
   [:> ui/List
    (for [post-data @(rf/subscribe [::posts])]
      ^{:key post-data}
      [post post-data])]])

(kf/reg-chain
  ::fetch-posts
  (fn [{db :db} _]
    {:db (assoc db ::posts [{:title       "Test Post"
                             :description "a post about something"
                             :comments    10
                             :href        "/post/1"
                             :thumb       "/img/warning_clojure.png"}])}

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
