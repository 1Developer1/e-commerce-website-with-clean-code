import { useState } from 'react';
import { useProducts } from '../hooks/useProducts';
import { Navbar } from '../../../shared/components/Navbar/Navbar';
import { ProductGrid } from '../components/ProductGrid';
import { CreateProductModal } from '../components/CreateProductModal';
import { ProductGridSkeleton } from '../../../shared/components/Skeleton/Skeleton';
import { useAddToCart } from '../../cart/hooks/useAddToCart';
import styles from './CatalogPage.module.css';

/**
 * CatalogPage — the main product listing page.
 * Demonstrates all 4 states: Loading, Error, Empty, Success.
 */
export function CatalogPage() {
  const [isModalOpen, setIsModalOpen] = useState(false);
  const { data, isLoading, isError, error, refetch } = useProducts();
  const addToCart = useAddToCart();

  return (
    <div className={styles.page}>
      <Navbar />

      <main className={styles.main}>
        <div className={styles.header}>
          <div>
            <h1 className={styles.title}>Ürün Kataloğu</h1>
            <p className={styles.subtitle}>
              {data ? `${data.totalElements} ürün listeleniyor` : 'Yükleniyor...'}
            </p>
          </div>
          <button
            className={styles.addButton}
            onClick={() => setIsModalOpen(true)}
            aria-label="Yeni ürün ekle"
          >
            + Yeni Ürün
          </button>
        </div>

        {/* Loading State */}
        {isLoading && <ProductGridSkeleton count={6} />}

        {/* Error State */}
        {isError && (
          <div className={styles.errorState} role="alert">
            <span className={styles.errorIcon}>⚠️</span>
            <h2>Ürünler yüklenemedi</h2>
            <p className={styles.errorMessage}>{error.message}</p>
            <button className={styles.retryButton} onClick={() => refetch()}>
              Tekrar Dene
            </button>
          </div>
        )}

        {/* Success State */}
        {data && (
          <ProductGrid 
            products={data.products} 
            onAddToCart={(productId) => addToCart.mutate({ productId, quantity: 1 })}
          />
        )}
      </main>

      <CreateProductModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
      />
    </div>
  );
}
