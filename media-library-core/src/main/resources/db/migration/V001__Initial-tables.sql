CREATE TABLE collections (
    id UUID ,
    name TEXT,
    created_at TIMESTAMP,

    PRIMARY KEY (id)
);

CREATE TABLE assets (
    id UUID ,
    collection_id UUID NOT NULL,
    original_filename TEXT,
    created_at TIMESTAMP,

    PRIMARY KEY (id)
);

CREATE TABLE tags (
    id UUID ,
    collection_id UUID NOT NULL,
    name TEXT,
    created_at TIMESTAMP,

    PRIMARY KEY (id)
);
