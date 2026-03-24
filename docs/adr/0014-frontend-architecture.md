# ADR 0014: Frontend Mimari Kararları — React + Clean Architecture

**Tarih:** 2026-03-25
**Durum:** Accepted (Kabul Edildi)

## Bağlam (Context)

Backend, Clean Architecture prensipleriyle inşa edilmiş 5 Bounded Context (Product, Cart, Order, Payment, Shipping), JWT tabanlı kimlik doğrulama, RFC 7807 hata formatı ve Resilience4j dayanıklılık mekanizmalarına sahiptir. Frontend'in bu altyapıyla uyumlu, aynı mimari disiplini sürdüren ve SRE prensiplerini uygulayan bir yapıda tasarlanması gerekmektedir.

Değerlendirilen seçenekler:
1. **Angular** — Ağır framework, dik öğrenme eğrisi, projemiz için aşırı mühendislik (over-engineering).
2. **Vue.js** — Daha küçük ekosistem, TypeScript desteği React kadar olgunlaşmamış.
3. **React + Vite + TypeScript** — Component-based mimari Clean Architecture'daki View/Presenter ayrımına doğal uyum, Vite ile hızlı geliştirme deneyimi, TypeScript strict mode ile backend DTO'larla birebir tip güvenliği.

## Karar (Decision)

### Teknoloji Yığını
- **React 18 + Vite 5 + TypeScript (strict mode)** kullanacağız.
- **React Query (TanStack Query v5)** ile veri çekme, cache yönetimi ve SRE retry/stale stratejisi uygulanacak.
- **Axios** ile merkezi HTTP istemcisi kurulacak (JWT interceptor, 401 auto-logout, RFC 7807 parse, 5s timeout).
- **Vanilla CSS Modules** ile framework bağımsız stil yönetimi sağlanacak.
- **React Router v6** ile korumalı/açık rota ayrımı yapılacak.

### Mimari Katmanlar (Frontend Clean Architecture)
Frontend 3 katmanlı bir yapıda organize edilecek:
1. **View (Components/Pages):** Sadece JSX/CSS. Asla doğrudan API çağrısı yapmaz.
2. **Application (Custom Hooks):** useQuery/useMutation ile iş akışını orkestra eder.
3. **Infrastructure (API Services):** Axios ile backend iletişimi, DTO dönüşüm, token yönetimi.

Bağımlılık kuralı: `View → Hooks → Services` (her zaman içe doğru, asla geriye dönmez).

### Klasör Yapısı (Screaming Architecture)
`features/` dizini altında `catalog/`, `cart/`, `checkout/` şeklinde iş alanına göre organize edilecek. Teknik klasör isimleri (models, controllers) yerine iş domain'i görünecek.

### SRE Stratejileri
- **Timeout:** 5 saniye (Axios)
- **Retry:** GET istekleri 3 kez, exponential backoff (1s → 2s → 4s)
- **Error Boundary:** 3 seviye (Global, Feature, Component)
- **Token saklama:** localStorage (gerekçe: backend httpOnly cookie desteklemiyor)
- **401 handling:** Interceptor'da otomatik token silme ve /login yönlendirmesi

## Sonuçlar (Consequences)

**Pozitif:**
- Backend ile aynı mimari disiplin sürdürülecek (Screaming Architecture, katman ayrımı).
- TypeScript strict mode ile runtime hatalar derleme zamanında yakalanacak.
- React Query cache sayesinde gereksiz API çağrıları engellenecek.
- Vanilla CSS ile herhangi bir CSS framework bağımlılığı oluşmayacak.

**Negatif:**
- Custom hook + service katman ayrımı, basit bir component için ekstra dosya gerektirir (boilerplate).
- localStorage XSS riski taşır; Content-Security-Policy ve input sanitization ile azaltılmalıdır.
- Vanilla CSS, Tailwind gibi utility-first yaklaşımlara göre daha fazla CSS kodu yazılmasını gerektirir.

## Uyumluluk (Compliance)

- Code Review'da component içinde doğrudan `axios`/`fetch` çağrısı görülürse reddedilecek.
- Her yeni feature, `features/<domain>/` altında kendi `api/`, `hooks/`, `components/`, `types/` alt dizinlerine sahip olacak.
- TypeScript `any` kullanımı yasaktır; `strict: true` tsconfig'de zorunlu tutulacak.
