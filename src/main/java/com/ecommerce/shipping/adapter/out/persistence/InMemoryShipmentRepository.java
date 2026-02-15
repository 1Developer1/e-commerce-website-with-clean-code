package com.ecommerce.shipping.adapter.out.persistence;

import com.ecommerce.shipping.entity.Shipment;
import com.ecommerce.shipping.usecase.port.ShippingRepository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class InMemoryShipmentRepository implements ShippingRepository {
    private final Map<UUID, Shipment> store = new HashMap<>();

    @Override
    public void save(Shipment shipment) {
        store.put(shipment.getId(), shipment);
    }

    @Override
    public Optional<Shipment> findByOrderId(UUID orderId) {
        return store.values().stream()
                .filter(s -> s.getOrderId().equals(orderId))
                .findFirst();
    }

    @Override
    public Optional<Shipment> findById(UUID id) {
        return Optional.ofNullable(store.get(id));
    }
}
