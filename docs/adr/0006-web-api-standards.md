# ADR 006: Web API Standards — Versioning, CORS, OpenAPI, RFC 7807

**Tarih:** 2026-03-23
**Durum:** Accepted (Kabul Edildi)

## Bağlam (Context)
API, dış tüketicilere (frontend, mobil, 3. parti) sunulmak üzere tasarlandı ancak REST standartlarına tam uyumlu değildi. POST işlemleri 200 OK döndürüyor, API versiyonlaması yoktu, CORS yapılandırılmamıştı, RFC 7807 hata zarfı kullanılmıyordu ve Swagger/OpenAPI dokümantasyonu eksikti.

Değerlendirilen Seçenekler:
1. URI-based API Versioning (`/api/v1/...`) — En yaygın ve anlaşılır yöntem.
2. Header-based Versioning (`Accept: vnd.ecommerce.v1+json`) — Daha clean ama tooling desteği zayıf.

## Karar (Decision)
1. **URI-based API Versioning** tercih edilerek tüm Controller'lar `/api/v1/` prefix'ine taşınacaktır.
2. Tüm Controller metotları `ResponseEntity<T>` döndürerek HTTP status code'ları açıkça kontrol edilecektir.
3. POST → 201 Created, DELETE → 204 No Content, başarısız ödeme → 422 Unprocessable Entity standartlarına geçilecektir.
4. `springdoc-openapi-starter-webmvc-ui` ile otomatik Swagger UI sağlanacaktır.
5. CORS, `SecurityConfig` üzerinden global olarak yapılandırılacaktır.
6. `GlobalExceptionHandler` RFC 7807 Problem Detail formatına dönüştürülecek; generic exception mesajları güvenlik gereği gizlenecektir.
7. Cart URI'leri RESTful noun-based yapıya dönüştürülecektir (`/cart/add` → `/cart/items`).

## Sonuçlar (Consequences)
**Pozitif:**
* API artık RFC standartlarına uyumlu; frontend ekipleri HTTP status kodlarına güvenebilir.
* Swagger UI sayesinde API keşfedilebilirliği %100 artırıldı.
* Generic exception mesajları artık dışarıya sızdırılmıyor, güvenlik açığı kapatıldı.

**Negatif:**
* Mevcut API tüketicileri (`/products` → `/api/v1/products`) olarak güncellenmeli (Breaking Change).
* `ScenarioRunner` gibi iç bileşenler `.getBody()` çağrısı ile uyumlandırılmak zorunda kaldı.

## Uyumluluk (Compliance)
* Code Review'da tüm Controller metotlarının `ResponseEntity<T>` döndürmesi zorunlu tutulacaktır.
* Yeni endpoint eklenmeden önce Swagger UI üzerinden doğrulanması gerekecektir.
