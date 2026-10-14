# PickCar
 
> **Pick it, drive it.**
 
Car rental platform that connects drivers with rental companies, built with Spring Boot (REST API, HATEOAS, JPA) and React. Additional details: [HELP.md](HELP.md)

[![Open Issues](https://img.shields.io/github/issues-raw/UdL-EPS-SoftArch/spring-template?logo=github)](https://github.com/orgs/UdL-EPS-SoftArch/projects/12)
[![CI/CD](https://github.com/UdL-EPS-SoftArch/spring-template/actions/workflows/ci-cd.yml/badge.svg)](https://github.com/UdL-EPS-SoftArch/spring-template/actions)
[![Cucumber Reports](https://img.shields.io/badge/cucumber-reports-brightgreen)](https://UdL-EPS-SoftArch.github.io/spring-template/reports/cucumber-report.html)
[![Deployment status](https://img.shields.io/uptimerobot/status/m792691238-18db2a43adf8d8ded474f885)](https://spring-template.fly.dev/records)

## Vision
 
**For** drivers who need a car for a few days and rental companies that want to reach more customers
**who** want to rent and offer vehicles with verified users, transparent prices and a documented handover
**the project** is PickCar, a car rental platform with three roles (Driver, Company, Admin)
**that** covers the whole rental end to end: manual identity verification, availability without overlaps, payment with deposit, cancellation with refund rules, check-in and check-out with photos, and incident handling
**Unlike** a simple vehicle listing site, every booking is traceable and protected by business rules, and development is driven by BDD (Cucumber) tests

## Features per Stakeholder
 
| DRIVER                                   | COMPANY                                          | ADMIN                                      |
|------------------------------------------|--------------------------------------------------|--------------------------------------------|
| Register and verify email                | Register company                                 | Login                                      |
| Login, recover password                  | Login, recover password                          | Approve / reject drivers                   |
| Upload licence (front and back)          | Upload CIF and insurance documents               | Approve / reject companies                 |
| Search vehicles                          | Manage vehicles (add, edit, pause, retire)       | Approve / reject vehicles                  |
| Book and pay (price breakdown, deposit)  | Manage pickup points and extras                  | Resolve incidents and decide on deposit    |
| Cancel booking (refund by policy)        | Cancel a booking (full refund to the driver)     | View audit log                             |
| Check-in and check-out                   | Report incidents                                 |                                            |
| Report incidents                         | Receive notifications                            |                                            |
| Receive notifications and set preferences|                                                  |                                            |
 
Company users have a company role (`OWNER`, `FLEET_MANAGER`, `AGENT`, `ACCOUNTANT`) that decides what they can do inside their company.

## Booking Lifecycle
 
```mermaid
stateDiagram-v2
    [*] --> PENDING_PAYMENT
    PENDING_PAYMENT --> CONFIRMED: payment authorized
    PENDING_PAYMENT --> EXPIRED: payment failed / hold expired
    PENDING_PAYMENT --> CANCELLED: cancelled
    CONFIRMED --> CANCELLED: cancelled (refund by policy)
    CONFIRMED --> ACTIVE: check-in
    ACTIVE --> COMPLETED: check-out
```

## Entities Model

```mermaid
classDiagram
    class UriEntity {
        uri : String
    }
 
    class UserDetails
    <<interface>> UserDetails
 
    class User {
        email : String
        passwordHash : String
        phone : String
        fullName : String
        role : Role
        status : UserStatus
        emailVerifiedAt : ZonedDateTime
        notificationPreferences : JSON
        companyRole : CompanyRole
    }
 
    class EmailToken {
        type : TokenType
        tokenHash : String
        expiresAt : ZonedDateTime
        usedAt : ZonedDateTime
    }
 
    class DriverProfile {
        birthDate : LocalDate
        licenseNumber : String
        licenseExpiry : LocalDate
        verificationStatus : VerificationStatus
        rejectionReason : String
        reviewedAt : ZonedDateTime
    }
 
    class Document {
        type : DocumentType
        side : DocumentSide
        fileUrl : String
        mimeType : String
        sizeBytes : Long
        status : DocumentStatus
    }
 
    class Company {
        legalName : String
        taxId : String
        address : String
        iban : String
        status : CompanyStatus
        rejectionReason : String
    }
 
    class PickupPoint {
        name : String
        address : String
        latitude : Double
        longitude : Double
        openingHours : JSON
        active : Boolean
    }
 
    class Vehicle {
        plate : String
        brand : String
        model : String
        year : Integer
        category : String
        transmission : Transmission
        fuelType : String
        seats : Integer
        dailyPriceCents : Integer
        depositCents : Integer
        cancellationPolicy : CancellationPolicy
        status : VehicleStatus
        photos : JSON
        extras : JSON
    }
 
    class Booking {
        startDateTime : ZonedDateTime
        endDateTime : ZonedDateTime
        status : BookingStatus
        holdExpiresAt : ZonedDateTime
        version : Long
        dailyPriceCents : Integer
        days : Integer
        subtotalCents : Integer
        extras : JSON
        extrasCents : Integer
        taxCents : Integer
        totalCents : Integer
        depositCents : Integer
        cancellationPolicy : CancellationPolicy
        cancelledAt : ZonedDateTime
        refundAmountCents : Integer
    }
 
    class Payment {
        type : PaymentType
        amountCents : Integer
        status : PaymentStatus
        failureReason : String
        providerRef : String
        idempotencyKey : String
    }
 
    class Inspection {
        type : InspectionType
        mileage : Integer
        fuelLevel : FuelLevel
        notes : String
        latitude : Double
        longitude : Double
        lockedAt : ZonedDateTime
    }
 
    class InspectionPhoto {
        angle : PhotoAngle
        url : String
    }
 
    class Incident {
        type : IncidentType
        description : String
        photos : JSON
        estimatedCostCents : Integer
        status : IncidentStatus
        resolution : String
        depositDeductionCents : Integer
        resolvedAt : ZonedDateTime
    }
 
    class Notification {
        type : NotificationType
        message : String
        read : Boolean
        channel : Channel
        sentAt : ZonedDateTime
    }
 
    class AuditLog {
        action : String
        entityType : String
        entityId : String
        beforeJson : String
        afterJson : String
        ip : String
    }
 
    UriEntity <|-- User
    UserDetails <|-- User
    UriEntity <|-- Company
    UriEntity <|-- PickupPoint
    UriEntity <|-- Vehicle
    UriEntity <|-- Booking
 
    User "1" <-- "0..1" DriverProfile: profile
    User "1" <-- "*" EmailToken: tokens
    User "1" <-- "*" Document: owner
    User "1" <-- "*" Notification: recipient
    User "1" <-- "*" AuditLog: actor
    Company "0..1" <-- "*" User: staff
    Company "1" <-- "*" PickupPoint: owns
    Company "1" <-- "*" Vehicle: owns
    PickupPoint "1" <-- "*" Vehicle: base
    User "1" <-- "*" Booking: driver
    Vehicle "1" <-- "*" Booking: booked
    PickupPoint "1" <-- "*" Booking: pickup
    PickupPoint "1" <-- "*" Booking: return
    Booking "1" <-- "*" Payment: payments
    Booking "1" <-- "0..2" Inspection: inspections
    Inspection "1" <-- "*" InspectionPhoto: photos
    Booking "1" <-- "*" Incident: incidents
```

## Features
 
The behaviour of the platform is defined in Cucumber feature files:
 
| Feature file                  | Covers                                                |
|-------------------------------|-------------------------------------------------------|
| `driver_registration.feature` | Registration, email verification, document upload     |
| `authentication.feature`      | Login, logout, password recovery, access control      |
| `vehicle_search.feature`      | Search, filters, availability rules                   |
| `booking.feature`             | Price breakdown, payment, temporary hold, overlaps    |
| `cancellation.feature`        | Refund by policy and timing                           |
| `inspection.feature`          | Check-in and check-out                                |
| `incident.feature`            | Incidents, deposit retention and resolution           |
| `company_fleet.feature`       | Company registration, vehicles, pickup points         |
| `admin_verification.feature`  | Manual review of drivers, companies, vehicles; audit  |
| `notifications.feature`       | Notifications and preferences                         |

## AI-Assisted Spec-Driven Development (SDD)

This repository serves as a starter template for **AI-Assisted Spec-Driven Development (SDD)** with **Spring Data REST** and **Cucumber BDD**.

It is pre-configured with agent skills (`skills/`), repository instructions (`AGENTS.md`, `CONVENTIONS.md`), and Aider configuration (`.aider.conf.yml`) to support both:
1. **Full-Capability AI Agents** (OpenCode, Claude Code, Cursor, GitHub Copilot, Windsurf).
2. **Token-Constrained APIs** (Aider paired with Groq free tier - 8K token limits).

For complete stage-by-stage instructions and command workflows, see **[AI_DEVELOPMENT_GUIDE.md](AI_DEVELOPMENT_GUIDE.md)**.

### SDD Agent Skills (`skills/`)
- **`skills/sdd-feature-designer/SKILL.md`**: Draft Cucumber `.feature` specs for Spring Data REST endpoints (<50 lines).
- **`skills/sdd-cucumber-steps/SKILL.md`**: Implement MockMvc step definition classes extending `StepDefs.java`.
- **`skills/spring-datarest-entity/SKILL.md`**: Create JPA `@Entity` classes extending `UriEntity` and `@RepositoryRestResource` interfaces with SpEL row-level security queries.
- **`skills/spring-datarest-hander/SKILL.md`**: Create lifecycle event handlers based on the `@RepositoryEventHandler`.
- **`skills/sdd-bdd-verifier/SKILL.md`**: Run `mvn test -Dtest=CucumberTest` and analyze test failure summaries.
