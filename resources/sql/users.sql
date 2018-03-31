-- :name create-user! :! :n
-- :doc creates a new user record
INSERT INTO users
  (screenname, email, pass)
  VALUES (:screenname, :email, :pass)

-- :name update-user! :! :n
-- :doc update an existing user record with the given email
UPDATE users
  SET screenname = :screenname
  WHERE email = :email

-- :name get-user :? :1
-- :doc retrieve a user given the id.
SELECT * FROM users
  WHERE email = :email
