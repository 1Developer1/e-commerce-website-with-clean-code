# ADR 0005: Refactoring Product and Cart for Metrics

**Date:** 2026-02-13
**Status:** Accepted

## Context
Static analysis (PMD) flagged 2 critical issues:
1.  `Product` constructor had **Cyclomatic Complexity of 10** due to multiple validation checks.
2.  `PlaceOrderUseCase` violated **Law of Demeter** by calling `cart.getItems().isEmpty()`, creating tight coupling to Cart's internal list structure.

## Decision
We refactored the code to improve these metrics:
1.  **Product:** Extracted input validation into a private `validate()` method.
2.  **Cart:** Added a public `isEmpty()` method that delegates to the internal list.

## Consequences
**Positive:**
- **Readability:** Constructor is cleaner.
- **Loose Coupling:** Use Cases no longer know internal data structures of Entities.
- **Maintainability:** Validation logic is centralized and reusable.

**Negative:**
- Slight increase in line count (new methods).

## Compliance
- PMD complexity warnings for `Product` should be resolved or reduced.
- Law of Demeter violation in `PlaceOrderUseCase` is resolved.
