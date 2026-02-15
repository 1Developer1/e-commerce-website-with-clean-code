package com.ecommerce.shipping.internal;

import com.ecommerce.shipping.api.ShippingService;
import com.ecommerce.shipping.usecase.TrackShipmentUseCase;
import com.ecommerce.shipping.usecase.port.ShippingRepository;

public class ShippingModule {
    public static ShippingService createService(ShippingRepository repository, com.ecommerce.shipping.usecase.CreateShipmentUseCase createShipmentUseCase) {
        TrackShipmentUseCase trackShipmentUseCase = new TrackShipmentUseCase(repository);
        return new ShippingServiceImpl(trackShipmentUseCase, createShipmentUseCase);
    }
}
