package com.ecommerce.shipping.usecase;

import com.ecommerce.shipping.entity.Shipment;
import com.ecommerce.shipping.usecase.port.ShippingRepository;
import com.ecommerce.shipping.api.dto.ShipmentDto;

import java.util.Optional;
import java.util.UUID;

public class TrackShipmentUseCase {
    private final ShippingRepository repository;

    public TrackShipmentUseCase(ShippingRepository repository) {
        this.repository = repository;
    }

    public Optional<ShipmentDto> execute(UUID orderId) {
        return repository.findByOrderId(orderId)
                .map(this::toDto);
    }

    private ShipmentDto toDto(Shipment shipment) {
        return new ShipmentDto(
            shipment.getId(),
            shipment.getOrderId(),
            shipment.getTrackingCode(),
            shipment.getStatus().name(),
            shipment.getAddress()
        );
    }
}
