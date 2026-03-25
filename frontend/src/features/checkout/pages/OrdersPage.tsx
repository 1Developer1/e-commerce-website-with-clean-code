import { useOrders } from '../hooks/useOrders';
import styles from './OrdersPage.module.css';

export function OrdersPage() {
  const { data, isLoading, isError, error } = useOrders();

  return (
    <div className={styles.page}>
      <header className={styles.header}>
        <h1>Siparişlerim</h1>
        <p className={styles.subtitle}>Geçmiş siparişleriniz ve durumları</p>
      </header>
      
      <main className={styles.main}>
        {isLoading && <p>Siparişleriniz yükleniyor...</p>}
        {isError && <p className={styles.error}>{error.message}</p>}
        
        {!isLoading && !isError && (!data?.orders || data.orders.length === 0) && (
          <div className={styles.emptyState}>
            <span>📦</span>
            <p>Henüz hiç siparişiniz bulunmuyor.</p>
          </div>
        )}

        {!isLoading && !isError && data?.orders && data.orders.length > 0 && (
          <div className={styles.grid}>
            {data.orders.map((order) => (
              <div key={order.id} className={styles.orderCard}>
                <div className={styles.cardHeader}>
                  <div className={styles.orderId}>
                    <span className={styles.label}>Sipariş No</span>
                    <span className={styles.value}>#{order.id.split('-')[0]}</span>
                  </div>
                  <div className={styles.orderStatus}>
                    <span className={`${styles.badge} ${styles['status-' + order.status.toLowerCase().replace(/\s+/g, '-')]} `}>
                      {order.status}
                    </span>
                  </div>
                </div>
                
                <div className={styles.cardBody}>
                  <div className={styles.itemsInfo}>
                    <span className={styles.label}>Ürünler</span>
                    <span className={styles.value}>
                      {order.items.reduce((sum, item) => sum + item.quantity, 0)} Adet Ürün
                    </span>
                  </div>
                  <div className={styles.totalInfo}>
                    <span className={styles.label}>Toplam Tutar</span>
                    <span className={styles.totalValue}>{order.currency} {order.totalAmount}</span>
                  </div>
                </div>

                <div className={styles.cardFooter}>
                  <span className={styles.date}>
                    {new Date(order.createdAt).toLocaleDateString('tr-TR', {
                      year: 'numeric',
                      month: 'long',
                      day: 'numeric',
                      hour: '2-digit',
                      minute: '2-digit'
                    })}
                  </span>
                </div>
              </div>
            ))}
          </div>
        )}
      </main>
    </div>
  );
}
