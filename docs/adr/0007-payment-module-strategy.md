# ADR 0007: Payment Module Strategy

**Date:** 2026-02-13
**Status:** Accepted

## Context
The system currently has no real payment processing. We need to support multiple payment methods (Credit Card, Bank Transfer, etc.) and allow for easy addition of new ones (Open/Closed Principle).

## Decision
We will create a **New Bounded Context** (`com.ecommerce.payment`).
- **Pattern:** Strategy Pattern.
- **Port:** `PaymentGateway` interface will define the contract (`pay(Money amount)`).
- **Adapters:** Concrete implementations (e.g., `CreditCardAdapter`, `BankTransferAdapter`) will implement this interface.
- **Flow:** `PayOrderUseCase` (in Payment Module) will orchestrate the payment and update the **Order** status upon success.

## Consequences
**Positive:**
- **Extensibility:** New payment methods can be added as new classes without modifying core logic.
- **Isolation:** Payment logic (validation, gateway communication) is separated from Order management.

**Negative:**
- **Dependency:** Payment module will likely need to depend on `Order` module to update order status (or communicate via Events if we were using Event-Driven Architecture. For now, direct dependency is acceptable for Modular Monolith).

## Compliance
- `Order` module should NOT depend on `Payment` module.
- Payment details (CC numbers) should not be persisted in plain text.
