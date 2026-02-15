package com.ecommerce.shipping.api.dto;

import java.util.UUID;

public record ShipmentDto(
    UUID shipmentId,
    UUID orderId,
    String trackingCode,
    String status,
    String address
) {}
