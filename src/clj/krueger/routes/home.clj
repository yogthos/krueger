(ns krueger.routes.home
  (:require
    [clojure.java.io :as io]
    [clojure.tools.logging :as log]
    [krueger.db.users :as user-db]
    [krueger.layout :as layout]
    [krueger.middleware :as middleware]
    [krueger.client-routes :refer [routes]]
    [ring.util.http-response :as response]))

(defn home-page [_]
  (layout/render "home.html"))

(defn issue-token [{{{:keys [token]} :path} :parameters session :session}]
  (log/info "registration link activated for" token)
  (try
    (let [user (user-db/activate-user! token)]
      (-> (response/found "/")
          (assoc :session (assoc session :identity user))))
    (catch IllegalArgumentException e
      (layout/error-page
        {:status  400
         :title   "Registration failed!"
         :message (.getMessage e)}))))

(defn home-routes []
  (into
    [""
     {:middleware [middleware/wrap-csrf]}
     ["/registration/:token" {:get issue-token}]]
    (for [[route] routes]
      [route {:get home-page}])))


