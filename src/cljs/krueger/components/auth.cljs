(ns krueger.components.auth
  (:require
    [krueger.components.widgets :as widgets]
    [krueger.input-events :refer [dispatch-on]]
    [cljsjs.semantic-ui-react :as ui]
    [re-frame.core :as rf]
    [kee-frame.core :as kf]))

(rf/reg-event-db
  :auth/show-registration-modal
  (fn [db [_ show?]]
    (assoc db ::show-registration show?)))

(rf/reg-sub
  ::show-registration
  (fn [db _]
    (boolean (::show-registration db))))

(rf/reg-event-db
  ::registration-error
  (fn [db [_ error]]
    (assoc db ::registration-error (-> error :response :error))))

(rf/reg-sub
  ::registration-error
  (fn [db _]
    (::registration-error db)))


(kf/reg-chain
  :auth/handle-registration
  (fn [{db :db} _]
    {:db   (dissoc db ::registration-error)
     :http {:method      :post
            :url         "/api/register"
            :params      (::registration db)
            :error-event [::registration-error]}})
  (fn [{:keys [db]} [{:keys [user]}]]
    {:db (-> db
             (dissoc ::registration ::show-registration)
             (assoc :auth/user user))}))

(defn registration-modal []
  [:> ui/Modal
   {:close-on-dimmer-click false
    :open               @(rf/subscribe [::show-registration])
    :size               "tiny"}
   [:> ui/Modal.Header "User Registration"]
   [:> ui/Modal.Content
    [:> ui/Modal.Description
     [:> ui/Form
      [:> ui/Form.Field
       [widgets/password-input {:label "user name" :path [::registration :screenname]}]]
      [:> ui/Form.Field
       [widgets/email-input {:label "email" :path [::registration :email]}]]
      [:> ui/Form.Field
       [widgets/password-input {:label "password" :path [::registration :pass]}]]
      [:> ui/Form.Field
       [widgets/password-input {:label "confirm password" :path [::registration :pass-confirm]}]]]
     [widgets/error-notification ::registration-error]]]
   [:> ui/Modal.Actions
    [:> ui/Button
     {:basic   true
      :color   "red"
      :floated "left"
      :on-click #(rf/dispatch [:auth/show-registration-modal false])}
     "cancel"]
    [:> ui/Button
     {:primary true
      :on-click #(rf/dispatch [:auth/handle-registration false])}
     "register"]]])

(rf/reg-event-fx
  :auth/close-login-modal
  (fn [{db :db} [_ show?]]
    (merge
      {:db (assoc db ::show-login show?)}
      (when-not show?
        {:dispatch [:nav/by-route-name :home]}))))

(rf/reg-sub
  ::show-login
  (fn [db _]
    (boolean (::show-login db))))

(rf/reg-event-db
  ::login-error
  (fn [db [_ error]]
    (assoc db ::login-error (-> error :response :error))))

(rf/reg-sub
  ::login-error
  (fn [db _]
    (::login-error db)))

(kf/reg-chain
  :auth/handle-login
  (fn [{db :db} _]
    {:db   (dissoc db ::login-error)
     :http {:method      :post
            :url         "/api/login"
            :params      (::login db)
            :error-event [::login-error]}})
  (fn [{:keys [db]} [{:keys [user]}]]
    (println "login success" user)
    {:db (-> db
             (dissoc ::show-login ::login)
             (assoc :auth/user user))}))

(kf/reg-chain
  :auth/handle-logout
  (fn [_ _]
    {:http {:method      :post
            :url         "/api/restricted/logout"
            :error-event [:common/set-error]}})
  (fn [{:keys [db]} _]
    {:db (dissoc db :auth/user)}))

(defn login-modal []
  [:> ui/Modal
   {:close-on-dimmer-click false
    :open               @(rf/subscribe [::show-login])
    :size               "tiny"}
   [:> ui/Modal.Header "User Login"]
   [:> ui/Modal.Content
    [:> ui/Modal.Description
     [:> ui/Form
      [:> ui/Form.Field
       [widgets/email-input {:label "email" :path [::login :email]}]]
      [:> ui/Form.Field
       [widgets/password-input {:label     "password"
                                :path      [::login :pass]
                                :on-key-up (dispatch-on :enter [:auth/handle-login])}]]]
     [widgets/error-notification ::login-error]]]
   [:> ui/Modal.Actions
    [:> ui/Button
     {:basic   true
      :color   "red"
      :floated "left"
      :on-click #(rf/dispatch [:auth/close-login-modal false])}
     "cancel"]
    [:> ui/Button
     {:primary true
      :on-click #(rf/dispatch [:auth/handle-login])}
     "login"]]])