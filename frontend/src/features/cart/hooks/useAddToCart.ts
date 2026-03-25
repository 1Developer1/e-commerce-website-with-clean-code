import { useMutation, useQueryClient } from '@tanstack/react-query';
import { cartService } from '../api/cartService';
import type { AddToCartRequest, CartActionResponse, GetCartResponse } from '../types/cart.types';

export function useAddToCart() {
  const queryClient = useQueryClient();

  return useMutation<CartActionResponse, Error, AddToCartRequest, { previousCart?: GetCartResponse }>({
    mutationFn: (request) => cartService.addToCart(request),
    onMutate: async (newCartItem) => {
      // Cancel any outgoing refetches so they don't overwrite our optimistic update
      await queryClient.cancelQueries({ queryKey: ['cart'] });

      // Snapshot the previous value
      const previousCart = queryClient.getQueryData<GetCartResponse>(['cart']);

      // Optimistically update to the new value
      queryClient.setQueryData<GetCartResponse>(['cart'], (old) => {
        if (!old) return old;
        const currentItems = old.items || [];
        const existingItem = currentItems.find(i => i.productId === newCartItem.productId);
        
        const newItems = existingItem
          ? currentItems.map(i => i.productId === newCartItem.productId ? { ...i, quantity: i.quantity + newCartItem.quantity } : i)
          : [...currentItems, { productId: newCartItem.productId, quantity: newCartItem.quantity }];
          
        return {
          ...old,
          items: newItems
        };
      });

      // Return a context object with the snapshotted value
      return { previousCart };
    },
    onError: (_err, _newCartItem, context) => {
      if (context?.previousCart) {
        queryClient.setQueryData(['cart'], context.previousCart);
      }
    },
    onSettled: () => {
      // Sync with server once mutation is finally done
      queryClient.invalidateQueries({ queryKey: ['cart'] });
    },
  });
}
