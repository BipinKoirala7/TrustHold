# TrustHold

A rental security deposit escrow web app. A tenant pays the deposit into escrow, both sides document the property with photos, and the deposit is only released once the landlord and tenant agree on the split and an admin closes the lease.

> Final Year Project (CS6P05NP), Informatics College Pokhara, London Metropolitan University.

## How it works

TrustHold is not a marketplace. There are no public listings: a landlord creates a private lease record for one known tenant, and the property details live inside that lease.

1. **Create lease**: the landlord enters the property, tenant email, deposit and dates.
2. **Pay deposit**: the tenant pays through eSewa and the money is held in escrow.
3. **Move-in photos**: both sides upload labelled photos (walls, flooring, kitchen and so on) and confirm move-in.
4. **Move-out photos**: the same labels are captured again and shown side by side in a gallery. The system never judges condition; the parties compare the photos themselves.
5. **Settlement**: either party proposes how to split the deposit, and the other agrees or counter-proposes.
6. **Admin close**: once both agree, an admin gives the final confirmation that releases the deposit.

## Roles

| Role | Can do |
|------|--------|
| Landlord | Create leases, upload photos, propose and agree to settlements |
| Tenant | Pay the deposit, upload photos, propose and agree to settlements |
| Admin | Close agreed settlements (no mediation or dispute resolution). Seeded, not self-registered |

## Lease lifecycle

`PENDING_MOVE_IN` → `ACTIVE` → `MOVE_OUT_IN_PROGRESS` → `MOVED_OUT` → `AGREED` → `SETTLED`

A settled lease is locked and read-only. Every step is recorded in a per-lease **Activity Timeline**.

## Tech stack

- **Backend:** Java 21, Spring Boot 3, Spring Security (JWT), Spring Data JPA, PostgreSQL, Flyway
- **Auth:** email/password and Google OAuth
- **Payments:** eSewa (Stripe optional)
- **Storage:** AWS S3 for photos
- **Frontend:** React, TypeScript, Vite, Tailwind CSS, shadcn/ui

## Getting started

**Prerequisites:** Java 21, Maven, PostgreSQL, Node 20+

```bash
# Backend
cd backend
mvn spring-boot:run

# Frontend
cd frontend
npm install
npm run dev
```

Configure the backend with environment variables (names are examples):

```
DB_URL, DB_USERNAME, DB_PASSWORD
JWT_SECRET
GOOGLE_CLIENT_ID
ESEWA_PRODUCT_CODE, ESEWA_SECRET_KEY
AWS_REGION, S3_BUCKET
```

## Project status

In development using the Incremental Model over 26 weeks: auth, leases, payments, move-in/out photos, settlement and escrow logic, the activity timeline, then testing and deployment.

## Author

Bipin Koirala ([@BipinKoirala7](https://github.com/BipinKoirala7))