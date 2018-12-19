-- :name tags :? :*
-- :doc select all tags
select * from tags

-- :name tags-by-values :? :*
-- :doc select tags given a list of values
select * from tags where value in (:v*:values)

-- :name create-tag :! :1
-- :doc create a new tag for a topic
insert into tags (label, value, description)
values (:label, :value, :description)
