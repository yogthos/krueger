(ns krueger.input-events
  (:require
    [re-frame.core :as rf]))

(defn dispatch-on-enter [dispatch-vector]
  (fn [e]
    (when (= (.-keyCode e) 13)
      (rf/dispatch (conj dispatch-vector e)))))