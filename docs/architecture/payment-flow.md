# Payment Event Flow Diagram

This diagram illustrates how the `Payment` module communicates with `Order` and `Shipping` modules via the `Shared Kernel` (EventBus) after a successful payment.

```mermaid
sequenceDiagram
    autonumber
    participant Client as Client (Main/Web)
    participant Payment as Payment Module
    participant EventBus as Shared Kernel (EventBus)
    participant Order as Order Module
    participant Shipping as Shipping Module

    Note over Payment: User requests payment

    Client->>Payment: payOrder(orderId, amount)
    activate Payment
    
    Payment->>Payment: Process Payment (Gateway)
    
    rect rgb(200, 255, 200)
    Note over Payment, EventBus: Payment Successful! Publishing Event...
    Payment->>EventBus: publish(PaymentSucceededEvent)
    end
    
    Payment-->>Client: return Success
    deactivate Payment

    par Async Event Processing
        EventBus->>Order: handle(PaymentSucceededEvent)
        activate Order
        Note right of Order: OrderPaymentEventHandler
        Order->>Order: Update Status to PAID
        deactivate Order
    and
        EventBus->>Shipping: handle(PaymentSucceededEvent)
        activate Shipping
        Note right of Shipping: OrderPaidEventHandler
        Shipping->>Shipping: Create Shipment
        deactivate Shipping
    end
```

## Explanation
1.  **Payment Module:** Does its job (processing payment) and knows nothing about Order or Shipping logic. It just shouts "Payment Succeeded!" via `EventBus`.
2.  **Shared Kernel (EventBus):** Acts as the mediator. It doesn't know business logic, just routes messages to subscribers.
3.  **Order Module:** Subscribes to the event to update its own state (PAID).
4.  **Shipping Module:** Subscribes to the event to start its own process (Create Shipment).

This is **Decoupled Architecture**. If we remove the `Shipping` module, the `Payment` module continues to work without errors.
