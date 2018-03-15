(ns krueger.env
  (:require [selmer.parser :as parser]
            [clojure.tools.logging :as log]
            [krueger.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init
   (fn []
     (parser/cache-off!)
     (log/info "\n-=[krueger started successfully using the development profile]=-"))
   :stop
   (fn []
     (log/info "\n-=[krueger has shut down successfully]=-"))
   :middleware wrap-dev})
