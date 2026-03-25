import { useQuery } from '@tanstack/react-query';
import { cartService } from '../api/cartService';
import type { GetCartResponse } from '../types/cart.types';

/**
 * Custom hook for fetching the active cart.
 */
export function useCart() {
  return useQuery<GetCartResponse, Error>({
    queryKey: ['cart'],
    queryFn: () => cartService.getCart(),
  });
}
