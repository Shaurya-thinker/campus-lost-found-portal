<div align="center">

# 🏛️ Campus Lost & Found Portal
### University Property Desk Management & Ownership Claim System

[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.4.2-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Spring Security](https://img.shields.io/badge/Spring_Security-6.x-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white)](https://spring.io/projects/spring-security)
[![JWT](https://img.shields.io/badge/JWT-HMAC--SHA256-000000?style=for-the-badge&logo=jsonwebtokens&logoColor=white)](https://jwt.io/)
[![React](https://img.shields.io/badge/React-19.x-61DAFB?style=for-the-badge&logo=react&logoColor=black)](https://react.dev/)
[![TypeScript](https://img.shields.io/badge/TypeScript-5.7-3178C6?style=for-the-badge&logo=typescript&logoColor=white)](https://www.typescriptlang.org/)
[![Vite](https://img.shields.io/badge/Vite-6.x-646CFF?style=for-the-badge&logo=vite&logoColor=white)](https://vitejs.dev/)
[![MySQL](https://img.shields.io/badge/MySQL-8.0+-4479A1?style=for-the-badge&logo=mysql&logoColor=white)](https://www.mysql.com/)
[![Tests](https://img.shields.io/badge/Tests-83%20Passed-brightgreen?style=for-the-badge&logo=junit5&logoColor=white)](#-automated-testing--verification)

<p align="center">
  A production-ready, full-stack campus property tracking application built with <strong>Spring Boot 3</strong>, <strong>Spring Security 6</strong>, and <strong>React 19</strong>. Designed for high accountability, role-based workflows, automated database migrations, real-time unclaimed property analytics, and secure private visual evidence verification.
</p>

[Key Features](#-core-capabilities) • [Architecture](#-system-architecture) • [Security & Auth](#-security--authentication-architecture) • [Private Evidence](#-private-visual-evidence--claim-verification) • [API Guide](#-rest-api-documentation) • [Quickstart](#-quickstart-guide) • [Tests](#-automated-testing--verification)

---

</div>

## 🌟 Core Capabilities

<table>
  <tr>
    <td width="50%">
      <h3>🛡️ Robust Security & Identity</h3>
      <ul>
        <li><strong>Stateless JWT Bearer Authentication</strong> with HMAC-SHA256 signing and 30-minute auto-expiry.</li>
        <li><strong>Role-Based Access Control (RBAC)</strong> enforcing <code>ROLE_STUDENT</code> and <code>ROLE_ADMIN</code> boundaries.</li>
        <li><strong>Server-Side Identity Binding</strong>: Claimants and reporters are strictly resolved from authenticated principals.</li>
        <li><strong>Live Account Validity</strong>: Demoted roles or disabled accounts take effect immediately, neutralizing stale tokens.</li>
      </ul>
    </td>
    <td width="50%">
      <h3>🔍 Lost & Found Catalog</h3>
      <ul>
        <li><strong>Smart Filtering</strong>: Real-time discovery across <code>ALL</code>, <code>LOST</code>, and <code>FOUND</code> items.</li>
        <li><strong>Multi-Field Search</strong>: Query items by title keyword, category, or university location.</li>
        <li><strong>Zero Data Leakage</strong>: Private distinguishing details and photos are strictly excluded from public catalog feeds.</li>
        <li><strong>Editorial Custom CSS</strong>: Zero third-party component libraries; custom accessible design tokens.</li>
      </ul>
    </td>
  </tr>
  <tr>
    <td width="50%">
      <h3>🤝 Claim Verification Workflow</h3>
      <ul>
        <li><strong>Dual-Party Private Evidence</strong>: Independent identification collected from finder and claimant.</li>
        <li><strong>Self-Claim Prevention</strong>: Prevents users from claiming items they personally reported.</li>
        <li><strong>Duplicate Claim Lock</strong>: Eliminates repeat claim submissions per user on the same report.</li>
        <li><strong>Status Lifecycle</strong>: Strict transitions from <code>PENDING</code> &rarr; <code>MORE_INFO_REQUESTED</code> &rarr; <code>APPROVED</code> / <code>REJECTED</code> &rarr; <code>RETURNED</code>.</li>
      </ul>
    </td>
    <td width="50%">
      <h3>📊 Admin Dashboard & Visual Analytics</h3>
      <ul>
        <li><strong>Dual-Party Comparison View</strong>: Side-by-side assessment of finder's private record vs claimant's evidence.</li>
        <li><strong>High-Performance Aggregations</strong> powered by optimized Spring JDBC Template queries.</li>
        <li><strong>Unclaimed Property Distribution</strong>: Visual metrics by building/location and item category.</li>
        <li><strong>Return Processing</strong>: Handover verification marking claimed items as officially returned.</li>
      </ul>
    </td>
  </tr>
</table>

---

## 🏗️ System Architecture

```mermaid
flowchart TD
    subgraph Client["Frontend Client (React 19 + TypeScript + Vite)"]
        UI["Custom CSS Design System"]
        Router["Hash Router (#home, #report, #claims, #admin)"]
        Storage["Tab-Scoped SessionStorage (JWT + User Profile)"]
        BlobView["PrivateEvidenceImage (In-Memory Blob Streaming)"]
    end

    subgraph Gateway["Vite Reverse Proxy"]
        Proxy["/api/* -> http://localhost:8080"]
    end

    subgraph Backend["Spring Boot 3.4.2 REST Backend"]
        FilterChain["Spring Security Filter Chain"]
        JwtFilter["JwtAuthenticationFilter (HMAC-SHA256)"]
        UserPrincipal["CustomUserDetailsService (Live DB Lookups)"]

        subgraph Controllers["REST Controllers"]
            AuthController["AuthController (/api/auth)"]
            ReportController["ItemReportController (/api/reports)"]
            ClaimController["ClaimController (/api/claims)"]
            EvidenceController["EvidenceController (/api/evidence)"]
            AdminController["AnalyticsController (/api/analytics)"]
            UserController["UserController (/api/users)"]
        end

        subgraph Services["Domain Services & Abstractions"]
            AuthSvc["AuthService & BCrypt"]
            ReportSvc["ItemReportService"]
            ClaimSvc["ClaimService"]
            EvidenceSvc["EvidenceService & EvidenceValidator"]
            StorageAdapter["EvidenceStorageService (Abstraction)"]
            AnalyticsSvc["AnalyticsService (Spring JDBC)"]
        end
    end

    subgraph StorageEngine["Storage Layer"]
        DB["MySQL 8.0+ Database (users, item_reports, claims, evidence_files)"]
        Disk["Configured Private Directory (EVIDENCE_STORAGE_PATH)"]
    end

    Client -->|HTTP / JSON / Multipart| Gateway
    Gateway --> FilterChain
    FilterChain --> JwtFilter
    JwtFilter --> UserPrincipal
    JwtFilter --> Controllers
    Controllers --> Services
    Services --> DB
    StorageAdapter --> Disk
```

---

## 🔒 Security & Authentication Architecture

> [!IMPORTANT]
> **Zero Unauthenticated Writes**: Creating reports, updating records, submitting claims, and accessing administration endpoints strictly require valid JWT bearer credentials.

```
Incoming Request
       │
       ▼
┌──────────────────────────────┐
│  JwtAuthenticationFilter     │ ──▶ Validates HMAC-SHA256 signature & expiration
└──────────────────────────────┘
       │
       ▼
┌──────────────────────────────┐
│  CustomUserDetailsService    │ ──▶ Queries database for live role & account status
└──────────────────────────────┘
       │
       ├── Account Disabled / Locked ──▶ 401 Unauthorized
       ├── Account Deleted           ──▶ 401 Unauthorized
       │
       ▼
┌──────────────────────────────┐
│  SecurityContextHolder       │ ──▶ Binds live UserPrincipal (Student vs Admin)
└──────────────────────────────┘
       │
       ├── Student accessing /api/analytics/**          ──▶ 403 Forbidden
       ├── Student accessing /api/claims/*/verification ──▶ 403 Forbidden
       ├── Claimant accessing reporter private evidence ──▶ 403 Forbidden
       └── Valid permission verified                    ──▶ 200 / 201 Success
```

---

## 📸 Private Visual Evidence & Claim Verification

Milestone 13 introduces private, multi-party visual and descriptive evidence for FOUND item claim verification:

### 1. Storage Abstraction Architecture
- **Interface**: [`EvidenceStorageService`](file:///E:/Projects/Campus%20Lost%20&%20Found%20Portal/src/main/java/com/campus/lostfound/service/EvidenceStorageService.java) decouples controllers and business logic from the physical storage backend.
- **Local Implementation**: [`LocalEvidenceStorageService`](file:///E:/Projects/Campus%20Lost%20&%20Found%20Portal/src/main/java/com/campus/lostfound/service/LocalEvidenceStorageService.java) stores files on the local filesystem outside the git repository, outside `frontend/public`, and outside Spring Boot static asset paths.
- **Configurable Location**:
  - Environment Variable: `EVIDENCE_STORAGE_PATH`
  - Spring Property: `app.evidence.storage-path=${EVIDENCE_STORAGE_PATH:E:/CampusLostFoundPrivateUploads}`
- **Future AWS S3 Cloud Integration**: The storage interface is designed for zero-code refactoring when switching from local filesystem to private Amazon S3 bucket storage.

### 2. Validation & Threat Defense
- **MIME Whitelist**: `image/jpeg`, `image/png`, `image/webp`.
- **Size Limitation**: Configurable limit of **5 MB** per file (`app.evidence.max-file-size-bytes=5242880`).
- **Binary Content Verification**: [`EvidenceValidator`](file:///E:/Projects/Campus%20Lost%20&%20Found%20Portal/src/main/java/com/campus/lostfound/service/EvidenceValidator.java) inspects binary magic bytes and decodes image dimensions with `ImageIO`, immediately rejecting executables (`MZ`, `ELF`), shell scripts (`#!`), and HTML/PHP polyglots disguised with image extensions.
- **Opaque Storage Keys**: Storage filenames are generated as server-side random UUIDs (`UUID.randomUUID().toString() + extension`). User-supplied filenames are never used as storage paths.
- **Path Traversal Protection**: All paths are resolved and normalized against the configured root to prevent directory traversal attacks (`..`).

### 3. Access Control & Privacy Matrix

| Role / User | Public Report Details | Reporter Private Description | Reporter Photo Evidence | Claimant Claim & Proof | Claimant Photo Evidence |
|---|:---:|:---:|:---:|:---:|:---:|
| **Anonymous / Public** | ✅ Yes | ❌ No | ❌ No | ❌ No | ❌ No |
| **Claimant Student** | ✅ Yes | ❌ No | ❌ No | ✅ Own Claim | ✅ Own Photo |
| **Reporter Student** | ✅ Yes | ✅ Own Report | ✅ Own Photo | ❌ No | ❌ No |
| **Administrator (`ROLE_ADMIN`)** | ✅ Yes | ✅ Yes | ✅ Yes | ✅ All Claims | ✅ All Photos |

### 4. Retention & Backups
- Files on local disk should be included in periodic system backups.
- Deleting an evidence record cleanly removes both the MySQL metadata record and the corresponding physical file from disk.

---

## 📡 REST API Documentation

| Method | Endpoint | Access Level | Description |
|---|---|---|---|
| `POST` | `/api/auth/register` | Public | Registers a student (`collegeUid`, `name`, `email`, `password` $\ge 12$). |
| `POST` | `/api/auth/login` | Public | Authenticates via UID/Email + password; returns JWT bearer token. |
| `GET` | `/api/auth/me` | Authenticated | Retrieves current authenticated session user profile. |
| `GET` | `/api/reports` | Public | Catalogs all reports with optional `type` and `status` query params. |
| `GET` | `/api/reports/{id}` | Public | Detailed report inspection by primary key. |
| `GET` | `/api/reports/search` | Public | Searches catalog by category and/or campus location keywords. |
| `POST` | `/api/reports` | Authenticated | Files a `LOST` or `FOUND` report (`privateDescription` required for FOUND). |
| `GET` | `/api/reports/{id}/private-details` | Reporter / Admin | Retrieves confidential distinguishing details and photo evidence ID. |
| `POST` | `/api/reports/{reportId}/evidence` | Reporter / Admin | Uploads private image evidence for a FOUND report (multipart `file`). |
| `PUT` | `/api/reports/{id}` | Owner / Admin | Updates item title, category, location, and description. |
| `PATCH` | `/api/reports/{id}/returned` | **Admin Only** | Marks a claimed report as officially `RETURNED`. |
| `POST` | `/api/claims` | Authenticated | Submits an ownership claim (`description` + `identifyingFeatures`). |
| `GET` | `/api/claims` | Authenticated | Retrieves own claims (students) or filtered claims (administrators). |
| `GET` | `/api/claims/{id}` | Owner / Admin | Retrieves claim details by ID with claimant's evidence ID. |
| `POST` | `/api/claims/{claimId}/evidence` | Claimant / Admin | Uploads private supporting ownership photo for a claim (multipart `file`). |
| `GET` | `/api/claims/{id}/verification` | **Admin Only** | Dual-party verification payload (finder private details vs claimant evidence). |
| `PATCH` | `/api/claims/{id}/request-info` | **Admin Only** | Transitions claim status to `MORE_INFO_REQUESTED`. |
| `PATCH` | `/api/claims/{id}/approve` | **Admin Only** | Approves claim and automatically transitions item to `CLAIMED`. |
| `PATCH` | `/api/claims/{id}/reject` | **Admin Only** | Rejects an ownership claim. |
| `GET` | `/api/evidence/{id}` | Owner / Admin | Streams private evidence image securely with anti-caching headers. |
| `DELETE` | `/api/evidence/{id}` | Owner / Admin | Deletes evidence record and removes binary file from disk. |
| `GET` | `/api/analytics/**` | **Admin Only** | Aggregations by location, category, and unclaimed item details. |
| `POST` | `/api/users/{id}/link-legacy` | **Admin Only** | Assigns credentials to an unlinked historical account. |

---

## 🚀 Quickstart Guide

### 1. Prerequisites
- **Java**: JDK 21+ (`java -version`)
- **Node.js**: v18+ (`node -v`)
- **Database**: MySQL Server 8.0+ on port `3306`

### 2. Database Initialization
```sql
CREATE DATABASE campus_lost_found CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```
Import base schema:
```bash
mysql -u root -p campus_lost_found < database/schema.sql
```
*(All schema extensions—including authentication and Milestone 13 evidence tables—are migrated automatically and idempotently on startup via `DatabaseMigrationRunner`).*

### 3. Start the Backend API
From the repository root:
```powershell
# Set your MySQL root password
$env:DB_PASSWORD="your_mysql_password"

# Optional: Configure private evidence storage path (default: E:/CampusLostFoundPrivateUploads)
$env:EVIDENCE_STORAGE_PATH="E:/CampusLostFoundPrivateUploads"

# Optional: Enable admin provisioning for initial setup
$env:ADMIN_PROVISION_ENABLED="true"

# Launch Spring Boot
.\mvnw.cmd spring-boot:run
```
Backend API will listen at **`http://localhost:8080`**.

> [!TIP]
> **Default Admin Credentials**:
> - **Identifier**: `ADMIN001` or `admin@campus.edu`
> - **Password**: `Admin@Campus2026!`

### 4. Start the Frontend Application
From the `frontend/` directory in a second terminal:
```powershell
cd frontend
npm install
npm run dev
```
Open **`http://localhost:5173`** in your browser. The Vite proxy forwards all `/api/*` calls directly to the Spring Boot service.

---

## 🧪 Automated Testing & Verification

The test suite covers unit, integration, and security layer constraints:

```powershell
.\mvnw.cmd clean test
```

### Test Suite Breakdown (83/83 Passing)

| Test Suite | Tests | Purpose |
|---|:---:|---|
| [`SecurityAuthorizationTest`](file:///E:/Projects/Campus%20Lost%20&%20Found%20Portal/src/test/java/com/campus/lostfound/controller/SecurityAuthorizationTest.java) | **18** | 401 unauthenticated writes, 403 student restrictions, claim verification security, request-info restrictions, and admin endpoints. |
| [`EvidenceServiceTest`](file:///E:/Projects/Campus%20Lost%20&%20Found%20Portal/src/test/java/com/campus/lostfound/service/EvidenceServiceTest.java) | **12** | Fine-grained evidence access control: uploader/admin allowed, cross-claimant blocked, uploader deletion rules. |
| [`AuthServiceTest`](file:///E:/Projects/Campus%20Lost%20&%20Found%20Portal/src/test/java/com/campus/lostfound/service/AuthServiceTest.java) | **12** | Registration, duplicate checks, dual UID/Email login, BCrypt hashing, and legacy account safety. |
| [`ClaimServiceTest`](file:///E:/Projects/Campus%20Lost%20&%20Found%20Portal/src/test/java/com/campus/lostfound/service/ClaimServiceTest.java) | **10** | Identifying features submission, duplicate claims, self-claims, `MORE_INFO_REQUESTED` workflow, and approvals. |
| [`EvidenceValidatorTest`](file:///E:/Projects/Campus%20Lost%20&%20Found%20Portal/src/test/java/com/campus/lostfound/service/EvidenceValidatorTest.java) | **9** | Binary magic-byte checks for JPEG/PNG/WebP, oversized file rejection, executable rejection, and empty file blocks. |
| [`JwtServiceTest`](file:///E:/Projects/Campus%20Lost%20&%20Found%20Portal/src/test/java/com/campus/lostfound/security/JwtServiceTest.java) | **7** | Signature checks, expiration validation, disabled accounts, and stale token role neutralization. |
| [`ItemReportServiceTest`](file:///E:/Projects/Campus%20Lost%20&%20Found%20Portal/src/test/java/com/campus/lostfound/service/ItemReportServiceTest.java) | **6** | FOUND reports requiring private description, LOST reports succeeding without it, status transitions. |
| [`AdminProvisioningRunnerTest`](file:///E:/Projects/Campus%20Lost%20&%20Found%20Portal/src/test/java/com/campus/lostfound/config/AdminProvisioningRunnerTest.java) | **5** | Startup conflict prevention, idempotent restarts, and student elevation blocks. |
| [`UserServiceTest`](file:///E:/Projects/Campus%20Lost%20&%20Found%20Portal/src/test/java/com/campus/lostfound/service/UserServiceTest.java) | **4** | Core user entity retrieval and email collision checks. |

---

## 📁 Project Directory Structure

```
Campus Lost & Found Portal/
├── database/
│   ├── schema.sql                         # Baseline MySQL 8 schema
│   ├── migration_v2_auth.sql              # Authentication migration
│   └── migration_v3_evidence.sql          # Milestone 13 private evidence schema
├── frontend/
│   ├── src/
│   │   ├── components/
│   │   │   ├── admin/AdminDashboard.tsx   # Visual analytics & dual-party evidence review panel
│   │   │   ├── auth/LoginPage.tsx         # Dual UID/Email authentication form
│   │   │   ├── auth/RegisterPage.tsx      # Student self-registration with confirmation
│   │   │   ├── claims/ItemDetailsView.tsx # Item inspection & ownership claim filing with proof
│   │   │   ├── claims/MyClaimsView.tsx    # Authenticated student claims & evidence preview
│   │   │   ├── evidence/                  # PrivateEvidenceImage (authenticated blob streaming)
│   │   │   ├── home/BrowseSection.tsx     # Catalog search and LOST/FOUND filters
│   │   │   ├── layout/SiteHeader.tsx      # Role-aware responsive navigation bar
│   │   │   ├── report/ReportForm.tsx      # Property loss/find submission with confidential evidence
│   │   │   └── ui/                        # Reusable primitives (Card, Badge, Button, Input)
│   │   ├── services/                      # REST API client services (reports, claims, auth, evidence)
│   │   ├── styles/                        # Custom CSS tokens and global design system
│   │   └── types/                         # TypeScript interfaces (auth, report, claim, evidence)
│   ├── package.json
│   └── vite.config.ts
├── src/
│   ├── main/java/com/campus/lostfound/
│   │   ├── config/                        # SecurityConfig, DatabaseMigrationRunner, AdminRunner
│   │   ├── controller/                    # Spring MVC REST Controllers (incl. EvidenceController)
│   │   ├── dto/                           # Request & Response DTO payloads
│   │   ├── entity/                        # JPA Entities (User, ItemReport, Claim, EvidenceFile, Role)
│   │   ├── exception/                     # GlobalExceptionHandler (RFC-7807 responses)
│   │   ├── repository/                    # Spring Data JPA Repositories
│   │   ├── security/                      # JWT Service, AuthenticationFilter, UserPrincipal
│   │   └── service/                       # Business logic, EvidenceStorageService, & EvidenceValidator
│   └── main/resources/
│       └── application.properties         # Configurable database, JWT, admin, and evidence properties
├── pom.xml                                # Maven dependencies & build configuration
└── README.md
```

---

<div align="center">

**Developed with paired programming on Google Antigravity**  
Campus Lost & Found Portal • University Property Desk System

</div>
