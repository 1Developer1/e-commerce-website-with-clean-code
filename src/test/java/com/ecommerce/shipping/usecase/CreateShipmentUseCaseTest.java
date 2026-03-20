package com.ecommerce.shipping.usecase;

import com.ecommerce.shipping.entity.Shipment;
import com.ecommerce.shipping.usecase.port.ShippingProvider;
import com.ecommerce.shipping.usecase.port.ShippingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CreateShipmentUseCaseTest {

    private ShippingRepository repository;
    private ShippingProvider provider;
    private CreateShipmentUseCase useCase;

    @BeforeEach
    void setUp() {
        repository = mock(ShippingRepository.class);
        provider = mock(ShippingProvider.class);
        useCase = new CreateShipmentUseCase(repository, provider);
    }

    @Test
    void shouldCreateShipmentAndAssignTrackingCode() {
        UUID orderId = UUID.randomUUID();
        String address = "Test Address 123";
        String trackingCode = "TRK-9999";

        when(repository.findByOrderId(orderId)).thenReturn(Optional.empty());
        when(provider.generateTrackingCode(address)).thenReturn(trackingCode);

        useCase.execute(orderId, address);

        ArgumentCaptor<Shipment> shipmentCaptor = ArgumentCaptor.forClass(Shipment.class);
        verify(repository).save(shipmentCaptor.capture());

        Shipment savedShipment = shipmentCaptor.getValue();
        assertEquals(orderId, savedShipment.getOrderId());
        assertEquals(address, savedShipment.getAddress());
        assertEquals(trackingCode, savedShipment.getTrackingCode());
    }

    @Test
    void shouldNotCreateShipmentIfAlreadyExists() {
        UUID orderId = UUID.randomUUID();
        String address = "Test Address 123";
        Shipment existingShipment = Shipment.create(orderId, address);

        when(repository.findByOrderId(orderId)).thenReturn(Optional.of(existingShipment));

        useCase.execute(orderId, address);

        verify(provider, never()).generateTrackingCode(any());
        verify(repository, never()).save(any());
    }
}
