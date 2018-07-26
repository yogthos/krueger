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
    (:post/content db)))


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
      " " (ago timestamp)]]
    [spinner]))

(defn post-page []
  (when-let [post @(rf/subscribe [:post/content])]
    [:div
     [post-content post]
     [comments/submit-form (:id post) nil]
     (if-let [comments (not-empty @(rf/subscribe [:post/comments]))]
       [:div
        [:> ui/Header
         {:as       "h3"
          :dividing true}
         "Comments"]
        [comments/comments-list (:id post) comments]]
       [:> ui/Header
        {:as       "h3"
         :dividing true}
        "No comments"])]))

(kf/reg-chain
  ::fetch-post
  (fn [_ [post-id]]
    {:http {:method      :get
            :url         (str "/api/post/" post-id)
            :error-event [:common/set-error]}})
  (fn [{:keys [db]} [_ post]]
    {:db (assoc db :post/content (update post :comments comments/group-comments))}))

(kf/reg-controller
  ::post-controller
  {:params (fn [route] (when (match-route route :post)
                         (-> route :path-params :id)))
   :start  (fn [_ post-id] [::fetch-post post-id])})
