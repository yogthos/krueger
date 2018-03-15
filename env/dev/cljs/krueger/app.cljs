(ns ^:figwheel-no-load krueger.app
  (:require [krueger.core :as core]
            [devtools.core :as devtools]))

(enable-console-print!)

(devtools/install!)

(core/init!)
