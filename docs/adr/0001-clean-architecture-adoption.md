# ADR 0001: Clean Architecture Adoption

**Tarih:** 2026-02-13
**Durum:** Accepted (Kabul Edildi)

## Bağlam (Context)
E-ticaret projesinin geliştirilmesine başlarken, uygulamanın uzun ömürlü, test edilebilir ve değişime dirençli olması hedeflenmektedir. Geleneksel katmanlı mimarilerde (Layered Architecture) veritabanı bağımlılığının iş mantığına sızması ve framework değişikliklerinin maliyetli olması gibi sorunlar yaşanmaktadır.

Değerlendirilen Seçenekler:
1. Geleneksel Katmanlı Mimari (MVC vb.)
2. Hexagonal Architecture (Ports and Adapters)
3. Clean Architecture (Robert C. Martin)

## Karar (Decision)
Projeyi **Clean Architecture** prensiplerine göre yapılandıracağız.

Bu mimari ile:
*   İş kuralları (Entities/Use Cases) framework'lerden, UI'dan ve dış etkenlerden bağımsız olacak.
*   Bağımlılık kuralı (Dependency Rule) sıkı bir şekilde uygulanacak; kaynak kodu bağımlılıkları sadece içe doğru olacak.
*   Use Case odaklı paketleme (Screaming Architecture) benimsenecek.

## Sonuçlar (Consequences)
**Pozitif:**
*   Yüksek test edilebilirlik: İş mantığı UI veya DB olmadan test edilebilir.
*   Bağımsızlık: Veritabanı veya Web Framework değişse bile iş kuralları etkilenmeyecek.
*   Net sınırlar: Arayüzler (Ports) sayesinde modüller arası etkileşim kontrol altında olacak.

**Negatif:**
*   Başlangıç maliyeti (Overhead): Basit CRUD işlemleri için bile Interface, DTO, Entity dönüşümleri gerekecek.
*   Öğrenme eğrisi: Ekip üyelerinin Dependency Inversion ve Port/Adapter kavramlarına hakim olması gerekecek.

## Uyumluluk (Compliance)
*   Code Review süreçlerinde katman ihlalleri (örn: Entity içinde Controller kullanımı) reddedilecek.
*   Her Use Case için ayrı Input/Output modelleri tanımlanacak.
