import { useMutation, useQueryClient } from '@tanstack/react-query';
import { paymentService } from '../api/paymentService';
import type { PayOrderRequest, PayOrderResponse } from '../types/checkout.types';

export function usePayOrder() {
  const queryClient = useQueryClient();

  return useMutation<PayOrderResponse, Error, PayOrderRequest>({
    mutationFn: (request) => paymentService.payOrder(request),
    onSuccess: (_, variables) => {
      // Invalidate specific order to refresh status to PAID
      queryClient.invalidateQueries({ queryKey: ['order', variables.orderId] });
      queryClient.invalidateQueries({ queryKey: ['orders'] });
    },
  });
}
