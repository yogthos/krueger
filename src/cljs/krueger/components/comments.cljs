(ns krueger.components.comments
  (:require
    [krueger.components.widgets :as widgets]
    [reagent.core :as r]
    [re-frame.core :as rf]))

(defn link-comments [grouped-comments & [parent]]
  (let [root-comments (get grouped-comments parent)]
    (for [{:keys [id] :as comment} root-comments]
      (assoc comment
        :children
        (link-comments grouped-comments id)))))

(defn group-comments [comments]
  (link-comments (group-by :parent comments)))

(rf/reg-event-fx
  ::submit-comment
  (fn [_ {:keys [parent-id] :as comment}])
  )

(defn comment-editor [& [parent-id]]
  (when-let [user-id (:id @(rf/subscribe [:identity]))]
    (rf/dispatch [:comment/parent parent-id])
    [:div
     [:p "commenting as " [:a {:href (str "/" user-id)} user-id]]
     [widgets/input :textarea {:rows 5} :comment nil [:comment]]
     [:button.btn.btn-primary
      {:on-click #(rf/dispatch [::submit-comment parent-id user-id])}
      "save"]]))

(declare comment-component)

;;TODO format and print the timestamp
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
