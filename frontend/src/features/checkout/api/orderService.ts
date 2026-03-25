import axiosClient from '../../../infrastructure/api/axiosClient';
import type { PlaceOrderResponse, GetOrdersResponse, OrderViewModel } from '../types/checkout.types';

const BASE_PATH = '/api/v1/orders';

export const orderService = {
  /**
   * POST /api/v1/orders
   * Siparişi oluşturur (Aktif sepeti siparişe çevirir).
   */
  async placeOrder(): Promise<PlaceOrderResponse> {
    const response = await axiosClient.post<PlaceOrderResponse>(BASE_PATH, {});
    return response.data;
  },

  /**
   * GET /api/v1/orders
   */
  async getOrders(page = 0, size = 20): Promise<GetOrdersResponse> {
    const response = await axiosClient.get<GetOrdersResponse>(BASE_PATH, {
      params: { page, size }
    });
    return response.data;
  },

  /**
   * GET /api/v1/orders/{orderId}
   */
  async getOrderById(orderId: string): Promise<OrderViewModel> {
    const response = await axiosClient.get<OrderViewModel>(`${BASE_PATH}/${orderId}`);
    return response.data;
  }
};
