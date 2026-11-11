# API reference

Base URL: `http://localhost:8080`. JSON is used for request/response bodies. Protected routes require `Authorization: Bearer <JWT>`.

| Method | Path | Auth | Success |
|---|---|---:|---:|
| POST | `/api/auth/register` | No | 201 |
| POST | `/api/auth/login` | No | 200 |
| POST | `/api/urls` | Yes | 201 |
| GET | `/api/urls` | Yes | 200 paginated |
| GET | `/api/urls/{id}` | Yes | 200 |
| PATCH | `/api/urls/{id}` | Yes | 200 |
| DELETE | `/api/urls/{id}` | Yes | 204 |
| GET | `/api/urls/{id}/analytics` | Yes | 200 |
| GET | `/api/dashboard` | Yes | 200 |
| GET | `/r/{code}` | No | 302 |

Create body: `{"originalUrl":"https://example.org/path","customAlias":"my-link","expiresAt":null}`. `customAlias` is optional and must match `[A-Za-z0-9_-]{4,32}`. `expiresAt` is an ISO-8601 timestamp in the future.

Update body accepts any subset of `originalUrl`, `expiresAt`, and `enabled`. Set `clearExpiration: true` to remove an existing expiry; otherwise omitted/null `expiresAt` means no expiry change.

List query parameters: `page` (zero-based), `size` (1-100), `enabled` (optional boolean), and `sort` (for example `createdAt,desc`). Rate limit responses use HTTP 429. Unknown codes return 404; disabled/expired codes return 410.

Swagger UI is exposed at `/swagger-ui.html`; the generated OpenAPI document is `/v3/api-docs`.
