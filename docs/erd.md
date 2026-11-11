```mermaid
erDiagram
  USERS ||--o{ SHORTENED_URLS : owns
  SHORTENED_URLS ||--o{ CLICK_EVENTS : receives
  USERS { BIGINT id PK VARCHAR email UK VARCHAR password_hash VARCHAR role TIMESTAMP created_at }
  SHORTENED_URLS { BIGINT id PK BIGINT user_id FK VARCHAR short_code UK VARCHAR original_url BOOLEAN enabled TIMESTAMP expires_at TIMESTAMP created_at TIMESTAMP updated_at }
  CLICK_EVENTS { BIGINT id PK BIGINT shortened_url_id FK TIMESTAMP clicked_at VARCHAR referrer VARCHAR device_category VARCHAR browser }
```
