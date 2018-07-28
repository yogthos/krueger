(ns krueger.components.comments
  (:require
    [cljsjs.semantic-ui-react :as ui]
    [clojure.walk :refer [prewalk]]
    [krueger.terminology :refer [term]]
    [krueger.time :refer [ago]]
    [reagent.core :as r]
    [re-frame.core :as rf]
    [kee-frame.core :as kf]))

(defn link-comments [grouped-comments & [parent]]
  (let [root-comments (get grouped-comments parent)]
    (mapv
      (fn [{:keys [id] :as comment}]
        (assoc comment
          :replies
          (link-comments grouped-comments id)))
      root-comments)))

(defn group-comments [comments]
  (link-comments (group-by :parent comments)))

(rf/reg-sub
  :post/comments
  :<- [:post/content]
  (fn [post _]
    (:comments post)))

(rf/reg-event-fx
  ::add-comment
  (fn [{db :db} [_ {:keys [parent] :as comment}]]
    {:db (update-in db [:post/content :comments]
                    (fn [comments]
                      (if parent
                        (prewalk
                          (fn [node]
                            (if (= (:id node) parent)
                              (update node :replies conj comment)
                              node))
                          comments)
                        (conj comments comment))))}))

(rf/reg-event-db
  ::add-edit
  (fn [db [_ [post-id parent-comment-id]]]
    (update db :edit/comment assoc [post-id parent-comment-id]
            {:post   post-id
             :parent parent-comment-id})))

(rf/reg-event-db
  ::update-edit
  (fn [db [_ k content]]
    (assoc-in db [:edit/comment k :content] content)))

(rf/reg-event-fx
  ::finish-edit
  (fn [{db :db} [_ k {:keys [id]}]]
    (let [comment (assoc (get-in db [:edit/comment k])
                    :id id
                    :timestamp (js/Date.))]
      {:db       (update db :edit/comment dissoc k)
       :dispatch [::add-comment comment]})))

(rf/reg-event-db
  ::cancel-edit
  (fn [db [_ k]]
    (update db :edit/comment dissoc k)))

(rf/reg-sub
  ::edit-in-progress
  (fn [db [_ k]]
    (get-in db [:edit/comment k])))

(kf/reg-event-fx
  ::submit-comment
  (fn [{db :db} [k]]
    {:http {:method        :post
            :url           "/api/restricted/comment"
            :resource-id   :submit-comment
            :params        {:comment (get-in db [:edit/comment k])}
            :success-event [::finish-edit k]}}))

(defn submit-action [comment-id]
  (if @(rf/subscribe [:auth/user])
    (rf/dispatch [::submit-comment comment-id])
    (rf/dispatch [:auth/login-modal-shown true])))

(defn empty-comment? [comment-id]
  (empty? (:content @(rf/subscribe [::edit-in-progress comment-id]))))

(defn submit-form [post-id parent-comment-id]
  (let [comment-id [post-id parent-comment-id]]
    [:> ui/Form {:reply true}
     [:> ui/Form.Field
      [:textarea
       {:placeholder (term :post/comment)
        :on-focus    #(when (empty-comment? comment-id)
                        (rf/dispatch [::add-edit comment-id]))
        :on-blur     #(when (empty-comment? comment-id)
                        (rf/dispatch [::cancel-edit]))
        :value       (:content @(rf/subscribe [::edit-in-progress comment-id]))
        :on-change   #(rf/dispatch
                        [::update-edit
                         comment-id
                         (-> % .-target .-value)])}]]
     [:> ui/Form.Field
      (when parent-comment-id
        [:> ui/Form.Field
         [:> ui/Button
          {:basic    true
           :size     "tiny"
           :color    "red"
           :floated  "left"
           :on-click #(rf/dispatch [::cancel-edit comment-id])}
          (term :cancel)]])
      [:> ui/Button
       {:basic    true
        :size     "tiny"
        :disabled (or
                    (empty-comment? comment-id)
                    @(rf/subscribe [:http/loading? :submit-comment]))
        :on-click #(submit-action comment-id)}
       (term :submit)]]]))

(defn comment-reply [post-id comment-id]
  (if @(rf/subscribe [::edit-in-progress [post-id comment-id]])
    [submit-form post-id comment-id]
    [:> ui/Button
     {:basic    true
      :size     "tiny"
      :on-click #(rf/dispatch [::add-edit [post-id comment-id]])}
     (term :post/comment)]))

(rf/reg-event-db
  :comment/update-votes
  (fn [db [_ id f]]
    (update-in
      db
      [:post/content :comments]
      #(prewalk
         (fn [node]
           (if (= (:id node) id)
             (f node)
             node))
         %))))

(defn vote [id action]
  {:http
   {:method        :post
    :url           (case action
                     :upvote "/api/restricted/up-vote-comment"
                     :downvote "/api/restricted/down-vote-comment")
    :params        {:id id}
    :success-event [:comment/update-votes
                    (case action
                      :upvote #(update % :upvotes inc)
                      :downvote #(update % :downvotes inc))]}})

(rf/reg-event-fx
  :comment/upvote
  (fn [_ [_ id]]
    (vote id :upvote)))

(rf/reg-event-fx
  :comment/downvote
  (fn [_ [_ id]]
    (vote id :downvote)))

(defn comment-score [{:keys [id upvotes downvotes]}]
  (let [user @(rf/subscribe [:auth/user])]
    [:> ui/Comment.Metadata
     [:div
      (when user
        [:span
         {:on-click #(rf/dispatch [:comment/upvote id])}
         [:i.fas.fa-angle-up {:cursor "pointer"}]])
      [:div (if (and upvotes downvotes) (js/Math.ceil (/ upvotes downvotes)) "0")]
      (when user
        [:span
         {:on-click #(rf/dispatch [:comment/downvote id])}
         [:i.fas.fa-angle-down {:cursor "pointer"}]])]]))

(declare comments-list)

(defn comment-content [post-id {:keys [id author content replies timestamp] :as comment}]
  [:> ui/Comment
   [:> ui/Comment.Content
    [:div
     {:style {:display      "inline-block"
              :float        "left"
              :margin-right "10px"
              :margin-top   "-12px"}}
     [comment-score comment]]
    [:div
     [:> ui/Comment.Author {:as "a"} author]
     [:> ui/Comment.Metadata [:div (ago timestamp)]]
     [:> ui/Comment.Text content]
     [:> ui/Comment.Text
      [:div
       (when @(rf/subscribe [:auth/user])
         [comment-reply post-id id])
       (when replies
         [comments-list post-id replies])]]]]])

(defn comments-list [post-id comments]
  [:> ui/Comment.Group
   (for [comment-data comments]
     ^{:key comment-data}
     [comment-content post-id comment-data])])
