<div align="center">

# 📊 Coding Tracker

**A unified backend for tracking competitive programming and development activity across platforms**

[![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3-6DB33F?logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-Database-4169E1?logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Redis](https://img.shields.io/badge/Redis-Cache-DC382D?logo=redis&logoColor=white)](https://redis.io/)
[![Docker](https://img.shields.io/badge/Docker-Ready-2496ED?logo=docker&logoColor=white)](https://www.docker.com/)
[![License](https://img.shields.io/badge/License-MIT-lightgrey.svg)](#license)

</div>

---

## 📖 Overview

**Coding Tracker** is a robust Spring Boot backend application that aggregates competitive programming and development activity from platforms such as **LeetCode**, **Codeforces**, and **GitHub**. It gives developers a unified dashboard to track progress, analyze coding habits, view upcoming contests, and receive personalized recommendations and email notifications to keep their coding streaks alive.

---

## 📑 Table of Contents

- [Architecture](#-architecture)
- [Tech Stack](#-tech-stack)
- [Features](#-features)
- [API Endpoints](#-api-endpoints)
- [Database Design](#-database-design)
- [Getting Started](#-getting-started)
- [API Documentation](#-api-documentation)

---

## 🏗 Architecture

```
                     ┌────────────────┐
                     │     Client     │
                     └───────┬────────┘
                             │
                             ▼
                ┌────────────────────────┐
                │  Spring Security + JWT │
                └───────────┬────────────┘
                             │
                             ▼
                ┌────────────────────────┐
                │    REST Controllers    │
                └───────────┬────────────┘
                             │
                             ▼
                ┌────────────────────────┐        ┌───────────────────────────┐
                │      Service Layer     │◄──────►│  Resilience4j (Circuit    │
                │                        │        │  Breaker / Retry)          │
                └───────────┬────────────┘        └───────────────────────────┘
                             │
                             ▼
                ┌────────────────────────┐
                │   PostgreSQL / Redis   │
                └───────────┬────────────┘
                             │
                             ▼
                ┌────────────────────────┐
                │      External APIs     │
                │  LeetCode · Codeforces │
                │        · GitHub        │
                └────────────────────────┘
```

---

## 🛠 Tech Stack

### Backend
| Component | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 3 |
| Security | Spring Security (Stateless JWT Authentication) |
| Persistence | Spring Data JPA |
| Resiliency | Resilience4j (Circuit Breaker & Retry) |
| Caching | Spring Cache |
| Email | Spring Mail & Thymeleaf (Email Templating) |

### Database
| Component | Purpose |
|---|---|
| PostgreSQL | Primary relational data store |
| Redis | Caching & rate limiting |
| Flyway | Database migrations |

### External APIs
- LeetCode GraphQL API
- Codeforces REST API
- GitHub REST API

### Testing
- JUnit 5
- Mockito
- Spring Boot Test
- Testcontainers (PostgreSQL & Redis)

### DevOps & Deployment
- Docker & Docker Compose
- GitHub Actions (CI/CD Pipeline)

---

## ✨ Features

- 🔐 **JWT Authentication** — Secure, stateless user registration and login
- 📦 **DTO-Based API Design** — Clean separation of internal entities and external representations
- ✅ **Data Validation & Error Handling** — Comprehensive input validation and a global exception handler
- 🔗 **Platform Integrations** — Live data fetching from LeetCode, Codeforces, and GitHub
- 📈 **Historical Snapshots** — Automated daily snapshots of user statistics to calculate streaks and growth over time
- 🖥 **Unified Dashboard** — Aggregated statistics, recent activity, and upcoming contests in a single API response
- 🧠 **Analytics & Recommendations** — Custom logic to analyze difficulty distributions, calculate coding streaks, and provide actionable advice
- 📧 **Automated Notifications** — Email alerts for upcoming contests, streak warnings, and weekly progress reports
- ⚡ **Caching** — Redis integration to cache external API responses and heavy dashboard queries
- 🛡 **Resiliency** — Circuit breakers and retry mechanisms for fault-tolerant external API calls
- 🚦 **Rate Limiting** — Redis-based API rate limiting to prevent abuse
- 🐳 **Containerization** — Fully Dockerized application with a ready-to-use `docker-compose.yml`
- 🔄 **CI/CD** — Automated testing and build verification via GitHub Actions

---

## 🔌 API Endpoints

### Authentication
| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/auth/register` | Register a new user |
| `POST` | `/auth/login` | Authenticate and receive a JWT |

### Dashboard & Profile
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/dashboard` | Get unified dashboard data |
| `GET` | `/profile` | Get basic user profile info |
| `GET` | `/profile/resume-stats` | Get aggregated stats for resume building |

### LeetCode
| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/leetcode/connect` | Link a LeetCode account |
| `GET` | `/leetcode/me` | Get logged-in user's LeetCode stats |
| `POST` | `/leetcode/refresh` | Force refresh LeetCode cache |
| `GET` | `/leetcode/{username}` | Look up any LeetCode user |

### Codeforces
| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/codeforces/connect` | Link a Codeforces handle |
| `GET` | `/codeforces/me` | Get logged-in user's Codeforces stats |
| `GET` | `/codeforces/history` | Get user's Codeforces contest history |

### GitHub
| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/github/connect` | Link a GitHub account |
| `GET` | `/github/me` | Get logged-in user's GitHub profile |
| `GET` | `/github/repos` | Get user's repositories |
| `GET` | `/github/languages` | Get user's language distribution |

### Analytics & Recommendations
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/analytics/summary` | Get weekly growth and overall summary |
| `GET` | `/analytics/streak` | Get current coding streak |
| `GET` | `/recommendations` | Get personalized coding advice |

### Contests & Notifications
| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/contests/upcoming` | List upcoming contests across platforms |
| `GET` | `/contests/live` | List currently running contests |
| `GET` | `/notifications` | View user's inbox notifications |

---

## 🗄 Database Design

The database is heavily normalized and tracks users, their connected profiles, and chronological snapshots of their statistics to calculate growth over time.

```
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
```

---

## 🚀 Getting Started

Run the application locally in four steps:

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd coding-tracker
   ```

2. **Ensure prerequisites are installed**
   Docker and Docker Compose must be available on your machine.

3. **Configure environment variables**
   Rename `.env.example` to `.env` and fill in your secrets (JWT secret, database passwords, email app passwords).

4. **Start the application**
   ```bash
   docker-compose up --build -d
   ```

The application will start on **`http://localhost:8080`**.

---

## 📚 API Documentation

Once the application is running, explore the interactive Swagger UI at:

👉 **http://localhost:8080/swagger-ui/index.html**

---

<div align="center">

Made with ☕ and Spring Boot

</div>