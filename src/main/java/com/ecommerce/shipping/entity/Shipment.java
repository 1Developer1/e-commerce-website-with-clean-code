package com.ecommerce.shipping.entity;

import java.time.LocalDateTime;
import java.util.UUID;

public class Shipment {
    private final UUID id;
    private final UUID orderId;
    private String trackingCode;
    private Status status;
    private final String address;
    private final LocalDateTime createdAt;

    public enum Status {
        PREPARING,
        SHIPPED,
        DELIVERED
    }

    private Shipment(UUID id, UUID orderId, String address, LocalDateTime createdAt) {
        this.id = id;
        this.orderId = orderId;
        this.address = address;
        this.createdAt = createdAt;
        this.status = Status.PREPARING;
    }

    public static Shipment create(UUID orderId, String address) {
        return new Shipment(UUID.randomUUID(), orderId, address, LocalDateTime.now());
    }

    public void assignTrackingCode(String trackingCode) {
        if (this.status != Status.PREPARING) {
            throw new IllegalStateException("Tracking code can only be assigned when PREPARING");
        }
        this.trackingCode = trackingCode;
        this.status = Status.SHIPPED;
    }

    public void markAsDelivered() {
        if (this.status != Status.SHIPPED) {
            throw new IllegalStateException("Shipment must be SHIPPED before DELIVERED");
        }
        this.status = Status.DELIVERED;
    }

    public UUID getId() { return id; }
    public UUID getOrderId() { return orderId; }
    public String getTrackingCode() { return trackingCode; }
    public Status getStatus() { return status; }
    public String getAddress() { return address; }
}
