# 🎯 Job & Recruitment Management System (ATS)

A production-style, enterprise-grade **Applicant Tracking System (ATS)** built with **Java 21**, **Spring Boot 3**, **Spring Security**, **JWT**, **Hibernate / Spring Data JPA**, **MySQL**, and a modern **React 19** frontend powered by **Vite** and **Bootstrap 5**.

The system facilitates end-to-end recruitment workflows supporting three distinct role-based personas: **CANDIDATE**, **RECRUITER**, and **ADMIN**.

---

## 🚀 Key Highlights & Architectural Strengths

- **Layered Clean Architecture**: Strict separation of concerns (Controller ➔ Service ➔ Repository ➔ Entity / DTO Projections).
- **⚡ Real-Time Fresher Job Aggregator**: Automated ingestion from official company ATS feeds (Greenhouse, Lever, Ashby) with heuristic Fresher Classification, Java/Full Stack relevance matching, and direct redirection to official company application pages.
- **Stateless Authentication & RBAC**: JWT (HMAC-SHA384) with Spring Security 6 filter chain, role-based URL guards, and method security.
- **Relational Integrity & Normalization**: MySQL 8 database schema designed with foreign keys, composite indexes (`idx_job_title`, `idx_job_location`, `idx_job_status`), and duplicate application prevention at both database and service layers.
- **Server-Side Pagination & Dynamic Filtering**: Full server-side search across keyword, location, employment type, experience, and salary range.
- **Multi-Status Recruitment Pipeline**: Strict validation of candidate recruitment workflow transitions (`APPLIED` ➔ `UNDER_REVIEW` ➔ `SHORTLISTED` ➔ `INTERVIEW` ➔ `SELECTED` / `REJECTED`).
- **Secure File Storage**: Resume upload and secure download with MIME validation and filename sanitization.
- **Robust Automated Testing**: Comprehensive unit tests (Fresher classifier, ATS normalizers, duplicate detection, failure isolation) + full Spring Boot integration tests.
- **Interactive API Documentation**: OpenAPI 3.0 & Swagger UI integrated.

---

## 🛠 Technology Stack

### Backend
- **Language**: Java 21 LTS
- **Framework**: Spring Boot 3.2.5
- **Security**: Spring Security 6, JJWT 0.12.5 (BCrypt password hashing)
- **Persistence**: Spring Data JPA, Hibernate ORM
- **Database**: MySQL 8.0 (Production) / H2 in-memory (Automated Tests)
- **Validation**: Jakarta Bean Validation (`@Valid`, `@NotBlank`, `@Size`, `@Email`)
- **API Documentation**: SpringDoc OpenAPI 2.4.0 (Swagger UI)
- **Build Tool**: Apache Maven (via Maven Wrapper `mvnw`)
- **Testing**: JUnit 5, Mockito, Spring Boot Test, MockMvc

### Frontend
- **Framework**: React 19
- **Bundler & Dev Server**: Vite 8
- **Routing**: React Router DOM 7
- **Styling**: Bootstrap 5.3 & React Bootstrap
- **Icons**: React Icons (Heroicons/FontAwesome)
- **HTTP Client**: Axios with JWT Interceptors and automatic 401 redirection

### DevOps & Infrastructure
- **Containerization**: Docker, Docker Compose
- **Version Control**: Git & GitHub

---

## 👥 Role-Based Capabilities

### 1. 🎓 Candidate
- **Account & Security**: Self-registration, secure login, JWT authentication.
- **Profile & Resume**: Edit bio, headline, phone, location, years of experience, LinkedIn/GitHub links.
- **Skills Management**: Add and delete tagged competencies (Java, React, SQL, etc.).
- **Resume Upload & Download**: Upload PDF/DOCX resumes (up to 5MB) and re-download anytime.
- **Job Search & Filter**: Search by keyword (title/description/company), location, employment type, experience, and salary.
- **One-Click Application**: Apply with tailored cover letter.
- **Duplicate Prevention**: System guarantees candidates cannot submit multiple applications to the same job.
- **Application Tracking & Withdrawal**: Track application status in real-time, withdraw applications prior to interview stage.
- **Personal Dashboard**: Live metrics of total applications, review progress, interviews, and offers.

### 2. 💼 Recruiter
- **Recruiter Registration**: Onboard with company profile, company website, and description.
- **Job Lifecycle**: Create, edit, and close job postings with required skill tags and salary parameters.
- **Ownership Enforcement**: Recruiters can only modify or view applications for jobs belonging to their profile.
- **Applicant Review**: View all candidates per job, read cover letters, inspect profile, and download candidate resumes.
- **Status Workflow**: Move candidates through sequential recruitment stages (`APPLIED` ➔ `UNDER_REVIEW` ➔ `SHORTLISTED` ➔ `INTERVIEW` ➔ `SELECTED` or `REJECTED`).
- **Recruiter Dashboard**: Live statistics for active jobs, total applicants, shortlisted talent, and interview counts.

### 3. 🛡 System Admin
- **User Governance**: View all registered candidates and recruiters with pagination and role filters.
- **Account Moderation**: Activate or deactivate rogue users instantly.
- **Job Moderation**: Delete inappropriate or spam job postings system-wide.
- **System Telemetry**: System dashboard showing active user count, job count, total applications, and placement metrics.

### 4. ⚡ Real-Time Fresher & Entry-Level Job Aggregator
- **Automated Public ATS Feeds**: Direct ingestion from official company career boards (Greenhouse, Lever, Ashby). No fake listings, no middlemen.
- **FresherJobClassifier Engine**: Multi-factor heuristic classifier analyzing titles, descriptions, and experience requirements:
  - Positive signals: `Fresher`, `Graduate`, `0-1 yrs`, `0-2 yrs`, `Entry-Level`, `Associate`, `Trainee`, `Campus`.
  - Negative signals: `Senior`, `Lead`, `Principal`, `Architect`, `5+ yrs`, `7+ yrs`. Hard exclusion regardless of title.
- **Java & Full Stack Relevance**: Automated extraction and tagging for `Java`, `Spring Boot`, `React`, `SQL`, `Microservices`, `REST API`, `Docker`, `Git`.
- **Deduplication & Expiry Tracking**: Deduplication by `sourceProvider + externalJobId` and `company + title + location`. Unlisted jobs automatically marked `isActive = false`.
- **Direct Official Redirection**: Every job card provides a direct link to the company's official application page (`applicationUrl`).
- **REST API Endpoints**:
  - `GET /api/jobs/fresher` (Search with keyword, location, role, company, remote, pagination)
  - `GET /api/jobs/search` (Unified search across all aggregated listings)
  - `GET /api/jobs/statistics` (Verified counts of active fresher jobs, unique hiring companies, locations)
  - `GET /api/jobs/companies` (Unique companies currently hiring freshers)
  - `GET /api/jobs/sources` (Configured company sources and synchronization telemetry)
  - `POST /api/jobs/sync` (Admin-authorized manual synchronization trigger)

---

## 🗄 Database Design & Schema

```text
+------------------+         +----------------------+         +-----------------------+
|      users       | 1 --- 1 |  candidate_profiles  | * --- * |        skills         |
|------------------|         |----------------------|         |-----------------------|
| id (PK)          |         | id (PK)              |         | id (PK)               |
| email (UQ)       |         | user_id (FK, UQ)     |         | name (UQ)             |
| password (hash)  |         | headline, summary    |         +-----------------------+
| first_name       |         | phone, location      |                     *
| last_name        |         | experience_years     |                     |
| role (ENUM)      |         | resume_file_name     |                     |
| is_active (bool) |         | resume_file_path     |                     *
+------------------+         +----------------------+         +-----------------------+
        | 1                                                   |      job_skills       |
        |                                                     +-----------------------+
        | 1                  +----------------------+                     *
        +------------------ 1|  recruiter_profiles  |                     |
        |                    |----------------------|                     |
        |                    | id (PK)              |                     |
        |                    | user_id (FK, UQ)     |                     |
        |                    | company_name         |                     |
        |                    +----------------------+                     |
        |                                                                 |
        | 1                                                               |
        v *                                                               v *
+-------------------------------------------------------------------------------------+
|                                        jobs                                         |
|-------------------------------------------------------------------------------------|
| id (PK), recruiter_id (FK), company_name, title, description, location              |
| employment_type (FULL_TIME, PART_TIME, CONTRACT, HYBRID, REMOTE)                    |
| experience_min, experience_max, salary_min, salary_max, status (OPEN, CLOSED)      |
| created_at, updated_at, deadline                                                   |
+-------------------------------------------------------------------------------------+
        ^ 1
        |
        | *
+-------------------------------------------------------------------------------------+
|                                    applications                                     |
|-------------------------------------------------------------------------------------|
| id (PK), job_id (FK), candidate_id (FK), status (ENUM), cover_letter, notes         |
| applied_at, updated_at                                                              |
| CONSTRAINT uq_candidate_job UNIQUE (job_id, candidate_id)                           |
+-------------------------------------------------------------------------------------+
```

---

## 📡 RESTful API Reference

### 1. Authentication (`/api/auth`)
| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/api/auth/register/candidate` | Public | Register new Candidate |
| `POST` | `/api/auth/register/recruiter` | Public | Register new Recruiter |
| `POST` | `/api/auth/login` | Public | Authenticate user & issue JWT |

### 2. Jobs (`/api/jobs`)
| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/api/jobs` | Public | Search/filter jobs with pagination |
| `GET` | `/api/jobs/{id}` | Public | Retrieve job details by ID |
| `POST` | `/api/jobs` | RECRUITER | Create a job posting |
| `PUT` | `/api/jobs/{id}` | RECRUITER (Owner) | Update an existing job |
| `PATCH` | `/api/jobs/{id}/close` | RECRUITER (Owner) | Close a job |
| `DELETE` | `/api/jobs/{id}` | RECRUITER / ADMIN | Delete a job |

### 3. Applications (`/api/applications` & `/api/jobs/{id}/applications`)
| Method | Endpoint | Access | Description |
|---|---|---|---|
| `POST` | `/api/jobs/{id}/applications` | CANDIDATE | Apply for a job |
| `GET` | `/api/applications/my` | CANDIDATE | Get authenticated candidate applications |
| `DELETE` | `/api/applications/{id}/withdraw` | CANDIDATE | Withdraw pending application |

### 4. Recruiter Management (`/api/recruiter`)
| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/api/recruiter/jobs` | RECRUITER | Get jobs posted by recruiter |
| `GET` | `/api/recruiter/applications` | RECRUITER | Get applications for recruiter's jobs |
| `GET` | `/api/recruiter/jobs/{id}/applications` | RECRUITER | Get applications for specific job |
| `PATCH` | `/api/recruiter/applications/{id}/status` | RECRUITER | Update candidate application status |
| `GET` | `/api/recruiter/candidates/{userId}` | RECRUITER | View applicant profile |
| `GET` | `/api/recruiter/candidates/{userId}/resume` | RECRUITER | Download applicant resume |
| `GET` | `/api/recruiter/dashboard` | RECRUITER | Recruiter dashboard metrics |

### 5. Candidate Profile (`/api/candidate`)
| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/api/candidate/profile` | CANDIDATE | Get current candidate profile |
| `PUT` | `/api/candidate/profile` | CANDIDATE | Update candidate profile |
| `POST` | `/api/candidate/skills` | CANDIDATE | Add skills |
| `DELETE` | `/api/candidate/skills/{name}` | CANDIDATE | Remove a skill |
| `POST` | `/api/candidate/resume` | CANDIDATE | Upload resume file |
| `GET` | `/api/candidate/resume` | CANDIDATE | Download current resume |
| `GET` | `/api/candidate/dashboard` | CANDIDATE | Candidate dashboard metrics |

### 6. Administration (`/api/admin`)
| Method | Endpoint | Access | Description |
|---|---|---|---|
| `GET` | `/api/admin/users` | ADMIN | Get all users (filterable by role) |
| `PATCH` | `/api/admin/users/{id}/toggle-active` | ADMIN | Activate or deactivate user |
| `DELETE` | `/api/admin/jobs/{id}` | ADMIN | Delete inappropriate job |
| `GET` | `/api/admin/statistics` | ADMIN | System-wide statistics |

---

## 🧪 Testing Suite & Results

The project features a **39-test automated test suite** with 100% pass rate:
- **Unit Tests (20 tests)**:
  - `AuthServiceTest` (5 tests): candidate registration, recruiter registration, duplicate email handling, login validation, BadCredentials handling.
  - `JobServiceTest` (6 tests): job creation, ownership validation, closed job updates, job closing, non-existent job handling, unauthorized deletion.
  - `ApplicationServiceTest` (9 tests): job application, duplicate prevention, closed job check, valid and invalid status transitions, application withdrawal rules.
- **Spring Boot Controller & Integration Tests (19 tests)**:
  - `AuthControllerIntegrationTest` (6 tests): registration, duplicate conflict responses, validation error payloads, login tokens, invalid password rejections.
  - `JobControllerIntegrationTest` (8 tests): public search, keyword & location filtering, 404 for non-existent jobs, 401 unauthorized requests, 403 forbidden for candidates creating jobs, recruiter creation success, validation errors.
  - `ApplicationControllerIntegrationTest` (5 tests): candidate submission, duplicate application rejection (`409 Conflict`), recruiter forbidden from applying (`403 Forbidden`), candidate dashboard stats, admin protection.

### Running Automated Tests
```bash
cd backend
./mvnw test
```

---

## 💻 Local Setup & Execution

### Prerequisites
- **JDK 21** or newer
- **Node.js** v20+ and **npm**
- **MySQL 8.0** running locally or in Docker

### 1. Database Setup
```sql
CREATE DATABASE IF NOT EXISTS job_recruitment_db;
```

### 2. Backend Setup
```bash
cd backend
# Set environment variables or rely on application.properties defaults:
# DB_URL=jdbc:mysql://localhost:3306/job_recruitment_db
# DB_USERNAME=root
# DB_PASSWORD=your_password

./mvnw spring-boot:run
```
The backend starts at `http://localhost:8080`.
Swagger UI is accessible at: `http://localhost:8080/swagger-ui.html`.

### 3. Frontend Setup
```bash
cd frontend
npm install
npm run dev
```
The frontend starts at `http://localhost:5173`.

---

## 🔑 Demo Credentials

| Role | Email | Password | Pre-seeded Features |
|---|---|---|---|
| **ADMIN** | `admin@jobrecruitment.com` | `Admin@123` | System stats, manage users, remove jobs |
| **RECRUITER** | `recruiter@techcorp.com` | `Recruiter@123` | TechCorp Global profile, 4 active jobs, applicants |
| **CANDIDATE** | `candidate@dev.com` | `Candidate@123` | Java/React skills, resume, 1 active application |

---

## 🐳 Docker Deployment

To launch the complete multi-tier system with one command:
```bash
docker-compose up --build
```
This launches:
- **MySQL 8.0** on `localhost:3306`
- **Spring Boot API** on `localhost:8080`
- **React UI (Nginx)** on `localhost:5173`
