(ns krueger.input-events
  (:require
    [re-frame.core :as rf]))

(def events
  {:up 38
   :down 40
   :enter 13
   :escape 27
   :backspace 8})

(defn dispatch-on [event dispatch-vector]
  (fn [e]
    (when (= (.-keyCode e) (events event))
      (rf/dispatch dispatch-vector))))