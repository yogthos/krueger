(ns krueger.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init
   (fn []
     (log/info "\n-=[krueger started successfully]=-"))
   :stop
   (fn []
     (log/info "\n-=[krueger has shut down successfully]=-"))
   :middleware identity})
