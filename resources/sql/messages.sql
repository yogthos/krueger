-- :name add-message! :<! :n
-- :doc add a new message
INSERT INTO messages
  (recipient, author, content)
  VALUES (:recipient, :author, :content)
  RETURNING id

-- :name get-messages :? :*
-- :doc select all comments for the post
SELECT id, recipient, author, content, timestamp FROM messages
  WHERE recipient = :recipient
  LIMIT :limit OFFSET :offset

-- :name get-unread-messages :? :*
-- :doc select all comments for the post
SELECT id, recipient, author, content, timestamp FROM messages
  WHERE recipient = :recipient AND unread = true
  LIMIT :limit OFFSET :offset