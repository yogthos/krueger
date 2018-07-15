(ns krueger.core
  (:require
    [kee-frame.core :as kf]
    [ajax.core :refer [GET POST]]
    [krueger.ajax :refer [load-interceptors!]]
    [krueger.auth]
    [krueger.common :as common]
    [krueger.components.auth :as auth]
    [krueger.components.navbar :refer [navbar]]
    [krueger.effects :as effects]
    #_[krueger.feeds :as feeds]
    [krueger.pages.comments :refer [comments-page]]
    [krueger.pages.home :refer [home-page]]
    [krueger.pages.messages :refer [messages-page]]
    [krueger.pages.post :refer [post-page]]
    [krueger.pages.profile :refer [profile-page]]
    [krueger.routing :as routing]))

(defn root-component []
  [:div
   [navbar]
   [auth/login-modal]
   [auth/registration-modal]
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
              :initial-db     {:auth/user (js->clj js/user :keywordize-keys true)}
              :root-component [root-component]})
  (load-interceptors!)
  #_(feeds/start-router!)
  (routing/hook-browser-navigation!))
