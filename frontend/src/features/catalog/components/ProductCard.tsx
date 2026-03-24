import type { ProductViewModel } from '../types/product.types';
import styles from './ProductCard.module.css';

interface ProductCardProps {
  product: ProductViewModel;
  onAddToCart?: (productId: string) => void;
}

/**
 * ProductCard — displays a single product.
 * View layer: no business logic, only presentation.
 */
export function ProductCard({ product, onAddToCart }: ProductCardProps) {
  return (
    <article className={styles.card} aria-label={`Ürün: ${product.name}`}>
      <div className={styles.imageArea}>
        <span className={styles.imagePlaceholder}>🛍️</span>
      </div>
      <div className={styles.content}>
        <h3 className={styles.name}>{product.name}</h3>
        <p className={styles.price}>{product.displayPrice}</p>
      </div>
      <div className={styles.actions}>
        <button
          className={styles.addButton}
          onClick={() => onAddToCart?.(product.id)}
          aria-label={`${product.name} sepete ekle`}
        >
          Sepete Ekle
        </button>
      </div>
    </article>
  );
}
