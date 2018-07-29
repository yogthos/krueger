-- :name add-comment! :<! :n
-- :doc add a new comment for a post
INSERT INTO comments
  (parent, author, content, post)
  VALUES (:parent, :author, :content, :post)
  RETURNING id

-- :name get-comments :? :*
-- :doc select all comments for the post
SELECT id, upvotes, downvotes, parent, author, content, timestamp FROM comments
  WHERE post = :post

-- :name upvote! :! :n
-- :doc increase comment votes
UPDATE comments
  SET upvotes = upvotes + 1
  WHERE id = :id

--:name upvoted? :? :1
-- :doc selects the upvoted flag for the user and comment
SELECT upvoted
  FROM comment_votes
  WHERE email     = :email
  AND   commentid = :commentid

-- :name downvote! :! :n
-- :doc increase comment votes
UPDATE comments
  SET downvotes = downvotes + 1
  WHERE id = :id

--:name downvoted? :? :1
-- :doc selects the downvoted flag for the user and post
SELECT downvoted
  FROM comment_votes
  WHERE email     = :email
  AND   commentid = :commentid

--:name set-votes! :! :n
-- :doc sets the votes for the user and post
UPDATE comment_votes
  SET upvoted     = :upvoted, downvoted = :downvoted
  WHERE email     = :email
  AND   commentid = :commentid