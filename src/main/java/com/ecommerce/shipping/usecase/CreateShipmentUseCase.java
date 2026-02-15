package com.ecommerce.shipping.usecase;

import com.ecommerce.shipping.entity.Shipment;
import com.ecommerce.shipping.usecase.port.ShippingRepository;
import com.ecommerce.shipping.usecase.port.ShippingProvider;

import java.util.UUID;

public class CreateShipmentUseCase {
    private final ShippingRepository repository;
    private final ShippingProvider shippingProvider;

    public CreateShipmentUseCase(ShippingRepository repository, ShippingProvider shippingProvider) {
        this.repository = repository;
        this.shippingProvider = shippingProvider;
    }

    public void execute(UUID orderId, String address) {
        // Idempotency check: Don't create if already exists
        if (repository.findByOrderId(orderId).isPresent()) {
            System.out.println("[Shipping] Shipment already exists for order " + orderId);
            return;
        }

        Shipment shipment = Shipment.create(orderId, address);
        
        // Use external provider to generate tracking code
        String trackingCode = shippingProvider.generateTrackingCode(address);
        shipment.assignTrackingCode(trackingCode);
        
        repository.save(shipment);
        System.out.println("[Shipping] Shipment created: " + shipment.getId() + ", Tracking: " + shipment.getTrackingCode());
    }
}
