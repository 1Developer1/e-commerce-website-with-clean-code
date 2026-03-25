/**
 * TypeScript interfaces matching Backend Order & Payment DTOs.
 */

// --- ORDERS --- //

export interface PlaceOrderResponse {
  success: boolean;
  message: string;
  orderId?: string;
  orderStatus?: string;
  displayTotal?: string;
  timestamp: string;
}

export interface OrderItemViewModel {
  productId: string;
  quantity: number;
  price: string;
}

export interface OrderViewModel {
  id: string;
  status: string;
  createdAt: string;
  totalAmount?: string;
  currency?: string;
  items: OrderItemViewModel[];
}

export interface GetOrdersResponse {
  error?: string;
  orders?: OrderViewModel[];
}

// --- PAYMENTS --- //

export interface PayOrderRequest {
  orderId: string;
  amount: number;
  currency: string;
  method: string; // "CREDIT_CARD" veya "BANK_TRANSFER"
}

export interface PayOrderResponse {
  success: boolean;
  message: string;
  timestamp: string;
}
