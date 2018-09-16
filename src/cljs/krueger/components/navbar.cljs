(ns krueger.components.navbar
  (:require
    [krueger.terminology :refer [term]]
    [cljsjs.semantic-ui-react :as ui]
    [re-frame.core :as rf]))

(defn nav-link [route content & [opts]]
  [:> ui/MenuItem
   (merge
     {:active  (= @(rf/subscribe [:nav/page]) route)
      :onClick #(rf/dispatch [:nav/by-route-name route])}
     opts)
   content])

(defn nav-action [title action & [opts]]
  [:> ui/MenuItem
   (merge
     {:name    title
      :onClick #(rf/dispatch action)}
     opts)])

(defn navbar []
  [:> ui/Menu
   [nav-link :home [:span (term :nav/home)] {}]
   (if-let [username (:id @(rf/subscribe [:auth/user]))]
     [:> ui/Menu.Menu {:position "right"}
      [nav-link :submit-post [:i.fas.fa-feather] {:icon true}]
      [nav-link :comments [:i.far.fa-comments] {:icon true}]
      [nav-link :messages [:i.far.fa-envelope]]
      [nav-link :profile [:span username]]
      [nav-action (term :nav/logout) [:auth/handle-logout]]]
     [:> ui/Menu.Menu {:position "right"}
      [nav-action (term :nav/login) [:auth/login-modal-shown true]]
      [nav-action (term :nav/register) [:auth/show-registration-modal true]]])])