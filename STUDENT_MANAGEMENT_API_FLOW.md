# Complete Student Management API Flow

## Overview
This document describes the complete API flow for student management in the college management system, including admission applications, student CRUD operations, enrollments, parent management, and all related modules (documents, disciplinary cases, leave requests, promotions, PTM bookings).

**Last Updated:** Generated after full implementation of all student-related modules

---

## Table of Contents

1. [Student Admission & Enrollment Flow](#student-admission--enrollment-flow)
2. [Student CRUD Operations](#student-crud-operations)
3. [Parent Management](#parent-management)
4. [Document Management](#document-management)
5. [Disciplinary Case Management](#disciplinary-case-management)
6. [Leave Request Management](#leave-request-management)
7. [Student Promotion Management](#student-promotion-management)
8. [PTM Booking Management](#ptm-booking-management)
9. [Complete API Endpoints Reference](#complete-api-endpoints-reference)

---

## Student Admission & Enrollment Flow

### Flow Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                    ADMISSION APPLICATION                         │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│  Step 1: POST /api/v1/admissions                               │
│  - Creates AdmissionApplication                                 │
│  - Status: DRAFT                                                │
│  - Auto-generates application number                            │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│  Step 2: PUT /api/v1/admissions/{uuid}                         │
│  - Update application details (DRAFT only)                      │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│  Step 3: POST /api/v1/admissions/{uuid}/submit                 │
│  - Submit application: DRAFT → SUBMITTED                        │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│  Step 4: POST /api/v1/admissions/{uuid}/verify                 │
│  - Verify application: SUBMITTED → VERIFIED                     │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│  Step 5: POST /api/v1/admissions/{uuid}/approve                │
│  - Approve & create Student                                     │
│  - Creates User account                                         │
│  - Creates Student record                                       │
│  - Optionally creates Enrollment                                │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    STUDENT MANAGEMENT                            │
│  - Create/Update/View Students                                  │
│  - Manage Enrollments                                           │
│  - Assign Parents                                               │
│  - Upload Documents                                             │
│  - Manage Disciplinary Cases                                    │
│  - Handle Leave Requests                                        │
│  - Promote Students                                             │
│  - Manage PTM Bookings                                          │
└─────────────────────────────────────────────────────────────────┘
```

For detailed admission flow documentation, see: `STUDENT_ADMISSION_ENROLLMENT_FLOW.md`

---

## Student CRUD Operations

### **1. Create Student**

```http
POST /api/v1/students
Authorization: Bearer {accessToken}
Content-Type: application/json

Request Body:
{
  "name": "John Doe",
  "email": "john.doe@college.edu",
  "password": "SecurePass123!",
  "dob": "2010-05-15T00:00:00Z",
  "gender": "MALE",
  "rollNumber": "2024-001",
  "registrationNumber": "REG-2024-001",
  "admissionDate": "2024-01-20T00:00:00Z",
  "bloodGroup": "O+",
  "address": "123 Main St, City, State"
}

Response:
{
  "success": true,
  "status": 200,
  "message": "Student created successfully",
  "data": {
    "uuid": "student-uuid-here",
    "name": "John Doe",
    "email": "john.doe@college.edu",
    "rollNumber": "2024-001",
    "registrationNumber": "REG-2024-001",
    "dob": "2010-05-15T00:00:00Z",
    "gender": "MALE",
    "admissionDate": "2024-01-20T00:00:00Z",
    "bloodGroup": "O+",
    "address": "123 Main St, City, State",
    "status": "ACTIVE",
    "collegeId": 1
  }
}
```

**Roles:** `COLLEGE_ADMIN`, `SUPER_ADMIN`

---

### **2. Update Student**

```http
PUT /api/v1/students/{studentUuid}
Authorization: Bearer {accessToken}
Content-Type: application/json

Request Body:
{
  "name": "John Doe Updated",
  "bloodGroup": "O-",
  "address": "456 New St, City, State"
}

Response:
{
  "success": true,
  "status": 200,
  "message": "Student updated successfully",
  "data": { ... }
}
```

**Roles:** `COLLEGE_ADMIN`, `SUPER_ADMIN`

---

### **3. Get Student**

```http
GET /api/v1/students/{studentUuid}
Authorization: Bearer {accessToken}

Response:
{
  "success": true,
  "status": 200,
  "message": "Student retrieved successfully",
  "data": {
    "uuid": "student-uuid-here",
    "name": "John Doe",
    "email": "john.doe@college.edu",
    "rollNumber": "2024-001",
    "registrationNumber": "REG-2024-001",
    "dob": "2010-05-15T00:00:00Z",
    "gender": "MALE",
    "admissionDate": "2024-01-20T00:00:00Z",
    "bloodGroup": "O+",
    "address": "123 Main St, City, State",
    "status": "ACTIVE",
    "collegeId": 1
  }
}
```

**Roles:** `COLLEGE_ADMIN`, `SUPER_ADMIN`, `TEACHER`, `STUDENT`

---

### **4. Get Student Details (With Related Data)**

```http
GET /api/v1/students/{studentUuid}/details
Authorization: Bearer {accessToken}

Response:
{
  "success": true,
  "status": 200,
  "message": "Student details retrieved successfully",
  "data": {
    "uuid": "student-uuid-here",
    "name": "John Doe",
    "email": "john.doe@college.edu",
    "rollNumber": "2024-001",
    "registrationNumber": "REG-2024-001",
    "dob": "2010-05-15T00:00:00Z",
    "gender": "MALE",
    "admissionDate": "2024-01-20T00:00:00Z",
    "bloodGroup": "O+",
    "address": "123 Main St, City, State",
    "status": "ACTIVE",
    "collegeId": 1,
    "parents": [
      {
        "uuid": "parent-uuid-here",
        "name": "Jane Doe",
        "email": "jane.doe@example.com",
        "phone": "+1234567890",
        "relation": "MOTHER"
      }
    ],
    "enrollments": [
      {
        "uuid": "enrollment-uuid-here",
        "academicYearUuid": "year-uuid-here",
        "academicYearName": "2024-2025",
        "classUuid": "class-uuid-here",
        "className": "Grade 10",
        "rollNumber": "2024-001-A",
        "status": "ACTIVE"
      }
    ]
  }
}
```

**Roles:** `COLLEGE_ADMIN`, `SUPER_ADMIN`, `TEACHER`, `STUDENT`

---

### **5. List All Students**

```http
GET /api/v1/students?page=0&size=20&sortBy=name&sortDir=ASC
Authorization: Bearer {accessToken}

Response:
{
  "success": true,
  "status": 200,
  "message": "Students retrieved successfully",
  "data": {
    "content": [ ... ],
    "totalElements": 100,
    "totalPages": 5,
    "size": 20,
    "number": 0
  }
}
```

**Roles:** `COLLEGE_ADMIN`, `SUPER_ADMIN`, `TEACHER`

---

### **6. Search Students**

```http
GET /api/v1/students/search?q=john&page=0&size=20
Authorization: Bearer {accessToken}

Response:
{
  "success": true,
  "status": 200,
  "message": "Students retrieved successfully",
  "data": {
    "content": [ ... ],
    "totalElements": 5,
    "totalPages": 1,
    "size": 20,
    "number": 0
  }
}
```

Searches by name, roll number, registration number, or email.

**Roles:** `COLLEGE_ADMIN`, `SUPER_ADMIN`, `TEACHER`

---

### **7. Filter Students by Status**

```http
GET /api/v1/students/status/ACTIVE?page=0&size=20
Authorization: Bearer {accessToken}
```

**Roles:** `COLLEGE_ADMIN`, `SUPER_ADMIN`, `TEACHER`

---

### **8. Filter Students by Class**

```http
GET /api/v1/students/class/{classUuid}?page=0&size=20
Authorization: Bearer {accessToken}
```

**Roles:** `COLLEGE_ADMIN`, `SUPER_ADMIN`, `TEACHER`

---

### **9. Filter Students by Academic Year**

```http
GET /api/v1/students/academic-year/{academicYearUuid}?page=0&size=20
Authorization: Bearer {accessToken}
```

**Roles:** `COLLEGE_ADMIN`, `SUPER_ADMIN`, `TEACHER`

---

### **10. Delete Student**

```http
DELETE /api/v1/students/{studentUuid}
Authorization: Bearer {accessToken}

Response:
{
  "success": true,
  "status": 200,
  "message": "Student deleted successfully",
  "data": null
}
```

**Roles:** `COLLEGE_ADMIN`, `SUPER_ADMIN`

---

### **11. Get Student Summary Statistics**

```http
GET /api/v1/students/summary
Authorization: Bearer {accessToken}

Response:
{
  "success": true,
  "status": 200,
  "message": "Student summary retrieved successfully",
  "data": {
    "totalStudents": 500,
    "activeStudents": 480,
    "suspendedStudents": 20
  }
}
```

**Roles:** `COLLEGE_ADMIN`, `SUPER_ADMIN`

---

## Parent Management

### **1. Assign Parent to Student**

```http
POST /api/v1/students/{studentUuid}/parents
Authorization: Bearer {accessToken}
Content-Type: application/json

Request Body:
{
  "parentUuid": "parent-uuid-here",
  "relation": "FATHER"
}

Response:
{
  "success": true,
  "status": 200,
  "message": "Parent assigned successfully",
  "data": null
}
```

**Valid Relations:** `FATHER`, `MOTHER`, `GUARDIAN`, `STEPFATHER`, `STEPMOTHER`, `OTHER`

**Roles:** `COLLEGE_ADMIN`, `SUPER_ADMIN`

---

### **2. Remove Parent from Student**

```http
DELETE /api/v1/students/{studentUuid}/parents/{parentUuid}
Authorization: Bearer {accessToken}

Response:
{
  "success": true,
  "status": 200,
  "message": "Parent removed successfully",
  "data": null
}
```

**Roles:** `COLLEGE_ADMIN`, `SUPER_ADMIN`

---

## Document Management

### **1. Upload Document**

```http
POST /api/v1/documents
Authorization: Bearer {accessToken}
Content-Type: application/json

Request Body:
{
  "ownerUuid": "student-uuid-here",
  "ownerType": "STUDENT",
  "documentType": "BIRTH_CERTIFICATE",
  "fileUrl": "https://storage.example.com/files/birth-cert.pdf",
  "fileName": "birth-certificate.pdf",
  "contentType": "application/pdf",
  "fileSize": 102400
}

Response:
{
  "success": true,
  "status": 200,
  "message": "Document uploaded successfully",
  "data": {
    "uuid": "document-uuid-here",
    "ownerType": "STUDENT",
    "ownerId": "student-uuid-here",
    "ownerName": "John Doe",
    "documentType": "BIRTH_CERTIFICATE",
    "fileName": "birth-certificate.pdf",
    "fileUrl": "https://storage.example.com/files/birth-cert.pdf",
    "contentType": "application/pdf",
    "fileSize": 102400,
    "uploadedAt": "2024-01-20T10:00:00Z",
    "collegeId": 1
  }
}
```

**Owner Types:** `STUDENT`, `STAFF`, `PARENT`

**Document Types:** Varies based on `DocumentType` enum (e.g., `BIRTH_CERTIFICATE`, `MARKSHEET`, `PHOTO`, etc.)

**Roles:** `COLLEGE_ADMIN`, `SUPER_ADMIN`, `TEACHER`, `STUDENT`, `PARENT`

---

### **2. Get Document by UUID**

```http
GET /api/v1/documents/{documentUuid}
Authorization: Bearer {accessToken}
```

**Roles:** `COLLEGE_ADMIN`, `SUPER_ADMIN`, `TEACHER`, `STUDENT`, `PARENT`

---

### **3. Get Documents by Owner**

```http
GET /api/v1/documents/owner/{ownerUuid}?ownerType=STUDENT&page=0&size=20
Authorization: Bearer {accessToken}
```

**Roles:** `COLLEGE_ADMIN`, `SUPER_ADMIN`, `TEACHER`, `STUDENT`, `PARENT`

---

### **4. Get Documents by Type**

```http
GET /api/v1/documents/type/BIRTH_CERTIFICATE?page=0&size=20
Authorization: Bearer {accessToken}
```

**Roles:** `COLLEGE_ADMIN`, `SUPER_ADMIN`, `TEACHER`

---

### **5. Delete Document**

```http
DELETE /api/v1/documents/{documentUuid}
Authorization: Bearer {accessToken}
```

**Roles:** `COLLEGE_ADMIN`, `SUPER_ADMIN`

---

## Disciplinary Case Management

### **1. Create Disciplinary Case**

```http
POST /api/v1/disciplinary-cases
Authorization: Bearer {accessToken}
Content-Type: application/json

Request Body:
{
  "studentUuid": "student-uuid-here",
  "incidentDate": "2024-01-20",
  "description": "Student was involved in a fight during lunch break."
}

Response:
{
  "success": true,
  "status": 200,
  "message": "Disciplinary case created successfully",
  "data": {
    "uuid": "case-uuid-here",
    "studentUuid": "student-uuid-here",
    "studentName": "John Doe",
    "reportedByUserUuid": "user-uuid-here",
    "reportedByName": "Teacher Name",
    "incidentDate": "2024-01-20",
    "description": "Student was involved in a fight during lunch break.",
    "actionTaken": null,
    "status": "REPORTED",
    "collegeId": 1
  }
}
```

**Status Flow:** `REPORTED` → `UNDER_REVIEW` → `ACTION_TAKEN` → `CLOSED`

**Roles:** `COLLEGE_ADMIN`, `SUPER_ADMIN`, `TEACHER`

---

### **2. Update Disciplinary Case**

```http
PUT /api/v1/disciplinary-cases/{caseUuid}
Authorization: Bearer {accessToken}
Content-Type: application/json

Request Body:
{
  "status": "ACTION_TAKEN",
  "actionTaken": "Student received a warning and parent meeting scheduled.",
  "description": "Updated description if needed"
}

Response:
{
  "success": true,
  "status": 200,
  "message": "Disciplinary case updated successfully",
  "data": { ... }
}
```

**Roles:** `COLLEGE_ADMIN`, `SUPER_ADMIN`

---

### **3. Get Disciplinary Case**

```http
GET /api/v1/disciplinary-cases/{caseUuid}
Authorization: Bearer {accessToken}
```

**Roles:** `COLLEGE_ADMIN`, `SUPER_ADMIN`, `TEACHER`, `STUDENT`, `PARENT`

---

### **4. Get Disciplinary Cases by Student**

```http
GET /api/v1/disciplinary-cases/student/{studentUuid}?page=0&size=20
Authorization: Bearer {accessToken}
```

**Roles:** `COLLEGE_ADMIN`, `SUPER_ADMIN`, `TEACHER`, `STUDENT`, `PARENT`

---

### **5. Get Disciplinary Cases by Status**

```http
GET /api/v1/disciplinary-cases/status/REPORTED?page=0&size=20
Authorization: Bearer {accessToken}
```

**Roles:** `COLLEGE_ADMIN`, `SUPER_ADMIN`, `TEACHER`

---

### **6. Get Disciplinary Cases by Date Range**

```http
GET /api/v1/disciplinary-cases/date-range?startDate=2024-01-01&endDate=2024-01-31&page=0&size=20
Authorization: Bearer {accessToken}
```

**Roles:** `COLLEGE_ADMIN`, `SUPER_ADMIN`, `TEACHER`

---

## Leave Request Management

### **1. Create Leave Request**

```http
POST /api/v1/leave-requests
Authorization: Bearer {accessToken}
Content-Type: application/json

Request Body:
{
  "userUuid": "user-uuid-here",
  "ownerType": "STUDENT",
  "leaveType": "SICK",
  "startDate": "2024-02-01",
  "endDate": "2024-02-03",
  "reason": "Fever and cold"
}

Response:
{
  "success": true,
  "status": 200,
  "message": "Leave request created successfully",
  "data": {
    "uuid": "leave-request-uuid-here",
    "userUuid": "user-uuid-here",
    "userName": "John Doe",
    "ownerType": "STUDENT",
    "leaveType": "SICK",
    "startDate": "2024-02-01",
    "endDate": "2024-02-03",
    "status": "PENDING",
    "reason": "Fever and cold",
    "approvedByUserUuid": null,
    "approvedByName": null,
    "approverComment": null,
    "collegeId": 1
  }
}
```

**Owner Types:** `STUDENT`, `STAFF`

**Leave Types:** `CASUAL`, `SICK`, `ANNUAL`, `EMERGENCY`, etc.

**Status Flow:** `PENDING` → `APPROVED` / `REJECTED` / `CANCELLED`

**Roles:** `COLLEGE_ADMIN`, `SUPER_ADMIN`, `TEACHER`, `STUDENT`, `PARENT`

**Note:** Validates overlapping leaves and prevents duplicate requests for the same date range.

---

### **2. Update Leave Request (PENDING only)**

```http
PUT /api/v1/leave-requests/{leaveRequestUuid}
Authorization: Bearer {accessToken}
Content-Type: application/json

Request Body:
{
  "leaveType": "EMERGENCY",
  "startDate": "2024-02-02",
  "endDate": "2024-02-04",
  "reason": "Updated reason"
}
```

**Roles:** `COLLEGE_ADMIN`, `SUPER_ADMIN`, `TEACHER`, `STUDENT`, `PARENT`

---

### **3. Approve/Reject Leave Request**

```http
PUT /api/v1/leave-requests/{leaveRequestUuid}/approve-reject
Authorization: Bearer {accessToken}
Content-Type: application/json

Request Body:
{
  "status": "APPROVED",
  "approverComment": "Approved. Please take rest."
}

Response:
{
  "success": true,
  "status": 200,
  "message": "Leave request APPROVED successfully",
  "data": {
    "uuid": "leave-request-uuid-here",
    "status": "APPROVED",
    "approvedByUserUuid": "admin-uuid-here",
    "approvedByName": "Admin Name",
    "approverComment": "Approved. Please take rest.",
    ...
  }
}
```

**Roles:** `COLLEGE_ADMIN`, `SUPER_ADMIN`

---

### **4. Cancel Leave Request**

```http
PUT /api/v1/leave-requests/{leaveRequestUuid}/cancel
Authorization: Bearer {accessToken}
```

**Roles:** `COLLEGE_ADMIN`, `SUPER_ADMIN`, `TEACHER`, `STUDENT`, `PARENT`

**Note:** Only PENDING or APPROVED requests can be cancelled.

---

### **5. Get Leave Requests by User**

```http
GET /api/v1/leave-requests/user/{userUuid}?page=0&size=20
Authorization: Bearer {accessToken}
```

**Roles:** `COLLEGE_ADMIN`, `SUPER_ADMIN`, `TEACHER`, `STUDENT`, `PARENT`

---

### **6. Get Leave Requests by Status**

```http
GET /api/v1/leave-requests/status/PENDING?page=0&size=20
Authorization: Bearer {accessToken}
```

**Roles:** `COLLEGE_ADMIN`, `SUPER_ADMIN`, `TEACHER`

---

### **7. Get Leave Requests by Date Range**

```http
GET /api/v1/leave-requests/date-range?startDate=2024-02-01&endDate=2024-02-28&page=0&size=20
Authorization: Bearer {accessToken}
```

**Roles:** `COLLEGE_ADMIN`, `SUPER_ADMIN`, `TEACHER`

---

## Student Promotion Management

### **1. Promote Student**

```http
POST /api/v1/student-promotions
Authorization: Bearer {accessToken}
Content-Type: application/json

Request Body:
{
  "studentUuid": "student-uuid-here",
  "academicYearUuid": "academic-year-uuid-here",
  "toClassUuid": "class-uuid-here",
  "rollNumber": "2025-001",
  "enrollmentStatus": "ACTIVE",
  "remarks": "Promoted based on excellent academic performance."
}

Response:
{
  "success": true,
  "status": 200,
  "message": "Student promoted successfully",
  "data": {
    "uuid": "promotion-log-uuid-here",
    "studentUuid": "student-uuid-here",
    "studentName": "John Doe",
    "fromClassUuid": "old-class-uuid-here",
    "fromClassName": "Grade 9",
    "toClassUuid": "new-class-uuid-here",
    "toClassName": "Grade 10",
    "academicYearUuid": "academic-year-uuid-here",
    "academicYearName": "2024-2025",
    "promotedByUserUuid": "admin-uuid-here",
    "promotedByName": "Admin Name",
    "remarks": "Promoted based on excellent academic performance.",
    "collegeId": 1
  }
}
```

**What Happens:**
1. Finds current active enrollment (from class)
2. Updates old enrollment status to `PROMOTED`
3. Creates new enrollment for new academic year and class
4. Creates promotion log entry
5. Sends notifications to student and parents

**Roles:** `COLLEGE_ADMIN`, `SUPER_ADMIN`

---

### **2. Get Promotion Log**

```http
GET /api/v1/student-promotions/{promotionLogUuid}
Authorization: Bearer {accessToken}
```

**Roles:** `COLLEGE_ADMIN`, `SUPER_ADMIN`, `TEACHER`, `STUDENT`, `PARENT`

---

### **3. Get Promotion History for Student**

```http
GET /api/v1/student-promotions/student/{studentUuid}
Authorization: Bearer {accessToken}

Response:
{
  "success": true,
  "status": 200,
  "message": "Promotion history retrieved successfully",
  "data": [
    {
      "uuid": "promotion-log-uuid-1",
      "fromClassName": "Grade 9",
      "toClassName": "Grade 10",
      "academicYearName": "2024-2025",
      "promotedByName": "Admin Name",
      "remarks": "Promoted based on excellent academic performance.",
      ...
    },
    ...
  ]
}
```

**Roles:** `COLLEGE_ADMIN`, `SUPER_ADMIN`, `TEACHER`, `STUDENT`, `PARENT`

---

### **4. Get All Promotion Logs**

```http
GET /api/v1/student-promotions?page=0&size=20
Authorization: Bearer {accessToken}
```

**Roles:** `COLLEGE_ADMIN`, `SUPER_ADMIN`, `TEACHER`

---

### **5. Get Promotion Logs by Academic Year**

```http
GET /api/v1/student-promotions/academic-year/{academicYearUuid}?page=0&size=20
Authorization: Bearer {accessToken}
```

**Roles:** `COLLEGE_ADMIN`, `SUPER_ADMIN`, `TEACHER`

---

## PTM Booking Management

### **1. Create PTM Booking**

```http
POST /api/v1/ptm-bookings
Authorization: Bearer {accessToken}
Content-Type: application/json

Request Body:
{
  "slotUuid": "ptm-slot-uuid-here",
  "studentUuid": "student-uuid-here",
  "remarks": "Would like to discuss student's progress in mathematics."
}

Response:
{
  "success": true,
  "status": 200,
  "message": "PTM booking created successfully",
  "data": {
    "uuid": "booking-uuid-here",
    "slotUuid": "ptm-slot-uuid-here",
    "slotDate": "2024-02-15",
    "slotStartTime": "10:00:00",
    "slotEndTime": "10:30:00",
    "teacherUuid": "teacher-uuid-here",
    "teacherName": "Teacher Name",
    "parentUuid": "parent-uuid-here",
    "parentName": "Parent Name",
    "studentUuid": "student-uuid-here",
    "studentName": "John Doe",
    "bookedAt": "2024-02-01T10:00:00Z",
    "remarks": "Would like to discuss student's progress in mathematics.",
    "collegeId": 1
  }
}
```

**What Happens:**
1. Validates slot is active and not already booked
2. Validates current user is a parent
3. Validates parent is associated with the student
4. Creates booking
5. Sends notifications to teacher and parent

**Roles:** `COLLEGE_ADMIN`, `SUPER_ADMIN`, `PARENT`

---

### **2. Cancel PTM Booking**

```http
DELETE /api/v1/ptm-bookings/{bookingUuid}
Authorization: Bearer {accessToken}

Response:
{
  "success": true,
  "status": 200,
  "message": "PTM booking cancelled successfully",
  "data": null
}
```

**Roles:** `COLLEGE_ADMIN`, `SUPER_ADMIN`, `PARENT`

**Note:** Only the parent who made the booking or admins can cancel.

---

### **3. Get PTM Booking**

```http
GET /api/v1/ptm-bookings/{bookingUuid}
Authorization: Bearer {accessToken}
```

**Roles:** `COLLEGE_ADMIN`, `SUPER_ADMIN`, `TEACHER`, `PARENT`, `STUDENT`

---

### **4. Get PTM Bookings by Parent**

```http
GET /api/v1/ptm-bookings/parent/{parentUuid}?page=0&size=20
Authorization: Bearer {accessToken}
```

**Roles:** `COLLEGE_ADMIN`, `SUPER_ADMIN`, `PARENT`

---

### **5. Get PTM Bookings by Student**

```http
GET /api/v1/ptm-bookings/student/{studentUuid}?page=0&size=20
Authorization: Bearer {accessToken}
```

**Roles:** `COLLEGE_ADMIN`, `SUPER_ADMIN`, `TEACHER`, `PARENT`, `STUDENT`

---

### **6. Get PTM Bookings by Teacher**

```http
GET /api/v1/ptm-bookings/teacher/{teacherUuid}?page=0&size=20
Authorization: Bearer {accessToken}
```

**Roles:** `COLLEGE_ADMIN`, `SUPER_ADMIN`, `TEACHER`

---

### **7. Get PTM Bookings by Date**

```http
GET /api/v1/ptm-bookings/date/2024-02-15?page=0&size=20
Authorization: Bearer {accessToken}
```

**Roles:** `COLLEGE_ADMIN`, `SUPER_ADMIN`, `TEACHER`

---

## Enrollment Management

### **1. Create Enrollment**

```http
POST /api/v1/students/{studentUuid}/enrollments
Authorization: Bearer {accessToken}
Content-Type: application/json

Request Body:
{
  "academicYearUuid": "academic-year-uuid-here",
  "classUuid": "class-uuid-here",
  "rollNumber": "2024-001-A",
  "status": "ACTIVE"
}

Response:
{
  "success": true,
  "status": 200,
  "message": "Enrollment created successfully",
  "data": null
}
```

**Roles:** `COLLEGE_ADMIN`, `SUPER_ADMIN`

---

### **2. Update Enrollment Status**

```http
PUT /api/v1/students/{studentUuid}/enrollments/{enrollmentUuid}/status?status=PROMOTED
Authorization: Bearer {accessToken}

Response:
{
  "success": true,
  "status": 200,
  "message": "Enrollment status updated successfully",
  "data": null
}
```

**Valid Statuses:** `ACTIVE`, `PROMOTED`, `DROPPED`, `COMPLETED`

**Roles:** `COLLEGE_ADMIN`, `SUPER_ADMIN`

---

## Complete API Endpoints Reference

### **Student Endpoints**

| Method | Endpoint | Description | Roles |
|--------|----------|-------------|-------|
| POST | `/api/v1/students` | Create student | COLLEGE_ADMIN, SUPER_ADMIN |
| PUT | `/api/v1/students/{uuid}` | Update student | COLLEGE_ADMIN, SUPER_ADMIN |
| GET | `/api/v1/students/{uuid}` | Get student | COLLEGE_ADMIN, SUPER_ADMIN, TEACHER, STUDENT |
| GET | `/api/v1/students/{uuid}/details` | Get student details (with parents, enrollments) | COLLEGE_ADMIN, SUPER_ADMIN, TEACHER, STUDENT |
| GET | `/api/v1/students` | List all students | COLLEGE_ADMIN, SUPER_ADMIN, TEACHER |
| GET | `/api/v1/students/search?q={term}` | Search students | COLLEGE_ADMIN, SUPER_ADMIN, TEACHER |
| GET | `/api/v1/students/status/{status}` | Filter by status | COLLEGE_ADMIN, SUPER_ADMIN, TEACHER |
| GET | `/api/v1/students/class/{classUuid}` | Filter by class | COLLEGE_ADMIN, SUPER_ADMIN, TEACHER |
| GET | `/api/v1/students/academic-year/{academicYearUuid}` | Filter by academic year | COLLEGE_ADMIN, SUPER_ADMIN, TEACHER |
| DELETE | `/api/v1/students/{uuid}` | Delete student | COLLEGE_ADMIN, SUPER_ADMIN |
| GET | `/api/v1/students/summary` | Get statistics | COLLEGE_ADMIN, SUPER_ADMIN |
| POST | `/api/v1/students/{uuid}/enrollments` | Create enrollment | COLLEGE_ADMIN, SUPER_ADMIN |
| PUT | `/api/v1/students/{uuid}/enrollments/{enrollmentUuid}/status` | Update enrollment status | COLLEGE_ADMIN, SUPER_ADMIN |
| POST | `/api/v1/students/{uuid}/parents` | Assign parent | COLLEGE_ADMIN, SUPER_ADMIN |
| DELETE | `/api/v1/students/{uuid}/parents/{parentUuid}` | Remove parent | COLLEGE_ADMIN, SUPER_ADMIN |

### **Admission Endpoints**

| Method | Endpoint | Description | Roles |
|--------|----------|-------------|-------|
| POST | `/api/v1/admissions` | Create admission application | COLLEGE_ADMIN, SUPER_ADMIN |
| PUT | `/api/v1/admissions/{uuid}` | Update application (DRAFT only) | COLLEGE_ADMIN, SUPER_ADMIN |
| GET | `/api/v1/admissions/{uuid}` | Get application details | COLLEGE_ADMIN, SUPER_ADMIN |
| GET | `/api/v1/admissions` | List all applications | COLLEGE_ADMIN, SUPER_ADMIN |
| GET | `/api/v1/admissions/search?q={term}` | Search applications | COLLEGE_ADMIN, SUPER_ADMIN |
| GET | `/api/v1/admissions/status/{status}` | Filter by status | COLLEGE_ADMIN, SUPER_ADMIN |
| GET | `/api/v1/admissions/class/{classUuid}` | Filter by class | COLLEGE_ADMIN, SUPER_ADMIN |
| POST | `/api/v1/admissions/{uuid}/submit` | Submit application | COLLEGE_ADMIN, SUPER_ADMIN |
| POST | `/api/v1/admissions/{uuid}/verify` | Verify application | COLLEGE_ADMIN, SUPER_ADMIN |
| POST | `/api/v1/admissions/{uuid}/approve` | Approve & create student | COLLEGE_ADMIN, SUPER_ADMIN |
| POST | `/api/v1/admissions/{uuid}/reject` | Reject application | COLLEGE_ADMIN, SUPER_ADMIN |
| DELETE | `/api/v1/admissions/{uuid}` | Delete application (DRAFT only) | COLLEGE_ADMIN, SUPER_ADMIN |
| GET | `/api/v1/admissions/summary` | Get statistics | COLLEGE_ADMIN, SUPER_ADMIN |

### **Document Endpoints**

| Method | Endpoint | Description | Roles |
|--------|----------|-------------|-------|
| POST | `/api/v1/documents` | Upload document | COLLEGE_ADMIN, SUPER_ADMIN, TEACHER, STUDENT, PARENT |
| GET | `/api/v1/documents/{uuid}` | Get document | COLLEGE_ADMIN, SUPER_ADMIN, TEACHER, STUDENT, PARENT |
| GET | `/api/v1/documents` | List all documents | COLLEGE_ADMIN, SUPER_ADMIN, TEACHER |
| GET | `/api/v1/documents/owner/{ownerUuid}?ownerType={type}` | Get documents by owner | COLLEGE_ADMIN, SUPER_ADMIN, TEACHER, STUDENT, PARENT |
| GET | `/api/v1/documents/type/{documentType}` | Get documents by type | COLLEGE_ADMIN, SUPER_ADMIN, TEACHER |
| DELETE | `/api/v1/documents/{uuid}` | Delete document | COLLEGE_ADMIN, SUPER_ADMIN |

### **Disciplinary Case Endpoints**

| Method | Endpoint | Description | Roles |
|--------|----------|-------------|-------|
| POST | `/api/v1/disciplinary-cases` | Create disciplinary case | COLLEGE_ADMIN, SUPER_ADMIN, TEACHER |
| PUT | `/api/v1/disciplinary-cases/{uuid}` | Update case | COLLEGE_ADMIN, SUPER_ADMIN |
| GET | `/api/v1/disciplinary-cases/{uuid}` | Get case | COLLEGE_ADMIN, SUPER_ADMIN, TEACHER, STUDENT, PARENT |
| GET | `/api/v1/disciplinary-cases` | List all cases | COLLEGE_ADMIN, SUPER_ADMIN, TEACHER |
| GET | `/api/v1/disciplinary-cases/student/{studentUuid}` | Get cases by student | COLLEGE_ADMIN, SUPER_ADMIN, TEACHER, STUDENT, PARENT |
| GET | `/api/v1/disciplinary-cases/status/{status}` | Filter by status | COLLEGE_ADMIN, SUPER_ADMIN, TEACHER |
| GET | `/api/v1/disciplinary-cases/student/{studentUuid}/status/{status}` | Filter by student and status | COLLEGE_ADMIN, SUPER_ADMIN, TEACHER, STUDENT, PARENT |
| GET | `/api/v1/disciplinary-cases/date-range?startDate={date}&endDate={date}` | Filter by date range | COLLEGE_ADMIN, SUPER_ADMIN, TEACHER |

### **Leave Request Endpoints**

| Method | Endpoint | Description | Roles |
|--------|----------|-------------|-------|
| POST | `/api/v1/leave-requests` | Create leave request | COLLEGE_ADMIN, SUPER_ADMIN, TEACHER, STUDENT, PARENT |
| PUT | `/api/v1/leave-requests/{uuid}` | Update request (PENDING only) | COLLEGE_ADMIN, SUPER_ADMIN, TEACHER, STUDENT, PARENT |
| PUT | `/api/v1/leave-requests/{uuid}/approve-reject` | Approve/reject request | COLLEGE_ADMIN, SUPER_ADMIN |
| PUT | `/api/v1/leave-requests/{uuid}/cancel` | Cancel request | COLLEGE_ADMIN, SUPER_ADMIN, TEACHER, STUDENT, PARENT |
| GET | `/api/v1/leave-requests/{uuid}` | Get request | COLLEGE_ADMIN, SUPER_ADMIN, TEACHER, STUDENT, PARENT |
| GET | `/api/v1/leave-requests` | List all requests | COLLEGE_ADMIN, SUPER_ADMIN, TEACHER |
| GET | `/api/v1/leave-requests/user/{userUuid}` | Get requests by user | COLLEGE_ADMIN, SUPER_ADMIN, TEACHER, STUDENT, PARENT |
| GET | `/api/v1/leave-requests/owner-type/{ownerType}` | Filter by owner type | COLLEGE_ADMIN, SUPER_ADMIN, TEACHER |
| GET | `/api/v1/leave-requests/status/{status}` | Filter by status | COLLEGE_ADMIN, SUPER_ADMIN, TEACHER |
| GET | `/api/v1/leave-requests/user/{userUuid}/status/{status}` | Filter by user and status | COLLEGE_ADMIN, SUPER_ADMIN, TEACHER, STUDENT, PARENT |
| GET | `/api/v1/leave-requests/date-range?startDate={date}&endDate={date}` | Filter by date range | COLLEGE_ADMIN, SUPER_ADMIN, TEACHER |
| GET | `/api/v1/leave-requests/leave-type/{leaveType}` | Filter by leave type | COLLEGE_ADMIN, SUPER_ADMIN, TEACHER |

### **Student Promotion Endpoints**

| Method | Endpoint | Description | Roles |
|--------|----------|-------------|-------|
| POST | `/api/v1/student-promotions` | Promote student | COLLEGE_ADMIN, SUPER_ADMIN |
| GET | `/api/v1/student-promotions/{uuid}` | Get promotion log | COLLEGE_ADMIN, SUPER_ADMIN, TEACHER, STUDENT, PARENT |
| GET | `/api/v1/student-promotions` | List all promotion logs | COLLEGE_ADMIN, SUPER_ADMIN, TEACHER |
| GET | `/api/v1/student-promotions/student/{studentUuid}` | Get promotion history | COLLEGE_ADMIN, SUPER_ADMIN, TEACHER, STUDENT, PARENT |
| GET | `/api/v1/student-promotions/academic-year/{academicYearUuid}` | Filter by academic year | COLLEGE_ADMIN, SUPER_ADMIN, TEACHER |

### **PTM Booking Endpoints**

| Method | Endpoint | Description | Roles |
|--------|----------|-------------|-------|
| POST | `/api/v1/ptm-bookings` | Create PTM booking | COLLEGE_ADMIN, SUPER_ADMIN, PARENT |
| DELETE | `/api/v1/ptm-bookings/{uuid}` | Cancel booking | COLLEGE_ADMIN, SUPER_ADMIN, PARENT |
| GET | `/api/v1/ptm-bookings/{uuid}` | Get booking | COLLEGE_ADMIN, SUPER_ADMIN, TEACHER, PARENT, STUDENT |
| GET | `/api/v1/ptm-bookings` | List all bookings | COLLEGE_ADMIN, SUPER_ADMIN, TEACHER |
| GET | `/api/v1/ptm-bookings/parent/{parentUuid}` | Get bookings by parent | COLLEGE_ADMIN, SUPER_ADMIN, PARENT |
| GET | `/api/v1/ptm-bookings/student/{studentUuid}` | Get bookings by student | COLLEGE_ADMIN, SUPER_ADMIN, TEACHER, PARENT, STUDENT |
| GET | `/api/v1/ptm-bookings/teacher/{teacherUuid}` | Get bookings by teacher | COLLEGE_ADMIN, SUPER_ADMIN, TEACHER |
| GET | `/api/v1/ptm-bookings/date/{date}` | Filter by date | COLLEGE_ADMIN, SUPER_ADMIN, TEACHER |

---

## Status Flows

### **Admission Status Flow**
```
DRAFT → SUBMITTED → VERIFIED → APPROVED
                         │
                         └──→ REJECTED
```

### **Enrollment Status Flow**
```
ACTIVE → PROMOTED / DROPPED / COMPLETED
```

### **Disciplinary Case Status Flow**
```
REPORTED → UNDER_REVIEW → ACTION_TAKEN → CLOSED
```

### **Leave Request Status Flow**
```
PENDING → APPROVED / REJECTED / CANCELLED
```

---

## Error Handling

### **Common HTTP Status Codes**

- **200 OK:** Success
- **201 Created:** Resource created successfully
- **400 Bad Request:** Invalid input data or business rule violation
- **401 Unauthorized:** Missing or invalid authentication token
- **403 Forbidden:** Insufficient permissions or wrong college (tenant isolation)
- **404 Not Found:** Resource not found
- **409 Conflict:** Duplicate resource (email, roll number, etc.)

### **Error Response Format**

```json
{
  "success": false,
  "status": 409,
  "message": "Student with email already exists",
  "data": null,
  "timestamp": "2024-01-20T10:00:00Z"
}
```

---

## Security & Isolation

- ✅ **College Isolation:** All queries filter by `collegeId` (tenant isolation via `TenantAccessGuard`)
- ✅ **Role-Based Access Control:** Endpoints protected with `@PreAuthorize` annotations
- ✅ **JWT Authentication:** All protected endpoints require valid JWT token
- ✅ **Tenant Context:** Automatically set from authenticated user's college
- ✅ **Status Guards:** Business rules enforce valid status transitions
- ✅ **Data Validation:** Request validation using Jakarta Validation annotations

---

## Integration Notes

### **Notification Integration**

All modules integrate with the notification service to send alerts for:
- Disciplinary cases (to students and parents)
- Leave request status changes
- Student promotions
- PTM bookings

### **College Isolation**

All operations automatically enforce college isolation through:
- `TenantAccessGuard.getCurrentTenantId()` - Gets current college ID
- Repository queries filter by `collegeId`
- Service layer validates tenant access

### **Related Modules**

Student management integrates with:
- **Fees Module:** Student fees are linked to students
- **Attendance Module:** Attendance records are linked to students
- **Exam Module:** Student marks and transcripts are linked to students
- **Library Module:** Book issues are linked to students
- **Transport Module:** Transport allocations are linked to students
- **Hostel Module:** Hostel allocations are linked to students

---

## Frontend Integration Recommendations

### **Recommended State Structure**

```javascript
// Student State
{
  students: {
    items: [],
    filters: {
      status: 'ALL',
      classUuid: null,
      academicYearUuid: null,
      searchTerm: ''
    },
    pagination: {
      page: 0,
      size: 20,
      total: 0
    },
    loading: false,
    error: null
  },
  currentStudent: {
    data: null,
    enrollments: [],
    parents: [],
    documents: [],
    disciplinaryCases: [],
    leaveRequests: [],
    promotionHistory: [],
    ptmBookings: [],
    loading: false,
    error: null
  }
}
```

---

## Future Enhancements

1. **Bulk Operations:**
   - Bulk student creation
   - Bulk enrollment creation
   - Bulk promotion

2. **Advanced Reporting:**
   - Student progression reports
   - Disciplinary statistics
   - Attendance summaries
   - Leave request analytics

3. **Email Notifications:**
   - Email alerts for disciplinary cases
   - Email confirmations for leave requests
   - Email notifications for promotions

4. **Document Management:**
   - Direct file upload integration
   - Document verification workflow
   - Document expiry tracking

5. **Mobile App Support:**
   - Student mobile app
   - Parent mobile app
   - Real-time notifications

---

**Document Version:** 1.0  
**Last Updated:** After complete implementation of all student-related modules  
**Maintained By:** Development Team

