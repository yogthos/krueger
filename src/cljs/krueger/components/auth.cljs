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

(defn registration-modal []
  [:> ui/Modal
   {:closeOnDimmerClick false
    :open               @(rf/subscribe [::show-registration])}
   [:> ui/Modal.Header "User Registration"]
   [:> ui/Modal.Content
    [:> ui/Modal.Description
     [:> ui/Form
      [:> ui/Form.Field
       [:label "user name"]
       [:input {:placeholder "user name"}]]
      [:> ui/Form.Field
       [:label "email"]
       [:input {:placeholder "your email address"}]]
      [:> ui/Form.Field
       [:label "password"]
       [:input]]
      [:> ui/Form.Field
       [:label "confirm password"]
       [:input]]]]]
   [:> ui/Modal.Actions
    [:> ui/Button
     {:basic   true
      :color   "red"
      :floated "left"
      :onClick #(rf/dispatch [:auth/show-registration-modal false])}
     "cancel"]
    [:> ui/Button
     {:primary true
      :onClick #(rf/dispatch [:auth/show-registration-modal false])}
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
    (assoc db ::login-error error)))

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
             (dissoc ::show-login)
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
       [widgets/text-input {:label "email" :path [::login-email]}]]
      [:> ui/Form.Field
       [widgets/text-input {:label "password" :path [::login-pass]}]]]
     (when-let [error @(rf/subscribe [::login-error])]
       [:p (str error)])]]
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