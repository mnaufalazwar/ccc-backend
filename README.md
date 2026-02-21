# ChitChatClub — Backend

Spring Boot 3 REST API for the ChitChatClub English conversation practice platform.

## Tech Stack

- Java 17
- Spring Boot 3.2 (Web, Security, Data JPA, Validation, Mail)
- PostgreSQL
- JWT authentication with refresh token rotation
- Gradle

## Related Repositories

| Repository | Description |
|------------|-------------|
| [ccc-frontend](https://github.com/YOUR_USERNAME/ccc-frontend) | React + TypeScript frontend |
| [ccc-postgres](https://github.com/YOUR_USERNAME/ccc-postgres) | Docker Compose for PostgreSQL + pgAdmin |
| [ccc-documentation](https://github.com/YOUR_USERNAME/ccc-documentation) | Full project documentation |

## Getting Started

### Prerequisites

- Java 17+
- Docker (for PostgreSQL — see `ccc-postgres` repo)

### 1. Start PostgreSQL

Follow the `ccc-postgres` repo instructions, or run:

```bash
# In the ccc-postgres repo
cd dev
docker compose up -d
```

### 2. Configure Environment

```bash
cp .env.example .env
# Edit .env and fill in your MAIL_PASSWORD (Resend API key)
```

### 3. Run

```bash
./gradlew bootRun
```

The API starts on http://localhost:8080. Swagger UI is available at http://localhost:8080/swagger-ui/index.html.

On first startup, Hibernate creates all tables and `data.sql` seeds test data (users, a sample session, app config).

## Environment Variables

| Variable | Description |
|----------|-------------|
| `DB_URL` | PostgreSQL JDBC URL |
| `DB_USER` | Database username |
| `DB_PASS` | Database password |
| `JWT_SECRET` | JWT signing secret (256+ bits) |
| `MAIL_PASSWORD` | Resend API key for email |
| `FRONTEND_URL` | Frontend URL for email links |

See `.env.example` for defaults.

## Seed Data

All seed users have password `123456`. See the documentation repo for the full list.
