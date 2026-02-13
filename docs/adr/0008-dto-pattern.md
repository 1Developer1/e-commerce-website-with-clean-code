# ADR 0008: DTO Pattern for Knowledge Sharing

**Date:** 2026-02-13
**Status:** Accepted

## Context
Currently, `Order` module directly accesses `Cart` entity from `Cart` module. This creates a strong coupling between the two modules. If `Cart` entity changes (internal details), `Order` module might break.

## Decision
We will use **Data Transfer Objects (DTOs)** for communication between modules.
The `Cart` module will expose a `CartDto` which contains only the data needed by other modules, decoupling the internal Entity structure from the external contract.

## Consequences
**Positive:**
- **Decoupling:** Internal changes to `Cart` entity won't affect `Order` module as long as DTO mapping remains consistent.
- **Security:** We can hide internal fields (like specific discount logic details) from other modules.

**Negative:**
- **Boilerplate:** Requires creating DTO classes and Mappers.
