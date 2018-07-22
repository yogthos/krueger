-- :name tags :? :*
-- :doc select all tags
select * from tags

-- :name tags-by-ids :? :*
-- :doc select tags given a list of ids
select * from tags where id in (:v*:ids)

-- :name create-tag :<! :1
-- :doc create a new tag for a topic
insert into tags (label, description)
values (:label, :description)
returning id
