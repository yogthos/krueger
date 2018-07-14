(ns krueger.components.auth
  (:require
    [cljsjs.semantic-ui-react :as ui]
    [re-frame.core :as rf]))

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

(defn login-modal []
  [:> ui/Modal
   {:closeOnDimmerClick false
    :open               @(rf/subscribe [::show-login])}
   [:> ui/Modal.Header "User Login"]
   [:> ui/Modal.Content
    [:> ui/Modal.Description
     [:> ui/Form
      [:> ui/Form.Field
       [:label "email"]
       [:input {:placeholder "your email address"}]]
      [:> ui/Form.Field
       [:label "password"]
       [:input]]]]]
   [:> ui/Modal.Actions
    [:> ui/Button
     {:basic   true
      :color   "red"
      :floated "left"
      :onClick #(rf/dispatch [:auth/show-login-modal false])}
     "cancel"]
    [:> ui/Button
     {:primary true
      :onClick #(rf/dispatch [:auth/show-login-modal false])}
     "login"]]])