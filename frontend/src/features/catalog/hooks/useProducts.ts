import { useQuery } from '@tanstack/react-query';
import { productService } from '../api/productService';
import type { ProductListResponse } from '../types/product.types';

/**
 * Custom hook for fetching product list.
 * Application layer — orchestrates between View and Service.
 *
 * SRE: React Query handles retry (3x), caching (30s stale), and loading states.
 */
export function useProducts(page = 0, size = 20) {
  return useQuery<ProductListResponse, Error>({
    queryKey: ['products', page, size],
    queryFn: () => productService.getAll(page, size),
  });
}
