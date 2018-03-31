(ns krueger.routes.services.comments
  (:require
    [schema.core :as s])
  (:import java.util.Date))

(s/defschema Comment
             {:id                      s/Num
              (s/optional-key :parent) s/Num
              :author                  String
              :content                 String
              :timestamp               Date
              :upvotes                 s/Num
              :downvotes               s/Num})

(s/defschema CommentSubmission
             {:post                    s/Num
              (s/optional-key :parent) s/Num
              :author                  String
              :content                 String})
