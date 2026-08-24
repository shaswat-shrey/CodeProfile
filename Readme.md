Coding Tracker

Overview

Coding Tracker is a robust Spring Boot backend application that aggregates competitive programming and development activity from platforms such as LeetCode, Codeforces, and GitHub. It provides developers with a unified dashboard to track their progress, analyze their coding habits, view upcoming contests, and receive personalized recommendations and email notifications to keep their coding streaks alive.

Architecture

       Client
         │
         ▼
Spring Security + JWT
         │
         ▼
  REST Controllers
         │
         ▼
   Service Layer ◄──► Resilience4j (Circuit Breaker/Retry)
         │
         ▼
 PostgreSQL / Redis
         │
         ▼
   External APIs (LeetCode, Codeforces, GitHub)


Tech Stack

Backend

Java 21

Spring Boot 3

Spring Security (Stateless JWT Authentication)

Spring Data JPA

Resilience4j (Circuit Breaker & Retry)

Spring Cache

Spring Mail & Thymeleaf (Email Templating)

Database

PostgreSQL

Redis (Caching & Rate Limiting)

Flyway (Database Migrations)

External APIs

LeetCode GraphQL API

Codeforces REST API

GitHub REST API

Testing

JUnit 5

Mockito

Spring Boot Test

Testcontainers (PostgreSQL & Redis)

DevOps & Deployment

Docker & Docker Compose

GitHub Actions (CI/CD Pipeline)

Features

JWT Authentication: Secure, stateless user registration and login.

DTO-Based API Design: Clean separation of internal entities and external representations.

Data Validation & Error Handling: Comprehensive input validation and a global exception handler.

Platform Integrations: Live data fetching from LeetCode, Codeforces, and GitHub.

Historical Snapshots: Automated daily snapshots of user statistics to calculate streaks and growth over time.

Unified Dashboard: Aggregated statistics, recent activity, and upcoming contests in a single API response.

Analytics & Recommendations: Custom logic to analyze difficulty distributions, calculate coding streaks, and provide actionable advice.

Automated Notifications: Email alerts for upcoming contests, streak warnings, and weekly progress reports.

Caching: Redis integration to cache external API responses and heavy dashboard queries.

Resiliency: Circuit breakers and retry mechanisms for fault-tolerant external API calls.

Rate Limiting: Redis-based API rate limiting to prevent abuse.

Containerization: Fully Dockerized application with a ready-to-use docker-compose.yml.

CI/CD: Automated testing and build verification via GitHub Actions.

API Endpoints

Authentication

POST /auth/register - Register a new user

POST /auth/login - Authenticate and receive a JWT

Dashboard & Profile

GET /dashboard - Get unified dashboard data

GET /profile - Get basic user profile info

GET /profile/resume-stats - Get aggregated stats for resume building

LeetCode

POST /leetcode/connect - Link a LeetCode account

GET /leetcode/me - Get logged-in user's LeetCode stats

POST /leetcode/refresh - Force refresh LeetCode cache

GET /leetcode/{username} - Look up any LeetCode user

Codeforces

POST /codeforces/connect - Link a Codeforces handle

GET /codeforces/me - Get logged-in user's Codeforces stats

GET /codeforces/history - Get user's Codeforces contest history

GitHub

POST /github/connect - Link a GitHub account

GET /github/me - Get logged-in user's GitHub profile

GET /github/repos - Get user's repositories

GET /github/languages - Get user's language distribution

Analytics & Recommendations

GET /analytics/summary - Get weekly growth and overall summary

GET /analytics/streak - Get current coding streak

GET /recommendations - Get personalized coding advice

Contests & Notifications

GET /contests/upcoming - List upcoming contests across platforms

GET /contests/live - List currently running contests

GET /notifications - View user's inbox notifications

API Documentation (Swagger UI):

Once the application is running, view the interactive API documentation at:

http://localhost:8080/swagger-ui/index.html

Database Design

The database is heavily normalized and tracks users, their connected profiles, and chronological snapshots of their statistics to calculate growth over time.

      users
        │
        ├── 1:1 ──► leetcode_profiles ── 1:N ──► leetcode_stats_snapshots
        │
        ├── 1:1 ──► codeforces_profiles ── 1:N ──► codeforces_snapshots
        │
        ├── 1:1 ──► github_profiles ── 1:N ──► github_snapshots
        │
        ├── 1:N ──► contest_participations
        │
        └── 1:N ──► notifications


Running the Application Locally

Clone the repository.

Ensure Docker and Docker Compose are installed on your machine.

Rename .env.example to .env and fill in your secrets (JWT Secret, Database Passwords, Email App Passwords).

Run the application using Docker Compose:

docker-compose up --build -d


The application will start on http://localhost:8080.