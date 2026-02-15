package com.ecommerce.shipping.adapter.out.provider;

import com.ecommerce.shipping.usecase.port.ShippingProvider;
import java.util.UUID;

public class DummyShippingProvider implements ShippingProvider {
    @Override
    public String generateTrackingCode(String address) {
        // Simulate external API call
        return "TRK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
