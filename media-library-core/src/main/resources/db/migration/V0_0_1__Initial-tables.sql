CREATE TABLE assets (
    id UUID ,
    original_filename TEXT,
    created_at TIMESTAMP,

    PRIMARY KEY (id)
);

CREATE TABLE tags (
    id UUID ,
    name TEXT,
    created_at TIMESTAMP,

    PRIMARY KEY (id)
);