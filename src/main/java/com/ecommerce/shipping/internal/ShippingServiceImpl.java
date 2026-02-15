package com.ecommerce.shipping.internal;

import com.ecommerce.shipping.api.ShippingService;
import com.ecommerce.shipping.api.dto.ShipmentDto;
import com.ecommerce.shipping.usecase.TrackShipmentUseCase;

import java.util.Optional;
import java.util.UUID;

class ShippingServiceImpl implements ShippingService {
    private final TrackShipmentUseCase trackShipmentUseCase;
    private final com.ecommerce.shipping.usecase.CreateShipmentUseCase createShipmentUseCase;

    ShippingServiceImpl(TrackShipmentUseCase trackShipmentUseCase, com.ecommerce.shipping.usecase.CreateShipmentUseCase createShipmentUseCase) {
        this.trackShipmentUseCase = trackShipmentUseCase;
        this.createShipmentUseCase = createShipmentUseCase;
    }

    @Override
    public Optional<ShipmentDto> trackShipment(UUID orderId) {
        return trackShipmentUseCase.execute(orderId);
    }

    @Override
    public void createShipment(UUID orderId, String address) {
        createShipmentUseCase.execute(orderId, address);
    }
}
