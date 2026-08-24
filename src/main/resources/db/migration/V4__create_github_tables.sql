CREATE TABLE github_profiles (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id),
    connected_at TIMESTAMP,
    last_synced_at TIMESTAMP
);

CREATE TABLE github_snapshots (
    id BIGSERIAL PRIMARY KEY,
    profile_id BIGINT NOT NULL REFERENCES github_profiles(id),
    followers INT NOT NULL,
    following INT NOT NULL,
    public_repos INT NOT NULL,
    total_stars INT NOT NULL,
    fetched_at TIMESTAMP
);