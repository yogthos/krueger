(ns krueger.db.posts
  (:require
    [krueger.db.core :refer [*db*]]
    [alpha-id.core :refer [encode-64 decode-64]]
    [camel-snake-kebab.extras :refer [transform-keys]]
    [camel-snake-kebab.core :refer [->kebab-case-keyword]]
    [cheshire.core :refer [generate-string parse-string]]
    [clojure.java.jdbc :as jdbc]
    [conman.core :as conman]
    [krueger.config :refer [env]]
    [mount.core :refer [defstate]]))

(conman/bind-connection *db*
                        "sql/comments.sql"
                        "sql/posts.sql"
                        "sql/tags.sql"
                        "sql/attachments.sql")

(defstate posts-per-page
  :start (or (env :posts-per-page) 10))

;;; posts
(defn get-post-previews [category page]
  ;;TODO: filter by category
  {:posts (map
            #(update % :id encode-64)
            (post-previews {:limit posts-per-page :offset page}))})

(defn save-post! [post]
  (add-post!
    (merge {:text      nil
            :url       nil
            :preview   nil
            :upvotes   0
            :downvotes 0
            :tags      nil}
           post)))

(defn get-post-by-slug [slug]
  (let [id (decode-64 slug)]
    (-> (post-by-id {:id id})
        (assoc :id slug)
        (update :tags #(when (not-empty %) (tags-by-ids {:ids %})))
        (assoc :comments (get-comments {:post id})))))

(defn upvote-comment! [email id]
  (jdbc/with-db-transaction [t-conn *db*]
    (when-not (upvoted? t-conn {:email email :id id})
      (upvote! t-conn {:id id})
      (set-votes! t-conn {:upvoted   true
                          :downvoted false
                          :email     email
                          :id        id}))))

(defn downvote-comment! [email id]
  (jdbc/with-db-transaction [t-conn *db*]
    (when-not (upvoted? t-conn {:email email :postid id})
      (downvote! t-conn {:id id})
      (set-votes! t-conn
                  {:upvoted   false
                   :downvoted true
                   :email     email
                   :id        id}))))

(defn add-post-comment! [comment]
  (-> (merge {:parent nil} comment)
      (update :post decode-64)
      (assoc :upvotes 0 :downvotes 0)
      (add-comment!)))

#_(add-post-comment!
    {:parent  1
     :author  "bob@bob.com"
     :content "another test comment"
     :post    "5"})

#_(jdbc/query *db* ["select * from comments"])

#_(tags-by-ids {:ids (:tags (first (jdbc/query *db* ["select * from posts"])))})
#_(tags-by-ids {:ids [1 2]})

#_(get-post-by-slug "5")

#_(save-post! {:title  "foo"
               :tags   [1 2]
               :text   "blah"
               :author "bob@bob.com"})

#_(get-post-previews "foo" 0)

#_(create-tag {:label "clojure" :description "stories related to Clojure"})