import axiosClient from '../../../infrastructure/api/axiosClient';
import type {
  GetCartResponse,
  AddToCartRequest,
  ApplyDiscountRequest,
  CartActionResponse,
} from '../types/cart.types';

const BASE_PATH = '/api/v1/cart';

/**
 * Cart API Service — Infrastructure layer.
 * Communicates with Backend CartController endpoints.
 */
export const cartService = {
  /**
   * GET /api/v1/cart
   */
  async getCart(): Promise<GetCartResponse> {
    const response = await axiosClient.get<GetCartResponse>(BASE_PATH);
    return response.data;
  },

  /**
   * POST /api/v1/cart/items
   */
  async addToCart(request: AddToCartRequest): Promise<CartActionResponse> {
    const response = await axiosClient.post<CartActionResponse>(`${BASE_PATH}/items`, request);
    return response.data;
  },

  /**
   * POST /api/v1/cart/discounts
   */
  async applyDiscount(request: ApplyDiscountRequest): Promise<CartActionResponse> {
    const response = await axiosClient.post<CartActionResponse>(`${BASE_PATH}/discounts`, request);
    return response.data;
  },
};
