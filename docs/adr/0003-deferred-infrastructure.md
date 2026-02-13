# ADR 0003: Deferring Infrastructure Decisions

**Date:** 2026-02-13
**Status:** Accepted

## Context
Frameworks (Spring Boot) and Databases (PostgreSQL) are implementation details. Introducing them too early often couples the Business Logic to specific technologies.

## Decision
We will **defer** the adoption of a Web Framework and a persistent Database until the core Use Cases are implemented and verified.
- Use `InMemoryRepository` (HashMaps) for persistence.
- Use `Main.java` (Console App) for entry point.

## Consequences
**Positive:**
- Faster development of core logic (Green-field).
- Logic is proven to be framework-agnostic.
- 100% Unit Testable without mocking heavy infrastructure.

**Negative:**
- Data is lost on restart.
- No real-world concurrency or transaction handling yet.

## Compliance
- Code must not contain framework annotations (e.g., `@Service`, `@Entity`) in Domain/UseCase layers.
