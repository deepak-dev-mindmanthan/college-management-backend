# Complete API Flow: College Registration → Payment → Dashboard

## Overview
This document describes the complete API flow from college registration through payment processing to accessing the college admin dashboard.

---

## Flow Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                    COLLEGE REGISTRATION                          │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│  Step 1: POST /api/v1/auth/register-college                    │
│  - Creates College entity                                      │
│  - Creates College Admin user                                  │
│  - Returns UserDto with college info                           │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│  Step 2: POST /api/v1/auth/login                               │
│  - Authenticates College Admin                                 │
│  - Returns JWT tokens (access + refresh)                       │
│  - Returns subscription status (NONE initially)                │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│  Step 3: GET /api/v1/subscription-plans                        │
│  - Fetches available subscription plans                        │
│  - Shows plan types (STARTER, STANDARD, PREMIUM)               │
│  - Shows billing cycles (MONTHLY, QUARTERLY, YEARLY)          │
│  - Shows pricing information                                   │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│  Step 4: POST /api/v1/subscriptions                            │
│  - Creates subscription with selected plan                     │
│  - Status: PENDING (waiting for payment)                        │
│  - Calculates expiry date based on billing cycle               │
│  - Returns SubscriptionResponse                                │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│  Step 5: POST /api/v1/invoices/generate/{subscriptionUuid}      │
│  - Generates invoice for subscription                          │
│  - Invoice number: INV-YYYYMMDD-XXXXX                          │
│  - Amount: Based on subscription plan price                    │
│  - Status: UNPAID                                               │
│  - Due date: 7 days from generation                            │
│  - Returns InvoiceResponse                                      │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│  Step 6: POST /api/v1/payments/initiate                        │
│  - Creates payment record                                       │
│  - Processes payment through gateway (default: SUCCESS)         │
│  - Updates payment status                                       │
│  - Returns PaymentResponse                                      │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│  Step 7: Automatic Payment Processing                          │
│  - PaymentGatewayService.processPayment()                      │
│  - Default: Returns SUCCESS (for now)                          │
│  - TODO: Integrate actual gateway (Razorpay/Stripe)            │
│  - Updates invoice status to PAID if fully paid                │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│  Step 8: PUT /api/v1/subscriptions/{uuid}/activate             │
│  - Activates subscription                                       │
│  - Status: PENDING → ACTIVE                                     │
│  - Subscription becomes usable                                 │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│  Step 9: Dashboard Access                                      │
│  - All protected endpoints now accessible                      │
│  - Subscription check in login response                         │
│  - canAccessCoreApis: true                                     │
└─────────────────────────────────────────────────────────────────┘
```

---

## Detailed API Flow

### **Phase 1: College Registration**

#### **1.1 Register College**
```http
POST /api/v1/auth/register-college
Content-Type: application/json
Authorization: Not required (public endpoint)

Request Body:
{
  "collegeName": "ABC College",
  "collegeEmail": "admin@abccollege.edu",
  "collegePhone": "+1234567890",
  "collegeShortCode": "ABC",
  "country": "USA",
  "adminName": "John Doe",
  "adminEmail": "john@abccollege.edu",
  "password": "SecurePass123!"
}

Response:
{
  "success": true,
  "status": 200,
  "message": "College registered successfully.",
  "data": {
    "uuid": "user-uuid-here",
    "email": "john@abccollege.edu",
    "name": "John Doe",
    "collegeId": 1,
    "roles": ["ROLE_COLLEGE_ADMIN"]
  }
}
```

**What Happens:**
- ✅ Creates `College` entity in database
- ✅ Creates `User` entity with `ROLE_COLLEGE_ADMIN` role
- ✅ Links user to college (tenant isolation)
- ✅ Returns user information

**Database State:**
- `colleges` table: 1 new record
- `users` table: 1 new record (college admin)
- `subscriptions` table: No subscription yet

---

### **Phase 2: Authentication**

#### **2.1 Login**
```http
POST /api/v1/auth/login
Content-Type: application/json
Authorization: Not required (public endpoint)

Request Body:
{
  "email": "john@abccollege.edu",
  "password": "SecurePass123!"
}

Response:
{
  "success": true,
  "status": 200,
  "message": "Login successfully.",
  "data": {
    "user": {
      "uuid": "user-uuid-here",
      "email": "john@abccollege.edu",
      "roles": ["ROLE_COLLEGE_ADMIN"],
      "collegeId": 1
    },
    "subscription": {
      "plan": "NONE",
      "expiresAt": null,
      "canAccessCoreApis": false
    },
    "auth": {
      "tokenType": "Bearer",
      "accessToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
      "refreshToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
      "accessTokenExpiresIn": 3600,
      "refreshTokenExpiresIn": 86400
    }
  }
}
```

**What Happens:**
- ✅ Validates credentials
- ✅ Generates JWT tokens (access + refresh)
- ✅ Checks subscription status (NONE initially)
- ✅ Returns subscription summary with `canAccessCoreApis: false`

**Note:** Without active subscription, core APIs may be restricted.

---

### **Phase 3: Subscription Selection**

#### **3.1 Get Available Plans**
```http
GET /api/v1/subscription-plans
Authorization: Bearer {accessToken}

Response:
{
  "success": true,
  "status": 200,
  "message": "Active plans retrieved successfully",
  "data": [
    {
      "id": 1,
      "planType": "STARTER",
      "billingCycle": "MONTHLY",
      "price": 99.00,
      "currency": "USD",
      "active": true
    },
    {
      "id": 2,
      "planType": "STARTER",
      "billingCycle": "YEARLY",
      "price": 999.00,
      "currency": "USD",
      "active": true
    },
    {
      "id": 3,
      "planType": "STANDARD",
      "billingCycle": "MONTHLY",
      "price": 199.00,
      "currency": "USD",
      "active": true
    },
    {
      "id": 4,
      "planType": "PREMIUM",
      "billingCycle": "MONTHLY",
      "price": 299.00,
      "currency": "USD",
      "active": true
    }
  ]
}
```

**What Happens:**
- ✅ Returns all active subscription plans
- ✅ Shows pricing for different plan types and billing cycles
- ✅ User can select a plan

---

#### **3.2 Create Subscription**
```http
POST /api/v1/subscriptions
Authorization: Bearer {accessToken}
Content-Type: application/json

Request Body:
{
  "planType": "STANDARD",
  "billingCycle": "MONTHLY"
}

Response:
{
  "success": true,
  "status": 200,
  "message": "Subscription created successfully",
  "data": {
    "uuid": "subscription-uuid-here",
    "planType": "STANDARD",
    "billingCycle": "MONTHLY",
    "price": 199.00,
    "currency": "USD",
    "status": "PENDING",
    "startsAt": "2024-01-15",
    "expiresAt": "2024-02-15",
    "collegeId": 1,
    "collegeName": "ABC College",
    "invoiceCount": 0,
    "isActive": false,
    "isExpired": false,
    "daysRemaining": 0
  }
}
```

**What Happens:**
- ✅ Creates `Subscription` entity
- ✅ Links to selected plan
- ✅ Sets status to `PENDING` (waiting for payment)
- ✅ Calculates `startsAt` (today) and `expiresAt` (based on billing cycle)
- ✅ Links to college (tenant isolation)

**Database State:**
- `subscriptions` table: 1 new record (status: PENDING)

---

### **Phase 4: Invoice Generation**

#### **4.1 Generate Invoice**
```http
POST /api/v1/invoices/generate/{subscriptionUuid}
Authorization: Bearer {accessToken}

Response:
{
  "success": true,
  "status": 200,
  "message": "Invoice generated successfully",
  "data": {
    "uuid": "invoice-uuid-here",
    "invoiceNumber": "INV-20240115-A1B2C3D4",
    "subscriptionUuid": "subscription-uuid-here",
    "planName": "STANDARD",
    "billingCycle": "MONTHLY",
    "amount": 199.00,
    "currency": "USD",
    "status": "UNPAID",
    "dueDate": "2024-01-22",
    "periodStart": "2024-01-15",
    "periodEnd": "2024-02-15",
    "paymentCount": 0,
    "totalPaidAmount": 0.00
  }
}
```

**What Happens:**
- ✅ Generates unique invoice number (INV-YYYYMMDD-XXXXX)
- ✅ Sets invoice amount = subscription plan price
- ✅ Sets status to `UNPAID`
- ✅ Sets due date (7 days from generation)
- ✅ Links invoice to subscription

**Database State:**
- `invoices` table: 1 new record (status: UNPAID)

---

### **Phase 5: Payment Processing**

#### **5.1 Initiate Payment**
```http
POST /api/v1/payments/initiate
Authorization: Bearer {accessToken}
Content-Type: application/json

Request Body:
{
  "invoiceUuid": "invoice-uuid-here",
  "gateway": "RAZORPAY",
  "transactionId": "txn_1234567890",
  "amount": 199.00
}

Response:
{
  "success": true,
  "status": 200,
  "message": "Payment initiated and processed successfully",
  "data": {
    "uuid": "payment-uuid-here",
    "invoiceUuid": "invoice-uuid-here",
    "invoiceNumber": "INV-20240115-A1B2C3D4",
    "gateway": "RAZORPAY",
    "transactionId": "txn_1234567890",
    "amount": 199.00,
    "status": "SUCCESS",
    "paymentDate": "2024-01-15T10:30:00Z"
  }
}
```

**What Happens:**
1. ✅ Creates `Payment` record (status: PENDING initially)
2. ✅ Calls `PaymentGatewayService.processPayment()`
   - **Current:** Returns `SUCCESS` by default
   - **Future:** Will call actual gateway API (Razorpay/Stripe)
3. ✅ Updates payment status to `SUCCESS`
4. ✅ Checks if invoice is fully paid
5. ✅ Updates invoice status to `PAID` if fully paid
6. ✅ Sets `paidAt` timestamp on invoice

**Database State:**
- `payments` table: 1 new record (status: SUCCESS)
- `invoices` table: Status updated to PAID

---

### **Phase 6: Subscription Activation**

#### **6.1 Activate Subscription**
```http
PUT /api/v1/subscriptions/{subscriptionUuid}/activate
Authorization: Bearer {accessToken}

Response:
{
  "success": true,
  "status": 200,
  "message": "Subscription activated successfully",
  "data": {
    "uuid": "subscription-uuid-here",
    "planType": "STANDARD",
    "billingCycle": "MONTHLY",
    "price": 199.00,
    "currency": "USD",
    "status": "ACTIVE",
    "startsAt": "2024-01-15",
    "expiresAt": "2024-02-15",
    "collegeId": 1,
    "collegeName": "ABC College",
    "invoiceCount": 1,
    "isActive": true,
    "isExpired": false,
    "daysRemaining": 31
  }
}
```

**What Happens:**
- ✅ Updates subscription status: `PENDING` → `ACTIVE`
- ✅ Subscription becomes usable
- ✅ College can now access all core APIs

**Database State:**
- `subscriptions` table: Status updated to ACTIVE

---

### **Phase 7: Dashboard Access**

#### **7.1 Login (After Subscription Activation)**
```http
POST /api/v1/auth/login
Content-Type: application/json

Request Body:
{
  "email": "john@abccollege.edu",
  "password": "SecurePass123!"
}

Response:
{
  "success": true,
  "status": 200,
  "message": "Login successfully.",
  "data": {
    "user": {
      "uuid": "user-uuid-here",
      "email": "john@abccollege.edu",
      "roles": ["ROLE_COLLEGE_ADMIN"],
      "collegeId": 1
    },
    "subscription": {
      "plan": "STANDARD",
      "expiresAt": "2024-02-15",
      "canAccessCoreApis": true  ← Now true!
    },
    "auth": {
      "tokenType": "Bearer",
      "accessToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
      "refreshToken": "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9...",
      "accessTokenExpiresIn": 3600,
      "refreshTokenExpiresIn": 86400
    }
  }
}
```

**What Happens:**
- ✅ Subscription check returns active subscription
- ✅ `canAccessCoreApis: true` - Full access granted
- ✅ All protected endpoints now accessible

---

#### **7.2 Access Dashboard Endpoints**

Now the college admin can access all protected endpoints:

```http
# Get all teachers
GET /api/v1/teachers
Authorization: Bearer {accessToken}

# Get all students
GET /api/v1/students
Authorization: Bearer {accessToken}

# Get subscription details
GET /api/v1/subscriptions/current
Authorization: Bearer {accessToken}

# Get payment history
GET /api/v1/payments
Authorization: Bearer {accessToken}

# Get invoice history
GET /api/v1/invoices
Authorization: Bearer {accessToken}
```

---

## Complete Flow Summary

| Step | Endpoint | Purpose | Status Change |
|------|----------|---------|---------------|
| 1 | `POST /api/v1/auth/register-college` | Register college & admin | College created, Admin created |
| 2 | `POST /api/v1/auth/login` | Authenticate | JWT tokens issued |
| 3 | `GET /api/v1/subscription-plans` | View plans | - |
| 4 | `POST /api/v1/subscriptions` | Create subscription | Subscription: PENDING |
| 5 | `POST /api/v1/invoices/generate/{uuid}` | Generate invoice | Invoice: UNPAID |
| 6 | `POST /api/v1/payments/initiate` | Process payment | Payment: SUCCESS, Invoice: PAID |
| 7 | `PUT /api/v1/subscriptions/{uuid}/activate` | Activate subscription | Subscription: ACTIVE |
| 8 | `POST /api/v1/auth/login` | Re-login | `canAccessCoreApis: true` |
| 9 | Various GET endpoints | Access dashboard | Full access granted |

---

## Important Notes

### **Payment Gateway Integration**
- Currently, all payments default to `SUCCESS` status
- `DefaultPaymentGatewayService` is a placeholder
- TODO: Replace with actual gateway integration (Razorpay/Stripe)
- See `PaymentGatewayService` interface for integration points

### **Subscription Status Flow**
```
NONE → PENDING → ACTIVE → EXPIRED/CANCELLED
  ↑       ↑        ↑
  │       │        └─ After payment + activation
  │       └─ After subscription creation
  └─ Initial state (no subscription)
```

### **Invoice Status Flow**
```
UNPAID → PAID → (FAILED if payment fails)
  ↑       ↑
  │       └─ After successful payment
  └─ After invoice generation
```

### **Payment Status Flow**
```
PENDING → SUCCESS (or FAILED)
  ↑         ↑
  │         └─ After gateway processing
  └─ After payment creation
```

---

## Security & Isolation

- ✅ **College Isolation:** All queries filter by `collegeId` (tenant isolation)
- ✅ **Role-Based Access:** `COLLEGE_ADMIN` and `SUPER_ADMIN` roles enforced
- ✅ **JWT Authentication:** All protected endpoints require valid JWT token
- ✅ **Tenant Context:** Automatically set from authenticated user's college

---

## Error Handling

Common error scenarios:
- **409 Conflict:** College/email already exists during registration
- **404 Not Found:** Subscription/invoice/payment not found
- **403 Forbidden:** Access denied (wrong college or insufficient role)
- **400 Bad Request:** Invalid request data or validation errors

---

## Future Enhancements

1. **Automatic Subscription Activation:** After payment success, automatically activate subscription
2. **Trial Period:** Add trial subscription option
3. **Payment Webhooks:** Handle gateway webhooks for status updates
4. **Email Notifications:** Send emails for invoice generation, payment success/failure
5. **Subscription Renewal:** Automatic renewal before expiry

