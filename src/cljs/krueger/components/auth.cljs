(ns krueger.components.auth
  (:require
    [krueger.components.widgets :as widgets]
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
            :params      {:screenname   (::registration-screenname db)
                          :email        (::registration-email db)
                          :pass         (::registration-pass db)
                          :pass-confirm (::registration-pass-confirm db)}
            :error-event [::login-error]}})
  (fn [{:keys [db]} [{:keys [user]}]]
    {:db (-> db
             (dissoc ::show-registration
                     ::registration-screenname
                     ::registration-email
                     ::registration-pass
                     ::registration-pass-confirm)
             (assoc :auth/user user))}))

(defn registration-modal []
  [:> ui/Modal
   {:closeOnDimmerClick false
    :open               @(rf/subscribe [::show-registration])}
   [:> ui/Modal.Header "User Registration"]
   [:> ui/Modal.Content
    [:> ui/Modal.Description
     [:> ui/Form
      [:> ui/Form.Field
       [widgets/password-input {:label "user name" :path [::reg-screenname]}]]
      [:> ui/Form.Field
       [widgets/email-input {:label "email" :path [::registration-email]}]]
      [:> ui/Form.Field
       [widgets/password-input {:label "password" :path [::registration-pass]}]]
      [:> ui/Form.Field
       [widgets/password-input {:label "confirm password" :path [::registration-pass-confirm]}]
       [:label "confirm password"]
       [:input]]]
     (when-let [error @(rf/subscribe [::registration-error])]
       [:> ui/Message {:negative true}
        [:> ui/Message.Header (str error)]])]]
   [:> ui/Modal.Actions
    [:> ui/Button
     {:basic   true
      :color   "red"
      :floated "left"
      :onClick #(rf/dispatch [:auth/show-registration-modal false])}
     "cancel"]
    [:> ui/Button
     {:primary true
      :onClick #(rf/dispatch [:auth/handle-registration false])}
     "register"]]])

(rf/reg-event-db
  :auth/show-login-modal
  (fn [db [_ show?]]
    (assoc db ::show-login show?)))

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
            :params      {:email (::login-email db)
                          :pass  (::login-pass db)}
            :error-event [::login-error]}})
  (fn [{:keys [db]} [{:keys [user]}]]
    {:db (-> db
             (dissoc ::show-login
                     ::login-email
                     ::login-pass)
             (assoc :auth/user user))}))

(kf/reg-chain
  :auth/handle-logout
  (fn [_ _]
    {:http {:method      :post
            :url         "/api/logout"
            :error-event [:common/set-error]}})
  (fn [{:keys [db]} _]
    {:db (dissoc db :auth/user)}))

(defn login-modal []
  [:> ui/Modal
   {:closeOnDimmerClick false
    :open               @(rf/subscribe [::show-login])}
   [:> ui/Modal.Header "User Login"]
   [:> ui/Modal.Content
    [:> ui/Modal.Description
     [:> ui/Form
      [:> ui/Form.Field
       [widgets/email-input {:label "email" :path [::login-email]}]]
      [:> ui/Form.Field
       [widgets/password-input {:label "password" :path [::login-pass]}]]]
     (when-let [error @(rf/subscribe [::login-error])]
       [:> ui/Message {:negative true}
        [:> ui/Message.Header (str error)]])]]
   [:> ui/Modal.Actions
    [:> ui/Button
     {:basic   true
      :color   "red"
      :floated "left"
      :onClick #(rf/dispatch [:auth/show-login-modal false])}
     "cancel"]
    [:> ui/Button
     {:primary true
      :onClick #(rf/dispatch [:auth/handle-login])}
     "login"]]])