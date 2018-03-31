(ns krueger.email
  (:require [krueger.config :refer [env]]
            [postal.core :as email]
            [hiccup.core :as html]
            [selmer.parser :as parser]))

(defn send-registration [to website invitation-link]
  (let [{:keys [host user pass port domain ssl]} (:email env)]
    (email/send-message
      {:host host
       :user user
       :pass pass
       :port port
       :ssl  ssl}
      {:from    (str "registration@" domain)
       :to      to
       :subject (str "Join " website)
       :body    [{:type    "text/html"
                  :content (parser/render-file
                             "registration-email.html"
                             {:website website :invitation-link invitation-link})}]})))
