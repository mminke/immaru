CREATE TABLE videos (
    id UUID NOT NULL REFERENCES assets ON DELETE CASCADE,
    frame_rate integer not null,
    width integer not null,
    height integer not null,

    PRIMARY KEY (id)
);

ALTER TABLE images
    RENAME COLUMN image_width TO width;

ALTER TABLE images
    RENAME COLUMN image_height TO height;