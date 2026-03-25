import { useUI } from '../../common/context/UIContext';
import { useCartWithProducts } from '../hooks/useCartWithProducts';
import { useApplyDiscount } from '../hooks/useApplyDiscount';
import { useNavigate } from 'react-router-dom';
import { CartItem } from './CartItem';
import { useState } from 'react';
import styles from './CartDrawer.module.css';

/**
 * Slide-out panel for the shopping cart.
 * Shows enriched cart items and handles discount application.
 */
export function CartDrawer() {
  const { isCartOpen, closeCart } = useUI();
  const { cartData, enrichedItems, isLoading, isError } = useCartWithProducts();
  const applyDiscount = useApplyDiscount();
  const navigate = useNavigate();
  
  const [discountCode, setDiscountCode] = useState('');

  // Calculate Subtotal dynamically based on enriched items (temporary until backend returns full price details)
  const subtotal = enrichedItems.reduce((sum, item) => {
    const priceStr = item.product?.displayPrice || '0';
    // Basic extraction if it's "USD 1999.99"
    const amount = parseFloat(priceStr.replace(/[^0-9.]/g, '')) || 0;
    return sum + amount * item.quantity;
  }, 0);

  const handleApplyDiscount = (e: React.FormEvent) => {
    e.preventDefault();
    if (!discountCode.trim()) return;
    applyDiscount.mutate({ code: discountCode.trim() }, {
      onSuccess: () => setDiscountCode('')
    });
  };

  if (!isCartOpen) return null;

  return (
    <>
      <div className={styles.overlay} onClick={closeCart} aria-hidden="true" />
      <aside className={styles.drawer} role="dialog" aria-label="Alışveriş Sepeti">
        <header className={styles.header}>
          <h2>Sepetim</h2>
          <button className={styles.closeButton} onClick={closeCart} aria-label="Sepeti kapat">✕</button>
        </header>

        <div className={styles.content}>
          {isLoading && <p className={styles.message}>Sepet yükleniyor...</p>}
          
          {isError && <p className={styles.error}>Sepet bilgileri alınamadı.</p>}

          {!isLoading && !isError && enrichedItems.length === 0 && (
            <div className={styles.emptyState}>
              <span className={styles.emptyIcon}>🛒</span>
              <p>Sepetiniz şu an boş.</p>
            </div>
          )}

          {!isLoading && !isError && enrichedItems.length > 0 && (
            <div className={styles.itemsList}>
              {enrichedItems.map(item => (
                <CartItem key={item.productId} item={item} />
              ))}
            </div>
          )}
        </div>

        {enrichedItems.length > 0 && (
          <footer className={styles.footer}>
            <form onSubmit={handleApplyDiscount} className={styles.discountForm}>
              <input 
                type="text" 
                placeholder="İndirim Kodu" 
                value={discountCode}
                onChange={e => setDiscountCode(e.target.value)}
                className={styles.discountInput}
              />
              <button 
                type="submit" 
                className={styles.discountButton}
                disabled={applyDiscount.isPending || !discountCode.trim()}
              >
                Uygula
              </button>
            </form>
            
            {applyDiscount.isError && (
              <p className={styles.discountError}>{applyDiscount.error.message}</p>
            )}

            <div className={styles.summaryRow}>
              <span>Ara Toplam</span>
              <span>USD {subtotal.toFixed(2)}</span>
            </div>
            {cartData?.discountAmount && (
              <div className={`${styles.summaryRow} ${styles.discountRow}`}>
                <span>İndirim</span>
                <span>- {cartData.discountCurrency} {cartData.discountAmount}</span>
              </div>
            )}
            <div className={`${styles.summaryRow} ${styles.totalRow}`}>
              <span>Ödenecek Tutar</span>
              <span>
                USD {cartData?.discountAmount 
                  ? (subtotal - parseFloat(cartData.discountAmount)).toFixed(2)
                  : subtotal.toFixed(2)}
              </span>
            </div>

            <button className={styles.checkoutButton} onClick={() => {
              closeCart();
              navigate('/checkout');
            }}>
              Siparişi Tamamla
            </button>
          </footer>
        )}
      </aside>
    </>
  );
}
