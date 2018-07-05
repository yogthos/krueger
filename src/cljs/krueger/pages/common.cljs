(ns krueger.pages.common
  (:require
    [cljsjs.semantic-ui-react :as ui]
    [re-frame.core :as rf]))

(defn nav-link [route title & [opts]]
  [:> ui/MenuItem
   (merge
     {:name    title
      :active  (= @(rf/subscribe [:nav/page]) route)
      :onClick #(rf/dispatch [:navigate-by-route-name route])}
     opts)])

(defn nav-action [title action & [opts]]
  [:> ui/MenuItem
   (merge
     {:name    title
      :onClick action}
     opts)])

(defn navbar []
  (into
    [:> ui/Menu]
    (into
      [[nav-link :home "Home"]
       [nav-link :threads "Threads"]]
      (if-let [username (:username @(rf/subscribe [:auth/user]))]
        [[nav-link :profile username {:position "right"}]
         [nav-action "Logout" #(rf/dispatch [:logout]) {:position "right"}]]
        [[nav-action "Login" #(rf/dispatch [:login]) {:position "right"}]]))))