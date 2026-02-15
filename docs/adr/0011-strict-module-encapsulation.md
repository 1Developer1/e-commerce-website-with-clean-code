# ADR 0011: Strict Module Encapsulation and Decoupling

**Date:** 2026-02-14
**Status:** Accepted

## Context
Two architectural weaknesses were identified in the previous iteration:
1.  **Payment-Order Coupling:** `PayOrderUseCase` depended on `OrderRepository` to fetch the order amount and check status. This violated the Single Responsibility Principle (Payment module shouldn't read Order DB) and created a dependency on Order persistence.
2.  **Cart Module Leakage:** `CartServiceImpl` was a public class in the `usecase` package. This allowed other modules to potentially instantiate it directly or depend on the implementation class rather than the interface, violating the "Public API vs Internal Implementation" separation.

## Decision
We decided to enforce stricter boundaries:

### 1. Payment Module Decoupling
*   **Decision:** Remove `OrderRepository` dependency from `PayOrderUseCase`.
*   **Mechanism:** Pass the `amount` to be paid explicitly via `PayOrderInput`.
*   **Flow:** The caller (Controller) provides the amount. `PayOrderUseCase` processes payment and publishes `PaymentSucceededEvent`. The `OrderPaymentEventHandler` (in Order module) listens to this event to update the status.

### 2. Cart Module Encapsulation
*   **Decision:** Split the Cart module into `api` and `internal` packages.
*   **Structure:**
    *   `com.ecommerce.cart.api`: Contains `CartService` interface (Public).
    *   `com.ecommerce.cart.internal`: Contains `CartServiceImpl` (Package-Private).
*   **Instantiation:** Introduced `CartModule` (Factory) to allow the Infrastructure layer (`Main`) to instantiate the service without seeing the implementation class.

## Consequences
**Positive:**
*   **True Decoupling:** Payment module is now purely functional; it takes money and emits an event. It has zero knowledge of Order database.
*   **Strong Encapsulation:** It is now impossible for other modules to access `CartServiceImpl` directly (compilation error). They must use `CartService` interface.
*   **SRP:** Responsibilities are clearly separated (Payment -> Transact, Order -> Manage State).

**Negative:**
*   **Wiring Complexity:** Requires a Factory (`CartModule`) for dependency injection handling in `Main`.
*   **Signature Change:** `PayOrderInput` now requires `amount`, pushing the responsibility of providing correct amount to the caller (Controller/Client).
