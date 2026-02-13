# ADR 0002: Modular Monolith Package Structure

**Date:** 2026-02-13
**Status:** Accepted

## Context
The project needs to grow while maintaining clean boundaries. Traditional "Layered Architecture" (Controller/Service/Repository packages) often leads to a "Big Ball of Mud" where unrelated features depend on each other.

Alternatives considered:
1.  Microservices (Too complex for current stage).
2.  Layered Monolith (Horizontal Slicing).
3.  Modular Monolith (Vertical Slicing).

## Decision
We will adopt a **Modular Monolith** with **Package-by-Component** structure.
The top-level packages will represent Business Domains (Bounded Contexts):
- `com.ecommerce.product`
- `com.ecommerce.order`
- `com.ecommerce.cart`

Each module will internally follow Clean Architecture principles (Entity, UseCase, Adapter).

## Consequences
**Positive:**
- High Cohesion: Related code stays together.
- Easier extraction to Microservices later.
- Screaming Architecture: Project structure reveals intent.

**Negative:**
- Requires strict discipline to avoid cyclic dependencies between modules.
- Shared code must be carefully managed in a `shared` kernel to avoid coupling.

## Compliance
- ArchUnit tests will enforce `no cycles` between slices.
