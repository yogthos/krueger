CREATE TABLE posts
(
  id            SERIAL PRIMARY KEY,
  author        TEXT      NOT NULL,
  title         CITEXT    NOT NULL,
  preview       CITEXT,
  url           CITEXT,
  text          CITEXT,
  tags          INTEGER[],
  upvotes       INTEGER,
  downvotes     INTEGER,
  timestamp     TIMESTAMP NOT NULL DEFAULT (now() AT TIME ZONE 'utc'),
  search_vector TSVECTOR
);
--;;
-- index our search vector for faster searches
CREATE INDEX idx_post_search_vector ON posts USING GIN (search_vector);
--;;
-- create the update/insert search_vector function for posts.
-- This trigger creates a concatenated, weighted vector across
-- title, url, and text.
-- for more info conuslt
-- http://www.postgresql.org/docs/9.1/static/textsearch-features.html#TEXTSEARCH-MANIPULATE-TSVECTOR
-- try switching to 'english_nostop' to avoid breaking on stop words
CREATE OR REPLACE FUNCTION tsvector_update_trigger_posts()
  RETURNS TRIGGER AS $searchable_post$
BEGIN
  new.search_vector :=
  setweight(to_tsvector('english', coalesce(new.title, '')), 'A') ||
  setweight(to_tsvector('english', coalesce(new.url, '')), 'B') ||
  setweight(to_tsvector('english', coalesce(new.text, '')), 'B');

  new.timestamp := now() AT TIME ZONE 'utc';
  RETURN new;
END;
$searchable_post$ LANGUAGE plpgsql;
--;;
-- updates the search vectors
CREATE TRIGGER posts_update_search_vector
BEFORE INSERT OR UPDATE ON posts
FOR EACH ROW EXECUTE PROCEDURE tsvector_update_trigger_posts();
--;;
-- create the find_post function
CREATE OR REPLACE FUNCTION find_post(TEXT)
  RETURNS TABLE(id INTEGER)
AS $$ SELECT
        p.id
      FROM posts p
        INNER JOIN (
                     SELECT DISTINCT
                       id,
                       ts_rank_cd(search_vector, to_tsquery('english', $1)) AS rank
                     FROM posts, to_tsquery('english', $1) query
                     WHERE query @@ search_vector
                     ORDER BY rank DESC
                   ) x ON x.id = p.id
      GROUP BY p.id
$$
LANGUAGE SQL;
