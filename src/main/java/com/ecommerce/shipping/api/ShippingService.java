package com.ecommerce.shipping.api;

import com.ecommerce.shipping.api.dto.ShipmentDto;
import java.util.Optional;
import java.util.UUID;

public interface ShippingService {
    Optional<ShipmentDto> trackShipment(UUID orderId);
    void createShipment(UUID orderId, String address);
}
