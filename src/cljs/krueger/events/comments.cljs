(ns krueger.events.comments
  (:require [re-frame.core :as rf]))

(rf/reg-event-fx
  :comments/submit
  (fn [_ {:keys [parent-id] :as comment}])
  )
