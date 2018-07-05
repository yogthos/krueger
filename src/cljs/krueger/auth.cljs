(ns krueger.auth
  (:require
    [re-frame.core :as rf]))

(rf/reg-sub
  :auth/user
  (fn [db _]
    (:user db)))

(rf/reg-event-fx
  :auth/login
  (fn [{:keys [db]} _]
    ))

(rf/reg-event-fx
  :auth/logout
  (fn [{:keys [db]} _]
    ))


