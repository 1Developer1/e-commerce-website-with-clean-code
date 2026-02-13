package com.ecommerce.cart.usecase;

import com.ecommerce.cart.entity.Cart;
import com.ecommerce.cart.usecase.port.DiscountProvider;
import com.ecommerce.shared.domain.Money;
import java.util.Optional;

public class ApplyDiscountUseCase {
    private final CartRepository cartRepository;
    private final DiscountProvider discountProvider;

    public ApplyDiscountUseCase(CartRepository cartRepository, DiscountProvider discountProvider) {
        this.cartRepository = cartRepository;
        this.discountProvider = discountProvider;
    }

    public ApplyDiscountOutput execute(ApplyDiscountInput input) {
        Optional<Cart> cartOpt = cartRepository.findByUserId(input.userId());
        if (cartOpt.isEmpty()) {
            return new ApplyDiscountOutput(false, "Cart not found", null);
        }
        Cart cart = cartOpt.get();

        Optional<Money> discountAmountOpt = discountProvider.getDiscount(input.discountCode());
        if (discountAmountOpt.isEmpty()) {
            return new ApplyDiscountOutput(false, "Invalid discount code", cart.getTotalPrice());
        }

        cart.applyDiscount(discountAmountOpt.get());
        cartRepository.save(cart);

        return new ApplyDiscountOutput(true, "Discount applied", cart.getTotalPrice());
    }
}
