(ns krueger.core
  (:require
    [cljsjs.semantic-ui-react :as ui]
    [kee-frame.core :as kf]
    [krueger.ajax :refer [load-interceptors!]]
    [krueger.auth]
    [krueger.common :as common]
    [krueger.components.auth :as auth]
    [krueger.components.navbar :refer [navbar]]
    #_[krueger.feeds :as feeds]
    [krueger.pages.comments :refer [comments-page]]
    [krueger.pages.home :refer [home-page]]
    [krueger.pages.messages :refer [messages-page]]
    [krueger.pages.post :refer [post-page]]
    [krueger.pages.post-submission :refer [submit-post-page]]
    [krueger.pages.profile :refer [profile-page]]
    [krueger.routing :as routing]
    [krueger.terminology]))

(defn root-component []
  [:div
   [navbar]
   [auth/login-modal]
   [auth/registration-modal]
   [common/error-modal]
   [:> ui/Container
    {:text true}
    [kf/switch-route (fn [route] (get-in route [:data :name]))
     :home home-page
     :comments comments-page
     :messages messages-page
     :post post-page
     :submit-post submit-post-page
     :profile profile-page
     nil nil]]])

(defn init! []
  (kf/start! {:router         (routing/->ReititRouter routing/router)
              :chain-links    common/chain-links
              :initial-db     {:auth/user (js->clj js/user :keywordize-keys true)}
              :root-component [root-component]})
  (load-interceptors!)
  #_(feeds/start-router!)
  (routing/hook-browser-navigation!))
