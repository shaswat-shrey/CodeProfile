CREATE TABLE contests (
    id BIGSERIAL PRIMARY KEY,
    platform VARCHAR(255) NOT NULL,
    contest_id VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    start_time TIMESTAMP NOT NULL,
    duration_seconds INT NOT NULL,
    url VARCHAR(255),
    status VARCHAR(255) NOT NULL
);

CREATE TABLE contest_participations (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    contest_id VARCHAR(255),
    platform VARCHAR(255),
    rank INT,
    rating_change INT,
    attended BOOLEAN NOT NULL
);