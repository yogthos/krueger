(ns krueger.components.comments
  (:require
    [krueger.components.widgets :as widgets]
    [krueger.terminology :refer [term]]
    [krueger.time :refer [ago]]
    [cljsjs.semantic-ui-react :as ui]
    [reagent.core :as r]
    [re-frame.core :as rf]
    [kee-frame.core :as kf]))

(defn link-comments [grouped-comments & [parent]]
  (let [root-comments (get grouped-comments parent)]
    (for [{:keys [id] :as comment} root-comments]
      (assoc comment
        :replies
        (link-comments grouped-comments id)))))

(defn group-comments [comments]
  (link-comments (group-by :parent comments)))

(rf/reg-sub
  ::comments
  :<- [:post/content]
  (fn [post _]
    (:comments post)))

(rf/reg-event-db
  ::add-edit
  (fn [db [_ {:keys [post parent] :as comment}]]
    (update db :edit/comment assoc [post parent] comment)))

(rf/reg-event-db
  ::update-edit
  (fn [db [_ k content]]
    (assoc-in db [:edit/comment k :content] content)))

(rf/reg-event-db
  ::stop-edit
  (fn [db [_ k {:keys [id]}]]
    (let [comment (get-in db [:edit/comment k])]
      (-> db
          (update-in [::post :comments] (fnil conj []) (assoc comment :id id))
          (update :edit/comment dissoc k)))))

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
            :success-event [::stop-edit k]}}))

(defn submit-action [comment-id]
  (if @(rf/subscribe [:auth/user])
    (rf/dispatch [::submit-comment comment-id])
    (rf/dispatch [:auth/login-modal-shown true])))

(defn submit-form [post-id parent-comment-id]
  (let [comment-id [post-id parent-comment-id]]
    [:> ui/Form
     [:> ui/Form.Field
      [:textarea
       {:placeholder (term :post/comment)
        :on-change   #(rf/dispatch
                        [::update-edit
                         comment-id
                         (-> % .-target .-value)])}]]
     [:> ui/Form.Field
      (when parent-comment-id
        [:> ui/Form.Field
         [:> ui/Button
          {:basic    true
           :color    "red"
           :floated  "left"
           :on-click #(rf/dispatch [::cancel-edit comment-id])}
          (term :cancel)]])
      [:> ui/Button
       {:basic    true
        :disabled (or
                    (empty? (:content @(rf/subscribe [::edit-in-progress comment-id])))
                    @(rf/subscribe [:http/loading? :submit-comment]))
        :on-click #(submit-action comment-id)}
       (term :post/comment)]]]))

(defn comment-reply [post-id comment-id]
  (if @(rf/subscribe [::edit-in-progress [post-id comment-id]])
    [submit-form post-id comment-id]
    [:> ui/Button
     {:basic    true
      :size     "mini"
      :on-click #(rf/dispatch [::add-edit
                               {:post   post-id
                                :parent comment-id}])}
     (term :post/comment)]))

(declare comments-list)

(defn comment-content [post-id {:keys [id author content replies timestamp]}]
  [:> ui/Comment
   [:> ui/Comment.Content
    [:> ui/Comment.Author {:as "a"} author]
    [:> ui/Comment.Metadata [:div (ago timestamp)]]
    [:> ui/Comment.Text content]
    [:> ui/Comment.Metadata
     [:div
      (when @(rf/subscribe [:auth/user])
        [comment-reply post-id id])
      (when replies
        [comments-list post-id id replies])]]]])

(defn comments-list [post-id]
  (when-let [comments @(rf/subscribe [::comments])]
    [:> ui/Comment.Group
     (for [comment-data comments]
       ^{:key comment-data}
       [comment-content post-id comment-data])]))



#_#_#_(declare comment-component)

    ;;TODO format comments using markdown
    (defn comment-component [{:keys [id author timestamp content upvotes downvotes children]}]
      (r/with-let [expanded? (atom true)
                   show-reply? (atom false)]
        [:div
         [:div.comment
          [:p.text-muted
           [:span.expand.clickable
            {:on-click #(do (swap! expanded? not) nil)}
            (if @expanded? "[–]" "[+]")]
           " "
           [:a {:href (str "/user/" author)} author] " " (- upvotes downvotes) " points"]
          (when @expanded?
            [:div
             [:p content]
             [:p [:span.clickable {:on-click #(do (swap! show-reply? not) nil)} [:em "reply"]]]
             (when @show-reply?
               [comment-editor id])
             (for [comment children]
               ^{:key (:id comment)}
               [comment-component comment])])]]))

    (defn render-comments [comments]
      (let [grouped-comments (group-comments comments)]
        [:div
         (for [comment grouped-comments]
           ^{:key (:id comment)}
           [comment-component comment])]))
