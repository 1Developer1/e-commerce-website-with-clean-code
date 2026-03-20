# Veritabanı Analiz Raporu: "Database is a Detail" İlkesi İhlal Edildi Mi?

## Yönetici Özeti (Executive Summary)
Sistemi kod seviyesinde baştan sona inceledim. **Clean Architecture'ın "Veritabanı bir detaydır" ilkesi HİÇBİR ŞEKİLDE delinmemiştir.** Sistem, veritabanı okuma, yazma ve sorgulama işlemlerini tam olarak mimarinin en dış katmanlarına (Frameworks & Drivers ve Interface Adapters) başarıyla itmiştir. İç katmanlar veritabanından tamamen habersizdir.

Aşağıda bu mimarinin teknik kanıtları ve işleyişi detaylandırılmıştır.

---

## 1. Domain Katmanı (Enterprise Business Rules)

Sistemin kalbinde yer alan Domain Entity'leri incelenmiştir.
Örn: `com.ecommerce.order.entity.Order` sınıfı.

**Kanıt:**
* `Order` sınıfı içerisinde Hibernate veya Spring JPA'ya ait **tek bir anotasyon (@Entity, @Table, @Column, @Id vb.) dahi BULUNMAMAKTADIR.**
* Tamamen "Saf Java" (POJO) kullanılarak yazılmıştır.
* Veritabanında nasıl saklanacağına (ilişkisel mi, NoSQL mi tablo adı ne olacak) dair zerre kadar bilgi barındırmaz. İş mantığı olan `pay()` ve hesaplama kısımlarını kendi içinde barındırır.

---

## 2. Use Case Katmanı (Application Business Rules)

Sorgulama ve yazma işlemlerinin tetiklendiği katmandır.
Örn: `com.ecommerce.order.usecase.PlaceOrderUseCase`

**Kanıt:**
* `PlaceOrderUseCase` sınıfı, doğrudan Spring Data'nın `JpaRepository` arayüzünü çağırmaz veya `EntityManager` kullanmaz.
* Bunun yerine `com.ecommerce.order.usecase.OrderRepository` isimli bir **arayüze (Interface/Output Port)** bağımlıdır.

```java
// Sadece bir UseCase Output Portu (Framework bağımsız)
public interface OrderRepository {
    void save(Order order);
    Optional<Order> findById(UUID id);
    // JPA bağımlılığı YOK. Spring import'u YOK. Database import'u YOK.
}
```

**Sonuç:** Dependency Inversion (Bağımlılığı Tersine Çevirme) kuralı eksiksiz uygulanmıştır. Kaynak kodu bağımlılığı Use Case'den dışarıya doğru değil, dışarıdan Use Case'e doğru bakmaktadır.

---

## 3. Interface Adapters ve Frameworks Katmanı (Geçek Veritabanı İşlemleri)

Clean Architecture'a göre veritabanı "kirli" bir detaydır ve dışarıda tutulmalıdır. E-commerce projesinde de bu işlemler `adapter.out.persistence.jpa` paketi içerisinde izole edilmiştir.

### 3.1. JPA Entity'leri (Veritabanı Tablosu Temsilleri)
Modüllerin persistence katmanında `OrderJpaEntity` veya `ProductJpaEntity` gibi ayrı sınıflar bulunur. Veritabanı anotasyonları sadece bu dış katmandaki sınıflara uygulanmıştır.

```java
// adapter/out/persistence/jpa/entity/OrderJpaEntity.java
@Entity
@Table(name = "orders")
public class OrderJpaEntity { ... }
```

### 3.2. Mapping ve Adapter Sınıfları (Interface Adapters)
Veritabanına asıl kaydın yapıldığı sınıf `OrderPersistenceAdapter`'dır. Bu sınıf, Use Case katmanındaki temiz `OrderRepository` arayüzünü implemente eder.

**Nasıl Çalışır? (Kanıt Niteliğinde Akış):**

1. Use Case, kaydetmesi için temiz `Order` entity tipinde bir nesneyi bu adapter'a gönderir.
2. `OrderPersistenceAdapter`, Framework'e özgü olan `OrderSpringRepository`'yi (JpaRepository) enjekte eder.
3. İçerde **Mapper** işlemi yapılır. Yani temiz olan `Order` (Domain Entity) okunur, verileri alınarak veritabanının anladığı `OrderJpaEntity` (JPA Entity) oluşturulur.
4. Framework'e özgü olan kayıt işlemi `orderSpringRepository.save(entity);` şeklinde icra edilir.

```java
@Component
public class OrderPersistenceAdapter implements OrderRepository {
    
    // ... repository injection ...

    @Override
    public void save(Order order) {
        // [1] Domain nesnesi geldi (Order)
        // [2] Onu kaba kuvvete dayalı veritabanı (JPA) nesnesine dönüştür
        OrderJpaEntity entity = mapToJpaEntity(order);
        // [3] Framework kullanarak kaydet
        orderSpringRepository.save(entity);
    }
}
```

---

## Sonuç ve Risk Değerlendirmesi

Sistem, "Veritabanı bir detaydır" ilkesi çerçevesinde **mükemmelen izole edilmiştir.**

**Olası Riskleri Nasıl Çözdük?**
* **ORM Çöküşü (Vendor Lock-in):** Şu anda Spring Data JPA (Hibernate) ve H2 kullanılmaktadır. Yarın MongoDB'ye geçilmek istenirse projede `Domain`, `UseCase` veya `Web Controller` kodlarından hiçbirine DO-KUN-MAYACAKSINIZ. Sadece `MongoPersistenceAdapter` yazıp `OrderRepository`'yi implemente etmeniz yeterli olacaktır.
* **Domain Sızıntısı:** JPA entity'lerinin UI veya diğer modüller tarafından doğrudan kullanılamaması (bunun için DTO'lar veya Mapperlar kullanılması) Clean prensipleri güçlü tutmuştur.

Mimaride bu anlamda hiçbir delinme, ihmal veya taviz **söz konusu değildir.**
