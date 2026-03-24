import { useState, type FormEvent } from 'react';
import { useCreateProduct } from '../hooks/useCreateProduct';
import styles from './CreateProductModal.module.css';

interface CreateProductModalProps {
  isOpen: boolean;
  onClose: () => void;
}

/**
 * Modal form for creating a new product.
 * Client-side validation matches backend Jakarta constraints.
 */
export function CreateProductModal({ isOpen, onClose }: CreateProductModalProps) {
  const createProduct = useCreateProduct();

  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [priceAmount, setPriceAmount] = useState('');
  const [priceCurrency, setPriceCurrency] = useState('USD');
  const [initialStock, setInitialStock] = useState('');

  if (!isOpen) return null;

  const handleSubmit = (e: FormEvent) => {
    e.preventDefault();
    createProduct.mutate(
      {
        name: name.trim(),
        description: description.trim() || undefined,
        priceAmount: parseFloat(priceAmount),
        priceCurrency,
        initialStock: parseInt(initialStock, 10),
      },
      {
        onSuccess: () => {
          setName('');
          setDescription('');
          setPriceAmount('');
          setInitialStock('');
          onClose();
        },
      }
    );
  };

  return (
    <div className={styles.overlay} onClick={onClose} role="dialog" aria-modal="true" aria-label="Yeni Ürün Ekle">
      <div className={styles.modal} onClick={(e) => e.stopPropagation()}>
        <div className={styles.header}>
          <h2 className={styles.title}>Yeni Ürün Ekle</h2>
          <button className={styles.closeButton} onClick={onClose} aria-label="Kapat">✕</button>
        </div>

        <form onSubmit={handleSubmit} className={styles.form}>
          <div className={styles.field}>
            <label htmlFor="product-name" className={styles.label}>Ürün Adı *</label>
            <input
              id="product-name"
              type="text"
              className={styles.input}
              value={name}
              onChange={(e) => setName(e.target.value)}
              required
              minLength={2}
              maxLength={200}
              placeholder="Minimum 2, maksimum 200 karakter"
            />
          </div>

          <div className={styles.field}>
            <label htmlFor="product-desc" className={styles.label}>Açıklama</label>
            <textarea
              id="product-desc"
              className={styles.textarea}
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              maxLength={1000}
              rows={3}
              placeholder="Opsiyonel, maksimum 1000 karakter"
            />
          </div>

          <div className={styles.row}>
            <div className={styles.field}>
              <label htmlFor="product-price" className={styles.label}>Fiyat *</label>
              <input
                id="product-price"
                type="number"
                className={styles.input}
                value={priceAmount}
                onChange={(e) => setPriceAmount(e.target.value)}
                required
                min="0.01"
                step="0.01"
                placeholder="0.00"
              />
            </div>
            <div className={styles.field}>
              <label htmlFor="product-currency" className={styles.label}>Para Birimi *</label>
              <select
                id="product-currency"
                className={styles.input}
                value={priceCurrency}
                onChange={(e) => setPriceCurrency(e.target.value)}
              >
                <option value="USD">USD</option>
                <option value="TRY">TRY</option>
                <option value="EUR">EUR</option>
              </select>
            </div>
          </div>

          <div className={styles.field}>
            <label htmlFor="product-stock" className={styles.label}>Stok Adedi *</label>
            <input
              id="product-stock"
              type="number"
              className={styles.input}
              value={initialStock}
              onChange={(e) => setInitialStock(e.target.value)}
              required
              min="1"
              placeholder="Minimum 1"
            />
          </div>

          {createProduct.isError && (
            <div className={styles.error} role="alert">
              {createProduct.error.message}
            </div>
          )}

          <button
            type="submit"
            className={styles.submitButton}
            disabled={createProduct.isPending}
          >
            {createProduct.isPending ? 'Ekleniyor...' : 'Ürün Ekle'}
          </button>
        </form>
      </div>
    </div>
  );
}
