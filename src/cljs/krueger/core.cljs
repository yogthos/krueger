(ns krueger.core
  (:require [reagent.core :as r]
            [re-frame.core :as rf]
            [kee-frame.core :as kf]
            [ajax.core :refer [GET POST]]
            [krueger.ajax :refer [load-interceptors!]]
            [krueger.common :as common]
            [krueger.effects :as effects]
            [krueger.pages.common :refer [navbar]]
            [krueger.pages.home :refer [home-page]]
            [krueger.routing :as routing]))

(defn root-component []
  [:div
   [navbar]
   [common/error-modal]
   [kf/switch-route (fn [route] (get-in route [:data :name]))
    :home home-page
    nil [:div>h1 "404"]]])

(defn mount-components []
  (r/render [#'root-component] (.getElementById js/document "app")))

(defn init! []
  (kf/start! {:debug?         true
              :router         (routing/->ReititRouter routing/router)
              :chain-links    effects/chain-links
              :initial-db     {:user (js->clj js/user)}
              :root-component [root-component]})
  (load-interceptors!)
  (routing/hook-browser-navigation!))
