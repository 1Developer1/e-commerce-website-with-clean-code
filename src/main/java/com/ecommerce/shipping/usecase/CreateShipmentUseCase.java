package com.ecommerce.shipping.usecase;

import com.ecommerce.shipping.entity.Shipment;
import com.ecommerce.shipping.usecase.port.ShippingRepository;

import java.util.UUID;

public class CreateShipmentUseCase {
    private final ShippingRepository repository;

    public CreateShipmentUseCase(ShippingRepository repository) {
        this.repository = repository;
    }

    public void execute(UUID orderId, String address) {
        // Idempotency check: Don't create if already exists
        if (repository.findByOrderId(orderId).isPresent()) {
            System.out.println("[Shipping] Shipment already exists for order " + orderId);
            return;
        }

        Shipment shipment = Shipment.create(orderId, address);
        // Simulate getting tracking code from external provider
        String trackingCode = "TRK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        shipment.assignTrackingCode(trackingCode);
        
        repository.save(shipment);
        System.out.println("[Shipping] Shipment created: " + shipment.getId() + ", Tracking: " + shipment.getTrackingCode());
    }
}
