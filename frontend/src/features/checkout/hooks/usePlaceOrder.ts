import { useMutation, useQueryClient } from '@tanstack/react-query';
import { orderService } from '../api/orderService';
import type { PlaceOrderResponse } from '../types/checkout.types';

export function usePlaceOrder() {
  const queryClient = useQueryClient();

  return useMutation<PlaceOrderResponse, Error, void>({
    mutationFn: () => orderService.placeOrder(),
    onSuccess: () => {
      // Invalidate cart since it's converted to an order
      queryClient.invalidateQueries({ queryKey: ['cart'] });
      queryClient.invalidateQueries({ queryKey: ['orders'] });
    },
  });
}
