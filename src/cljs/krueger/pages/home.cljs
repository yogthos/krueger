(ns krueger.pages.home
  (:require
    [cljsjs.semantic-ui-react :as ui]
    [re-frame.core :as rf]
    [kee-frame.core :as kf]))

(rf/reg-sub
  ::posts
  (fn [db _]
    (::posts db)))

#_{:id                       String
   :author                   String
   :tags                     [Tag]
   :title                    String
   (s/optional-key :preview) (s/maybe String)
   (s/optional-key :url)     (s/maybe String)
   (s/optional-key :text)    (s/maybe String)
   :upvotes                  s/Num
   :downvotes                s/Num
   :timestamp                Date
   :comments                 [Comment]}

(defn post [{:keys [id url preview title description comments]}]
  [:> ui/List.Item
   [:> ui/Image {:src preview}]
   [:> ui/List.Content
    [:> ui/List.Header {:as "header"} [:a {:href url} title]]
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
                             :url         "http://mastodon.social/yogthos"
                             :preview     "/img/warning_clojure.png"}])}

    #_{:http {:method      :get
              :url         "/api/page"
              :params      {:category :all
                            :offset   0}
              :error-event [:common/set-error]}})
  #_(fn [{:keys [db]} [_ posts]]
      {:db (assoc db ::posts posts)}))

(kf/reg-controller
  ::home-controller
  {:params (fn [route] (-> route :path-params))
   :start  [::fetch-posts]})
