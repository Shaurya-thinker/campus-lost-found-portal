# Campus Lost & Found Portal

A full-stack, secure campus management portal for reporting, tracking, and claiming lost and found property across university grounds.

---

## 1. Architecture Overview

### Backend
- **Framework**: Spring Boot 3.4.2 (Java 21 target)
- **Security**: Spring Security 6 + stateless JWT (HMAC-SHA256, 30-minute expiration) + BCrypt (cost factor 10)
- **Persistence**: Spring Data JPA / Hibernate + Spring JDBC Template (for analytics)
- **Database**: MySQL 8.0+ (utf8mb4, InnoDB)
- **Port**: `8080` (base path `/api`)

### Frontend
- **Framework**: React 19 + TypeScript
- **Tooling**: Vite 6+
- **Architecture**: Single Page Application (SPA) with hash-based routing (`#home`, `#report`, `#item-details`, `#my-claims`, `#login`, `#register`, `#admin`)
- **Styling**: Editorial Custom CSS Design System (no external CSS framework dependencies)
- **Session**: Tab-scoped `sessionStorage` for JWT bearer tokens

---

## 2. Prerequisites

- **Java**: JDK 21+ (`java -version`)
- **Node.js**: Node 18+ and npm
- **Database**: MySQL Server 8.0+ running on port 3306

---

## 3. Configuration & Environment Variables

All sensitive values are parameterized via environment variables and should be configured in your deployment environment or system variables.

| Variable | Description | Default (Development) |
|---|---|---|
| `DB_USERNAME` | MySQL database username | `root` |
| `DB_PASSWORD` | MySQL database password | *(None, required)* |
| `JWT_SECRET` | 256-bit (32+ chars) signing key | *Internal dev fallback* |
| `JWT_EXPIRATION_MS` | JWT token lifetime in milliseconds | `1800000` (30 minutes) |
| `ADMIN_PROVISION_ENABLED` | Bootstraps initial admin on startup | `false` |
| `ADMIN_PROVISION_UID` | Initial admin identifier | `ADMIN001` |
| `ADMIN_PROVISION_NAME` | Initial admin display name | `Campus Property Administrator` |
| `ADMIN_PROVISION_EMAIL` | Initial admin campus email | `admin@campus.edu` |
| `ADMIN_PROVISION_PASSWORD` | Initial admin password (min 12 chars) | *(Required if enabled)* |

> [!IMPORTANT]
> **Production JWT Secret**: Set `JWT_SECRET` in your server environment before running in production. The application emits a startup security alert if the development fallback key is used.
> **Admin Provisioning**: Set `ADMIN_PROVISION_ENABLED=true` alongside credentials only during initial setup. Once bootstrapped, set `ADMIN_PROVISION_ENABLED=false` to prevent runner activity.

---

## 4. Getting Started

### Database Setup
1. Create the MySQL database:
   ```sql
   CREATE DATABASE campus_lost_found CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```
2. Apply the initial schema:
   ```bash
   mysql -u root -p campus_lost_found < database/schema.sql
   ```
3. Database migrations (adding `college_uid`, `password_hash`, `role`, and `enabled` to `users`) run **automatically** and idempotently on startup via `DatabaseMigrationRunner`.

### Running the Backend
From the project root:
```powershell
# Set database password
$env:DB_PASSWORD="your_mysql_password"

# Optional: enable admin bootstrapping on first run
$env:ADMIN_PROVISION_ENABLED="true"
$env:ADMIN_PROVISION_PASSWORD="YourSecureAdminPass2026!"

# Run Spring Boot
.\mvnw.cmd spring-boot:run
```
The REST API will be available at `http://localhost:8080`.

### Running the Frontend
From the `frontend/` directory:
```powershell
cd frontend
npm install
npm run dev
```
Open `http://localhost:5173` in your browser. The Vite development proxy forwards all `/api` requests to `http://localhost:8080`.

---

## 5. Security Architecture

1. **Role-Based Access Control**:
   - `STUDENT`: Public self-registration (`POST /api/auth/register`). Permitted to report items, view catalog, and submit/view own claims. Blocked from administrative review, analytics, marking items returned, and claim approval/rejection.
   - `ADMIN`: Controlled provisioning only. Full dashboard access, analytics reporting, claim approvals/rejections, and marking items returned.
2. **Server-Side Identity Binding**:
   - `reporterId` and `claimantId` in write operations are strictly derived from the validated JWT `UserPrincipal` on the server. Client-supplied IDs in request bodies are ignored.
3. **Stale Token Mitigation**:
   - The `JwtAuthenticationFilter` validates token cryptographic integrity and re-queries the database for live account status and roles. Demoted roles or disabled accounts take effect immediately without waiting for token expiry.
4. **Legacy Account Protection**:
   - Public self-registration matching an uncredentialed historical account is rejected. Historical accounts must be linked via administrator verification (`POST /api/users/{id}/link-legacy`).
5. **Session Storage Decision**:
   - Tokens are stored in tab-scoped `sessionStorage`. Closing the browser tab destroys the token, preventing persistent credential exposure on shared campus lab terminals.

---

## 6. Testing & Quality Assurance

### Run Backend Tests (53 tests)
```powershell
.\mvnw.cmd test
```

### Build Backend JAR
```powershell
.\mvnw.cmd package -DskipTests
```
Artifact generated at `target/campus-lost-found-0.0.1-SNAPSHOT.jar`.

### Build Frontend Production Assets
```powershell
cd frontend
node ./node_modules/typescript/bin/tsc -b
node ./node_modules/vite/bin/vite.js build
```
Production assets compiled to `frontend/dist/`.

