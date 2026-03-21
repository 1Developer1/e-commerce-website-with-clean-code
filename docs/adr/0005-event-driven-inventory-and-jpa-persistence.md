# ADR 005: Event-Driven Envanter Yönetimi ve Tam Kapsamlı JPA Veritabanı Geçişi

**Tarih:** 2026-03-22
**Durum:** Accepted (Kabul Edildi)

## Bağlam (Context)
Sipariş oluşturulduğunda ürün stoklarının düşürülmesi (inventory deduction) gerekiyordu. Aynı zamanda, sepet (Cart) ve indirim (Discount) modülleri geçici In-Memory repository'ler kullandığı için sistem tamamen Production-Ready (Kalıcı veritabanı ile) değildi. Bu iki sürecin birbirini kilitlemeyen, genişletilebilir ve temiz mimariye sadık bir şekilde çözülmesi gerekti.

Değerlendirilen Seçenekler:
1. **Asenkron Event-Bus ve JPA Entity Adapter'ları kullanmak.** (Temiz Mimari'nin kalbine dokunmadan).
2. **Use Case içinden doğrudan Repository çağırmak ve Tüm nesneleri tek bir God-JPA ile sarmak.** (SOLID ve Clean Architecture kurallarını ihlal eder).

## Karar (Decision)
**Sipariş anında stok düşürme işlemi için In-Memory Event-Bus (SimpleEventBus) tasarlayarak asenkron mantığı simüle eden bir Event-Driven yapı kuracağız. Cart ve Discount modülleri için ise Clean Architecture'un Interface Adapter katmanında JPA Entity'leri (CartJpaEntity, DiscountJpaEntity) ve Persistence Adapter'ları (CartPersistenceAdapter, DiscountPersistenceAdapter) oluşturarak kalıcı veritabanı altyapısına bağlayacağız.**

Bu karar sayesinde `Order` modülü, `Product` modülündeki güncellemelerden habersiz kalarak kendi sorumluluğuna odaklanır. Sepet ve indirimler ise Persistence Adapter'lar aracılığıyla domain kurallarından yalıtılmış olarak veritabanına yansıtılır.

## Sonuçlar (Consequences)
**Pozitif:**
* Domain katmanı kalıcı depolama kütüphanelerinden (`@Entity`, `@Table`) %100 soyutlanmış olarak kalmaya devam etti.
* `PlaceOrderUseCase` sadece `OrderPlacedEvent` isimli bir DomainEvent oluşturup fırlatıyor, böylelikle `Product` modülündeki stock düşürme adımı loosely-coupled (gevşek bağlı) şekilde bağlandı.
* Sistem artık In-Memory'den tamamen JPA altyapısına geçmiş olup Production-Ready standartlarına erişti.

**Negatif:**
* Entity ile JpaEntity arasındaki Dönüştürme (Mapping) sınıfları/metotları veriyi çift defa yazmamıza sebep oldu ve Boilerplate kod miktarını artırdı.
* Event-Bus hafıza (in-memory) bazlı kullanıldığı için gerçek bir asenkron/dağıtık (Kafka/RabbitMQ) ortamına geçildiğinde EventBus altyapısının değiştirilmesi gerekecek.

## Uyumluluk (Compliance)
* Mimari ekipleri Code Review aşamasında Use Case'ler arasında doğrudan (direct call) haberleşme olup olmadığını denetleyecek, dış sınırları ilgilendiren senaryolarda `DomainEvent` kullanılmasını zorunlu tutacaktır.
* Herhangi bir Domain varlığına Database-Specific (JPA gibi) Annotation eklenmesi ArchUnit testleriyle engellenecektir.
