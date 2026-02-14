# Library Management System – Project P1

## Introduction
This document provides an overview of **Previous Project**, which focuses on the **architectural and technical evolution** of the previously developed Library Management System.  
It includes:
- Project context  
- Identified problems and limitations  
- Goals and requirements for the new version  
- Proposed solution and improvements  
- Testing and validation strategy  

---

## Context
In the previous project, a **REST-oriented backend service** was developed for managing a library system.  
The application provided API endpoints for managing the following entities:

- **Books**  
- **Genres**  
- **Authors**  
- **Readers**  
- **Lendings**  

This service supported basic CRUD operations and enabled communication through REST APIs, offering the core functionality of a digital library.

---

## Problem
Although functional, the current version of the system presents several limitations that affect its **maintainability**, **evolution**, and **scalability**.

| Aspect | Description |
|--------|--------------|
| **Extensibility** | The system cannot easily support new features or external integrations. |
| **Configurability** | Configuration options are rigid, preventing different runtime behaviors based on setup parameters. |
| **Reliability** | The system lacks mechanisms to ensure consistency, fault tolerance, and robust operation. |
| **Testability** | Automated testing is limited and does not cover multiple integration levels. |

---

## Project P1 Goals
The **P1 Project** aims to improve the **architecture**, **quality**, and **adaptability** of the existing system.

### Objectives and Requirements
- **Comprehensive documentation**, including:
  - Reverse-engineered “System-as-is” architecture  
  - Requirement identification, focusing on *Architecturally Significant Requirements (ASRs)*  
  - Adoption of the **ADD (Attribute-Driven Design)** process  
  - Classification and rationale of architectural decisions  
  - Applied **architectural patterns**, **tactics**, and **reference architectures**  

- **Data persistence across different database models and systems:**
  1. **SQL + Redis**
  2. **MongoDB + Redis**
  3. **Elasticsearch**

- **External integrations** for retrieving a book’s ISBN by title through:
  - [ISBNdb](https://isbndb.com/)
  - [Google Books API](https://developers.google.com/books)
  - [Open Library API](https://openlibrary.org/developers/api)
  - Optionally combining two of the above systems

- **Configurable ID generation** for other entities, supporting multiple formats defined during setup time.

---

## Testing Improvements
Testing is a major focus of Project P1. The goal is to ensure **robustness and confidence** in every layer of the system through automated and mutation testing.

| Test Type | System Under Test (SUT) | Description |
|------------|--------------------------|--------------|
| **Functional (Black-box)** | Classes | Verifies external class behavior. |
| **Functional (White-box)** | Domain classes | Tests internal business logic. |
| **Mutation Testing** | Classes | Evaluates the effectiveness of existing test suites. |
| **Functional (Black-box)** | Controller + Service + {Domain, Repository, Gateways} | Modular integration tests. |
| **Functional (Black-box)** | Entire System | End-to-end (E2E) testing. |

These tests will serve as **key evidence** for validating the architectural and functional improvements implemented.

---

## Conclusion
**Project P1** marks a significant step forward in transforming a functional yet limited system into a **configurable, extensible, and reliable platform** ready for future growth and integration.  

---