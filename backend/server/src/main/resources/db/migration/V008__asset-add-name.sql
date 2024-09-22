ALTER TABLE assets
    ADD COLUMN name TEXT NOT NULL DEFAULT '';

UPDATE assets
    SET  name = original_filename;