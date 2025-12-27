# Complete Transport Management Flow Documentation

## Overview
This document describes the complete API flow for Transport Management in the college management system, including transport manager user management, transport route management, student transport allocation, and comprehensive transport reporting for different roles (COLLEGE_ADMIN, TRANSPORT_MANAGER, TEACHER, STUDENT).

---

## User Roles & Permissions

| Role | Transport Manager Management | Route Management | Allocation Management | View Routes | View Allocations | View Summary |
|------|----------------------------|------------------|---------------------|-------------|------------------|--------------|
| **SUPER_ADMIN** | ✅ Full Access | ✅ Full Access | ✅ Full Access | ✅ Yes | ✅ Yes | ✅ Yes |
| **COLLEGE_ADMIN** | ✅ Full Access | ✅ Full Access | ✅ Full Access | ✅ Yes | ✅ Yes | ✅ Yes |
| **TRANSPORT_MANAGER** | ❌ No (View own only) | ✅ Full Access | ✅ Full Access | ✅ Yes | ✅ Yes | ✅ Yes |
| **TEACHER** | ❌ No | ❌ No | ❌ No | ✅ Yes | ✅ Yes (Read-only) | ❌ No |
| **STUDENT** | ❌ No | ❌ No | ❌ No | ✅ Yes | ✅ Yes (Own only) | ❌ No |

---

## Flow Diagram: Complete Transport Management Lifecycle

```
┌─────────────────────────────────────────────────────────────────┐
│         TRANSPORT SETUP (COLLEGE_ADMIN)                          │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│  Step 1: POST /api/v1/transport-managers                       │
│  - Create transport manager accounts                            │
│  - Assign ROLE_TRANSPORT_MANAGER                                │
│  - Setup staff profiles                                         │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│  Step 2: POST /api/v1/transport/routes                         │
│  - Create transport routes                                      │
│  - Define route name, vehicle number, driver                    │
│  - Routes are college-specific                                  │
│  - Can be done by ADMIN or TRANSPORT_MANAGER                   │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│              ROUTE DISCOVERY (ALL USERS)                          │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│  Step 3: GET /api/v1/transport/routes                          │
│  - Browse all routes                                            │
│  - Search by route name/vehicle/driver                          │
│  - View route details                                           │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│    ALLOCATE TRANSPORT (ADMIN/TRANSPORT_MANAGER)                 │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│  Step 4: POST /api/v1/transport/allocations                    │
│  - Allocate route to student                                    │
│  - Set pickup point                                             │
│  - Auto-release previous active allocations                     │
│  - Status: Active (releasedAt = null)                           │
│  - Transport Manager typically handles this                     │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│              ACTIVE TRANSPORT PERIOD                              │
│  - Student has active transport allocation                      │
│  - Can view their transport details                            │
│  - System tracks allocation history                            │
│  - Transport Manager can monitor all allocations                │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│    RELEASE ALLOCATION (ADMIN/TRANSPORT_MANAGER)                  │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│  Step 5: PUT /api/v1/transport/allocations/{uuid}/release      │
│  - Release transport allocation                                 │
│  - Set releasedAt timestamp                                     │
│  - Status: Active → Inactive                                    │
│  - Transport Manager handles releases                           │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│      MONITORING & STATISTICS (ADMIN/TRANSPORT_MANAGER)           │
│  - View active allocations                                      │
│  - Track transport usage                                        │
│  - Transport summary statistics                                 │
│  - Manage transport manager accounts                            │
└─────────────────────────────────────────────────────────────────┘
```

---

## Detailed API Flows by User Role

### **Role 0: COLLEGE_ADMIN - Transport Manager & Route Setup**

#### **Flow 0.1: Create Transport Manager Account**

**Step 1: Create Transport Manager**
```http
POST /api/v1/transport-managers
Authorization: Bearer {adminAccessToken}
Content-Type: application/json

Request Body:
{
  "name": "John Smith",
  "email": "john.smith@college.edu",
  "password": "SecurePass123!",
  "designation": "Transport Manager",
  "salary": 50000.00,
  "joiningDate": "2024-01-01",
  "phone": "+1234567890",
  "address": "123 Transport St, City"
}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Transport manager created successfully",
  "data": {
    "uuid": "transport-manager-uuid-123",
    "name": "John Smith",
    "email": "john.smith@college.edu",
    "designation": "Transport Manager",
    "salary": 50000.00,
    "joiningDate": "2024-01-01",
    "phone": "+1234567890",
    "address": "123 Transport St, City",
    "status": "ACTIVE",
    "emailVerified": false,
    "collegeId": 1,
    "createdAt": "2024-01-15T10:00:00",
    "updatedAt": "2024-01-15T10:00:00"
  }
}
```

**What Happens:**
- ✅ User account created with `ROLE_TRANSPORT_MANAGER`
- ✅ Staff profile created with transport manager details
- ✅ Transport manager can now access transport management features
- ✅ Email validated for uniqueness within college

---

**Step 2: View All Transport Managers**
```http
GET /api/v1/transport-managers?page=0&size=20&sortBy=name&direction=ASC
Authorization: Bearer {adminAccessToken}

Response:
{
  "success": true,
  "status": 200,
  "message": "Transport managers retrieved successfully",
  "data": {
    "content": [
      {
        "uuid": "transport-manager-uuid-123",
        "name": "John Smith",
        "email": "john.smith@college.edu",
        "designation": "Transport Manager",
        "status": "ACTIVE"
      },
      ...
    ],
    "totalElements": 2,
    "totalPages": 1,
    "size": 20,
    "number": 0
  }
}
```

---

**Step 3: Search Transport Managers**
```http
GET /api/v1/transport-managers/search?q=john&page=0&size=20
Authorization: Bearer {adminAccessToken}
```

---

**Step 4: Update Transport Manager Information**
```http
PUT /api/v1/transport-managers/{transportManagerUuid}
Authorization: Bearer {adminAccessToken}
Content-Type: application/json

Request Body:
{
  "name": "John Smith",
  "email": "john.smith.updated@college.edu",
  "designation": "Senior Transport Manager",
  "salary": 55000.00,
  "phone": "+1234567891"
}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Transport manager updated successfully",
  "data": {
    "uuid": "transport-manager-uuid-123",
    "name": "John Smith",
    "email": "john.smith.updated@college.edu",
    "designation": "Senior Transport Manager",
    "salary": 55000.00,
    ...
    "updatedAt": "2024-01-20T10:00:00"
  }
}
```

---

**Step 5: Delete Transport Manager**
```http
DELETE /api/v1/transport-managers/{transportManagerUuid}
Authorization: Bearer {adminAccessToken}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Transport manager deleted successfully",
  "data": null
}
```

---

### **Role 1: COLLEGE_ADMIN / TRANSPORT_MANAGER - Route Management**

#### **Flow 1.1: Create Transport Route**

**Step 1: Create Transport Route**
```http
POST /api/v1/transport/routes
Authorization: Bearer {adminAccessToken | transportManagerAccessToken}
Content-Type: application/json

Request Body:
{
  "routeName": "Route A - City Center",
  "vehicleNo": "MH-12-AB-1234",
  "driverName": "Rajesh Kumar"
}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Transport route created successfully",
  "data": {
    "uuid": "route-uuid-456",
    "routeName": "Route A - City Center",
    "vehicleNo": "MH-12-AB-1234",
    "driverName": "Rajesh Kumar",
    "collegeId": 1,
    "createdAt": "2024-01-15T08:00:00",
    "updatedAt": "2024-01-15T08:00:00"
  }
}
```

**What Happens:**
- ✅ Transport route created for the college
- ✅ Route name must be unique within college
- ✅ Validates college context
- ✅ Accessible by COLLEGE_ADMIN, SUPER_ADMIN, and TRANSPORT_MANAGER roles

**Error Response (409 Conflict):**
```json
{
  "success": false,
  "status": 409,
  "message": "Transport route with name 'Route A - City Center' already exists in this college"
}
```

---

#### **Flow 1.2: Update Transport Route**

**Update Route Information**
```http
PUT /api/v1/transport/routes/{routeUuid}
Authorization: Bearer {adminAccessToken | transportManagerAccessToken}
Content-Type: application/json

Request Body:
{
  "routeName": "Route A - City Center (Updated)",
  "vehicleNo": "MH-12-AB-5678",
  "driverName": "Amit Sharma"
}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Transport route updated successfully",
  "data": {
    "uuid": "route-uuid-456",
    "routeName": "Route A - City Center (Updated)",
    "vehicleNo": "MH-12-AB-5678",
    "driverName": "Amit Sharma",
    "updatedAt": "2024-01-20T10:00:00"
  }
}
```

**What Happens:**
- ✅ Updates route information
- ✅ Validates route name uniqueness (if changed)
- ✅ Updates vehicle and driver information

---

#### **Flow 1.3: View Transport Routes**

**Get All Routes (Paginated)**
```http
GET /api/v1/transport/routes?page=0&size=20&sortBy=routeName&direction=ASC
Authorization: Bearer {adminAccessToken | transportManagerAccessToken | teacherAccessToken | studentAccessToken}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Transport routes retrieved successfully",
  "data": {
    "content": [
      {
        "uuid": "route-uuid-456",
        "routeName": "Route A - City Center",
        "vehicleNo": "MH-12-AB-1234",
        "driverName": "Rajesh Kumar",
        "collegeId": 1,
        "createdAt": "2024-01-15T08:00:00",
        "updatedAt": "2024-01-15T08:00:00"
      },
      {
        "uuid": "route-uuid-457",
        "routeName": "Route B - Suburban Area",
        "vehicleNo": "MH-12-CD-5678",
        "driverName": "Vikram Singh",
        ...
      },
      ...
    ],
    "totalElements": 10,
    "totalPages": 1,
    "size": 20,
    "number": 0
  }
}
```

---

**Get All Routes (List - No Pagination)**
```http
GET /api/v1/transport/routes/all
Authorization: Bearer {adminAccessToken | transportManagerAccessToken | teacherAccessToken | studentAccessToken}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Transport routes retrieved successfully",
  "data": [
    {
      "uuid": "route-uuid-456",
      "routeName": "Route A - City Center",
      "vehicleNo": "MH-12-AB-1234",
      "driverName": "Rajesh Kumar",
      ...
    },
    ...
  ]
}
```

---

**Get Route by UUID**
```http
GET /api/v1/transport/routes/{routeUuid}
Authorization: Bearer {adminAccessToken | transportManagerAccessToken | teacherAccessToken | studentAccessToken}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Transport route retrieved successfully",
  "data": {
    "uuid": "route-uuid-456",
    "routeName": "Route A - City Center",
    "vehicleNo": "MH-12-AB-1234",
    "driverName": "Rajesh Kumar",
    "collegeId": 1,
    "createdAt": "2024-01-15T08:00:00",
    "updatedAt": "2024-01-15T08:00:00"
  }
}
```

---

**Search Routes**
```http
GET /api/v1/transport/routes/search?q=city&page=0&size=20
Authorization: Bearer {adminAccessToken | transportManagerAccessToken}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Search results retrieved successfully",
  "data": {
    "content": [
      {
        "uuid": "route-uuid-456",
        "routeName": "Route A - City Center",
        ...
      },
      ...
    ],
    "totalElements": 2
  }
}
```

---

**Delete Route**
```http
DELETE /api/v1/transport/routes/{routeUuid}
Authorization: Bearer {adminAccessToken | transportManagerAccessToken}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Transport route deleted successfully",
  "data": null
}
```

---

### **Role 2: COLLEGE_ADMIN / TRANSPORT_MANAGER - Transport Allocation Management**

#### **Flow 2.1: Allocate Transport to Student**

**Step 1: Create Transport Allocation**
```http
POST /api/v1/transport/allocations
Authorization: Bearer {adminAccessToken | transportManagerAccessToken}
Content-Type: application/json

Request Body:
{
  "studentUuid": "student-uuid-001",
  "routeUuid": "route-uuid-456",
  "pickupPoint": "Main Street Bus Stop",
  "allocatedAt": "2024-01-15T08:00:00"  // Optional, defaults to current time
}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Transport allocation created successfully",
  "data": {
    "uuid": "allocation-uuid-789",
    "studentUuid": "student-uuid-001",
    "studentName": "Alice Johnson",
    "rollNumber": "10A001",
    "routeUuid": "route-uuid-456",
    "routeName": "Route A - City Center",
    "vehicleNo": "MH-12-AB-1234",
    "driverName": "Rajesh Kumar",
    "pickupPoint": "Main Street Bus Stop",
    "allocatedAt": "2024-01-15T08:00:00",
    "releasedAt": null,
    "isActive": true,
    "createdAt": "2024-01-15T08:00:00",
    "updatedAt": "2024-01-15T08:00:00"
  }
}
```

**What Happens:**
- ✅ Creates transport allocation linking student to route
- ✅ Sets pickup point for the student
- ✅ **Automatically releases any existing active allocations** for the student
- ✅ Initial status: Active (releasedAt = null)
- ✅ Validates student and route exist and belong to college
- ✅ Prevents duplicate active allocations for same student-route combination

**Error Response (409 Conflict):**
```json
{
  "success": false,
  "status": 409,
  "message": "Student already has an active transport allocation for this route"
}
```

**Error Response (404 Not Found):**
```json
{
  "success": false,
  "status": 404,
  "message": "Student not found with UUID: student-uuid-001"
}
```

---

#### **Flow 2.2: Update Transport Allocation**

**Update Allocation Information**
```http
PUT /api/v1/transport/allocations/{allocationUuid}
Authorization: Bearer {adminAccessToken | transportManagerAccessToken}
Content-Type: application/json

Request Body:
{
  "routeUuid": "route-uuid-457",  // Change route
  "pickupPoint": "Park Avenue Bus Stop",  // Update pickup point
  "releasedAt": null  // Keep active, or set timestamp to release
}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Transport allocation updated successfully",
  "data": {
    "uuid": "allocation-uuid-789",
    "studentUuid": "student-uuid-001",
    "studentName": "Alice Johnson",
    "routeUuid": "route-uuid-457",
    "routeName": "Route B - Suburban Area",
    "pickupPoint": "Park Avenue Bus Stop",
    "isActive": true,
    ...
    "updatedAt": "2024-01-20T10:00:00"
  }
}
```

**What Happens:**
- ✅ Updates route if provided (validates new route exists)
- ✅ Updates pickup point if provided
- ✅ Can set releasedAt to deactivate allocation
- ✅ Validates no duplicate active allocations for new route

---

#### **Flow 2.3: Release Transport Allocation**

**Release/Deactivate Allocation**
```http
PUT /api/v1/transport/allocations/{allocationUuid}/release
Authorization: Bearer {adminAccessToken | transportManagerAccessToken}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Transport allocation released successfully",
  "data": {
    "uuid": "allocation-uuid-789",
    "studentUuid": "student-uuid-001",
    "studentName": "Alice Johnson",
    "routeUuid": "route-uuid-456",
    "routeName": "Route A - City Center",
    "pickupPoint": "Main Street Bus Stop",
    "allocatedAt": "2024-01-15T08:00:00",
    "releasedAt": "2024-01-25T10:00:00",  // Set to current time
    "isActive": false,
    "updatedAt": "2024-01-25T10:00:00"
  }
}
```

**What Happens:**
- ✅ Sets releasedAt timestamp to current time
- ✅ Changes isActive to false
- ✅ Allocation becomes inactive but remains in history
- ✅ Student can be allocated to a new route after release

**Error Response (409 Conflict):**
```json
{
  "success": false,
  "status": 409,
  "message": "Transport allocation is already released"
}
```

---

#### **Flow 2.4: View Transport Allocations**

**Get All Allocations (Paginated)**
```http
GET /api/v1/transport/allocations?page=0&size=20&sortBy=allocatedAt&direction=DESC
Authorization: Bearer {adminAccessToken | transportManagerAccessToken | teacherAccessToken}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Transport allocations retrieved successfully",
  "data": {
    "content": [
      {
        "uuid": "allocation-uuid-789",
        "studentUuid": "student-uuid-001",
        "studentName": "Alice Johnson",
        "rollNumber": "10A001",
        "routeUuid": "route-uuid-456",
        "routeName": "Route A - City Center",
        "vehicleNo": "MH-12-AB-1234",
        "driverName": "Rajesh Kumar",
        "pickupPoint": "Main Street Bus Stop",
        "allocatedAt": "2024-01-15T08:00:00",
        "releasedAt": null,
        "isActive": true,
        ...
      },
      ...
    ],
    "totalElements": 50,
    "totalPages": 3
  }
}
```

---

**Get Active Allocations Only**
```http
GET /api/v1/transport/allocations/active?page=0&size=20&sortBy=allocatedAt&direction=DESC
Authorization: Bearer {adminAccessToken | transportManagerAccessToken | teacherAccessToken | studentAccessToken}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Active transport allocations retrieved successfully",
  "data": {
    "content": [
      {
        "uuid": "allocation-uuid-789",
        "studentUuid": "student-uuid-001",
        "studentName": "Alice Johnson",
        "isActive": true,
        "releasedAt": null,
        ...
      },
      ...
    ],
    "totalElements": 35
  }
}
```

---

**Get Allocations by Student**
```http
GET /api/v1/transport/allocations/students/{studentUuid}
Authorization: Bearer {adminAccessToken | transportManagerAccessToken | teacherAccessToken | studentAccessToken}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Transport allocations retrieved successfully",
  "data": [
    {
      "uuid": "allocation-uuid-789",
      "studentUuid": "student-uuid-001",
      "studentName": "Alice Johnson",
      "routeName": "Route A - City Center",
      "pickupPoint": "Main Street Bus Stop",
      "allocatedAt": "2024-01-15T08:00:00",
      "releasedAt": null,
      "isActive": true,
      ...
    },
    {
      "uuid": "allocation-uuid-790",
      "routeName": "Route B - Suburban Area",
      "allocatedAt": "2023-12-01T08:00:00",
      "releasedAt": "2024-01-14T10:00:00",
      "isActive": false,
      ...
    }
  ]
}
```

---

**Get Active Allocation by Student**
```http
GET /api/v1/transport/allocations/students/{studentUuid}/active
Authorization: Bearer {adminAccessToken | transportManagerAccessToken | teacherAccessToken | studentAccessToken}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Active transport allocation retrieved successfully",
  "data": {
    "uuid": "allocation-uuid-789",
    "studentUuid": "student-uuid-001",
    "studentName": "Alice Johnson",
    "rollNumber": "10A001",
    "routeUuid": "route-uuid-456",
    "routeName": "Route A - City Center",
    "vehicleNo": "MH-12-AB-1234",
    "driverName": "Rajesh Kumar",
    "pickupPoint": "Main Street Bus Stop",
    "allocatedAt": "2024-01-15T08:00:00",
    "releasedAt": null,
    "isActive": true,
    ...
  }
}
```

**Error Response (404 Not Found):**
```json
{
  "success": false,
  "status": 404,
  "message": "No active transport allocation found for student with UUID: student-uuid-001"
}
```

---

**Get Allocations by Route**
```http
GET /api/v1/transport/allocations/routes/{routeUuid}?page=0&size=20
Authorization: Bearer {adminAccessToken | transportManagerAccessToken | teacherAccessToken}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Transport allocations retrieved successfully",
  "data": {
    "content": [
      {
        "uuid": "allocation-uuid-789",
        "studentUuid": "student-uuid-001",
        "studentName": "Alice Johnson",
        "rollNumber": "10A001",
        "routeUuid": "route-uuid-456",
        "routeName": "Route A - City Center",
        "pickupPoint": "Main Street Bus Stop",
        "isActive": true,
        ...
      },
      ...
    ],
    "totalElements": 25
  }
}
```

---

**Get Active Allocations by Route**
```http
GET /api/v1/transport/allocations/routes/{routeUuid}/active
Authorization: Bearer {adminAccessToken | transportManagerAccessToken | teacherAccessToken}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Active transport allocations retrieved successfully",
  "data": [
    {
      "uuid": "allocation-uuid-789",
      "studentUuid": "student-uuid-001",
      "studentName": "Alice Johnson",
      "rollNumber": "10A001",
      "pickupPoint": "Main Street Bus Stop",
      "isActive": true,
      ...
    },
    ...
  ]
}
```

---

**Search Allocations**
```http
GET /api/v1/transport/allocations/search?q=alice&page=0&size=20
Authorization: Bearer {adminAccessToken | transportManagerAccessToken}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Search results retrieved successfully",
  "data": {
    "content": [
      {
        "uuid": "allocation-uuid-789",
        "studentName": "Alice Johnson",
        "rollNumber": "10A001",
        "routeName": "Route A - City Center",
        ...
      },
      ...
    ],
    "totalElements": 3
  }
}
```

---

**Get Allocation by UUID**
```http
GET /api/v1/transport/allocations/{allocationUuid}
Authorization: Bearer {adminAccessToken | transportManagerAccessToken | teacherAccessToken | studentAccessToken}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Transport allocation retrieved successfully",
  "data": {
    "uuid": "allocation-uuid-789",
    "studentUuid": "student-uuid-001",
    "studentName": "Alice Johnson",
    "rollNumber": "10A001",
    "routeUuid": "route-uuid-456",
    "routeName": "Route A - City Center",
    "vehicleNo": "MH-12-AB-1234",
    "driverName": "Rajesh Kumar",
    "pickupPoint": "Main Street Bus Stop",
    "allocatedAt": "2024-01-15T08:00:00",
    "releasedAt": null,
    "isActive": true,
    "createdAt": "2024-01-15T08:00:00",
    "updatedAt": "2024-01-15T08:00:00"
  }
}
```

---

**Delete Allocation**
```http
DELETE /api/v1/transport/allocations/{allocationUuid}
Authorization: Bearer {adminAccessToken | transportManagerAccessToken}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Transport allocation deleted successfully",
  "data": null
}
```

---

### **Role 3: STUDENT - View Own Transport**

#### **Flow 3.1: View My Transport Allocation**

**Get My Active Transport**
```http
GET /api/v1/transport/allocations/students/{studentUuid}/active
Authorization: Bearer {studentAccessToken}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Active transport allocation retrieved successfully",
  "data": {
    "uuid": "allocation-uuid-789",
    "studentUuid": "student-uuid-001",
    "studentName": "Alice Johnson",
    "rollNumber": "10A001",
    "routeUuid": "route-uuid-456",
    "routeName": "Route A - City Center",
    "vehicleNo": "MH-12-AB-1234",
    "driverName": "Rajesh Kumar",
    "pickupPoint": "Main Street Bus Stop",
    "allocatedAt": "2024-01-15T08:00:00",
    "releasedAt": null,
    "isActive": true,
    ...
  }
}
```

---

**Get My Transport History**
```http
GET /api/v1/transport/allocations/students/{studentUuid}
Authorization: Bearer {studentAccessToken}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Transport allocations retrieved successfully",
  "data": [
    {
      "uuid": "allocation-uuid-789",
      "routeName": "Route A - City Center",
      "pickupPoint": "Main Street Bus Stop",
      "allocatedAt": "2024-01-15T08:00:00",
      "releasedAt": null,
      "isActive": true,
      ...
    },
    {
      "uuid": "allocation-uuid-790",
      "routeName": "Route B - Suburban Area",
      "allocatedAt": "2023-12-01T08:00:00",
      "releasedAt": "2024-01-14T10:00:00",
      "isActive": false,
      ...
    }
  ]
}
```

---

**Browse Available Routes**
```http
GET /api/v1/transport/routes/all
Authorization: Bearer {studentAccessToken}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Transport routes retrieved successfully",
  "data": [
    {
      "uuid": "route-uuid-456",
      "routeName": "Route A - City Center",
      "vehicleNo": "MH-12-AB-1234",
      "driverName": "Rajesh Kumar",
      ...
    },
    ...
  ]
}
```

---

### **Role 4: COLLEGE_ADMIN / TRANSPORT_MANAGER - Transport Summary & Reporting**

#### **Flow 4.1: Get Transport Summary**

**Get Transport Statistics**
```http
GET /api/v1/transport/routes/summary
Authorization: Bearer {adminAccessToken | transportManagerAccessToken}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Transport summary retrieved successfully",
  "data": {
    "totalRoutes": 10,
    "totalActiveAllocations": 35,
    "totalInactiveAllocations": 15,
    "totalStudentsWithTransport": 35,
    "totalStudentsWithoutTransport": 165,
    "totalStudents": 200
  }
}
```

**What This Provides:**
- ✅ Total number of transport routes in college
- ✅ Number of active transport allocations
- ✅ Number of inactive/historical allocations
- ✅ Students currently using transport
- ✅ Students without transport
- ✅ Total student count for comparison

---

## Frontend Integration Guide

### **1. Transport Manager Management Page**

**Component Structure:**
```
TransportManagerManagement/
  ├── TransportManagerList.tsx (List view with pagination)
  ├── CreateTransportManagerModal.tsx (Create form)
  ├── UpdateTransportManagerModal.tsx (Update form)
  └── TransportManagerCard.tsx (Individual manager card)
```

**API Integration:**
```typescript
// Fetch all transport managers
const fetchTransportManagers = async (page: number, size: number) => {
  const response = await fetch(
    `/api/v1/transport-managers?page=${page}&size=${size}`,
    {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    }
  );
  return response.json();
};

// Create transport manager
const createTransportManager = async (data: CreateTransportManagerRequest) => {
  const response = await fetch('/api/v1/transport-managers', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(data)
  });
  return response.json();
};
```

---

### **2. Transport Route Management Page**

**Component Structure:**
```
TransportRouteManagement/
  ├── RouteList.tsx (List view with search)
  ├── CreateRouteModal.tsx (Create form)
  ├── UpdateRouteModal.tsx (Update form)
  ├── RouteCard.tsx (Individual route card)
  └── RouteDetails.tsx (Route details with allocations)
```

**API Integration:**
```typescript
// Fetch all routes (for dropdowns)
const fetchAllRoutes = async () => {
  const response = await fetch('/api/v1/transport/routes/all', {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
  return response.json();
};

// Create route
const createRoute = async (data: CreateTransportRouteRequest) => {
  const response = await fetch('/api/v1/transport/routes', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(data)
  });
  return response.json();
};
```

---

### **3. Transport Allocation Management Page**

**Component Structure:**
```
TransportAllocationManagement/
  ├── AllocationList.tsx (List view with filters)
  ├── CreateAllocationModal.tsx (Create form with student/route selection)
  ├── UpdateAllocationModal.tsx (Update form)
  ├── AllocationCard.tsx (Individual allocation card)
  └── StudentTransportView.tsx (Student's transport view)
```

**API Integration:**
```typescript
// Create allocation
const createAllocation = async (data: CreateTransportAllocationRequest) => {
  const response = await fetch('/api/v1/transport/allocations', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(data)
  });
  return response.json();
};

// Get active allocation for student
const getStudentActiveTransport = async (studentUuid: string) => {
  const response = await fetch(
    `/api/v1/transport/allocations/students/${studentUuid}/active`,
    {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    }
  );
  return response.json();
};

// Release allocation
const releaseAllocation = async (allocationUuid: string) => {
  const response = await fetch(
    `/api/v1/transport/allocations/${allocationUuid}/release`,
    {
      method: 'PUT',
      headers: {
        'Authorization': `Bearer ${token}`
      }
    }
  );
  return response.json();
};
```

---

### **4. Student Transport Dashboard**

**Component Structure:**
```
StudentTransportDashboard/
  ├── MyTransportCard.tsx (Current active transport)
  ├── TransportHistory.tsx (Historical allocations)
  └── AvailableRoutesList.tsx (Browse available routes)
```

**API Integration:**
```typescript
// Get student's active transport
const getMyActiveTransport = async () => {
  const response = await fetch(
    `/api/v1/transport/allocations/students/${studentUuid}/active`,
    {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    }
  );
  return response.json();
};

// Get student's transport history
const getMyTransportHistory = async () => {
  const response = await fetch(
    `/api/v1/transport/allocations/students/${studentUuid}`,
    {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    }
  );
  return response.json();
};
```

---

### **5. Transport Summary Dashboard**

**Component Structure:**
```
TransportSummaryDashboard/
  ├── SummaryCards.tsx (Statistics cards)
  ├── ActiveAllocationsChart.tsx (Visualization)
  └── RouteUtilizationChart.tsx (Route usage)
```

**API Integration:**
```typescript
// Get transport summary
const getTransportSummary = async () => {
  const response = await fetch('/api/v1/transport/routes/summary', {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
  return response.json();
};
```

---

## Common Use Cases

### **Use Case 1: Assign Transport to New Student**

**Flow:**
1. Admin/Transport Manager navigates to Transport Allocation page
2. Clicks "Allocate Transport"
3. Selects student from dropdown (or searches)
4. Selects route from dropdown
5. Enters pickup point
6. Submits form
7. System automatically releases any previous active allocation
8. New allocation created and displayed

**API Calls:**
```typescript
POST /api/v1/transport/allocations
{
  "studentUuid": "student-uuid-001",
  "routeUuid": "route-uuid-456",
  "pickupPoint": "Main Street Bus Stop"
}
```

---

### **Use Case 2: Change Student's Route**

**Flow:**
1. Admin/Transport Manager views student's current allocation
2. Clicks "Update Allocation"
3. Selects new route
4. Optionally updates pickup point
5. Submits form
6. System validates no duplicate active allocation for new route
7. Allocation updated

**API Calls:**
```typescript
PUT /api/v1/transport/allocations/{allocationUuid}
{
  "routeUuid": "route-uuid-457",
  "pickupPoint": "Park Avenue Bus Stop"
}
```

---

### **Use Case 3: Release Student's Transport**

**Flow:**
1. Admin/Transport Manager views student's active allocation
2. Clicks "Release Transport"
3. Confirms action
4. System sets releasedAt timestamp
5. Allocation becomes inactive
6. Student can now be allocated to a new route

**API Calls:**
```typescript
PUT /api/v1/transport/allocations/{allocationUuid}/release
```

---

### **Use Case 4: View Route Utilization**

**Flow:**
1. Admin/Transport Manager navigates to Routes page
2. Clicks on a specific route
3. Views route details
4. Clicks "View Allocations" or "Active Allocations"
5. Sees all students assigned to that route

**API Calls:**
```typescript
GET /api/v1/transport/allocations/routes/{routeUuid}/active
```

---

### **Use Case 5: Student Views Their Transport**

**Flow:**
1. Student logs in
2. Navigates to "My Transport" section
3. Views active transport allocation (if any)
4. Sees route details, vehicle, driver, pickup point
5. Can view transport history

**API Calls:**
```typescript
GET /api/v1/transport/allocations/students/{studentUuid}/active
GET /api/v1/transport/allocations/students/{studentUuid}
```

---

## Error Handling

### **Common Error Responses**

**404 Not Found:**
```json
{
  "success": false,
  "status": 404,
  "message": "Transport route not found with UUID: route-uuid-456"
}
```

**409 Conflict:**
```json
{
  "success": false,
  "status": 409,
  "message": "Student already has an active transport allocation for this route"
}
```

**403 Forbidden:**
```json
{
  "success": false,
  "status": 403,
  "message": "Access Denied"
}
```

**400 Bad Request:**
```json
{
  "success": false,
  "status": 400,
  "message": "Validation failed",
  "errors": [
    {
      "field": "routeName",
      "message": "Route name is required"
    }
  ]
}
```

---

## Complete Setup Flow Summary

### **Initial Transport Setup (Admin)**

1. **Create Transport Manager Accounts**
   ```
   POST /api/v1/transport-managers
   → Creates transport manager with ROLE_TRANSPORT_MANAGER
   → Transport manager can now manage transport operations
   ```

2. **Add Transport Routes**
   ```
   POST /api/v1/transport/routes
   → Populate transport with routes
   → Set route names, vehicle numbers, drivers
   ```

3. **Transport Manager Takes Over**
   ```
   → Transport manager logs in
   → Can manage all transport operations
   → Can allocate/release transport
   → Can monitor statistics
   ```

---

## Conclusion

This document provides a complete guide for frontend developers to integrate the Transport Management system with transport manager management capabilities. All endpoints are RESTful, follow consistent patterns, and include proper error handling and security measures.

### **Key Features:**
- ✅ **Transport Manager Management:** Full CRUD operations for transport manager accounts
- ✅ **Route Management:** Complete route catalog management
- ✅ **Allocation Management:** Comprehensive student transport allocation system
- ✅ **Active/Inactive Tracking:** Automatic allocation lifecycle management
- ✅ **Statistics & Reporting:** Transport analytics and monitoring
- ✅ **Multi-Role Support:** Admin, Transport Manager, Teacher, Student roles
- ✅ **College Isolation:** Full tenant isolation for SaaS architecture

### **Role Hierarchy:**
```
SUPER_ADMIN / COLLEGE_ADMIN
    ↓
  Manage Transport Managers
    ↓
  TRANSPORT_MANAGER
    ↓
  Manage Routes & Allocations
    ↓
  TEACHER / STUDENT
    ↓
  View Routes (Teacher)
  View Own Transport (Student)
```

For any questions or clarifications, refer to the API documentation or contact the backend team.

