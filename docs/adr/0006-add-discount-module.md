# ADR 0006: Discount Module Strategy

**Date:** 2026-02-13
**Status:** Accepted

## Context
The system requires a way to apply discounts to the Cart.
We need to decide where to place the Discount logic.
Options:
1.  Inside `Cart` module (Low cohesion, Cart does too much).
2.  Inside `Product` module (Limited to product-specific discounts).
3.  **New Bounded Context** (`com.ecommerce.discount`).

## Decision
We will create a **New Bounded Context** (`com.ecommerce.discount`) to handle discount rules, codes, and logic.
- **Integration:** The `Cart` module will define a `DiscountProvider` interface (Port).
- **Adaptation:** The `Infrastructure` layer (or a specific Adapter) will implement this interface by calling the `Discount` module.
- **Persistence:** Discounts will have their own Repository.

## Consequences
**Positive:**
- Decouples Discount logic from Cart logic.
- Allows complex discount rules (e.g., "Buy X Get Y") in the future without changing Cart.

**Negative:**
- Adds a new module and wiring complexity.

## Compliance
- `Cart` module must NOT depend on `Discount` module source code directly.
- Interaction must happen via Interfaces/Ports.
