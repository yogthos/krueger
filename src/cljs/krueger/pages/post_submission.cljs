(ns krueger.pages.post-submission
  (:require
    [cljsjs.semantic-ui-react :as ui]
    [re-frame.core :as rf]
    [kee-frame.core :as kf]
    [krueger.components.widgets :as widgets]))

(defn submit-post-page
  [:> ui/Form
   [:> ui/Form.Field
    [widgets/text-input {:label "URL:" :path [::post :url]}]]
   [:> ui/Form.Field
    [widgets/text-input {:label "Title:" :path [::post :title]}]]
   [:> ui/Form.Field
    [widgets/text-input {:label "Tags:" :path [::registration-pass]}]]
   [:> ui/Form.Field
    [widgets/textarea {:label "Text:" :path [::registration-pass-confirm]}]]
   [:div
    [:> ui/Button
     {:basic   true
      :color   "red"
      :floated "left"
      :onClick #(rf/dispatch [:auth/show-registration-modal false])}
     "cancel"]
    [:> ui/Button
     {:primary true
      :onClick #(rf/dispatch [:auth/handle-registration false])}
     "submit"]]])
