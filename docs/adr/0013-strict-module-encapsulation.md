# ADR 0013: Strict Module Encapsulation for Adapters

**Date:** 2026-02-16
**Status:** Accepted

## Context
In the `Shipping` module, we implemented an `OrderPaidEventHandler` (Driving Adapter) that listens to payment events. Initially, this handler was directly injecting `CreateShipmentUseCase` to execute logic.

While this is technically allowed within the same module, it creates an inconsistency where "Internal" adapters (like Handlers) bypass the Module Facade (`ShippingService`), while "External" consumers (like `Main`) are forced to use it.

## Decision
We will enforce **Strict Module Encapsulation** even for internal adapters (Event Handlers, Controllers).

*   **Rule:** All Driving Adapters (`adapter.in.*`) MUST use the Public API (`ShippingService` Interface) to interact with the domain.
*   **Prohibition:** Adapters MUST NOT depend directly on Use Cases (`usecase.*`).

### Changes
1.  Updated `ShippingService` interface to include `createShipment(...)`.
2.  Refactored `OrderPaidEventHandler` to depend on `ShippingService`.
3.  `ShippingServiceImpl` now implements the delegation to `CreateShipmentUseCase`.

## Consequences
**Positive:**
*   **Consistency:** All entry points to the module use the same API.
*   **Encapsulation:** We can now hide Use Case classes (make them package-private) if desired, as no one outside the `internal` package needs to see them.
*   **Refactoring Safety:** Changing Use Case signatures only affects the `ServiceImpl`, not the Adapters.

**Negative:**
*   **Boilerplate:** We must expose internal methods (like `createShipment`) in the Service Interface, even if they are only used by the module's own adapters.
*   **Indirection:** Adds one extra hop (Handler -> Service -> UseCase) for execution.
