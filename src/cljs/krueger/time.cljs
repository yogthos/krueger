(ns krueger.time
  (:require [cljs-time.core :as t]
            [cljs-time.coerce :as c]))

(defn ago [date]
  (let [start (c/from-date date)
        end (t/now)]
    (if (<= (.getTime start) (.getTime end))
      (let [interval (t/interval start end)]
        (reduce
          (fn [_ [in-period label]]
            (let [period (in-period interval)]
              (if (pos? period)
                (reduced (str period " " label " ago")))))
          [[t/in-years "years"]
           [t/in-months "months"]
           [t/in-days "days"]
           [t/in-hours "hours"]
           [t/in-minutes "minutes"]]))
      (str "just now"))))




