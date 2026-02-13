package com.ecommerce.cart.usecase.dto;

import com.ecommerce.cart.entity.Cart;
import com.ecommerce.cart.entity.CartItem;

import java.util.List;
import java.util.stream.Collectors;

public class CartMapper {
    public static CartDto toDto(Cart cart) {
        if (cart == null) return null;

        List<CartItemDto> itemDtos = cart.getItems().stream()
            .map(CartMapper::toDto)
            .collect(Collectors.toList());

        return new CartDto(
            cart.getUserId(),
            itemDtos,
            cart.getDiscount(),
            cart.getTotalPrice()
        );
    }

    private static CartItemDto toDto(CartItem item) {
        return new CartItemDto(
            item.getProductId(),
            item.getQuantity(),
            item.getUnitPrice(),
            item.getTotalPrice()
        );
    }
}
