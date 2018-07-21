(ns krueger.pages.home
  (:require
    [cljsjs.semantic-ui-react :as ui]
    [re-frame.core :as rf]
    [kee-frame.core :as kf]
    [krueger.common :refer [match-route]]))

(rf/reg-sub
  ::posts
  (fn [db _]
    (::posts db)))

(defn post [{:keys [id author url preview title comment-count timestamp]}]
  [:> ui/List.Item
   (when preview
     [:> ui/Image {:src preview}])
   [:> ui/List.Content
    [:> ui/List.Header {:as "header"}
     (if url
       [:a {:href url} [:h3 title]]
       [:span title])]
    [:a
     {:href (str "/post/" id)}
     [:> ui/List.Description (str "by " author " | " timestamp " | " comment-count " comments")]]]])

(defn home-page []
  [:> ui/Container
   {:fluid true}
   [:> ui/List
    (for [post-data @(rf/subscribe [::posts])]
      ^{:key post-data}
      [post post-data])]])

(kf/reg-chain
  ::fetch-posts
  (fn [_ _]
    {:http {:method      :get
            :url         "/api/page"
            :params      {:category "all"
                          :page     0}
            :error-event [:common/set-error]}})
  (fn [{:keys [db]} [_ {:keys [posts]}]]
    {:db (assoc db ::posts posts)}))

(kf/reg-controller
  ::home-controller
  {:params (fn [route] (match-route route :home))
   :start  [::fetch-posts]})
