# Candle Craft backend

Spring Boot API backed by Spring Data JPA and MySQL.

## Local configuration

Spring Boot does not load a generic `.env` file automatically. Set the environment variables in your terminal or IDE launch configuration. Keep Aiven credentials and the JWT signing secret out of source control and frontend configuration.

```powershell
$env:DB_URL = "jdbc:mysql://HOST:PORT/defaultdb?sslMode=REQUIRED"
$env:DB_USERNAME = "YOUR_AIVEN_USERNAME"
$env:DB_PASSWORD = "YOUR_AIVEN_PASSWORD"
$env:JWT_SECRET = "BASE64_ENCODED_SECRET_OF_AT_LEAST_32_BYTES"
$env:CORS_ALLOWED_ORIGINS = "http://localhost:5173"
$env:AUTH_COOKIE_SECURE = "false"
.\mvnw.cmd spring-boot:run
```

`AUTH_COOKIE_SECURE=false` is only for local HTTP development. In deployment, leave it unset so the cookie is `Secure`. Store the Aiven password and `JWT_SECRET` only in the backend provider's secret settings. The database password is not an app-user password and must never be sent to the browser.

`JWT_SECRET` must be a Base64-encoded random value containing at least 32 bytes. Generate one locally without printing or sharing it:

```powershell
$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$bytes = New-Object byte[] 32
$rng.GetBytes($bytes)
$env:JWT_SECRET = [Convert]::ToBase64String($bytes)
$rng.Dispose()
```

The application uses Hibernate `update` for schema changes and seeds missing stock symbols with static educational prices. Existing stock rows are not overwritten at startup.

## Authentication and API

- `POST /api/auth/register` and `POST /api/auth/login` are public. Successful responses contain the username and set a signed, one-hour JWT in an `HttpOnly` cookie; the token is not returned in JSON.
- `POST /api/auth/logout` clears the cookie. `GET /api/auth/me` returns the signed-in user and requires the cookie.
- Stocks, portfolio, transactions, and trades require authentication. `GET /api/health` remains public for health checks.
- The cookie is `HttpOnly`, `SameSite=Lax`, scoped to `/api`, and `Secure` by default. Do not copy it into JavaScript storage or an Authorization header.
- Unsafe requests require an exact allowed `Origin` to defend cookie-authenticated requests against CSRF. Configure exact comma-separated browser origins in `CORS_ALLOWED_ORIGINS`; wildcard origins are rejected.

Registration request:

```json
{"username":"learner01","email":"learner@example.test","password":"a-strong-password"}
```

Login request:

```json
{"identifier":"learner01","password":"a-strong-password"}
```

Successful auth responses contain only `{"username":"learner01"}` plus the `Set-Cookie` response header. A trade request uses `{"symbol":"RELIANCE","quantity":2}`. The server loads the user and simulated price from trusted sources; client-supplied balances and prices are not accepted.

## Data model

- `AppUser` stores a unique username, email, BCrypt password hash, and virtual cash balance.
- `Stock` stores a unique symbol, company name, and illustrative INR price.
- `Holding` links one user to one stock and stores quantity and average purchase price.
- `TradeTransaction` records a user's BUY or SELL entry, quantity, execution price, and timestamp.

The trade service updates the account balance, holding, and transaction together and locks the user's row to serialize their trades.

## Verification

Run the backend tests:

```powershell
.\mvnw.cmd test
```

Tests use a disposable in-memory H2 database, not Aiven. They cover cookie issuance, protected endpoints, stock access control, BUY/SELL, insufficient-funds rejection, Origin enforcement, logout, and per-user transaction isolation. A passing H2 test does not verify Aiven connectivity.

## Deployment

The Docker image uses Java 21 and listens on Render's `PORT` (default `8080`). Configure `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, and `CORS_ALLOWED_ORIGINS` in Render's secret environment settings; leave `AUTH_COOKIE_SECURE` unset in production.

Configure Vercel with `frontend/` as project root, `npm run build`, and `dist` as output. Its `/api` rewrite proxies requests to Render so browser cookies remain same-origin. Do not set a production `VITE_API_BASE_URL` to the Render host. Restrict `CORS_ALLOWED_ORIGINS` to the exact deployed frontend origin, adding the local Vite origin only if needed.
