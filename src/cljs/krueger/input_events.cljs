(ns krueger.input-events
  (:require
    [re-frame.core :as rf]))

(def events
  {:enter 13})

(defn dispatch-on [event dispatch-vector]
  (fn [e]
    (when (= (.-keyCode e) (events event))
      (rf/dispatch dispatch-vector))))