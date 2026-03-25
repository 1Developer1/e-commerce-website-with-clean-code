/**
 * TypeScript interfaces matching Backend CartPresenter output.
 */

export interface CartItemViewModel {
  productId: string;
  quantity: number;
}

export interface GetCartResponse {
  error?: string;
  userId?: string;
  items?: CartItemViewModel[];
  discountAmount?: string;
  discountCurrency?: string;
}

export interface AddToCartRequest {
  productId: string;
  quantity: number;
}

export interface ApplyDiscountRequest {
  code: string;
}

export interface CartActionResponse {
  success: boolean;
  message: string;
  timestamp: string;
}
