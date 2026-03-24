import type { ProductViewModel } from '../types/product.types';
import { ProductCard } from './ProductCard';
import styles from './ProductGrid.module.css';

interface ProductGridProps {
  products: ProductViewModel[];
  onAddToCart?: (productId: string) => void;
}

/**
 * ProductGrid — renders a responsive grid of ProductCards.
 * Handles the empty state when no products are available.
 */
export function ProductGrid({ products, onAddToCart }: ProductGridProps) {
  if (products.length === 0) {
    return (
      <div className={styles.emptyState}>
        <span className={styles.emptyIcon}>📦</span>
        <h2 className={styles.emptyTitle}>Henüz ürün yok</h2>
        <p className={styles.emptyMessage}>Yeni ürün ekleyerek kataloğu oluşturmaya başlayın.</p>
      </div>
    );
  }

  return (
    <div className={styles.grid}>
      {products.map((product) => (
        <ProductCard
          key={product.id}
          product={product}
          onAddToCart={onAddToCart}
        />
      ))}
    </div>
  );
}
