(ns krueger.components.widgets
  (:require
    [re-frame.core :as rf]))

(defn input [type opts id placeholder path]
  [type
   (merge
     {:placeholder placeholder
      :on-focus    #((-> % .-target .-value (set! @(rf/subscribe [:input/value path]))))
      :on-blur     #(rf/dispatch [:input/set-value path (-> % .-target .-value)])}
     (when-not (= :textarea type)
       {:type type})
     opts)])

(defn form-input [type opts label id placeholder path optional?]
  [:div
   [:label label]
   (if optional?
     [input type opts id placeholder path]
     [:div
      [input type opts id placeholder path]
      [:span.glyphicon.glyphicon-asterisk]])])

(defn text-input [label id placeholder fields & [optional?]]
  (form-input :text {} label id placeholder fields optional?))

(defn textarea [rows label id placeholder fields & [optional?]]
  (form-input :textarea {:rows (or rows 5)} label id placeholder fields optional?))

(defn email-input [label id placeholder fields & [optional?]]
  (form-input :email {} label id placeholder fields optional?))

(defn password-input [label id placeholder fields & [optional?]]
  (form-input :password {} label id placeholder fields optional?))
