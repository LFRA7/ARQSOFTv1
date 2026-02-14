# Library Management System - ARQSOFT v1

A comprehensive library management REST API built with **Spring Boot 3.2.5** and **Java 17**, developed as part of the Master's degree in Computer Engineering at ISEP. The project focuses on extensibility, configurability, reliability, and testability, with support for multiple database backends.

## Tech Stack

| Layer | Technologies |
|-------|-------------|
| **Backend** | Spring Boot 3.2.5, Spring Web, Spring Data JPA |
| **Databases** | PostgreSQL 15, H2 (dev), MongoDB 7.0, Redis 7.2 (cache) |
| **Authentication** | Spring Security, OAuth2 Resource Server, JWT (RSA) |
| **API Docs** | OpenAPI / Swagger UI (springdoc-openapi) |
| **Build & CI/CD** | Maven 3.9.6, Docker, Jenkins |
| **Testing** | JUnit 5, Testcontainers, JaCoCo, PITest, SonarQube |
| **Other** | MapStruct, Lombok, OWASP HTML Sanitizer |

## Project Structure

```
src/main/java/pt/psoft/g1/psoftg1/
├── auth/                    # Authentication & authorization
├── authormanagement/        # Author management module
├── bookmanagement/          # Book management module
├── genremanagement/         # Genre management module
├── lendingmanagement/       # Lending & fine management
├── readermanagement/        # Reader management module
├── usermanagement/          # User management module
├── bootstrapping/           # Database seeding
├── configuration/           # Spring configs (JPA, Mongo, Redis, Security)
├── external/                # External integrations (Google Books, Open Library)
├── exceptions/              # Custom exceptions
└── shared/                  # Base models, DTOs & shared repositories
```

Each module follows the structure: `api/` (controllers) -> `services/` (business logic) -> `repositories/` (data access) -> `infrastructure/` (JPA/MongoDB implementations).

## Prerequisites

- Java 17+
- Maven 3.9.6+
- Docker & Docker Compose (for the full stack)
- Redis (for caching)

## Installation & Running

### Development (H2 + Redis)

```bash
# Build
mvn clean install

# Run
mvn spring-boot:run
```

The application will be available at `http://localhost:8081`.

### Docker Compose (Full Stack)

```bash
docker-compose up -d
```

Available services:
- **API**: http://localhost:8081
- **Swagger UI**: http://localhost:8081/swagger-ui
- **Jenkins**: http://localhost:8090
- **SonarQube**: http://localhost:9000
- **H2 Console**: http://localhost:8081/h2-console (dev only)

### Run Profiles

| Profile | Command | Description |
|---------|---------|-------------|
| **Default** | `mvn spring-boot:run` | In-memory H2 + Redis |
| **Dev** | `-Dspring.profiles.active=dev` | H2 with debug logging |
| **MongoDB** | `-Dspring.profiles.active=mongodb-redis` | MongoDB + Redis |
| **Production** | `-Dspring.profiles.active=prod` | PostgreSQL |

## Testing

```bash
# Unit tests
mvn test

# Integration tests
mvn verify

# Code coverage (JaCoCo)
mvn clean test jacoco:report

# Mutation testing (PITest)
mvn pitest:mutationCoverage

# SonarQube analysis
mvn sonar:sonar
```

## Key Features

- **Multi-Database Support** - Switch between PostgreSQL, H2, and MongoDB via configuration
- **Book Management** - Full CRUD with ISBN lookup via Google Books / Open Library
- **Lending Management** - Complete lending lifecycle with automatic fine calculation
- **JWT Authentication** - Stateless security with RSA key pairs
- **Redis Caching** - Transparent caching layer with 10-minute TTL
- **Photo Management** - File upload and association with entities (books, readers)
- **RESTful API** - Auto-generated documentation via Swagger UI
- **CI/CD Pipeline** - Jenkins pipeline with integrated quality analysis

## API Endpoints

| Resource | Endpoint |
|----------|----------|
| Books | `/api/books` |
| Authors | `/api/authors` |
| Genres | `/api/genres` |
| Readers | `/api/readers` |
| Lendings | `/api/lendings` |
| Users | `/api/users` |
| Authentication | `/api/auth/login` |

Postman collections are available in the `Docs/` folder.

## Documentation

Architecture documentation can be found in the `ARQSOFTv1Wiki/` folder, including:
- Architecture Documentation
- Project Objectives
- Design Decisions