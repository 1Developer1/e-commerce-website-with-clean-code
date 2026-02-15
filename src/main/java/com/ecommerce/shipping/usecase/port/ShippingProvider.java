package com.ecommerce.shipping.usecase.port;

public interface ShippingProvider {
    String generateTrackingCode(String address);
}
