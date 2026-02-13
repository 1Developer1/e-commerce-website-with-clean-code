# ADR 0009: Domain Events for Side Effects

**Date:** 2026-02-13
**Status:** Accepted

## Context
The `Payment` module currently calls `OrderRepository` directly to update the order status to `PAID`. This creates a tight coupling where Payment module needs to know about Order persistence details.

## Decision
We will use **Domain Events (Observer Pattern)** to handle side effects across modules.
1.  `Payment` module will publish a `PaymentSucceededEvent`.
2.  `Order` module will subscribe to this event and update the order status.

## Consequences
**Positive:**
- **Decoupling:** Payment module no longer depends on `OrderRepository`. It just says "Payment Happened".
- **Extensibility:** Other modules (e.g., Email Service) can also listen to this event without changing Payment code.

**Negative:**
- **Complexity:** Harder to trace flow (indirect invocation).
- **Consistency:** Initial implementation will be synchronous (in-memory bus), but in distributed systems this introduces Eventual Consistency challenges.
