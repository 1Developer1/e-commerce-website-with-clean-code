import type { EnrichedCartItem } from '../hooks/useCartWithProducts';
import styles from './CartItem.module.css';

interface CartItemProps {
  item: EnrichedCartItem;
  onRemove?: (productId: string) => void;
}

export function CartItem({ item, onRemove }: CartItemProps) {
  const { product, quantity, productId } = item;

  // Fallback for when product details aren't loaded yet
  const name = product?.name || 'Bilinmeyen Ürün';
  const priceDisplay = product?.displayPrice || 'Fiyat Hesaplanıyor...';
  
  return (
    <div className={styles.item}>
      <div className={styles.imagePlaceholder}>🛍️</div>
      <div className={styles.content}>
        <h4 className={styles.name}>{name}</h4>
        <div className={styles.priceRow}>
          <span className={styles.price}>{priceDisplay}</span>
          <span className={styles.quantity}>x {quantity}</span>
        </div>
      </div>
      <div className={styles.actions}>
        <button 
          className={styles.removeButton}
          onClick={() => onRemove?.(productId)}
          aria-label="Ürünü sil"
          disabled={!onRemove}
        >
          🗑️
        </button>
      </div>
    </div>
  );
}
