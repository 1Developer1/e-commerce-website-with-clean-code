package com.ecommerce.shipping.usecase.port;

import com.ecommerce.shipping.entity.Shipment;
import java.util.Optional;
import java.util.UUID;

public interface ShippingRepository {
    void save(Shipment shipment);
    Optional<Shipment> findByOrderId(UUID orderId);
    Optional<Shipment> findById(UUID id);
}
