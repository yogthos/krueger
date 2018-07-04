(ns krueger.pages.common
  (:require
    [cljsjs.semantic-ui-react :as ui]
    [reagent.core :as r]
    [re-frame.core :as rf]))

(defn nav-link [route title]
  [:> ui/MenuItem
   {:name title
    :active (= @(rf/subscribe [:nav/page]) route)
    :onClick #(rf/dispatch [:navigate-by-route-name route])}])

(defn navbar []
  [:> ui/Menu
   [nav-link :home "Home"]
   [nav-link :threads "Threads"]])