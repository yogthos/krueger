(ns krueger.effects
  (:require
    [ajax.core :as ajax]
    [re-frame.core :as rf]))

(rf/reg-fx
  :ajax
  (fn [comment parent-id]
    (ajax/POST "/comment"
               {:params        comment
                :handler       #(rf/dispatch [:comment/add comment parent-id])
                :error-handler #(rf/dispatch [:error {:message "failed to submit the comment"
                                                      :cause   "unknown"}])})))
