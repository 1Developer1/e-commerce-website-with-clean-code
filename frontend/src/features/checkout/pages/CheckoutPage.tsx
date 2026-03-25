import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { usePlaceOrder } from '../hooks/usePlaceOrder';
import { usePayOrder } from '../hooks/usePayOrder';
import styles from './CheckoutPage.module.css';

type Step = 'ADDRESS' | 'PAYMENT' | 'SUCCESS';

export function CheckoutPage() {
  const [step, setStep] = useState<Step>('ADDRESS');
  const [orderInfo, setOrderInfo] = useState<{ id: string; currency: string; amount: number } | null>(null);
  const navigate = useNavigate();

  const placeOrder = usePlaceOrder();
  const payOrder = usePayOrder();

  const handleAddressSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    
    // Address is mocked, directly place order
    placeOrder.mutate(undefined, {
      onSuccess: (data) => {
        if (data.success && data.orderId && data.displayTotal) {
          const parts = data.displayTotal.split(' ');
          const currency = parts[0];
          const amount = parseFloat(parts[1]);
          setOrderInfo({ id: data.orderId, currency, amount });
          setStep('PAYMENT');
        } else {
          // Handle logic error from backend
          alert(data.message);
        }
      }
    });
  };

  const handlePaymentSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!orderInfo) return;

    payOrder.mutate({
      orderId: orderInfo.id,
      amount: orderInfo.amount,
      currency: orderInfo.currency,
      method: 'CREDIT_CARD' // Mocked method for UI simplicity
    }, {
      onSuccess: () => setStep('SUCCESS')
    });
  };

  return (
    <div className={styles.page}>
      <div className={styles.container}>
        {/* Stepper Header */}
        <div className={styles.stepper}>
          <div className={`${styles.step} ${step === 'ADDRESS' ? styles.active : ''}`}>1. Adres</div>
          <div className={styles.connector}></div>
          <div className={`${styles.step} ${step === 'PAYMENT' ? styles.active : ''}`}>2. Ödeme</div>
          <div className={styles.connector}></div>
          <div className={`${styles.step} ${step === 'SUCCESS' ? styles.active : ''}`}>3. Tamamlandı</div>
        </div>

        {/* Contents */}
        <div className={styles.content}>
          {step === 'ADDRESS' && (
            <form onSubmit={handleAddressSubmit} className={styles.form}>
              <h2>Teslimat Bilgileri</h2>
              <div className={styles.formGroup}>
                <label>Ad Soyad</label>
                <input type="text" required placeholder="John Doe" />
              </div>
              <div className={styles.formGroup}>
                <label>Açık Adres</label>
                <textarea required placeholder="Mahalle, sokak, no..." rows={3}></textarea>
              </div>
              {placeOrder.isError && <p className={styles.error}>{placeOrder.error.message}</p>}
              <button 
                type="submit" 
                className={styles.primaryButton}
                disabled={placeOrder.isPending}
              >
                {placeOrder.isPending ? 'Sipariş Oluşturuluyor...' : 'Devam Et'}
              </button>
            </form>
          )}

          {step === 'PAYMENT' && (
            <form onSubmit={handlePaymentSubmit} className={styles.form}>
              <h2>Ödeme ({orderInfo?.currency} {orderInfo?.amount})</h2>
              <div className={styles.formGroup}>
                <label>Kart Numarası</label>
                <input type="text" required placeholder="0000 0000 0000 0000" maxLength={19} />
              </div>
              <div className={styles.rowGroup}>
                <div className={styles.formGroup}>
                  <label>Son Kullanma</label>
                  <input type="text" required placeholder="MM/YY" maxLength={5} />
                </div>
                <div className={styles.formGroup}>
                  <label>CVC</label>
                  <input type="text" required placeholder="123" maxLength={3} />
                </div>
              </div>
              {payOrder.isError && <p className={styles.error}>{payOrder.error.message}</p>}
              <button 
                type="submit" 
                className={styles.primaryButton}
                disabled={payOrder.isPending}
              >
                {payOrder.isPending ? 'Ödeme İşleniyor...' : 'Güvenli Ödeme Yap'}
              </button>
            </form>
          )}

          {step === 'SUCCESS' && (
            <div className={styles.successState}>
              <div className={styles.successIcon}>🎉</div>
              <h2>Siparişiniz Alındı!</h2>
              <p>Ödemeniz başarıyla gerçekleşti ve siparişiniz hazırlanıyor.</p>
              <button className={styles.secondaryButton} onClick={() => navigate('/catalog')}>
                Alışverişe Devam Et
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
