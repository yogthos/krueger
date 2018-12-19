(ns krueger.components.widgets
  (:require
    [re-frame.core :as rf]
    [cljsjs.semantic-ui-react :as ui]))

(rf/reg-event-db
  :input/set-value
  (fn [db [_ path value]]
    (assoc-in db path value)))

(rf/reg-sub
  :input/value
  (fn [db [_ path]]
    (get-in db path)))

(defn input [type path opts]
  [type
   (merge
     {:value     @(rf/subscribe [:input/value path])
      :on-change #(rf/dispatch [:input/set-value path (-> % .-target .-value)])}
     opts)])

(defn tag-dropdown [{:keys [path options]}]
  [:> ui/Dropdown
   {:fluid     true
    :multiple  true
    :search    true
    :selection true
    :options   (clj->js options)
    :value     (clj->js @(rf/subscribe [:input/value path]))
    :on-change (fn [event data]
                 (rf/dispatch [:input/set-value path (:value (js->clj data :keywordize-keys true))]))}])

(defn form-input [type {:keys [label path optional?] :as opts}]
  (let [opts (dissoc opts :label :path :optional?)]
    [:div
     [:label label]
     (if optional?
       [input type path opts]
       [:div
        [input type path opts]
        [:span.glyphicon.glyphicon-asterisk]])]))

(defn text-input [opts]
  (form-input :input (assoc opts :type :text)))

(defn textarea [opts]
  (form-input :textarea (update opts :rows #(or % 5))))

(defn email-input [opts]
  (form-input :input (assoc opts :type :email)))

(defn password-input [opts]
  (form-input :input (assoc opts :type :password)))

(defn error-notification [error-path]
  (when-let [error @(rf/subscribe [error-path])]
    [:> ui/Message {:negative true}
     [:> ui/Message.Header (str error)]]))

(defn ajax-button [opts label]
  [:> ui/Button
   (-> opts
       (assoc :disabled @(rf/subscribe [:http/loading? (:resource-id opts)]))
       (dissoc :resource-id))
   label])

(defn spinner []
  [:div.sk-circle
   [:div.sk-circle1.sk-child]
   [:div.sk-circle2.sk-child]
   [:div.sk-circle3.sk-child]
   [:div.sk-circle4.sk-child]
   [:div.sk-circle5.sk-child]
   [:div.sk-circle6.sk-child]
   [:div.sk-circle7.sk-child]
   [:div.sk-circle8.sk-child]
   [:div.sk-circle9.sk-child]
   [:div.sk-circle10.sk-child]
   [:div.sk-circle11.sk-child]
   [:div.sk-circle12.sk-child]])
