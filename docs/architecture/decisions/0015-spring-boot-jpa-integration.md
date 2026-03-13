# ADR 015: Spring Boot ve Spring Data JPA'in Altyapı Katmanına Entegre Edilmesi

**Tarih:** 2026-03-14
**Durum:** Accepted (Kabul Edildi)

## Bağlam (Context)
Sistemimiz, Robert C. Martin'in Temiz Mimari (Clean Architecture) ve SOLID prensiplerine sıkı sıkıya bağlı kalarak tasarlanmış yapı üzerindedir. Sistemin çalışabilmesi için geçici olarak In-Memory Repository ve manuel Controller'lar kurulmuştu.
Üretim ortamı (Production) standartlarına geçiş yapabilmek adına, gerçek bir HTTP Web Sunucusu'na ve ilişkisel bir veritabanı (RDBMS) kalıcılık (persistence) çözümüne ihtiyaç bulunmaktadır.

Değerlendirilen Seçenekler:
1. Micronaut / Quarkus ile minimal web sunucusu, saf JDBC ile kalıcılık.
2. Spring Boot (Web + Actuator) ile uygulama bağlamını (Application Context) yönetmek ve Spring Data JPA (Hibernate) ile ORM haritalamalarını (mapping) sağlamak.

## Karar (Decision)
Dış dünyaya açılan kapıları yönetmek için Spring Boot framework'ü ve veritabanı iletişimi için Spring Data JPA **kullanacağız**.

Bu entegrasyonu tamamen "Infrastructure (Altyapı)" ve "Adapters (Bağdaştırıcılar)" katmanlarında sınırlayacağız. `Entities` ve `Use Cases` katmanlarına Spring Boot'a ya da JPA'ye ait hiçbir framework anotasyonu (`@Entity`, `@Autowired` vs.) **sızdırılmayacaktır**.

## Sonuçlar (Consequences)
**Pozitif:**
*   Geliştirme hızı artacak; Spring Boot'un inversion of control (IoC) ve dependency injection yetenekleriyle bileşenler (adapters, use cases) dış katmanda kolayca kablolanacaktır.
*   Spring Data JPA ile Repository adaptörlerindeki tekrarlı (boilerplate) CRUD kodları otomatik oluşturulacaktır.
*   Actuator sayesinde Kubernetes (Control Plane) seviyesinde "liveness" ve "readiness" uç noktaları anında elde edilecektir.

**Negatif:**
*   Daha ağır bir başlatma süresi (Cold Start) ve artan bellek tüketimi (Footprint).
*   Sistemde JPA/Hibernate LazyInitializationException gibi "Object-Relational Impedance Mismatch" hataları yönetilmek zorunda kalınacaktır. (Bu durum Use Case dışına çıkan sınır testleriyle önlenecektir.)

## Uyumluluk (Compliance)
*   Hiçbir "Domain Entity"sinde (Örn: `Product.java`, `Order.java`) JPA bağımlılıkları veya anotasyonları kullanılamaz. Bunun yerine `OrderJpaEntity` gibi ayrı adaptör modelleri oluşturulacak ve "Persistence Adapters" vasıtasıyla eşleştirilecektir (Mapping).
*   ArchUnit veya JDepend testleri, Use Case ve Entity sınıflarında `org.springframework` import'larını engellemeye devam edecektir.
