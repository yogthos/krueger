(ns krueger.core
  (:require [reagent.core :as r]
            [kee-frame.core :as kf]
            [ajax.core :refer [GET POST]]
            [krueger.ajax :refer [load-interceptors!]]
            [krueger.auth]
            [krueger.common :as common]
            [krueger.effects :as effects]
            [krueger.feeds :as feeds]
            [krueger.pages.common :refer [navbar]]
            [krueger.pages.comments :refer [comments-page]]
            [krueger.pages.home :refer [home-page]]
            [krueger.pages.messages :refer [messages-page]]
            [krueger.pages.post :refer [post-page]]
            [krueger.pages.profile :refer [profile-page]]
            [krueger.routing :as routing]))

(defn root-component []
  [:div
   [navbar]
   [common/error-modal]
   [kf/switch-route (fn [route] (get-in route [:data :name]))
    :home home-page
    :comments comments-page
    :messages messages-page
    :post post-page
    :profile profile-page
    nil [:div>h1 "404"]]])

(defn init! []
  (kf/start! {:debug?         true
              :router         (routing/->ReititRouter routing/router)
              :chain-links    effects/chain-links
              :initial-db     {:auth/user #_{:id 1 :username "foo"} (js->clj js/user)}
              :root-component [root-component]})
  (load-interceptors!)
  (feeds/start-router!)
  (routing/hook-browser-navigation!))
