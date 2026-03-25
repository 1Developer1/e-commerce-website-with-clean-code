import { useMutation, useQueryClient } from '@tanstack/react-query';
import { cartService } from '../api/cartService';
import type { ApplyDiscountRequest, CartActionResponse } from '../types/cart.types';

export function useApplyDiscount() {
  const queryClient = useQueryClient();

  return useMutation<CartActionResponse, Error, ApplyDiscountRequest>({
    mutationFn: (request) => cartService.applyDiscount(request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['cart'] });
    },
  });
}
