(ns krueger.pages.common
  (:require
    [cljsjs.semantic-ui-react :as ui]
    [re-frame.core :as rf]))

(defn nav-link [route content & [opts]]
  [:> ui/MenuItem
   (merge
     {:active  (= @(rf/subscribe [:nav/page]) route)
      :onClick #(rf/dispatch [:navigate-by-route-name route])}
     opts)
   content])

(defn nav-action [title action & [opts]]
  [:> ui/MenuItem
   (merge
     {:name    title
      :onClick action}
     opts)])

(defn navbar []
  [:> ui/Menu
   [nav-link :home [:span "Home"] {}]
   (if-let [username (:username @(rf/subscribe [:auth/user]))]
     [:> ui/Menu.Menu {:position "right"}
      [nav-link :comments [:i.far.fa-comments] {:position "right" :icon true}]
      [nav-link :messages [:i.far.fa-envelope] {:position "right"}]
      [nav-link :profile [:span username] {:position "right"}]
      [nav-action "Logout" #(rf/dispatch [:logout]) {:position "right"}]]
     [:> ui/Menu.Menu {:position "right"}
      [nav-action "Login" #(rf/dispatch [:login]) {:position "right"}]])])