-- :name tags-by-ids :? :*
-- :doc select tags given a list of ids
select * from tags where id in (:v*:ids)
