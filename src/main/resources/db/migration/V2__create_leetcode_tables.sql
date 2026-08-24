CREATE TABLE leetcode_profiles (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id),
    connected_at TIMESTAMP,
    last_synced_at TIMESTAMP
);

CREATE TABLE leetcode_stats_snapshots (
    id BIGSERIAL PRIMARY KEY,
    profile_id BIGINT NOT NULL REFERENCES leetcode_profiles(id),
    total_solved INT NOT NULL,
    easy_solved INT NOT NULL,
    medium_solved INT NOT NULL,
    hard_solved INT NOT NULL,
    ranking INT NOT NULL,
    fetched_at TIMESTAMP
);