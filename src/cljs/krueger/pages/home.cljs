(ns krueger.pages.home
  (:require
    [cljsjs.semantic-ui-react :as ui]
    [re-frame.core :as rf]
    [kee-frame.core :as kf]
    [krueger.common :refer [match-route]]
    [krueger.terminology :refer [term]]
    [krueger.time :refer [ago]]))

(rf/reg-sub
  ::posts
  (fn [db _]
    (::posts db)))

(defn post [{:keys [id author url preview title comment-count timestamp]}]
  [:> ui/Feed.Event
   (when preview
     [:> Feed.Label {:image preview}])
   [:> ui/Feed.Content
    [:> ui/Feed.Summary
     (if url
       [:a {:href url} [:h3 title]]
       [:span title])]

    [:span
     (term :post/by) " " [:a {:href (str "/profile/" author)} author]
     " | " (ago timestamp)
     " | " [:a
            {:href (str "/post/" id)}
            (if (pos? comment-count)
              (str comment-count " " (term :post/comments))
              (term :post/no-comments))]]]])

(defn home-page []
  [:> ui/Feed
   (for [post-data @(rf/subscribe [::posts])]
     ^{:key post-data}
     [post post-data])])

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
