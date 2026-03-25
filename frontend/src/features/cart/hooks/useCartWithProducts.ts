import { useMemo } from 'react';
import { useCart } from './useCart';
import { useQueryClient } from '@tanstack/react-query';
import type { ProductListResponse, ProductViewModel } from '../../catalog/types/product.types';
import type { CartItemViewModel } from '../types/cart.types';

export interface EnrichedCartItem extends CartItemViewModel {
  product?: ProductViewModel;
}

/**
 * Combines Cart items with Product details from the Catalog cache.
 * Temporary workaround until GET /products/{id} is available.
 */
export function useCartWithProducts() {
  const { data: cartData, isLoading, isError, error } = useCart();
  const queryClient = useQueryClient();

  const enrichedItems = useMemo<EnrichedCartItem[]>(() => {
    if (!cartData?.items) return [];

    // Lookup across all cached pages of products
    const productQueries = queryClient.getQueriesData<ProductListResponse>({ queryKey: ['products'] });
    const allProducts = productQueries.flatMap(([, data]) => data?.products || []);

    return cartData.items.map((item) => {
      const product = allProducts.find((p) => p.id === item.productId);
      return { ...item, product };
    });
  }, [cartData?.items, queryClient]);

  return {
    cartData,
    enrichedItems,
    isLoading,
    isError,
    error,
  };
}
