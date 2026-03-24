import axiosClient from '../../../infrastructure/api/axiosClient';
import type {
  ProductListResponse,
  CreateProductRequest,
  CreateProductResponse,
} from '../types/product.types';

const BASE_PATH = '/api/v1/products';

/**
 * Product API Service — Infrastructure layer.
 * Communicates with Backend ProductController endpoints.
 * No React knowledge — pure async functions.
 */
export const productService = {
  /**
   * GET /api/v1/products?page=&size=
   */
  async getAll(page = 0, size = 20): Promise<ProductListResponse> {
    const response = await axiosClient.get<ProductListResponse>(BASE_PATH, {
      params: { page, size },
    });
    return response.data;
  },

  /**
   * POST /api/v1/products
   */
  async create(request: CreateProductRequest): Promise<CreateProductResponse> {
    const response = await axiosClient.post<CreateProductResponse>(BASE_PATH, request);
    return response.data;
  },

  /**
   * DELETE /api/v1/products/{id}
   * Returns 204 No Content
   */
  async remove(id: string): Promise<void> {
    await axiosClient.delete(`${BASE_PATH}/${id}`);
  },
};
