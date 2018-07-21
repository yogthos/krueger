-- :name add-post! :<! :n
-- :doc create a new post row
INSERT INTO posts
  (title, author, preview, url, text, upvotes, downvotes, tags)
  VALUES (:title, :author, :preview, :url, :text, :upvotes, :downvotes, :tags)
  RETURNING id

-- :name post-by-id :? :1
-- :doc retrieve a post using the id
SELECT p.id, u.screenname as author, p.title, p.preview, p.url, p.text, p.tags, p.upvotes, p.downvotes, p.timestamp
  FROM posts p
  JOIN users u on author = email
  WHERE id = :id

-- :name post-previews :? :*
-- :doc retrieve previews given the offset
SELECT p.id, u.screenname as author, p.title, p.preview, p.url, p.tags, p.upvotes, p.downvotes, p.timestamp
  FROM posts p
  JOIN users u on author = email
  LIMIT :limit OFFSET :offset

-- :name total-posts :? :1
-- :doc count posts
SELECT count(*)
  FROM posts

-- :name upvote! :! :n
-- :doc increase post votes
UPDATE posts
  SET upvotes = upvotes + 1
  WHERE id = :id

--:name upvoted? :? :1
-- :doc selects the upvoted flag for the user and post
SELECT upvoted
  FROM post_votes
  WHERE email  = :email
  AND   postid = :postid

-- :name downvote! :! :n
-- :doc increase post votes
UPDATE posts
  SET downvotes = downvotes + 1
  WHERE id = :id

--:name downvoted? :? :1
-- :doc selects the downvoted flag for the user and post
SELECT downvoted
  FROM post_votes
  WHERE email  = :email
  AND   postid = :postid

--:name set-votes! :! :n
-- :doc sets the votes for the user and post
UPDATE post_votes
  SET upvoted  = :upvoted, downvoted = :downvoted
  WHERE email  = :email
  AND   postid = :postid
