# ADR 0010: Module Facade Pattern

**Date:** 2026-02-14
**Status:** Accepted

## Context
Currently, `Order` module directly accesses `CartRepository` to retrieve cart data. Although we introduced DTOs (ADR 0008), the dependency on the *Repository* interface still leaks the implementation detail that "Cart data comes from a repository".
According to Modular Monolith principles, modules should only communicate via explicit Public APIs (Facades), keeping internal structures (like Repositories) package-private or hidden.

## Decision
We will implement the **Facade Pattern** for module communication.
1.  `Cart` module will expose a `CartService` interface (Public API).
2.  `PlaceOrderUseCase` will depend on `CartService`, not `CartRepository`.
3.  `CartRepository` will be hidden inside the Cart module (ideally package-private if supported by language features, or strictly internal convention).

## Consequences
**Positive:**
- **Encapsulation:** The internal architecture of `Cart` module (e.g., changing from Repository to a remote API call) is completely hidden from `Order`.
- **Maintainability:** Refactoring `Cart` internals becomes safer as long as the Facade contract is maintained.

**Negative:**
- **Indirection:** Adds an extra layer of abstraction/delegation.
