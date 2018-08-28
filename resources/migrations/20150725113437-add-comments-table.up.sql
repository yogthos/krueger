CREATE TABLE comments
(
  post          INTEGER,
  id            SERIAL PRIMARY KEY,
  upvotes       INTEGER default 0,
  downvotes     INTEGER default 0,
  parent        INTEGER,
  author        INTEGER   NOT NULL,
  content       CITEXT    NOT NULL,
  timestamp     TIMESTAMP NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
  search_vector TSVECTOR
);
--;;
-- index our search vector for faster searches
CREATE INDEX idx_search_vector ON comments USING GIN (search_vector);
--;;
-- create the update/insert search_vector function for comments.
-- This trigger creates a concatenated, weighted vector across
-- title, url, and text.
-- for more info conuslt
-- http://www.postgresql.org/docs/9.1/static/textsearch-features.html#TEXTSEARCH-MANIPULATE-TSVECTOR
CREATE OR REPLACE FUNCTION tsvector_update_trigger_comments()
  RETURNS TRIGGER AS $searchable_comment$
BEGIN
  new.search_vector :=
  setweight(to_tsvector('english', coalesce(new.content, '')), 'A');

  new.timestamp := now() AT TIME ZONE 'utc';
  RETURN new;
END;
$searchable_comment$ LANGUAGE plpgsql;
--;;
-- updates the search vectors
CREATE TRIGGER comments_update_search_vector
BEFORE INSERT OR UPDATE ON comments
FOR EACH ROW EXECUTE PROCEDURE tsvector_update_trigger_comments();
--;;
-- create the find_post function
CREATE OR REPLACE FUNCTION find_comment(TEXT)
  RETURNS TABLE(id INTEGER)
AS $$ SELECT
        c.id
      FROM comments c
        INNER JOIN (
                     SELECT DISTINCT
                       id,
                       ts_rank_cd(search_vector, to_tsquery('english', $1)) AS rank
                     FROM comments, to_tsquery('english', $1) query
                     WHERE query @@ search_vector
                     ORDER BY rank DESC
                   ) x ON x.id = c.id
      GROUP BY c.id
$$
LANGUAGE SQL;
