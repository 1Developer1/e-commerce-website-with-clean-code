import { useMutation, useQueryClient } from '@tanstack/react-query';
import { productService } from '../api/productService';
import type { CreateProductRequest, CreateProductResponse } from '../types/product.types';

/**
 * Custom hook for creating a new product.
 * On success, invalidates the 'products' cache to refresh the list.
 */
export function useCreateProduct() {
  const queryClient = useQueryClient();

  return useMutation<CreateProductResponse, Error, CreateProductRequest>({
    mutationFn: (request) => productService.create(request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['products'] });
    },
  });
}
