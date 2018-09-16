(ns krueger.components.auth
  (:require
    [krueger.components.widgets :as widgets]
    [krueger.input-events :refer [dispatch-on]]
    [krueger.terminology :refer [term]]
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
            :resource-id :register
            :params      (::registration db)
            :error-event [::registration-error]}})
  (fn [{:keys [db]} _]
    {:db (-> db
             (dissoc ::registration ::show-registration)
             (assoc :auth/user (::registration db)))}))

(defn registration-modal []
  [:> ui/Modal
   {:close-on-dimmer-click false
    :open                  @(rf/subscribe [::show-registration])
    :size                  "tiny"}
   [:> ui/Modal.Header (term :registration/header)]
   [:> ui/Modal.Content
    [:> ui/Modal.Description
     [:> ui/Form
      [:> ui/Form.Field
       [widgets/text-input
        {:label (term :registration/name)
         :path  [::registration :id]}]]
      [:> ui/Form.Field
       [widgets/email-input
        {:label (term :registration/email)
         :path  [::registration :email]}]]
      [:> ui/Form.Field
       [widgets/password-input
        {:label (term :registration/password)
         :path  [::registration :pass]}]]
      [:> ui/Form.Field
       [widgets/password-input
        {:label (term :registration/confirm-password)
         :path  [::registration :pass-confirm]}]]]
     [widgets/error-notification ::registration-error]]]
   [:> ui/Modal.Actions
    [:> ui/Button
     {:basic    true
      :color    "red"
      :floated  "left"
      :on-click #(rf/dispatch [:auth/show-registration-modal false])}
     (term :cancel)]
    [widgets/ajax-button
     {:primary     true
      :resource-id :register
      :on-click    #(rf/dispatch [:auth/handle-registration false])}
     (term :registration/register)]]])

(rf/reg-event-fx
  :auth/login-modal-shown
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
            :resource-id :login
            :error-event [::login-error]}})
  (fn [{:keys [db]} [{:keys [user]}]]
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
    :open                  @(rf/subscribe [::show-login])
    :size                  "tiny"}
   [:> ui/Modal.Header (term :login/header)]
   [:> ui/Modal.Content
    [:> ui/Modal.Description
     [:> ui/Form
      [:> ui/Form.Field
       [widgets/email-input
        {:label (term :login/email)
         :path  [::login :email]}]]
      [:> ui/Form.Field
       [widgets/password-input
        {:label     (term :login/password)
         :path      [::login :pass]
         :on-key-up (dispatch-on :enter [:auth/handle-login])}]]]
     [widgets/error-notification ::login-error]]]
   [:> ui/Modal.Actions
    [:> ui/Button
     {:basic    true
      :color    "red"
      :floated  "left"
      :on-click #(rf/dispatch [:auth/login-modal-shown false])}
     (term :cancel)]
    [widgets/ajax-button
     {:primary     true
      :resource-id :login
      :on-click    #(rf/dispatch [:auth/handle-login])}
     (term :login/login)]]])