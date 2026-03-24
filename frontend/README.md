# Clean Commerce — Frontend

Modern, dayanıklı ve tip güvenli React uygulaması. Backend'deki Clean Architecture ve SRE prensiplerini arayüze taşır.

## Kurulum

```bash
cd frontend
npm install
cp .env.example .env.local   # Ortam değişkenlerini ayarla
npm run dev                   # Geliştirme sunucusunu başlat (http://localhost:5173)
```

## Ortam Değişkenleri

| Değişken | Açıklama | Varsayılan |
|----------|----------|------------|
| `VITE_API_BASE_URL` | Backend API adresi | `http://localhost:8080` |
| `VITE_REQUEST_TIMEOUT_MS` | HTTP istek zaman aşımı (ms) | `5000` |

## Mimari

```
src/
├── app/               → Composition Root (routes, providers)
├── infrastructure/    → Frameworks & Drivers (Axios, Auth, ErrorBoundary)
├── features/          → Bounded Contexts (catalog, cart, checkout)
├── shared/            → Reusable UI primitives
└── styles/            → Design System tokens
```

**Katman Kuralı:** `View → Hooks → Services` — bağımlılık her zaman içe doğru.

## İlgili ADR'ler

- [ADR 0001 — Clean Architecture](../docs/adr/0001-clean-architecture-adoption.md)
- [ADR 0006 — Web API Standards](../docs/adr/0006-web-api-standards.md)

## Backend ile Çalıştırma

```bash
# Terminal 1: Backend
cd .. && mvn spring-boot:run

# Terminal 2: Frontend
npm run dev
```

Swagger UI: [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
