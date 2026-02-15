# ADR 0012: Shipping Module Design and Event-Driven Integration

**Date:** 2026-02-15
**Status:** Accepted

## Context
The e-commerce system needs to handle order delivery. We need to introduce a `Shipping` module that:
1.  Creates a shipment record when an order is successfully paid.
2.  Tracks the delivery status.

## Decision
We will implement the Shipping module with a strict **Event-Driven Architecture** to decouple it from the Order module.

### 1. Integration Strategy
*   **Trigger:** `Shipping` module will subscribe to `PaymentSucceededEvent` (published by Payment module) or a new `OrderPaidEvent`.
    *   *Decision:* We will use `PaymentSucceededEvent` since it already exists and signifies that the order is ready for processing.
*   **Flow:**
    1.  `Payment` module publishes `PaymentSucceededEvent`.
    2.  `Shipping` module's `OrderPaidEventHandler` catches this event.
    3.  Handler invokes `CreateShipmentUseCase`.
    4.  Use Case creates a `Shipment` entity and saves it.

### 2. Module Structure
We will strictly follow the **API vs Internal** encapsulation pattern established in ADR 0011.

*   `com.ecommerce.shipping.api`: Public interfaces (if any service is needed by others).
*   `com.ecommerce.shipping.internal`: All implementation details (Use Cases, Repository Impls, Event Handlers).
*   `com.ecommerce.shipping.entity`: Domain entities (`Shipment`).

### 3. Data Independence
The `Shipping` module will manage its own `Shipment` repository. It will not query `OrderRepository`. It will rely on the data carried by the Event (OrderId, etc.) or strictly necessary ID references.

## Consequences
**Positive:**
*   **Zero Coupling:** Order module doesn't know Shipping exists.
*   **Extensibility:** We can add more handlers (e.g., Email Notification) to the same event without touching Order code.
*   **Fail-Safety:** If Shipping fails, it doesn't rollback the Payment (conceptually). *Note: In a real distributed system, we'd need an Outbox pattern or Saga, but for this modular monolith, synchronous event bus is acceptable for now.*

**Negative:**
*   **Event Complexity:** Debugging event flows can be harder than direct method calls.
