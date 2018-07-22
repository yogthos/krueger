(ns krueger.routes.services.terminology
  (:require
    [krueger.config :refer [env]]
    [krueger.db.posts :as posts-db]
    [krueger.routes.services.posts :as posts]
    [clojure.edn :as edn]
    [clojure.java.io :as io]
    [schema.core :as s])
  (:import java.io.PushbackReader))

(s/defschema Terminology
  {:dictionary {s/Keyword {s/Keyword s/Any}}
   :tags       {s/Num posts/Tag}})

(def dictionary
  (memoize #(-> (io/resource (:dictionary env))
                (io/reader)
                (PushbackReader.)
                (edn/read))))

(defn terminology []
  {:dictionary (dictionary)
   :tags       (reduce (fn [m tag] (assoc m (:id tag) tag)) {} (posts-db/tags))})