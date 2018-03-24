(ns krueger.routes.services.attachments
  (:require [clojure.tools.logging :as log]
            [ring.util.http-response :refer :all]
            [krueger.layout :refer [error-page]]
            [krueger.db.core :as db]
            [krueger.routes.services.common :refer [handler]]
            [schema.core :as s])
  (:import [java.io ByteArrayOutputStream
                    ByteArrayInputStream
                    FileInputStream]))

(def AttachmentResult
  {(s/optional-key :name)  s/Str
   (s/optional-key :error) s/Str})

(defn file->byte-array [x]
  (with-open [input  (FileInputStream. x)
              buffer (ByteArrayOutputStream.)]
    (clojure.java.io/copy input buffer)
    (.toByteArray buffer)))

(handler upload-media! [{:keys [user-id] :as m} {:keys [tempfile filename content-type]}]
  (if (empty? filename)
    (bad-request "a file must be selected")
    (let [db-file-name (.replaceAll filename "[^a-zA-Z0-9-_\\.]" "")]
      (if (db/save-file! {:type content-type
                          :name db-file-name
                          :data (file->byte-array tempfile)})
        (ok {:name db-file-name})
        (bad-request {:error (str "Issue not found for: " (select-keys m [:user-id :support-issue-id]))})))))

(handler remove-media! [opts]
  (if-some [result (let [local-opts (dissoc opts :user-id)]
                     (db/delete-file<! local-opts)
                     (select-keys local-opts [:name]))]
    (ok result)
    (bad-request {:error (str "Issue not found for: " (select-keys opts [:user-id :support-issue-id]))})))

(handler load-file-data [file]
  (if-let [{:keys [type data]} (db/load-file-data (dissoc file :user-id))]
    (-> (ByteArrayInputStream. data)
        (ok)
        (content-type type))
    (error-page {:status 404
                 :title  "file not found"})))
