package com.ecommerce.discount.usecase;

import com.ecommerce.discount.entity.Discount;
import java.util.Optional;

public class GetDiscountUseCase {
    private final DiscountRepository discountRepository;

    public GetDiscountUseCase(DiscountRepository discountRepository) {
        this.discountRepository = discountRepository;
    }

    public GetDiscountOutput execute(GetDiscountInput input) {
        Optional<Discount> discountOpt = discountRepository.findByCode(input.code());

        if (discountOpt.isEmpty()) {
            return new GetDiscountOutput(false, "Invalid discount code", null);
        }

        Discount discount = discountOpt.get();
        return new GetDiscountOutput(true, "Discount applied", discount.getAmount());
    }
}
