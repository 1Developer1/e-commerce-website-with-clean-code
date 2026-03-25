import { useQuery } from '@tanstack/react-query';
import { orderService } from '../api/orderService';
import type { GetOrdersResponse } from '../types/checkout.types';

export function useOrders(page = 0, size = 20) {
  return useQuery<GetOrdersResponse, Error>({
    queryKey: ['orders', page, size],
    queryFn: () => orderService.getOrders(page, size),
  });
}
