# Candle Craft backend

The backend uses the existing Spring Boot package `com.example.backend`, Spring Data JPA, and MySQL.

## Local MySQL configuration

Copy `.env.example` only as a reference; Spring Boot does not load a generic `.env` file automatically. Set these variables in the terminal or in your IDE launch configuration. Use the Aiven JDBC URL, including the database name and SSL options. Do not commit actual connection credentials.

In PowerShell, set the values in your current terminal session:

```powershell
$env:DB_URL = "jdbc:mysql://HOST:PORT/defaultdb?sslMode=REQUIRED"
$env:DB_USERNAME = "YOUR_AIVEN_USERNAME"
$env:DB_PASSWORD = "YOUR_AIVEN_PASSWORD"
$env:JWT_SECRET = "BASE64_ENCODED_SECRET_OF_AT_LEAST_32_BYTES"
$env:CORS_ALLOWED_ORIGINS = "http://localhost:5173"
```

Then run the backend from this directory:

```powershell
.\mvnw.cmd spring-boot:run
```

The application uses Hibernate `update` for incremental development schema changes. It does not drop or recreate existing tables. Confirm the Aiven hostname, port, database name, SSL mode, credentials, and database permissions before starting the application.

`JWT_SECRET` must be a Base64-encoded random value containing at least 32 bytes. Generate one locally without printing or sharing it, for example with PowerShell:

```powershell
$rng = [System.Security.Cryptography.RandomNumberGenerator]::Create()
$bytes = New-Object byte[] 32
$rng.GetBytes($bytes)
$env:JWT_SECRET = [Convert]::ToBase64String($bytes)
$rng.Dispose()
```

Automated Spring context tests use a disposable in-memory H2 database in MySQL compatibility mode. Passing `.\mvnw.cmd test` validates JPA startup and stock seeding, but does not verify connectivity to Aiven. Verify Aiven separately by starting the application with the three MySQL environment variables set.

## Current data model

- `AppUser` stores a unique username, email, BCrypt-ready password hash, and virtual cash balance.
- `Stock` stores a unique symbol, company name, and illustrative INR price.
- `Holding` links one user to one stock and stores quantity and average purchase price; a unique constraint prevents duplicate user/stock rows.
- `TradeTransaction` records a user's BUY or SELL entry, quantity, execution price, and timestamp.

The initial stock list is inserted only when a symbol is missing. Seed prices are static educational values, not live market quotes, and existing stock rows are not overwritten at startup.

## API foundation

- `POST /api/auth/register` and `POST /api/auth/login` issue signed, one-hour bearer JWTs.
- `GET /api/stocks` and `GET /api/stocks/{symbol}` expose static educational prices.
- `/api/portfolio`, `/api/transactions`, and `/api/trades/buy` and `/api/trades/sell` require a bearer JWT.
- `GET /api/health` is public for service health checks.

The JWT secret and database credentials belong only in local environment variables or the hosting provider's secret settings. Do not place them in Vite variables or source control.

### Request examples

Registration request (`POST /api/auth/register`):

```json
{"username":"learner01","email":"learner@example.test","password":"a-strong-password"}
```

Login request (`POST /api/auth/login`):

```json
{"identifier":"learner01","password":"a-strong-password"}
```

Both return `accessToken`, `tokenType`, `expiresInSeconds`, and `username`. Send the token as `Authorization: Bearer <accessToken>` to protected endpoints. A trade request uses `{"symbol":"RELIANCE","quantity":2}`. The server loads the user and simulated price from trusted sources; client-supplied balances and prices are not accepted.

## Backend design in brief

Controllers validate HTTP input and return DTOs; services own the business rules; Spring Data repositories read and write the entities. `@Entity` classes map to MySQL tables. The `@Transactional` trade service updates the account balance, holding, and transaction together; it locks that user's row to serialize their trades. BCrypt hashes passwords, and a stateless Spring Security resource-server filter verifies the HMAC-signed JWT on protected requests. Browser origins must be configured as exact comma-separated origins, such as the local Vite origin and deployed frontend origin.

## Verification

Run the current backend test suite with:

```powershell
.\mvnw.cmd test
```

Tests use disposable H2, not Aiven. The integration test exercises registration, token-protected account reads, paper BUY/SELL, insufficient-funds rejection, and per-user transaction isolation. To test Aiven connectivity, start the application separately with valid local `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` environment variables set; a passing H2 test is not evidence of a remote database connection.

## Docker and Render preparation

The `Dockerfile` uses Java 21 for both build and runtime. It packages the Maven project (including its H2-backed tests), then copies the known executable JAR into a small non-root runtime image. The app listens on Render's `PORT`, defaulting to `8080` locally. `.dockerignore` excludes build output, Git metadata, and local environment files.

From the `backend` directory, build and run locally:

```powershell
docker build -t candle-craft-backend .
docker run --rm -p 8080:8080 `
  -e PORT=8080 `
  -e DB_URL `
  -e DB_USERNAME `
  -e DB_PASSWORD `
  -e JWT_SECRET `
  -e CORS_ALLOWED_ORIGINS `
  candle-craft-backend
```

The `-e NAME` form passes a value already set in the current PowerShell process without putting the value in the command text. Configure real values through local environment variables or Render's secret settings only. In Render, select the backend directory as the Docker build context/root directory and configure the five runtime environment variables there. A local image build does not establish Aiven or hosted service connectivity.

For Vercel, select `frontend/` as the project root, use `npm run build`, output `dist`, and set `VITE_API_BASE_URL` to the deployed Render service URL. Add that exact frontend origin to Render's `CORS_ALLOWED_ORIGINS`; redeploy the frontend after setting the variable. This repository is not yet verified on either hosting provider.
