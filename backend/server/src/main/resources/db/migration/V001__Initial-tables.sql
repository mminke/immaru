create TABLE collections (
    id UUID,
    name TEXT,
    created_at TIMESTAMP,

    PRIMARY KEY (id)
);

create TABLE assets (
    id UUID,
    collection_id UUID NOT NULL REFERENCES collections ON delete RESTRICT,
    original_filename TEXT,
    created_at TIMESTAMP,

    PRIMARY KEY (id)
);

create TABLE tags (
    id UUID,
    collection_id UUID NOT NULL REFERENCES collections ON delete CASCADE,
    name TEXT,
    created_at TIMESTAMP,

    PRIMARY KEY (id)
);
