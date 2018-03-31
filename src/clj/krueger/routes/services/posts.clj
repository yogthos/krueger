(ns krueger.routes.services.posts
  (:require
    [krueger.routes.services.comments :refer [Comment]]
    [schema.core :as s])
  (:import java.util.Date))

;;post can either have a URL pointing to an external link or
;;text for self submition/question post, e.g: how do I setup a REPL
(s/defschema Post {:id                       String
                   :author                   String
                   :tags                     [String]
                   :title                    String
                   (s/optional-key :preview) String
                   (s/optional-key :url)     String
                   (s/optional-key :text)    String
                   :timestamp                Date
                   :comments                 [Comment]})

(s/defschema PostSubmission
  {:title                 String
   (s/optional-key :tags) [String]
   (s/optional-key :url)  String
   (s/optional-key :text) String})

(s/defschema PostPreview
  {:id                       String
   :author                   String
   :tags                     [String]
   (s/optional-key :preview) String
   :title                    String
   (s/optional-key :url)     String
   :timestamp                Date
   :votes                    s/Num})

(s/defschema PostPreviews {:pages s/Num
                           :page  [PostPreview]})
