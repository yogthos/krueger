(ns ^:figwheel-no-load krueger.app
  (:require [krueger.core :as core]
            [cljs.spec.alpha :as s]
            [expound.alpha :as expound]
            [devtools.core :as devtools]
            [reagent.core :as r]))

(set! s/*explain-out* expound/printer)

(enable-console-print!)

(devtools/install!)

(core/init!)

(defn mount-components []
  (r/render [#'core/root-component] (.getElementById js/document "app")))