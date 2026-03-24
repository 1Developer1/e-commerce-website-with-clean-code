/**
 * TypeScript interfaces matching Backend ProductPresenter output.
 * These types are the single source of truth for the frontend–backend contract.
 */

// === Response Types (from ProductPresenter) ===

export interface ProductListResponse {
  page: number;
  size: number;
  totalElements: number;
  totalCount: number;
  products: ProductViewModel[];
}

export interface ProductViewModel {
  id: string;
  name: string;
  displayPrice: string; // "USD 29.99" formatted by presenter
}

export interface CreateProductResponse {
  success: boolean;
  message: string;
  productId?: string;
  productName?: string;
  timestamp: string;
}

export interface UpdateProductResponse {
  success: boolean;
  message: string;
  product: {
    id: string;
    name: string;
    description: string;
    price: string;
    stockQuantity: number;
  };
}

// === Request Types (matching Backend @Valid DTOs) ===

export interface CreateProductRequest {
  name: string;
  description?: string;
  priceAmount: number;
  priceCurrency: string;
  initialStock: number;
}

export interface UpdateProductRequest {
  name?: string;
  description?: string;
  priceAmount?: number;
  priceCurrency?: string;
  stockQuantity?: number;
}
