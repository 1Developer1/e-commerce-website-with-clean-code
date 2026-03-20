# ADR 0019: Controller/Presenter Veri Akışı ve Security/Rate Limit Filtre Sıralaması

**Tarih:** 2026-03-21
**Durum:** Accepted

## Bağlam (Context)

Clean Architecture projemizin entegrasyon testleri (ör. `RateLimitingFilterTest`, `ResilienceChaosTest`) ve `ArchitectureTest` suit'i yürütülürken iki temel mimari sorun tespit edildi:
1.  **Presenter ve ViewModel Dönüşümü:** Önceden Use Case `Output` nesneleri, doğrudan Spring `@RestController`'ları tarafından `ResponseEntity` veya API yanıtı olarak geri döndürülüyordu. Bu, API'nin (Web Çerçevesi Katmanı) Domain kurallarına bağlı sınıf adlarını ve şekillerini doğrudan sızdırmasına ve Clean Architecture prensiplerine ters düşmesine neden oluyordu.
2.  **Filter Zincirlemesi Problemi:** Uygulamamız IP tabanlı `RateLimitingFilter` (Resilience4j) ve `JwtAuthenticationFilter` kullanmaktadır. Rate Limiter, Spring Boot tabanlı entegrasyon testlerinde IP kısıtlamalarını beklenen şekilde test edemiyordu çünkü `SecurityFilterChain` hatalı isteklerde `401/403` döndürüp filtre zincirini erken kesiyordu. Bu nedenle `429 Too Many Requests` statusu testler sırasında ölçülemiyordu. 

Değerlendirilen Seçenekler:
1. API yanıt formatları için ayrı "ViewModel" Java Kayıtları (Records) oluşturmak vs. Dinamik bir `Map<String, Object>` döndürmek.
2. `RateLimitingFilter`'ı SecurityFilterChain içine bir Spring Security filtresi olarak eklemek vs. En dıştaki Servlet Filter katmanından (Ordered) en erken aşamada çalıştırmak.

## Karar (Decision)

1. **Presenter üzerinden Map Dönüşümü Kullanılacaktır:** Presenter sınıfları `Output` modellerini alıp `Map<String, Object>` dönecektir. Web katmanı (Controller), JSON serileştirmesinde bu Map yapısını dışarıya açacaktır. Bu yapı, Web Katmanı için fazladan statik ViewModel (DTO) sınıfları tanımlamaktan kaçınarak esnekliği artırmakta ve Domain nesnelerini API sözleşmesinden tamamen yalıtmaktadır.
2. **RateLimitingFilter @Order ile Korunacaktır:** `RateLimitingFilter`'a Spring'in `@Order(Ordered.HIGHEST_PRECEDENCE)` anotasyonu eklenmiştir. Bu nedenle Rate Limiter, `SecurityFilterChain`'den Geleneksel Kimlik Doğrulama denetiminden bile önce çalıştırılarak Denial-of-Service (DoS) saldırılarına karşı en erken safhada yükü azaltacaktır (Shed Load).

## Sonuçlar (Consequences)

**Pozitif:**
*   **Gevşek Bağlantı (Loose Coupling):** Entity ve Use Case modelleri tamamen izole edildi. Web framework'ü değişmiş olsa bile, dış API serileştirmesi (JSON yapıları) etkilenmeyecek.
*   **Erken Reddetme (Fail Fast):** Aşırı yük veya spam yapanlar, güvenlik filtreleri (veritabanından JWT parse etme, imza doğrulama vb.) çalıştırılmadan engellenecek, böylece CPU ve RAM tasarrufu sağlanacak.

**Negatif:**
*   **Tip Güvenliği (Type Safety):** ViewModel olarak `Map<String, Object>` dönülmesi Java seviyesinde tip güvenliğini kaldırır. Swagger/OpenAPI gibi otomatik dokümantasyon araçları dışa dönük API modelini belirlemekte zorlanabilir. İlerleyen aşamalarda statik `ViewModel` rekorlarına dönüş gerekebilir.
*   **İzleme:** Kimliği doğrulanmamış loglar (anonim istekler) Rate Limiter loglarında yoğun yer tutacak.

## Uyumluluk (Compliance)
*   **ArchitectureTest:** Controller sınıfları yalnızca Use Case nesnelerine değil, `Presenter` bean'lerine bağımlı olmak zorundadır.
*   **Integration Tests:** API sınırlarını test eden Spring testleri 429 yanıtını ilk filtreden beklemelidir.
