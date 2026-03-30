# Appraisal System — Application Flowchart

## System Overview

A **Performance Appraisal System** with role-based access for **Employees**, **Managers**, and **HR/Admin**. Built with Java Spring Boot (backend) + a web frontend.

---

## 1. Authentication & Authorization Flow

```mermaid
flowchart TD
    A([User Visits App]) --> B[Login Page]
    B --> C{Enter Credentials}
    C --> D[Spring Security Authentication]
    D --> E{Valid?}
    E -- No --> F[Show Error] --> B
    E -- Yes --> G{Check Role}
    G -- EMPLOYEE --> H[Employee Dashboard]
    G -- MANAGER --> I[Manager Dashboard]
    G -- HR/ADMIN --> J[Admin Dashboard]
    H --> K([Session Active — JWT Token Issued])
    I --> K
    J --> K
```

---

## 2. High-Level Application Flow

```mermaid
flowchart TD
    START([User Logged In]) --> ROLE{User Role?}

    ROLE -- Employee --> E1[View Own Goals]
    E1 --> E2[Submit Self-Appraisal]
    E2 --> E3[View Feedback & Rating]
    E3 --> E4[Acknowledge Final Rating]

    ROLE -- Manager --> M1[View Team Members]
    M1 --> M2[Set/Approve Goals for Reports]
    M2 --> M3[Review Self-Appraisals]
    M3 --> M4[Provide Manager Rating & Feedback]
    M4 --> M5[Submit to HR for Calibration]

    ROLE -- HR/Admin --> A1[Initiate Appraisal Cycle]
    A1 --> A2[Monitor Progress Dashboard]
    A2 --> A3[Calibration & Normalization]
    A3 --> A4[Finalize & Publish Ratings]
    A4 --> A5[Generate Reports]
```

---

## 3. Appraisal Cycle Lifecycle

```mermaid
flowchart LR
    subgraph "Phase 1: Setup"
        P1A[HR Creates Cycle] --> P1B[Define Rating Scale]
        P1B --> P1C[Set Deadlines]
    end

    subgraph "Phase 2: Goal Setting"
        P2A[Employee Sets Goals] --> P2B[Manager Reviews Goals]
        P2B --> P2C{Approved?}
        P2C -- No --> P2A
        P2C -- Yes --> P2D[Goals Finalized]
    end

    subgraph "Phase 3: Self-Appraisal"
        P3A[Employee Rates Own Goals]
        P3A --> P3B[Adds Achievements & Evidence]
        P3B --> P3C[Submits Self-Appraisal]
    end

    subgraph "Phase 4: Manager Review"
        P4A[Manager Reviews Submission]
        P4A --> P4B[Provides Rating & Comments]
        P4B --> P4C[Submits to HR]
    end

    subgraph "Phase 5: Finalization"
        P5A[HR Calibrates Ratings]
        P5A --> P5B[Final Rating Published]
        P5B --> P5C[Employee Acknowledges]
    end

    P1C --> P2A
    P2D --> P3A
    P3C --> P4A
    P4C --> P5A
```

---

## 4. Goal Management Flow

```mermaid
flowchart TD
    G1([Goal Setting Phase Opens]) --> G2[Employee Creates Goal]
    G2 --> G3[Set Title, Description, KPIs, Weightage]
    G3 --> G4[Submit for Manager Approval]
    G4 --> G5{Manager Decision}
    G5 -- Approved --> G6[Goal Status = ACTIVE]
    G5 -- Rejected --> G7[Manager Adds Feedback]
    G7 --> G2
    G5 -- Needs Edit --> G8[Sent Back to Employee]
    G8 --> G2
    G6 --> G9([Goal Tracking Begins])
```

---

## 5. Self-Appraisal Submission Flow

```mermaid
flowchart TD
    S1([Self-Appraisal Phase Opens]) --> S2[Employee Views Assigned Goals]
    S2 --> S3[Rate Each Goal — 1 to 5]
    S3 --> S4[Add Comments & Supporting Evidence]
    S4 --> S5[Review Summary]
    S5 --> S6{Submit?}
    S6 -- No --> S3
    S6 -- Yes --> S7[Status = SUBMITTED]
    S7 --> S8[Notification Sent to Manager]
    S8 --> S9([Awaiting Manager Review])
```

---

## 6. Manager Review Flow

```mermaid
flowchart TD
    MR1([Manager Opens Review Queue]) --> MR2[Select Employee]
    MR2 --> MR3[View Self-Appraisal & Goals]
    MR3 --> MR4[Provide Manager Rating per Goal]
    MR4 --> MR5[Add Overall Comments]
    MR5 --> MR6[Calculate Weighted Score]
    MR6 --> MR7{Approve & Submit?}
    MR7 -- No --> MR4
    MR7 -- Yes --> MR8[Status = MANAGER_REVIEWED]
    MR8 --> MR9[Forwarded to HR for Calibration]
    MR9 --> MR10([Awaiting HR Finalization])
```

---

## 7. HR Calibration & Finalization Flow

```mermaid
flowchart TD
    HR1([HR Opens Calibration View]) --> HR2[View All Ratings by Department]
    HR2 --> HR3[Bell Curve / Normalization Analysis]
    HR3 --> HR4{Adjustments Needed?}
    HR4 -- Yes --> HR5[Discuss with Manager]
    HR5 --> HR6[Adjust Ratings]
    HR6 --> HR3
    HR4 -- No --> HR7[Finalize Ratings]
    HR7 --> HR8[Publish to Employees]
    HR8 --> HR9[Employees Acknowledge]
    HR9 --> HR10([Cycle Closed])
```

---

## 8. Notification System Flow

```mermaid
flowchart TD
    N1{Event Triggered?} -- Goal Submitted --> N2[Notify Manager]
    N1 -- Goal Approved/Rejected --> N3[Notify Employee]
    N1 -- Self-Appraisal Submitted --> N4[Notify Manager]
    N1 -- Manager Review Done --> N5[Notify HR]
    N1 -- Rating Published --> N6[Notify Employee]
    N1 -- Deadline Approaching --> N7[Notify All Pending Users]
    N2 & N3 & N4 & N5 & N6 & N7 --> N8[Send via Email + In-App]
```

---

## 9. Key Entities (Data Model Overview)

```mermaid
erDiagram
    USER ||--o{ GOAL : creates
    USER ||--o{ APPRAISAL : submits
    USER {
        Long id
        String name
        String email
        String role
        Long managerId
        Long departmentId
    }
    APPRAISAL_CYCLE ||--o{ APPRAISAL : contains
    APPRAISAL_CYCLE {
        Long id
        String name
        Date startDate
        Date endDate
        String status
    }
    APPRAISAL ||--o{ GOAL_RATING : includes
    APPRAISAL {
        Long id
        Long employeeId
        Long cycleId
        String status
        Double finalRating
    }
    GOAL ||--o{ GOAL_RATING : rated_in
    GOAL {
        Long id
        String title
        String description
        Double weightage
        String status
    }
    GOAL_RATING {
        Long id
        Long goalId
        Long appraisalId
        Integer selfRating
        Integer managerRating
        String comments
    }
    DEPARTMENT ||--o{ USER : belongs_to
    DEPARTMENT {
        Long id
        String name
    }
```

---

## 10. API Layer Overview

| Module | Endpoint Prefix | Key Operations |
|---|---|---|
| **Auth** | `/api/auth` | Login, Register, Refresh Token |
| **Users** | `/api/users` | CRUD, Profile, Role Mgmt |
| **Cycles** | `/api/cycles` | Create, Start, Close Cycle |
| **Goals** | `/api/goals` | Create, Approve, Reject |
| **Appraisals** | `/api/appraisals` | Submit, Review, Finalize |
| **Ratings** | `/api/ratings` | Self-Rate, Manager-Rate, Calibrate |
| **Reports** | `/api/reports` | Department, Individual, Bell Curve |
| **Notifications** | `/api/notifications` | List, Mark Read |

---

## Tech Stack Recommendation

| Layer | Technology |
|---|---|
| Backend | Java 17+ / Spring Boot 3.x |
| Security | Spring Security + JWT |
| Database | PostgreSQL / MySQL |
| ORM | Spring Data JPA / Hibernate |
| API Docs | SpringDoc OpenAPI (Swagger) |
| Frontend | React / Angular / Thymeleaf |
| Notifications | Spring Mail + WebSocket |
| Build | Maven / Gradle |

