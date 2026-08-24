CREATE TABLE codeforces_profiles (
    id BIGSERIAL PRIMARY KEY,
    handle VARCHAR(255) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id),
    connected_at TIMESTAMP,
    last_synced_at TIMESTAMP
);

CREATE TABLE codeforces_snapshots (
    id BIGSERIAL PRIMARY KEY,
    profile_id BIGINT NOT NULL REFERENCES codeforces_profiles(id),
    rating INT,
    max_rating INT,
    rank VARCHAR(255),
    fetched_at TIMESTAMP
);