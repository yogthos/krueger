(ns krueger.pages.post
  (:require
    [cljsjs.semantic-ui-react :as ui]
    [re-frame.core :as rf]
    [kee-frame.core :as kf]
    [krueger.components.comments :as comments]
    [krueger.common :refer [match-route]]
    [krueger.components.widgets :refer [spinner]]
    [krueger.terminology :refer [term]]
    [krueger.time :refer [ago]]))

(rf/reg-sub
  :post/content
  (fn [db _]
    (::post db)))


(defn post-content [post]
  (if-let [{:keys [tags title author url text timestamp]} post]
    [:div
     [:h3 (if url [:a {:href url} title] title)
      (for [id tags]
        ^{:key id}
        [:> ui/Label @(rf/subscribe [:tag/label id])])]
     (when text [:p text])
     [:p (term :post/submitted-by)
      " " author
      " " (ago timestamp)
      " " (term :post/ago)]]
    [spinner]))

(defn post-page []
  (when-let [post @(rf/subscribe [:post/content])]
    [:div
     [post-content post]
     [comments/submit-form (:id post) nil]
     [:> ui/Header
      {:as       "h3"
       :dividing true}
      "Comments"]
     (when-let [comments @(rf/subscribe [:post/comments])]
       [comments/comments-list (:id post) (comments/group-comments comments)])]))

(kf/reg-chain
  ::fetch-post
  (fn [_ [post-id]]
    {:http {:method      :get
            :url         (str "/api/post/" post-id)
            :error-event [:common/set-error]}})
  (fn [{:keys [db]} [_ post]]
    {:db (assoc db ::post post)}))

(kf/reg-controller
  ::post-controller
  {:params (fn [route] (when (match-route route :post)
                         (-> route :path-params :id)))
   :start  (fn [_ post-id] [::fetch-post post-id])})
