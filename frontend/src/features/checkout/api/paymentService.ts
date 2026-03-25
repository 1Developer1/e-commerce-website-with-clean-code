import axiosClient from '../../../infrastructure/api/axiosClient';
import type { PayOrderRequest, PayOrderResponse } from '../types/checkout.types';

const BASE_PATH = '/api/v1/payments';

export const paymentService = {
  /**
   * POST /api/v1/payments
   * Siparişi öder.
   */
  async payOrder(request: PayOrderRequest): Promise<PayOrderResponse> {
    const response = await axiosClient.post<PayOrderResponse>(BASE_PATH, request);
    return response.data;
  }
};
