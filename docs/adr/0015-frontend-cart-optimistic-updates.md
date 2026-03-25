# ADR 015: Frontend Sepet Yönetimi ve Optimistic UI Kullanımı

**Tarih:** 2026-03-25
**Durum:** Accepted (Kabul Edildi)

## Bağlam (Context)
Frontend sisteminde sepet (Cart) yönetimi uygulanırken (Faz 3), ürünlerin sepete eklenmesi işlemlerinin sunucu yanıtı beklenmeden anında arayüze yansıması hedeflenmiştir. Aksi takdirde sunucu yanıt süresi kadar olan gecikme (latency), alışveriş akışında kesintiye ve kötü kullanıcı deneyimine ("zero-latency" hissinin kaybına) neden olmaktadır.

Kullanılabilecek yapılandırmalar:
1. Temel Loading Statede (Spinner) bekletmek.
2. React Query'nin `onMutate` fonksiyonelliğinden yararlanarak Optimistic Updates (İyimser Güncellemeler) uygulamak.

## Karar (Decision)
Sepete ürün ekleme ve miktar güncelleme işlemlerinde **React Query'nin Optimistic Updates mimarisini kullanacağız.** Arayüzdeki sepet sayıları, API isteği fırlatıldığında anında artırılacak, arka plandaki API isteği başarısız olursa geçmiş veriye (snapshot) geri dönüş (Rollback) yapılacaktır.

Sepet arayüzü ise global bir "CartDrawer" (Çekmece) bileşeni ile Context API (`UIContext`) üzerinden yönetilecektir.

## Sonuçlar (Consequences)
**Pozitif:**
* Kullanıcı etkileşimleri anlık yanıt verir; UI hızı inanılmaz derecede yüksek hissedilir.
* Müşteri satın alma yolculuğunda engeller veya donmalar ortadan kalkar.

**Negatif:**
* Cache manipülasyonu mantığı (manuel cache güncellemesi) nedeniyle kod karmaşıklığı artmıştır (`useAddToCart` hook içerisinde cache setleme kuralları).
* Eğer bağlantı anlık kopuksa, arka planda fail eden istek kullanıcıya bir anlık başarılı gibi görünüp ardından eski sayısına geri döner (Rollback hissi şaşırtıcı olabilir).

## Uyumluluk (Compliance)
* Herhangi bir sepet manipulasyonu mutasyonunda mutlak suretle `onError` içinde cache'e eski değerlerin dönmesi (rollback) uygulanmalıdır.
