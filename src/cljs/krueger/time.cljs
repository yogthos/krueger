(ns krueger.time
  (:require [cljs-time.core :as t]
            [cljs-time.coerce :as c]))

(defn ago [date]
  (let [interval (t/interval (c/from-date date) (t/now))]
    (reduce
      (fn [_ [in-period label]]
        (let [period (in-period interval)]
          (if (pos? period)
            (reduced (str period " " label)))))
      [[t/in-years "years"]
       [t/in-months "months"]
       [t/in-days "days"]
       [t/in-hours "hours"]
       [t/in-minutes "minutes"]])))




