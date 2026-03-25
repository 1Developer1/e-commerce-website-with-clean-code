# ADR 016: Frontend Checkout Form ve Ödeme Akışı Entegrasyonu

**Tarih:** 2026-03-25
**Durum:** Accepted (Kabul Edildi)

## Bağlam (Context)
Faz 4 (Checkout) geliştirilirken, kullanıcının Adres bilgilerini girmesi, ardından Ödeme bilgilerini doldurarak siparişi tamamlaması gerekmiştir. Bu aşamada backend sipariş ve ödeme süreçlerini iki farklı servis (Order ve Payment) olarak ele almaktadır. Sürecin Frontend üzerinde pürüzsüz akması için güvenli ve yönlendirmeli bir sihirbaz (Stepper) ihtiyacı vardır. 

Değerlendirilen Seçenekler:
1. Tüm formları (Adres + Kredi Kartı) tek bir karmaşık ve büyük sayfada sunmak.
2. Formik vs gibi ağır form kütüphanelerine bağımlı kalarak state tutmak.
3. React'in kendi "Controlled Components" mantığını kullanarak bir Stepper (3 adımlı) yaklaşımı oluşturmak ve veri iletimini lokal state'te korumak.

## Karar (Decision)
Checkout süreci `Adres -> Ödeme -> Başarılı` olmak üzere **3 adımlı lokal State (Stepper) üzerinden Vanilla React form yönetimi ile inşa edilecektir.** 

Siparişi oluşturma (`usePlaceOrder`) ve siparişi ödeme (`usePayOrder`) işlemleri mimari olarak birbirinden ayrılacak; Adres formundan sonra Order APIden Sipariş ID'si alınacak, Ödeme formunda bu Order ID'ye ait ödeme talebi Payment API'sine aktarılacaktır.

## Sonuçlar (Consequences)
**Pozitif:**
* Frontend ve Backend'in ayrılmış Bounded Context mimarisiyle (Order ve Payment) %100 uyumlu bir UX akışı sağlandı.
* Çok büyük dış bağımlılıklar (büyük form kütüphaneleri) kullanılmadan sade ve hızlı bir Clean View inşa edildi.

**Negatif:**
* Stepper logic'inde adımlar arası ileri-geri gidişler, Order ID'nin geçici bellekte tutulmasını zorunlu kılar; süreç tam ortada yarıda kesilirse "Ödenmemiş Sipariş" (Unpaid Order) oluşacaktır.

## Uyumluluk (Compliance)
* CheckoutPage içerisindeki formlar kendi adımı geçilmeden diğer adıma verileri sızdırmayacaktır.
* React Query Mutationları kullanılarak, API'den DTO yanıtı gelene kadar mutlaka Submit butonlarında Loading State (`isLoading=true`) gösterilmelidir.
