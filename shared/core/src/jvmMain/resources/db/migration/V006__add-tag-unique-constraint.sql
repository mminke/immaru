ALTER TABLE tags ADD CONSTRAINT unique_tag_names_per_collection UNIQUE ("collection_id", "name");
