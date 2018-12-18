(ns krueger.pages.admin
  (:require
    [reagent.core :as r]
    [krueger.components.widgets.tag-editor :as tag-editor]))

(defn admin-page []
  [tag-editor/tag-editor])


