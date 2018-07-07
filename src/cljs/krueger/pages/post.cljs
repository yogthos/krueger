(ns krueger.pages.post
  (:require
    [cljsjs.semantic-ui-react :as ui]
    [re-frame.core :as rf]
    [kee-frame.core :as kf]))

(rf/reg-sub
  ::comments
  (fn [db _]
    (::comments db)))

(defn post-comment [{:keys [id text]}]
  [:> ui/List.Item [:> ui/List.Content [:p.comment text]]])

(defn post-page []
  [:> ui/Container
   {:fluid true}
   [:p "post content"]
   [:> ui/Form
    [:> ui/Form.Field
     [:textarea
      {:placeholder "leave a comment"}]]]
   [:> ui/List
    (for [comment-data @(rf/subscribe [::comments])]
      ^{:key comment-data}
      [post-comment comment-data])]])

(kf/reg-chain
  ::fetch-post
  (fn [{db :db} _]
    {:db (assoc db ::comments [{:id 1 :text "some comment"}])}
    #_{:http {:method      :get
              :url         "/api/page"
              :params      {:category :all
                            :offset   0}
              :error-event [:common/set-error]}})
  #_(fn [{:keys [db]} [_ posts]]
      {:db (assoc db ::posts posts)}))

(kf/reg-controller
  ::post
  {:params (fn [route] (-> route :path-params))
   :start  (fn [_ post-id] [::fetch-post post-id])})
