CREATE TABLE images (
    id UUID NOT NULL REFERENCES assets ON DELETE CASCADE,
    image_width integer not null,
    image_height integer not null,

    PRIMARY KEY (id)
);

ALTER TABLE assets
    ADD COLUMN original_created_at TIMESTAMP,
    ADD COLUMN last_modified_at TIMESTAMP;
