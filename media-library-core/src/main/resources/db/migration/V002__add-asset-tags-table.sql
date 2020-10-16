CREATE TABLE asset_tags (
    asset_id UUID REFERENCES assets ON DELETE CASCADE,
    tag_id UUID REFERENCES tags ON DELETE CASCADE,

    PRIMARY KEY (asset_id, tag_id)
);