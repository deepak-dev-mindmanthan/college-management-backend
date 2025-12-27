# Complete Student Fees Management Flow Documentation

## Overview
This document describes the complete API flow for Student Fees Management in the college management system, including fee structure creation, student fee assignment, payment processing, and comprehensive fee reporting for different roles (COLLEGE_ADMIN, ACCOUNTANT, TEACHER, STUDENT).

---

## User Roles & Permissions

| Role | Create Fee Structure | Update Fee Structure | Assign Fees | Record Payments | View Own Fees | View All Fees | View Summaries | Delete Structure |
|------|---------------------|---------------------|-------------|----------------|--------------|---------------|----------------|------------------|
| **SUPER_ADMIN** | ✅ Yes | ✅ Yes | ✅ Yes | ✅ Yes | ✅ Yes | ✅ Yes | ✅ Yes | ✅ Yes |
| **COLLEGE_ADMIN** | ✅ Yes | ✅ Yes | ✅ Yes | ✅ Yes | ✅ Yes | ✅ Yes | ✅ Yes | ✅ Yes |
| **ACCOUNTANT** | ✅ Yes | ✅ Yes | ✅ Yes | ✅ Yes | ✅ Yes | ✅ Yes | ✅ Yes | ❌ No |
| **TEACHER** | ❌ No | ❌ No | ❌ No | ❌ No | ✅ Yes | ✅ Yes (Read-only) | ✅ Yes | ❌ No |
| **STUDENT** | ❌ No | ❌ No | ❌ No | ❌ No | ✅ Yes (Own only) | ❌ No | ✅ Yes (Own only) | ❌ No |

---

## Flow Diagram: Complete Fees Management Lifecycle

```
┌─────────────────────────────────────────────────────────────────┐
│    FEE STRUCTURE SETUP (COLLEGE_ADMIN / ACCOUNTANT)              │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│  Step 1: POST /api/v1/fees/structures                          │
│  - Create fee structure for a class                             │
│  - Define fee components (Tuition, Library, Sports, etc.)       │
│  - Calculate total amount                                        │
│  - One structure per class                                       │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│    ASSIGN FEES TO STUDENTS (COLLEGE_ADMIN / ACCOUNTANT)          │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│  Step 2: POST /api/v1/fees/assign                               │
│  - Assign fee structure to individual student                    │
│  OR                                                              │
│  Step 2b: POST /api/v1/fees/classes/{uuid}/assign/{structure}  │
│  - Bulk assign to all students in a class                       │
│  - Creates StudentFee record with status: PENDING                │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│      PAYMENT PROCESSING (COLLEGE_ADMIN / ACCOUNTANT)             │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│  Step 3: POST /api/v1/fees/payments                             │
│  - Record fee payment                                            │
│  - Update paid amount and due amount                             │
│  - Auto-update fee status (PENDING → PARTIALLY_PAID → PAID)     │
│  - Create payment history record                                 │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│         FEE TRACKING & REPORTING                                 │
│  - View fee status and history                                   │
│  - Generate fee summaries                                        │
│  - Track overdue fees                                            │
│  - Payment history                                               │
└─────────────────────────────────────────────────────────────────┘
```

---

## Detailed API Flows by User Role

### **Role 1: COLLEGE_ADMIN / ACCOUNTANT - Fee Structure Management**

#### **Flow 1.1: Create Fee Structure**

**Step 1: Create Fee Structure for a Class**
```http
POST /api/v1/fees/structures
Authorization: Bearer {adminAccessToken | accountantAccessToken}
Content-Type: application/json

Request Body:
{
  "classUuid": "class-uuid-123",
  "components": [
    {
      "name": "Tuition Fee",
      "amount": 50000.00
    },
    {
      "name": "Library Fee",
      "amount": 2000.00
    },
    {
      "name": "Sports Fee",
      "amount": 1500.00
    },
    {
      "name": "Lab Fee",
      "amount": 3000.00
    }
  ]
}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Fee structure created successfully",
  "data": {
    "uuid": "fee-structure-uuid-456",
    "classUuid": "class-uuid-123",
    "className": "Grade 10",
    "section": "A",
    "totalAmount": 56500.00,
    "components": [
      {
        "uuid": "component-uuid-001",
        "name": "Tuition Fee",
        "amount": 50000.00,
        "createdAt": "2024-01-15T08:00:00",
        "updatedAt": "2024-01-15T08:00:00"
      },
      {
        "uuid": "component-uuid-002",
        "name": "Library Fee",
        "amount": 2000.00,
        "createdAt": "2024-01-15T08:00:00",
        "updatedAt": "2024-01-15T08:00:00"
      },
      {
        "uuid": "component-uuid-003",
        "name": "Sports Fee",
        "amount": 1500.00,
        "createdAt": "2024-01-15T08:00:00",
        "updatedAt": "2024-01-15T08:00:00"
      },
      {
        "uuid": "component-uuid-004",
        "name": "Lab Fee",
        "amount": 3000.00,
        "createdAt": "2024-01-15T08:00:00",
        "updatedAt": "2024-01-15T08:00:00"
      }
    ],
    "createdAt": "2024-01-15T08:00:00",
    "updatedAt": "2024-01-15T08:00:00"
  }
}
```

**What Happens:**
- ✅ Fee structure created for specific class
- ✅ Total amount calculated automatically from components
- ✅ One fee structure per class (enforced by unique constraint)
- ✅ Validates class exists and belongs to college
- ✅ Components stored with individual amounts
- ✅ Accessible by COLLEGE_ADMIN, SUPER_ADMIN, and ACCOUNTANT roles

**Error Response (409 Conflict):**
```json
{
  "success": false,
  "status": 409,
  "message": "Fee structure already exists for class Grade 10 A"
}
```

---

#### **Flow 1.2: Update Fee Structure**

**Update Fee Structure Components**
```http
PUT /api/v1/fees/structures/{feeStructureUuid}
Authorization: Bearer {adminAccessToken}
Content-Type: application/json

Request Body:
{
  "components": [
    {
      "name": "Tuition Fee",
      "amount": 55000.00  // Updated amount
    },
    {
      "name": "Library Fee",
      "amount": 2000.00
    },
    {
      "name": "Sports Fee",
      "amount": 1500.00
    },
    {
      "name": "Lab Fee",
      "amount": 3000.00
    },
    {
      "name": "Computer Lab Fee",  // New component
      "amount": 2500.00
    }
  ]
}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Fee structure updated successfully",
  "data": {
    "uuid": "fee-structure-uuid-456",
    "classUuid": "class-uuid-123",
    "className": "Grade 10",
    "section": "A",
    "totalAmount": 64000.00,  // Updated total
    "components": [
      // ... updated components
    ],
    "updatedAt": "2024-01-20T10:00:00"
  }
}
```

**What Happens:**
- ✅ Replaces all existing components with new ones
- ✅ Recalculates total amount
- ✅ Updates existing fee structure
- ⚠️ Note: This does NOT update already assigned student fees

---

#### **Flow 1.3: View Fee Structures**

**Get All Fee Structures**
```http
GET /api/v1/fees/structures?page=0&size=20&sortBy=createdAt&direction=DESC
Authorization: Bearer {adminAccessToken}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Fee structures retrieved successfully",
  "data": {
    "content": [
      {
        "uuid": "fee-structure-uuid-456",
        "classUuid": "class-uuid-123",
        "className": "Grade 10",
        "section": "A",
        "totalAmount": 56500.00,
        "components": [...],
        ...
      },
      ...
    ],
    "totalElements": 15,
    "totalPages": 1,
    "size": 20,
    "number": 0
  }
}
```

**Get Fee Structure by Class**
```http
GET /api/v1/fees/classes/{classUuid}/structure
Authorization: Bearer {adminAccessToken}

Response (200 OK):
{
  "success": true,
  "data": {
    "uuid": "fee-structure-uuid-456",
    "classUuid": "class-uuid-123",
    "className": "Grade 10",
    "section": "A",
    "totalAmount": 56500.00,
    "components": [...]
  }
}
```

---

### **Role 2: COLLEGE_ADMIN / ACCOUNTANT - Student Fee Assignment**

#### **Flow 2.1: Assign Fee to Individual Student**

**Assign Fee Structure to Student**
```http
POST /api/v1/fees/assign
Authorization: Bearer {adminAccessToken}
Content-Type: application/json

Request Body:
{
  "studentUuid": "student-uuid-001",
  "feeStructureUuid": "fee-structure-uuid-456"
}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Fee assigned to student successfully",
  "data": {
    "uuid": "student-fee-uuid-789",
    "studentUuid": "student-uuid-001",
    "studentName": "Alice Johnson",
    "rollNumber": "10A001",
    "feeStructureUuid": "fee-structure-uuid-456",
    "className": "Grade 10",
    "section": "A",
    "totalAmount": 56500.00,
    "paidAmount": 0.00,
    "dueAmount": 56500.00,
    "status": "PENDING",
    "createdAt": "2024-01-15T09:00:00",
    "updatedAt": "2024-01-15T09:00:00"
  }
}
```

**What Happens:**
- ✅ Creates StudentFee record linking student to fee structure
- ✅ Initial status: PENDING
- ✅ Due amount equals total amount initially
- ✅ Validates student and fee structure exist
- ✅ Prevents duplicate assignments

**Error Response (409 Conflict):**
```json
{
  "success": false,
  "status": 409,
  "message": "Fee structure is already assigned to student Alice Johnson"
}
```

---

#### **Flow 2.2: Bulk Assign Fee to Class**

**Assign Fee Structure to All Students in Class**
```http
POST /api/v1/fees/classes/{classUuid}/assign/{feeStructureUuid}
Authorization: Bearer {adminAccessToken}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Fee assigned to class students successfully",
  "data": [
    {
      "uuid": "student-fee-uuid-789",
      "studentUuid": "student-uuid-001",
      "studentName": "Alice Johnson",
      "rollNumber": "10A001",
      "status": "PENDING",
      "totalAmount": 56500.00,
      "dueAmount": 56500.00,
      ...
    },
    {
      "uuid": "student-fee-uuid-790",
      "studentUuid": "student-uuid-002",
      "studentName": "Bob Smith",
      "rollNumber": "10A002",
      "status": "PENDING",
      ...
    },
    // ... more students
  ]
}
```

**What Happens:**
- ✅ Assigns fee to all active students in the class
- ✅ Skips students who already have this fee assigned
- ✅ Returns list of newly assigned fees
- ✅ Useful for bulk operations at start of academic year

---

### **Role 3: COLLEGE_ADMIN / ACCOUNTANT - Payment Processing**

#### **Flow 3.1: Record Fee Payment**

**Record Payment for Student Fee**
```http
POST /api/v1/fees/payments
Authorization: Bearer {adminAccessToken | accountantAccessToken}
Content-Type: application/json

Request Body:
{
  "studentFeeUuid": "student-fee-uuid-789",
  "amount": 20000.00,
  "paymentMode": "UPI",
  "transactionId": "TXN123456789",
  "paymentDate": "2024-01-20T14:30:00"  // Optional, defaults to current time
}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Fee payment recorded successfully",
  "data": {
    "uuid": "payment-uuid-999",
    "studentFeeUuid": "student-fee-uuid-789",
    "studentUuid": "student-uuid-001",
    "studentName": "Alice Johnson",
    "amount": 20000.00,
    "paymentMode": "UPI",
    "transactionId": "TXN123456789",
    "paymentDate": "2024-01-20T14:30:00",
    "createdAt": "2024-01-20T14:30:00",
    "updatedAt": "2024-01-20T14:30:00"
  }
}
```

**What Happens:**
- ✅ Creates FeePayment record
- ✅ Updates StudentFee: paidAmount += payment amount
- ✅ Updates StudentFee: dueAmount = totalAmount - paidAmount
- ✅ Auto-updates fee status:
  - If paidAmount == 0 → PENDING
  - If paidAmount == totalAmount → PAID
  - If paidAmount < totalAmount → PARTIALLY_PAID
- ✅ Validates payment amount doesn't exceed due amount
- ✅ Validates transaction ID uniqueness (if provided)
- ✅ Primary function for ACCOUNTANT role - recording payments is a core responsibility

**Payment Status Flow:**
```
PENDING → PARTIALLY_PAID → PAID
         (on first payment)  (when fully paid)
```

**Error Responses:**

**Payment Amount Exceeds Due Amount (409):**
```json
{
  "success": false,
  "status": 409,
  "message": "Payment amount (25000.00) cannot exceed due amount (20000.00)"
}
```

**Duplicate Transaction ID (409):**
```json
{
  "success": false,
  "status": 409,
  "message": "Transaction ID already exists: TXN123456789"
}
```

---

#### **Flow 3.2: View Payment History**

**Get All Payments for a Student Fee**
```http
GET /api/v1/fees/student-fees/{studentFeeUuid}/payments?page=0&size=20
Authorization: Bearer {adminAccessToken}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Fee payments retrieved successfully",
  "data": {
    "content": [
      {
        "uuid": "payment-uuid-999",
        "studentFeeUuid": "student-fee-uuid-789",
        "studentUuid": "student-uuid-001",
        "studentName": "Alice Johnson",
        "amount": 20000.00,
        "paymentMode": "UPI",
        "transactionId": "TXN123456789",
        "paymentDate": "2024-01-20T14:30:00",
        ...
      },
      {
        "uuid": "payment-uuid-1000",
        "amount": 15000.00,
        "paymentMode": "CASH",
        "paymentDate": "2024-02-15T10:00:00",
        ...
      },
      ...
    ],
    "totalElements": 3,
    "totalPages": 1
  }
}
```

**Get All Payments for a Student**
```http
GET /api/v1/fees/students/{studentUuid}/payments?page=0&size=20
Authorization: Bearer {adminAccessToken}

Response (200 OK):
{
  "success": true,
  "data": {
    "content": [
      // All payments across all fees for this student
    ],
    "totalElements": 5
  }
}
```

**Get Payments by Date Range**
```http
GET /api/v1/fees/payments/range?startDate=2024-01-01T00:00:00Z&endDate=2024-01-31T23:59:59Z&page=0&size=20
Authorization: Bearer {adminAccessToken}

Response (200 OK):
{
  "success": true,
  "data": {
    "content": [
      // All payments within date range
    ],
    "totalElements": 150
  }
}
```

---

### **Role 4: STUDENT - View Own Fees**

#### **Flow 4.1: View My Fees**

**Get All My Fees**
```http
GET /api/v1/fees/students/{studentUuid}/fees?page=0&size=20&sortBy=createdAt&direction=DESC
Authorization: Bearer {studentAccessToken}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Student fees retrieved successfully",
  "data": {
    "content": [
      {
        "uuid": "student-fee-uuid-789",
        "studentUuid": "student-uuid-001",
        "studentName": "Alice Johnson",
        "rollNumber": "10A001",
        "feeStructureUuid": "fee-structure-uuid-456",
        "className": "Grade 10",
        "section": "A",
        "totalAmount": 56500.00,
        "paidAmount": 20000.00,
        "dueAmount": 36500.00,
        "status": "PARTIALLY_PAID",
        "createdAt": "2024-01-15T09:00:00",
        "updatedAt": "2024-01-20T14:30:00"
      },
      {
        "uuid": "student-fee-uuid-790",
        "totalAmount": 30000.00,
        "paidAmount": 30000.00,
        "dueAmount": 0.00,
        "status": "PAID",
        ...
      },
      ...
    ],
    "totalElements": 3,
    "totalPages": 1
  }
}
```

---

#### **Flow 4.2: View My Fee Summary**

**Get My Fee Summary**
```http
GET /api/v1/fees/students/{studentUuid}/summary
Authorization: Bearer {studentAccessToken}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Student fee summary retrieved successfully",
  "data": {
    "studentUuid": "student-uuid-001",
    "studentName": "Alice Johnson",
    "rollNumber": "10A001",
    "className": "Grade 10",
    "totalFees": 86500.00,
    "totalPaid": 50000.00,
    "totalDue": 36500.00,
    "pendingCount": 0,
    "paidCount": 1,
    "partiallyPaidCount": 1,
    "overdueCount": 0,
    "fees": [
      {
        "uuid": "student-fee-uuid-789",
        "totalAmount": 56500.00,
        "paidAmount": 20000.00,
        "dueAmount": 36500.00,
        "status": "PARTIALLY_PAID",
        ...
      },
      {
        "uuid": "student-fee-uuid-790",
        "totalAmount": 30000.00,
        "paidAmount": 30000.00,
        "dueAmount": 0.00,
        "status": "PAID",
        ...
      }
    ]
  }
}
```

**What Happens:**
- ✅ Aggregates all fees for the student
- ✅ Calculates total fees, paid, and due amounts
- ✅ Counts fees by status
- ✅ Returns detailed list of all fees
- ✅ Useful for student dashboard

---

### **Role 5: COLLEGE_ADMIN / ACCOUNTANT - Fee Reports & Summaries**

#### **Flow 5.1: College Fee Summary**

**Get College-Wide Fee Summary**
```http
GET /api/v1/fees/summary/college
Authorization: Bearer {adminAccessToken}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "College fee summary retrieved successfully",
  "data": {
    "totalFees": 5000000.00,
    "totalPaid": 3500000.00,
    "totalDue": 1500000.00,
    "totalStudents": 500,
    "pendingCount": 50,
    "paidCount": 200,
    "partiallyPaidCount": 200,
    "overdueCount": 50
  }
}
```

**What Happens:**
- ✅ Aggregates all fees across all classes
- ✅ Provides college-wide financial overview
- ✅ Useful for financial reporting and planning
- ✅ Essential for ACCOUNTANT role for financial audits and reconciliation

---

#### **Flow 5.2: Class Fee Summary**

**Get Class Fee Summary**
```http
GET /api/v1/fees/classes/{classUuid}/summary
Authorization: Bearer {adminAccessToken}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Class fee summary retrieved successfully",
  "data": {
    "classUuid": "class-uuid-123",
    "className": "Grade 10",
    "section": "A",
    "totalFees": 1695000.00,  // 30 students × 56500
    "totalPaid": 1200000.00,
    "totalDue": 495000.00,
    "totalStudents": 30,
    "pendingCount": 5,
    "paidCount": 10,
    "partiallyPaidCount": 12,
    "overdueCount": 3
  }
}
```

---

#### **Flow 5.3: View Fees by Status**

**Get Overdue Fees**
```http
GET /api/v1/fees/student-fees/overdue?page=0&size=20
Authorization: Bearer {adminAccessToken}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Overdue student fees retrieved successfully",
  "data": {
    "content": [
      {
        "uuid": "student-fee-uuid-800",
        "studentUuid": "student-uuid-010",
        "studentName": "John Doe",
        "rollNumber": "10A010",
        "totalAmount": 56500.00,
        "paidAmount": 0.00,
        "dueAmount": 56500.00,
        "status": "OVERDUE",
        ...
      },
      ...
    ],
    "totalElements": 50
  }
}
```

**Get Fees by Status**
```http
GET /api/v1/fees/student-fees/status/PARTIALLY_PAID?page=0&size=20
Authorization: Bearer {adminAccessToken}

Response (200 OK):
{
  "success": true,
  "data": {
    "content": [
      // All partially paid fees
    ],
    "totalElements": 200
  }
}
```

**Available Status Values:**
- `PENDING` - No payment made yet
- `PARTIALLY_PAID` - Some payment made, but not full amount
- `PAID` - Fully paid
- `OVERDUE` - Payment past due date (can be set manually or via scheduled job)

---

## Complete User Journey Examples

### **Journey 1: Admin/Accountant Sets Up Fees for New Academic Year**

```
1. Admin/Accountant logs in
   POST /api/v1/auth/login

2. Create fee structure for Grade 10
   POST /api/v1/fees/structures
   {
     "classUuid": "class-uuid-123",
     "components": [
       { "name": "Tuition Fee", "amount": 50000.00 },
       { "name": "Library Fee", "amount": 2000.00 },
       { "name": "Sports Fee", "amount": 1500.00 },
       { "name": "Lab Fee", "amount": 3000.00 }
     ]
   }

3. Bulk assign fees to all students in class
   POST /api/v1/fees/classes/{classUuid}/assign/{feeStructureUuid}

4. Verify assignments
   GET /api/v1/fees/classes/{classUuid}/student-fees?page=0&size=20

5. View class fee summary
   GET /api/v1/fees/classes/{classUuid}/summary
```

---

### **Journey 2: Student Views and Tracks Fees**

```
1. Student logs in
   POST /api/v1/auth/login

2. View all my fees
   GET /api/v1/fees/students/{myStudentUuid}/fees?page=0&size=20

3. View my fee summary
   GET /api/v1/fees/students/{myStudentUuid}/summary

4. View payment history for a specific fee
   GET /api/v1/fees/student-fees/{studentFeeUuid}/payments?page=0&size=20

5. Check due amounts and payment status
   (Display from summary response)
```

---

### **Journey 3: Admin/Accountant Records Payment**

```
1. Admin/Accountant logs in
   POST /api/v1/auth/login

2. Search for student
   GET /api/v1/students?searchTerm=Alice

3. View student's fees
   GET /api/v1/fees/students/{studentUuid}/fees

4. Record payment
   POST /api/v1/fees/payments
   {
     "studentFeeUuid": "student-fee-uuid-789",
     "amount": 20000.00,
     "paymentMode": "UPI",
     "transactionId": "TXN123456789"
   }

5. Verify payment recorded
   GET /api/v1/fees/student-fees/{studentFeeUuid}/payments

6. Check updated fee status
   GET /api/v1/fees/student-fees/{studentFeeUuid}
   // Status should be updated to PARTIALLY_PAID or PAID
```

---

### **Journey 4: Admin/Accountant Generates Fee Reports**

```
1. Admin/Accountant logs in
   POST /api/v1/auth/login

2. View college-wide summary
   GET /api/v1/fees/summary/college

3. View class-wise summaries
   GET /api/v1/fees/classes/{classUuid}/summary
   (Repeat for each class)

4. View overdue fees
   GET /api/v1/fees/student-fees/overdue?page=0&size=20

5. View payments in date range
   GET /api/v1/fees/payments/range?startDate=2024-01-01T00:00:00Z&endDate=2024-01-31T23:59:59Z

6. Export data for reporting
   (Frontend can format the API responses into reports)
```

---

### **Journey 5: Accountant Daily Operations**

```
1. Accountant logs in
   POST /api/v1/auth/login

2. View all overdue fees
   GET /api/v1/fees/student-fees/overdue?page=0&size=20

3. View pending payments to process
   GET /api/v1/fees/student-fees/status/PENDING?page=0&size=20

4. Record multiple payments throughout the day
   POST /api/v1/fees/payments
   {
     "studentFeeUuid": "...",
     "amount": 20000.00,
     "paymentMode": "CASH",
     "transactionId": "TXN001"
   }
   (Repeat for each payment received)

5. View payment history for the day
   GET /api/v1/fees/payments/range?startDate=2024-01-20T00:00:00Z&endDate=2024-01-20T23:59:59Z

6. Generate daily collection report
   GET /api/v1/fees/summary/college
   (Extract payment data from summary)

7. View student fee details for reconciliation
   GET /api/v1/fees/students/{studentUuid}/fees
   GET /api/v1/fees/students/{studentUuid}/payments
```

---

## Frontend Integration Guidelines

### **1. Authentication Flow**

```javascript
// Step 1: Login
const loginResponse = await fetch('/api/v1/auth/login', {
  method: 'POST',
  headers: { 'Content-Type': 'application/json' },
  body: JSON.stringify({ email, password })
});

const { data } = await loginResponse.json();
const { accessToken, user } = data.auth;

// Step 2: Store token
localStorage.setItem('accessToken', accessToken);
localStorage.setItem('userUuid', user.uuid);
localStorage.setItem('userRole', user.roles[0]);

// Step 3: Use token for all fee API calls
const headers = {
  'Authorization': `Bearer ${accessToken}`,
  'Content-Type': 'application/json'
};
```

---

### **2. Create Fee Structure Component**

```javascript
async function createFeeStructure(classUuid, components) {
  const response = await fetch('/api/v1/fees/structures', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${getAccessToken()}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      classUuid,
      components // Array of { name, amount }
    })
  });
  
  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message);
  }
  
  return await response.json();
}

// Example usage:
const components = [
  { name: 'Tuition Fee', amount: 50000.00 },
  { name: 'Library Fee', amount: 2000.00 },
  { name: 'Sports Fee', amount: 1500.00 },
  { name: 'Lab Fee', amount: 3000.00 }
];

const result = await createFeeStructure('class-uuid-123', components);
console.log('Fee structure created:', result.data);
```

---

### **3. Assign Fee to Student Component**

```javascript
async function assignFeeToStudent(studentUuid, feeStructureUuid) {
  const response = await fetch('/api/v1/fees/assign', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${getAccessToken()}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      studentUuid,
      feeStructureUuid
    })
  });
  
  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message);
  }
  
  return await response.json();
}

// Bulk assign to class
async function assignFeeToClass(classUuid, feeStructureUuid) {
  const response = await fetch(
    `/api/v1/fees/classes/${classUuid}/assign/${feeStructureUuid}`,
    {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${getAccessToken()}`
      }
    }
  );
  
  return await response.json();
}
```

---

### **4. Record Payment Component**

```javascript
async function recordFeePayment(studentFeeUuid, amount, paymentMode, transactionId) {
  const response = await fetch('/api/v1/fees/payments', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${getAccessToken()}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      studentFeeUuid,
      amount: parseFloat(amount),
      paymentMode, // 'CASH', 'UPI', 'CARD', 'NET_BANKING', 'CHEQUE'
      transactionId, // Optional
      paymentDate: new Date().toISOString() // Optional
    })
  });
  
  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message);
  }
  
  return await response.json();
}

// Example usage:
await recordFeePayment(
  'student-fee-uuid-789',
  20000.00,
  'UPI',
  'TXN123456789'
);
```

---

### **5. View Student Fees Component**

```javascript
async function getStudentFees(studentUuid, page = 0, size = 20) {
  const response = await fetch(
    `/api/v1/fees/students/${studentUuid}/fees?page=${page}&size=${size}&sortBy=createdAt&direction=DESC`,
    {
      headers: {
        'Authorization': `Bearer ${getAccessToken()}`
      }
    }
  );
  
  const { data } = await response.json();
  return data; // Contains content[], totalElements, totalPages
}

async function getStudentFeeSummary(studentUuid) {
  const response = await fetch(
    `/api/v1/fees/students/${studentUuid}/summary`,
    {
      headers: {
        'Authorization': `Bearer ${getAccessToken()}`
      }
    }
  );
  
  const { data } = await response.json();
  return data; // Contains totalFees, totalPaid, totalDue, status counts, etc.
}
```

---

### **6. View Payment History Component**

```javascript
async function getPaymentHistory(studentFeeUuid, page = 0, size = 20) {
  const response = await fetch(
    `/api/v1/fees/student-fees/${studentFeeUuid}/payments?page=${page}&size=${size}`,
    {
      headers: {
        'Authorization': `Bearer ${getAccessToken()}`
      }
    }
  );
  
  const { data } = await response.json();
  return data;
}

async function getPaymentsByDateRange(startDate, endDate, page = 0, size = 20) {
  const start = new Date(startDate).toISOString();
  const end = new Date(endDate).toISOString();
  
  const response = await fetch(
    `/api/v1/fees/payments/range?startDate=${start}&endDate=${end}&page=${page}&size=${size}`,
    {
      headers: {
        'Authorization': `Bearer ${getAccessToken()}`
      }
    }
  );
  
  const { data } = await response.json();
  return data;
}
```

---

### **7. Fee Summary Components**

```javascript
async function getCollegeFeeSummary() {
  const response = await fetch('/api/v1/fees/summary/college', {
    headers: {
      'Authorization': `Bearer ${getAccessToken()}`
    }
  });
  
  const { data } = await response.json();
  return data; // { totalFees, totalPaid, totalDue, totalStudents, status counts }
}

async function getClassFeeSummary(classUuid) {
  const response = await fetch(`/api/v1/fees/classes/${classUuid}/summary`, {
    headers: {
      'Authorization': `Bearer ${getAccessToken()}`
    }
  });
  
  const { data } = await response.json();
  return data;
}

async function getOverdueFees(page = 0, size = 20) {
  const response = await fetch(
    `/api/v1/fees/student-fees/overdue?page=${page}&size=${size}`,
    {
      headers: {
        'Authorization': `Bearer ${getAccessToken()}`
      }
    }
  );
  
  const { data } = await response.json();
  return data;
}
```

---

## UI/UX Recommendations

### **1. Fee Structure Management Page (Admin/Accountant)**

```
┌──────────────────────────────────────────────────────────┐
│  Fee Structure Management                                 │
│  [Create New Structure] [View All Structures]            │
├──────────────────────────────────────────────────────────┤
│                                                          │
│  Create Fee Structure:                                    │
│  ┌────────────────────────────────────────────────────┐ │
│  │ Class: [Select Class ▼]                            │ │
│  │                                                     │ │
│  │ Fee Components:                                     │ │
│  │ ┌──────────────────────────────────────────────┐  │ │
│  │ │ Component Name    │ Amount                   │  │ │
│  │ ├──────────────────────────────────────────────┤  │ │
│  │ │ Tuition Fee       │ [50000.00] [Remove]      │  │ │
│  │ │ Library Fee       │ [2000.00]  [Remove]      │  │ │
│  │ │ Sports Fee        │ [1500.00]  [Remove]      │  │ │
│  │ │ Lab Fee           │ [3000.00]  [Remove]      │  │ │
│  │ └──────────────────────────────────────────────┘  │ │
│  │                                                     │ │
│  │ [Add Component]                                    │ │
│  │                                                     │ │
│  │ Total Amount: ₹56,500.00                           │ │
│  │                                                     │ │
│  │ [Cancel] [Save Fee Structure]                      │ │
│  └────────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────┘
```

**Features:**
- Dynamic component addition/removal
- Real-time total calculation
- Class selection dropdown
- Validation: At least one component required
- Show existing structure if already created for class

---

### **2. Student Fee Dashboard (Student View)**

```
┌──────────────────────────────────────────────────────────┐
│  My Fees                                                  │
│  Date: [2024-01-15]                                       │
├──────────────────────────────────────────────────────────┤
│                                                          │
│  Summary Card:                                            │
│  ┌─────────────┬─────────────┬─────────────┐           │
│  │ Total Fees  │ Total Paid  │ Total Due   │           │
│  │ ₹86,500.00  │ ₹50,000.00  │ ₹36,500.00  │           │
│  └─────────────┴─────────────┴─────────────┘           │
│                                                          │
│  Status Breakdown:                                       │
│  ┌─────────────┬─────────────┬─────────────┬─────────┐ │
│  │ Pending     │ Partially   │ Paid        │ Overdue  │ │
│  │     0       │ Paid: 1     │     1      │    0     │ │
│  └─────────────┴─────────────┴─────────────┴─────────┘ │
│                                                          │
│  Fee Details:                                            │
│  ┌────────────────────────────────────────────────────┐ │
│  │ Fee Structure │ Total   │ Paid    │ Due     │ Status│ │
│  ├────────────────────────────────────────────────────┤ │
│  │ Grade 10 Fee  │ 56,500  │ 20,000  │ 36,500  │ ⚠ Part│ │
│  │               │          │         │         │ Paid  │ │
│  │               │ [View Details] [Payment History]  │ │
│  ├────────────────────────────────────────────────────┤ │
│  │ Sports Fee    │ 30,000  │ 30,000  │ 0       │ ✓ Paid│ │
│  │               │          │         │         │       │ │
│  │               │ [View Details] [Payment History]  │ │
│  └────────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────┘
```

**Features:**
- Color-coded status indicators
- Quick summary cards
- Expandable fee details
- Payment history link
- Download fee receipt option

---

### **3. Payment Recording Page (Admin/Accountant)**

```
┌──────────────────────────────────────────────────────────┐
│  Record Fee Payment                                       │
│  Student: Alice Johnson (10A001)                         │
├──────────────────────────────────────────────────────────┤
│                                                          │
│  Student Fee Details:                                     │
│  ┌────────────────────────────────────────────────────┐ │
│  │ Fee Structure: Grade 10 Fee Structure             │ │
│  │ Total Amount: ₹56,500.00                           │ │
│  │ Paid Amount: ₹20,000.00                            │ │
│  │ Due Amount: ₹36,500.00                             │ │
│  │ Status: PARTIALLY_PAID                             │ │
│  └────────────────────────────────────────────────────┘ │
│                                                          │
│  Payment Information:                                     │
│  ┌────────────────────────────────────────────────────┐ │
│  │ Payment Amount: [20000.00]                         │ │
│  │ Payment Mode: [UPI ▼]                              │ │
│  │ Transaction ID: [TXN123456789]                     │ │
│  │ Payment Date: [2024-01-20] [14:30]                 │ │
│  │                                                     │ │
│  │ After Payment:                                      │ │
│  │ New Paid Amount: ₹40,000.00                        │ │
│  │ New Due Amount: ₹16,500.00                          │ │
│  │ New Status: PARTIALLY_PAID                         │ │
│  │                                                     │ │
│  │ [Cancel] [Record Payment]                          │ │
│  └────────────────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────┘
```

**Features:**
- Real-time calculation preview
- Payment mode selection
- Transaction ID validation
- Amount validation (cannot exceed due amount)
- Success confirmation with updated status
- Primary interface for ACCOUNTANT role for daily payment processing

---

### **4. Fee Reports Dashboard (Admin/Accountant)**

```
┌──────────────────────────────────────────────────────────┐
│  Fee Reports & Analytics                                 │
│  [College Summary] [Class Summary] [Overdue Fees]       │
├──────────────────────────────────────────────────────────┤
│                                                          │
│  College-Wide Summary:                                   │
│  ┌──────────────────────────────────────────────────┐   │
│  │ Total Fees: ₹50,00,000.00                        │   │
│  │ Total Paid: ₹35,00,000.00                         │   │
│  │ Total Due: ₹15,00,000.00                          │   │
│  │ Collection Rate: 70%                              │   │
│  │                                                    │   │
│  │ Total Students: 500                               │   │
│  │ Pending: 50  │ Partially Paid: 200  │ Paid: 200   │   │
│  │ Overdue: 50                                       │   │
│  └──────────────────────────────────────────────────┘   │
│                                                          │
│  Class-wise Breakdown:                                   │
│  ┌──────────────────────────────────────────────────┐   │
│  │ Class          │ Total    │ Paid     │ Due      │   │
│  ├──────────────────────────────────────────────────┤   │
│  │ Grade 10 A     │ 16,95,000│ 12,00,000│ 4,95,000 │   │
│  │ Grade 10 B     │ 16,95,000│ 11,50,000│ 5,45,000 │   │
│  │ Grade 11 A     │ 18,00,000│ 13,00,000│ 5,00,000 │   │
│  │ ...            │ ...      │ ...      │ ...      │   │
│  └──────────────────────────────────────────────────┘   │
│                                                          │
│  [Export Report] [Generate PDF] [Email Report]          │
└──────────────────────────────────────────────────────────┘
```

**Features:**
- Visual charts (pie charts, bar graphs)
- Collection rate percentage
- Class-wise comparison
- Export options (PDF, Excel, CSV)
- Date range filtering
- Essential for ACCOUNTANT role for financial reporting and reconciliation

---

## Error Handling & Validation

### **Common Error Responses**

**1. Fee Structure Already Exists (409)**
```json
{
  "success": false,
  "status": 409,
  "message": "Fee structure already exists for class Grade 10 A"
}
```

**2. Fee Already Assigned (409)**
```json
{
  "success": false,
  "status": 409,
  "message": "Fee structure is already assigned to student Alice Johnson"
}
```

**3. Payment Amount Exceeds Due (409)**
```json
{
  "success": false,
  "status": 409,
  "message": "Payment amount (25000.00) cannot exceed due amount (20000.00)"
}
```

**4. Duplicate Transaction ID (409)**
```json
{
  "success": false,
  "status": 409,
  "message": "Transaction ID already exists: TXN123456789"
}
```

**5. Cannot Delete Fee Structure (409)**
```json
{
  "success": false,
  "status": 409,
  "message": "Cannot delete fee structure. 30 student(s) have fees assigned to this structure."
}
```

**6. Student Not Found (404)**
```json
{
  "success": false,
  "status": 404,
  "message": "Student not found with UUID: invalid-uuid"
}
```

**7. Fee Structure Not Found (404)**
```json
{
  "success": false,
  "status": 404,
  "message": "Fee structure not found with UUID: invalid-uuid"
}
```

**8. Access Denied (403)**
```json
{
  "success": false,
  "status": 403,
  "message": "Access denied"
}
// Student trying to view another student's fees or unauthorized access
```

---

## Security & Isolation Notes

- ✅ **College Isolation:** All queries automatically filter by `collegeId`
- ✅ **Role-Based Access:** Permissions enforced at service layer with `@PreAuthorize` annotations
- ✅ **JWT Authentication:** All endpoints require valid token
- ✅ **Tenant Context:** Automatically set from authenticated user's college
- ✅ **Student Validation:** Students can only view their own fees
- ✅ **Payment Validation:** Payment amounts validated against due amounts
- ✅ **Transaction ID Uniqueness:** Prevents duplicate payment records
- ✅ **Accountant Role:** ACCOUNTANT has full access to fee management operations (create, update, assign, record payments, view summaries) except for fee structure deletion (admin-only)

---

## Payment Mode Options

| Payment Mode | Description | Use Case |
|-------------|-------------|----------|
| `CASH` | Cash payment | On-campus payments |
| `UPI` | UPI payment | Digital payments via UPI apps |
| `CARD` | Debit/Credit card | Card payments |
| `NET_BANKING` | Net banking | Online bank transfers |
| `CHEQUE` | Cheque payment | Cheque deposits |

---

## Fee Status Flow

```
PENDING
  │
  │ (First payment received)
  ▼
PARTIALLY_PAID
  │
  │ (Full payment received)
  ▼
PAID

OR

PENDING
  │
  │ (Due date passed, manually set or via scheduled job)
  ▼
OVERDUE
```

**Status Definitions:**
- **PENDING:** No payment made yet
- **PARTIALLY_PAID:** Some payment made, but not full amount
- **PAID:** Fully paid (paidAmount >= totalAmount)
- **OVERDUE:** Payment past due date (can be set manually or via scheduled job)

---

## API Endpoint Summary

| Endpoint | Method | Role | Purpose |
|----------|--------|------|---------|
| `/api/v1/fees/structures` | POST | Admin/Accountant | Create fee structure |
| `/api/v1/fees/structures/{uuid}` | GET | All | Get fee structure by UUID |
| `/api/v1/fees/structures/{uuid}` | PUT | Admin/Accountant | Update fee structure |
| `/api/v1/fees/structures/{uuid}` | DELETE | Admin | Delete fee structure |
| `/api/v1/fees/structures` | GET | Admin/Accountant/Teacher | Get all fee structures |
| `/api/v1/fees/classes/{uuid}/structure` | GET | All | Get fee structure by class |
| `/api/v1/fees/classes/{uuid}/structures` | GET | All | Get all fee structures for class |
| `/api/v1/fees/assign` | POST | Admin/Accountant | Assign fee to student |
| `/api/v1/fees/classes/{uuid}/assign/{structure}` | POST | Admin/Accountant | Bulk assign to class |
| `/api/v1/fees/student-fees/{uuid}` | GET | All | Get student fee by UUID |
| `/api/v1/fees/students/{uuid}/fees` | GET | All* | Get student's fees |
| `/api/v1/fees/structures/{uuid}/student-fees` | GET | Admin/Accountant/Teacher | Get fees by structure |
| `/api/v1/fees/student-fees/status/{status}` | GET | Admin/Accountant/Teacher | Get fees by status |
| `/api/v1/fees/student-fees/overdue` | GET | Admin/Accountant/Teacher | Get overdue fees |
| `/api/v1/fees/classes/{uuid}/student-fees` | GET | Admin/Accountant/Teacher | Get fees by class |
| `/api/v1/fees/payments` | POST | Admin/Accountant | Record fee payment |
| `/api/v1/fees/payments/{uuid}` | GET | All | Get payment by UUID |
| `/api/v1/fees/student-fees/{uuid}/payments` | GET | All* | Get payments for fee |
| `/api/v1/fees/students/{uuid}/payments` | GET | All* | Get student's payments |
| `/api/v1/fees/payments/range` | GET | Admin/Accountant/Teacher | Get payments by date range |
| `/api/v1/fees/students/{uuid}/summary` | GET | All* | Get student fee summary |
| `/api/v1/fees/summary/college` | GET | Admin/Accountant | Get college fee summary |
| `/api/v1/fees/classes/{uuid}/summary` | GET | Admin/Accountant/Teacher | Get class fee summary |

*All authenticated users can view their own fees/payments; Admin/Accountant/Teacher can view any student's fees/payments.

---

## Future Enhancements

1. **Due Date Management:** Add due date field to StudentFee for automatic overdue status
2. **Payment Reminders:** Automated email/SMS reminders for pending/overdue fees
3. **Payment Gateway Integration:** Direct payment processing via Razorpay/Stripe
4. **Fee Receipt Generation:** PDF receipt generation for payments
5. **Partial Payment Plans:** Support for installment-based fee payment
6. **Fee Waivers & Discounts:** Apply discounts or waivers to student fees
7. **Fee Refunds:** Handle fee refunds with proper tracking
8. **Parent Portal Integration:** Allow parents to view and pay fees
9. **Fee Analytics Dashboard:** Advanced analytics with charts and trends
10. **Bulk Payment Import:** Import payments from Excel/CSV files
11. **Fee Templates:** Save and reuse fee structure templates
12. **Multi-Currency Support:** Support for different currencies
13. **Fee Categories:** Categorize fees (Academic, Non-Academic, etc.)
14. **Payment Notifications:** Real-time notifications for payments
15. **Fee History Tracking:** Track fee structure changes over time

---

## Testing Scenarios

### **Test Case 1: Complete Fee Management Flow**
1. ✅ Create fee structure for a class
2. ✅ Assign fee to individual student
3. ✅ Record partial payment
4. ✅ Verify fee status updated to PARTIALLY_PAID
5. ✅ Record remaining payment
6. ✅ Verify fee status updated to PAID
7. ✅ View payment history
8. ✅ Generate fee summary

### **Test Case 2: Bulk Fee Assignment**
1. ✅ Create fee structure
2. ✅ Bulk assign to all students in class
3. ✅ Verify all students have fees assigned
4. ✅ Verify duplicate assignment prevention

### **Test Case 3: Payment Validation**
1. ✅ Try to pay more than due amount
2. ✅ Verify error: "Payment amount cannot exceed due amount"
3. ✅ Try duplicate transaction ID
4. ✅ Verify error: "Transaction ID already exists"

### **Test Case 4: Fee Status Updates**
1. ✅ Assign fee (status: PENDING)
2. ✅ Record partial payment (status: PARTIALLY_PAID)
3. ✅ Record full payment (status: PAID)
4. ✅ Verify status transitions correctly

### **Test Case 5: College Isolation**
1. ✅ Login as College A admin
2. ✅ Try to access College B's fee structures
3. ✅ Verify 404 or empty results (tenant isolation)

### **Test Case 6: Student Access Control**
1. ✅ Login as Student A
2. ✅ Try to view Student B's fees
3. ✅ Verify access denied or only own fees visible

### **Test Case 7: Fee Structure Deletion**
1. ✅ Create fee structure
2. ✅ Assign to students
3. ✅ Try to delete fee structure
4. ✅ Verify error: "Cannot delete fee structure. X students have fees assigned"
5. ✅ Remove all student fee assignments
6. ✅ Delete fee structure successfully

---

## Conclusion

This document provides a complete guide for frontend developers to integrate the Student Fees Management system. All endpoints are RESTful, follow consistent patterns, and include proper error handling and security measures.

**Key Features:**
- ✅ Comprehensive fee structure management
- ✅ Flexible fee assignment (individual or bulk)
- ✅ Payment processing with automatic status updates
- ✅ Detailed payment history tracking
- ✅ Multiple fee summary reports (student, class, college)
- ✅ Role-based access control
- ✅ College-level isolation
- ✅ Support for multiple payment modes

For any questions or clarifications, refer to the API documentation or contact the backend team.

