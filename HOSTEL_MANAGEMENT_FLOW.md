# Complete Hostel Management Flow Documentation

## Overview
This document describes the complete API flow for Hostel Management in the college management system, including hostel manager management, hostel warden management, hostel setup, room management, student hostel allocation, and comprehensive hostel reporting for different roles (COLLEGE_ADMIN, HOSTEL_MANAGER, HOSTEL_WARDEN, TEACHER, STUDENT).

---

## User Roles & Permissions

| Role | Hostel Manager Management | Hostel Warden Management | Hostel Management | Room Management | Allocation Management | View Hostels | View Rooms | View Allocations | View Summary |
|------|--------------------------|-------------------------|-------------------|-----------------|----------------------|--------------|------------|------------------|--------------|
| **SUPER_ADMIN** | ✅ Full Access | ✅ Full Access | ✅ Full Access | ✅ Full Access | ✅ Full Access | ✅ Yes | ✅ Yes | ✅ Yes | ✅ Yes |
| **COLLEGE_ADMIN** | ✅ Full Access | ✅ Full Access | ✅ Full Access | ✅ Full Access | ✅ Full Access | ✅ Yes | ✅ Yes | ✅ Yes | ✅ Yes |
| **HOSTEL_MANAGER** | ❌ No (View own only) | ❌ No | ✅ Full Access | ✅ Full Access | ✅ Full Access | ✅ Yes | ✅ Yes | ✅ Yes | ✅ Yes |
| **HOSTEL_WARDEN** | ❌ No | ❌ No (View own only) | ❌ No | ❌ No | ❌ No | ✅ Yes (Assigned hostels) | ✅ Yes (Assigned hostels) | ✅ Yes (Assigned hostels) | ❌ No |
| **TEACHER** | ❌ No | ❌ No | ❌ No | ❌ No | ❌ No | ✅ Yes (Read-only) | ✅ Yes (Read-only) | ✅ Yes (Read-only) | ❌ No |
| **STUDENT** | ❌ No | ❌ No | ❌ No | ❌ No | ❌ No | ✅ Yes (Read-only) | ✅ Yes (Read-only) | ✅ Yes (Own only) | ❌ No |

---

## Flow Diagram: Complete Hostel Management Lifecycle

```
┌─────────────────────────────────────────────────────────────────┐
│         HOSTEL SETUP (COLLEGE_ADMIN)                             │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│  Step 1: POST /api/v1/hostel-managers                          │
│  - Create hostel manager accounts                                │
│  - Assign ROLE_HOSTEL_MANAGER                                   │
│  - Setup staff profiles                                         │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│  Step 2: POST /api/v1/hostel-wardens                          │
│  - Create hostel warden accounts                                │
│  - Assign ROLE_HOSTEL_WARDEN                                    │
│  - Setup staff profiles                                         │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│  Step 3: POST /api/v1/hostels                                  │
│  - Create hostels (BOYS/GIRLS)                                 │
│  - Set capacity, assign warden                                  │
│  - Hostels are college-specific                                 │
│  - Can be done by ADMIN or HOSTEL_MANAGER                      │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│  Step 4: POST /api/v1/hostel-rooms                             │
│  - Create rooms within hostels                                  │
│  - Set room numbers and capacity                                │
│  - Track room occupancy                                         │
│  - Can be done by ADMIN or HOSTEL_MANAGER                      │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│              HOSTEL DISCOVERY (ALL USERS)                         │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│  Step 5: GET /api/v1/hostels                                   │
│  - Browse all hostels                                           │
│  - Filter by type (BOYS/GIRLS)                                  │
│  - View hostel details with warden info                         │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│    ALLOCATE HOSTEL (ADMIN/HOSTEL_MANAGER)                       │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│  Step 6: POST /api/v1/hostel-allocations                      │
│  - Allocate student to room                                     │
│  - Validates room capacity                                      │
│  - Auto-release previous active allocations                     │
│  - Status: Active (releasedAt = null)                           │
│  - Hostel Manager typically handles this                        │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│              ACTIVE HOSTEL STAY PERIOD                           │
│  - Student has active hostel allocation                         │
│  - Can view their hostel details                                │
│  - System tracks allocation history                             │
│  - Hostel Manager can monitor all allocations                   │
│  - Warden can view assigned hostels                             │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│    RELEASE ALLOCATION (ADMIN/HOSTEL_MANAGER)                     │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│  Step 7: POST /api/v1/hostel-allocations/{uuid}/release        │
│  - Release hostel allocation                                    │
│  - Set releasedAt timestamp                                     │
│  - Status: Active → Inactive                                    │
│  - Room capacity freed                                          │
│  - Hostel Manager handles releases                              │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│      MONITORING & STATISTICS (ADMIN/HOSTEL_MANAGER)              │
│  - View active allocations                                      │
│  - Track hostel occupancy                                       │
│  - Hostel summary statistics                                    │
│  - Manage hostel manager and warden accounts                    │
└─────────────────────────────────────────────────────────────────┘
```

---

## Detailed API Flows by User Role

### **Role 0: COLLEGE_ADMIN - Hostel Manager & Warden Setup**

#### **Flow 0.1: Create Hostel Manager Account**

**Step 1: Create Hostel Manager**
```http
POST /api/v1/hostel-managers
Authorization: Bearer {adminAccessToken}
Content-Type: application/json

Request Body:
{
  "name": "Sarah Johnson",
  "email": "sarah.johnson@college.edu",
  "password": "SecurePass123!",
  "designation": "Hostel Manager",
  "salary": 60000.00,
  "joiningDate": "2024-01-01",
  "phone": "+1234567890",
  "address": "123 Hostel St, City"
}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Hostel manager created successfully",
  "data": {
    "uuid": "hostel-manager-uuid-123",
    "name": "Sarah Johnson",
    "email": "sarah.johnson@college.edu",
    "designation": "Hostel Manager",
    "salary": 60000.00,
    "joiningDate": "2024-01-01",
    "phone": "+1234567890",
    "address": "123 Hostel St, City",
    "status": "ACTIVE",
    "emailVerified": false,
    "collegeId": 1,
    "createdAt": "2024-01-15T10:00:00",
    "updatedAt": "2024-01-15T10:00:00"
  }
}
```

**What Happens:**
- ✅ User account created with `ROLE_HOSTEL_MANAGER`
- ✅ Staff profile created with hostel manager details
- ✅ Hostel manager can now access hostel management features
- ✅ Email validated for uniqueness within college

---

**Step 2: View All Hostel Managers**
```http
GET /api/v1/hostel-managers?page=0&size=20&sortBy=name&direction=ASC
Authorization: Bearer {adminAccessToken}

Response:
{
  "success": true,
  "status": 200,
  "message": "Hostel managers retrieved successfully",
  "data": {
    "content": [
      {
        "uuid": "hostel-manager-uuid-123",
        "name": "Sarah Johnson",
        "email": "sarah.johnson@college.edu",
        "designation": "Hostel Manager",
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

**Step 3: Search Hostel Managers**
```http
GET /api/v1/hostel-managers/search?q=sarah&page=0&size=20
Authorization: Bearer {adminAccessToken}
```

---

**Step 4: Update Hostel Manager Information**
```http
PUT /api/v1/hostel-managers/{hostelManagerUuid}
Authorization: Bearer {adminAccessToken}
Content-Type: application/json

Request Body:
{
  "name": "Sarah Johnson",
  "email": "sarah.johnson.updated@college.edu",
  "designation": "Senior Hostel Manager",
  "salary": 65000.00,
  "phone": "+1234567891"
}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Hostel manager updated successfully",
  "data": {
    "uuid": "hostel-manager-uuid-123",
    "name": "Sarah Johnson",
    "email": "sarah.johnson.updated@college.edu",
    "designation": "Senior Hostel Manager",
    "salary": 65000.00,
    ...
    "updatedAt": "2024-01-20T10:00:00"
  }
}
```

---

**Step 5: Delete Hostel Manager**
```http
DELETE /api/v1/hostel-managers/{hostelManagerUuid}
Authorization: Bearer {adminAccessToken}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Hostel manager deleted successfully",
  "data": null
}
```

---

#### **Flow 0.2: Create Hostel Warden Account**

**Step 1: Create Hostel Warden**
```http
POST /api/v1/hostel-wardens
Authorization: Bearer {adminAccessToken}
Content-Type: application/json

Request Body:
{
  "name": "Michael Brown",
  "email": "michael.brown@college.edu",
  "password": "SecurePass123!",
  "designation": "Hostel Warden",
  "salary": 55000.00,
  "joiningDate": "2024-01-01",
  "phone": "+1234567892",
  "address": "456 Warden Ave, City"
}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Hostel warden created successfully",
  "data": {
    "uuid": "hostel-warden-uuid-456",
    "name": "Michael Brown",
    "email": "michael.brown@college.edu",
    "designation": "Hostel Warden",
    "salary": 55000.00,
    "joiningDate": "2024-01-01",
    "phone": "+1234567892",
    "address": "456 Warden Ave, City",
    "status": "ACTIVE",
    "emailVerified": false,
    "collegeId": 1,
    "assignedHostelUuids": [],
    "assignedHostelCount": 0,
    "createdAt": "2024-01-15T10:00:00",
    "updatedAt": "2024-01-15T10:00:00"
  }
}
```

**What Happens:**
- ✅ User account created with `ROLE_HOSTEL_WARDEN`
- ✅ Staff profile created with warden details
- ✅ Warden can be assigned to hostels later
- ✅ Email validated for uniqueness within college

---

**Step 2: View All Hostel Wardens**
```http
GET /api/v1/hostel-wardens?page=0&size=20&sortBy=name&direction=ASC
Authorization: Bearer {adminAccessToken}

Response:
{
  "success": true,
  "status": 200,
  "message": "Hostel wardens retrieved successfully",
  "data": {
    "content": [
      {
        "uuid": "hostel-warden-uuid-456",
        "name": "Michael Brown",
        "email": "michael.brown@college.edu",
        "designation": "Hostel Warden",
        "status": "ACTIVE",
        "assignedHostelCount": 1
      },
      ...
    ],
    "totalElements": 3,
    "totalPages": 1
  }
}
```

---

**Step 3: Get Hostels Assigned to Warden**
```http
GET /api/v1/hostel-wardens/{wardenUuid}/hostels
Authorization: Bearer {adminAccessToken | wardenAccessToken}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Hostels retrieved successfully",
  "data": [
    {
      "uuid": "hostel-uuid-789",
      "name": "Boys Hostel A",
      "type": "BOYS",
      "capacity": 200,
      "wardenUuid": "hostel-warden-uuid-456",
      "wardenName": "Michael Brown",
      "collegeId": 1,
      ...
    }
  ]
}
```

---

**Step 4: Update Hostel Warden Information**
```http
PUT /api/v1/hostel-wardens/{wardenUuid}
Authorization: Bearer {adminAccessToken}
Content-Type: application/json

Request Body:
{
  "name": "Michael Brown",
  "email": "michael.brown.updated@college.edu",
  "designation": "Senior Hostel Warden",
  "salary": 60000.00
}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Hostel warden updated successfully",
  "data": {
    "uuid": "hostel-warden-uuid-456",
    "name": "Michael Brown",
    "email": "michael.brown.updated@college.edu",
    "designation": "Senior Hostel Warden",
    "salary": 60000.00,
    "assignedHostelUuids": ["hostel-uuid-789"],
    "assignedHostelCount": 1,
    ...
    "updatedAt": "2024-01-20T10:00:00"
  }
}
```

---

**Step 5: Delete Hostel Warden**
```http
DELETE /api/v1/hostel-wardens/{wardenUuid}
Authorization: Bearer {adminAccessToken}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Hostel warden deleted successfully",
  "data": null
}
```

**Error Response (409 Conflict):**
```json
{
  "success": false,
  "status": 409,
  "message": "Cannot delete warden assigned to 1 hostel(s). Please unassign from all hostels first."
}
```

---

### **Role 1: COLLEGE_ADMIN / HOSTEL_MANAGER - Hostel Management**

#### **Flow 1.1: Create Hostel**

**Step 1: Create Hostel**
```http
POST /api/v1/hostels
Authorization: Bearer {adminAccessToken | hostelManagerAccessToken}
Content-Type: application/json

Request Body:
{
  "name": "Boys Hostel A",
  "type": "BOYS",
  "capacity": 200,
  "wardenUuid": "hostel-warden-uuid-456"  // Optional
}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Hostel created successfully",
  "data": {
    "uuid": "hostel-uuid-789",
    "name": "Boys Hostel A",
    "type": "BOYS",
    "capacity": 200,
    "wardenUuid": "hostel-warden-uuid-456",
    "wardenName": "Michael Brown",
    "collegeId": 1,
    "createdAt": "2024-01-15T08:00:00",
    "updatedAt": "2024-01-15T08:00:00"
  }
}
```

**What Happens:**
- ✅ Hostel created for the college
- ✅ Hostel name must be unique within college
- ✅ Warden assigned if provided (must have ROLE_HOSTEL_WARDEN)
- ✅ Validates college context
- ✅ Accessible by COLLEGE_ADMIN, SUPER_ADMIN, and HOSTEL_MANAGER roles

**Error Response (409 Conflict):**
```json
{
  "success": false,
  "status": 409,
  "message": "Hostel with name 'Boys Hostel A' already exists in this college"
}
```

**Error Response (404 Not Found):**
```json
{
  "success": false,
  "status": 404,
  "message": "Hostel warden not found with UUID: hostel-warden-uuid-456"
}
```

---

#### **Flow 1.2: Update Hostel Information**

**Update Hostel**
```http
PUT /api/v1/hostels/{hostelUuid}
Authorization: Bearer {adminAccessToken | hostelManagerAccessToken}
Content-Type: application/json

Request Body:
{
  "name": "Boys Hostel A (Updated)",
  "type": "BOYS",
  "capacity": 250,
  "wardenUuid": "hostel-warden-uuid-457"  // Change warden or "" to remove
}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Hostel updated successfully",
  "data": {
    "uuid": "hostel-uuid-789",
    "name": "Boys Hostel A (Updated)",
    "type": "BOYS",
    "capacity": 250,
    "wardenUuid": "hostel-warden-uuid-457",
    "wardenName": "John Smith",
    "updatedAt": "2024-01-20T10:00:00"
  }
}
```

**What Happens:**
- ✅ Updates hostel information
- ✅ Validates hostel name uniqueness (if changed)
- ✅ Updates capacity
- ✅ Can assign/change/remove warden
- ✅ Validates warden has ROLE_HOSTEL_WARDEN

---

#### **Flow 1.3: View Hostels**

**Get All Hostels (Paginated)**
```http
GET /api/v1/hostels?page=0&size=20&sortBy=name&direction=ASC
Authorization: Bearer {adminAccessToken | hostelManagerAccessToken | teacherAccessToken | studentAccessToken}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Hostels retrieved successfully",
  "data": {
    "content": [
      {
        "uuid": "hostel-uuid-789",
        "name": "Boys Hostel A",
        "type": "BOYS",
        "capacity": 200,
        "wardenUuid": "hostel-warden-uuid-456",
        "wardenName": "Michael Brown",
        "collegeId": 1,
        "createdAt": "2024-01-15T08:00:00",
        "updatedAt": "2024-01-15T08:00:00"
      },
      {
        "uuid": "hostel-uuid-790",
        "name": "Girls Hostel B",
        "type": "GIRLS",
        "capacity": 150,
        "wardenUuid": null,
        "wardenName": null,
        ...
      },
      ...
    ],
    "totalElements": 5,
    "totalPages": 1,
    "size": 20,
    "number": 0
  }
}
```

---

**Get All Hostels (List - No Pagination)**
```http
GET /api/v1/hostels/all
Authorization: Bearer {adminAccessToken | hostelManagerAccessToken | teacherAccessToken | studentAccessToken}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Hostels retrieved successfully",
  "data": [
    {
      "uuid": "hostel-uuid-789",
      "name": "Boys Hostel A",
      "type": "BOYS",
      "capacity": 200,
      ...
    },
    ...
  ]
}
```

---

**Get Hostels by Type**
```http
GET /api/v1/hostels/type/BOYS?page=0&size=20
Authorization: Bearer {adminAccessToken | hostelManagerAccessToken | teacherAccessToken | studentAccessToken}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Hostels retrieved successfully",
  "data": {
    "content": [
      {
        "uuid": "hostel-uuid-789",
        "name": "Boys Hostel A",
        "type": "BOYS",
        ...
      },
      ...
    ],
    "totalElements": 3
  }
}
```

---

**Get Hostel by UUID**
```http
GET /api/v1/hostels/{hostelUuid}
Authorization: Bearer {adminAccessToken | hostelManagerAccessToken | teacherAccessToken | studentAccessToken}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Hostel retrieved successfully",
  "data": {
    "uuid": "hostel-uuid-789",
    "name": "Boys Hostel A",
    "type": "BOYS",
    "capacity": 200,
    "wardenUuid": "hostel-warden-uuid-456",
    "wardenName": "Michael Brown",
    "collegeId": 1,
    "createdAt": "2024-01-15T08:00:00",
    "updatedAt": "2024-01-15T08:00:00"
  }
}
```

---

**Search Hostels**
```http
GET /api/v1/hostels/search?q=boys&page=0&size=20
Authorization: Bearer {adminAccessToken | hostelManagerAccessToken}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Search results retrieved successfully",
  "data": {
    "content": [
      {
        "uuid": "hostel-uuid-789",
        "name": "Boys Hostel A",
        ...
      },
      ...
    ],
    "totalElements": 2
  }
}
```

---

**Delete Hostel**
```http
DELETE /api/v1/hostels/{hostelUuid}
Authorization: Bearer {adminAccessToken | hostelManagerAccessToken}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Hostel deleted successfully",
  "data": null
}
```

---

### **Role 2: COLLEGE_ADMIN / HOSTEL_MANAGER - Room Management**

#### **Flow 2.1: Create Hostel Room**

**Step 1: Create Hostel Room**
```http
POST /api/v1/hostel-rooms
Authorization: Bearer {adminAccessToken | hostelManagerAccessToken}
Content-Type: application/json

Request Body:
{
  "hostelUuid": "hostel-uuid-789",
  "roomNumber": "101",
  "capacity": 4
}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Hostel room created successfully",
  "data": {
    "uuid": "room-uuid-101",
    "hostelUuid": "hostel-uuid-789",
    "hostelName": "Boys Hostel A",
    "roomNumber": "101",
    "capacity": 4,
    "currentOccupancy": 0,
    "availableSpots": 4,
    "createdAt": "2024-01-15T08:00:00",
    "updatedAt": "2024-01-15T08:00:00"
  }
}
```

**What Happens:**
- ✅ Room created within specified hostel
- ✅ Room number must be unique within hostel
- ✅ Capacity set for the room
- ✅ Initial occupancy is 0
- ✅ Validates hostel exists and belongs to college

**Error Response (409 Conflict):**
```json
{
  "success": false,
  "status": 409,
  "message": "Room number '101' already exists in this hostel"
}
```

---

#### **Flow 2.2: Update Hostel Room**

**Update Room Information**
```http
PUT /api/v1/hostel-rooms/{roomUuid}
Authorization: Bearer {adminAccessToken | hostelManagerAccessToken}
Content-Type: application/json

Request Body:
{
  "roomNumber": "101A",
  "capacity": 6
}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Hostel room updated successfully",
  "data": {
    "uuid": "room-uuid-101",
    "hostelUuid": "hostel-uuid-789",
    "hostelName": "Boys Hostel A",
    "roomNumber": "101A",
    "capacity": 6,
    "currentOccupancy": 2,
    "availableSpots": 4,
    "updatedAt": "2024-01-20T10:00:00"
  }
}
```

**What Happens:**
- ✅ Updates room number if provided (validates uniqueness)
- ✅ Updates capacity if provided
- ✅ Validates new capacity is not less than current occupancy
- ✅ Recalculates available spots

**Error Response (409 Conflict):**
```json
{
  "success": false,
  "status": 409,
  "message": "New capacity (3) cannot be less than current occupancy (4)"
}
```

---

#### **Flow 2.3: View Hostel Rooms**

**Get All Rooms (Paginated)**
```http
GET /api/v1/hostel-rooms?page=0&size=20&sortBy=roomNumber&direction=ASC
Authorization: Bearer {adminAccessToken | hostelManagerAccessToken | teacherAccessToken | studentAccessToken}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Hostel rooms retrieved successfully",
  "data": {
    "content": [
      {
        "uuid": "room-uuid-101",
        "hostelUuid": "hostel-uuid-789",
        "hostelName": "Boys Hostel A",
        "roomNumber": "101",
        "capacity": 4,
        "currentOccupancy": 2,
        "availableSpots": 2,
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

**Get Rooms by Hostel (Paginated)**
```http
GET /api/v1/hostel-rooms/hostel/{hostelUuid}?page=0&size=20
Authorization: Bearer {adminAccessToken | hostelManagerAccessToken | teacherAccessToken | studentAccessToken}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Hostel rooms retrieved successfully",
  "data": {
    "content": [
      {
        "uuid": "room-uuid-101",
        "hostelUuid": "hostel-uuid-789",
        "hostelName": "Boys Hostel A",
        "roomNumber": "101",
        "capacity": 4,
        "currentOccupancy": 2,
        "availableSpots": 2,
        ...
      },
      ...
    ],
    "totalElements": 20
  }
}
```

---

**Get Rooms by Hostel (List - No Pagination)**
```http
GET /api/v1/hostel-rooms/hostel/{hostelUuid}/all
Authorization: Bearer {adminAccessToken | hostelManagerAccessToken | teacherAccessToken | studentAccessToken}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Hostel rooms retrieved successfully",
  "data": [
    {
      "uuid": "room-uuid-101",
      "hostelUuid": "hostel-uuid-789",
      "hostelName": "Boys Hostel A",
      "roomNumber": "101",
      "capacity": 4,
      "currentOccupancy": 2,
      "availableSpots": 2,
      ...
    },
    ...
  ]
}
```

---

**Get Room by UUID**
```http
GET /api/v1/hostel-rooms/{roomUuid}
Authorization: Bearer {adminAccessToken | hostelManagerAccessToken | teacherAccessToken | studentAccessToken}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Hostel room retrieved successfully",
  "data": {
    "uuid": "room-uuid-101",
    "hostelUuid": "hostel-uuid-789",
    "hostelName": "Boys Hostel A",
    "roomNumber": "101",
    "capacity": 4,
    "currentOccupancy": 2,
    "availableSpots": 2,
    "createdAt": "2024-01-15T08:00:00",
    "updatedAt": "2024-01-15T08:00:00"
  }
}
```

---

**Search Rooms**
```http
GET /api/v1/hostel-rooms/search?q=101&page=0&size=20
Authorization: Bearer {adminAccessToken | hostelManagerAccessToken}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Search results retrieved successfully",
  "data": {
    "content": [
      {
        "uuid": "room-uuid-101",
        "roomNumber": "101",
        "hostelName": "Boys Hostel A",
        ...
      },
      ...
    ],
    "totalElements": 5
  }
}
```

---

**Delete Room**
```http
DELETE /api/v1/hostel-rooms/{roomUuid}
Authorization: Bearer {adminAccessToken | hostelManagerAccessToken}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Hostel room deleted successfully",
  "data": null
}
```

**Error Response (409 Conflict):**
```json
{
  "success": false,
  "status": 409,
  "message": "Cannot delete room with active allocations. Please release all students first."
}
```

---

### **Role 3: COLLEGE_ADMIN / HOSTEL_MANAGER - Hostel Allocation Management**

#### **Flow 3.1: Allocate Student to Hostel Room**

**Step 1: Create Hostel Allocation**
```http
POST /api/v1/hostel-allocations
Authorization: Bearer {adminAccessToken | hostelManagerAccessToken}
Content-Type: application/json

Request Body:
{
  "studentUuid": "student-uuid-001",
  "roomUuid": "room-uuid-101",
  "allocatedAt": "2024-01-15T08:00:00"  // Optional, defaults to current time
}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Hostel allocation created successfully",
  "data": {
    "uuid": "allocation-uuid-789",
    "studentUuid": "student-uuid-001",
    "studentName": "Alice Johnson",
    "rollNumber": "10A001",
    "roomUuid": "room-uuid-101",
    "roomNumber": "101",
    "hostelUuid": "hostel-uuid-789",
    "hostelName": "Boys Hostel A",
    "allocatedAt": "2024-01-15T08:00:00",
    "releasedAt": null,
    "isActive": true,
    "createdAt": "2024-01-15T08:00:00",
    "updatedAt": "2024-01-15T08:00:00"
  }
}
```

**What Happens:**
- ✅ Creates hostel allocation linking student to room
- ✅ **Automatically releases any existing active allocations** for the student
- ✅ Validates room has available capacity
- ✅ Initial status: Active (releasedAt = null)
- ✅ Validates student and room exist and belong to college
- ✅ Prevents duplicate active allocations for same student

**Error Response (409 Conflict):**
```json
{
  "success": false,
  "status": 409,
  "message": "Student already has an active hostel allocation"
}
```

**Error Response (409 Conflict - Room Full):**
```json
{
  "success": false,
  "status": 409,
  "message": "Room is at full capacity. Current occupancy: 4, Capacity: 4"
}
```

---

#### **Flow 3.2: Update Hostel Allocation**

**Update Allocation Information**
```http
PUT /api/v1/hostel-allocations/{allocationUuid}
Authorization: Bearer {adminAccessToken | hostelManagerAccessToken}
Content-Type: application/json

Request Body:
{
  "roomUuid": "room-uuid-102",  // Change room
  "releasedAt": null  // Keep active, or set timestamp to release
}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Hostel allocation updated successfully",
  "data": {
    "uuid": "allocation-uuid-789",
    "studentUuid": "student-uuid-001",
    "studentName": "Alice Johnson",
    "roomUuid": "room-uuid-102",
    "roomNumber": "102",
    "hostelUuid": "hostel-uuid-789",
    "hostelName": "Boys Hostel A",
    "isActive": true,
    ...
    "updatedAt": "2024-01-20T10:00:00"
  }
}
```

**What Happens:**
- ✅ Updates room if provided (validates new room exists and has capacity)
- ✅ Can set releasedAt to deactivate allocation
- ✅ Validates room capacity before room change

---

#### **Flow 3.3: Release Hostel Allocation**

**Release/Deactivate Allocation**
```http
POST /api/v1/hostel-allocations/{allocationUuid}/release
Authorization: Bearer {adminAccessToken | hostelManagerAccessToken}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Hostel allocation released successfully",
  "data": {
    "uuid": "allocation-uuid-789",
    "studentUuid": "student-uuid-001",
    "studentName": "Alice Johnson",
    "roomUuid": "room-uuid-101",
    "roomNumber": "101",
    "hostelUuid": "hostel-uuid-789",
    "hostelName": "Boys Hostel A",
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
- ✅ Room capacity freed
- ✅ Student can be allocated to a new room after release

**Error Response (409 Conflict):**
```json
{
  "success": false,
  "status": 409,
  "message": "Hostel allocation is already released"
}
```

---

#### **Flow 3.4: View Hostel Allocations**

**Get All Allocations (Paginated)**
```http
GET /api/v1/hostel-allocations?page=0&size=20&sortBy=allocatedAt&direction=DESC
Authorization: Bearer {adminAccessToken | hostelManagerAccessToken | teacherAccessToken}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Hostel allocations retrieved successfully",
  "data": {
    "content": [
      {
        "uuid": "allocation-uuid-789",
        "studentUuid": "student-uuid-001",
        "studentName": "Alice Johnson",
        "rollNumber": "10A001",
        "roomUuid": "room-uuid-101",
        "roomNumber": "101",
        "hostelUuid": "hostel-uuid-789",
        "hostelName": "Boys Hostel A",
        "allocatedAt": "2024-01-15T08:00:00",
        "releasedAt": null,
        "isActive": true,
        ...
      },
      ...
    ],
    "totalElements": 100,
    "totalPages": 5
  }
}
```

---

**Get Active Allocations Only**
```http
GET /api/v1/hostel-allocations/active?page=0&size=20&sortBy=allocatedAt&direction=DESC
Authorization: Bearer {adminAccessToken | hostelManagerAccessToken | teacherAccessToken | studentAccessToken}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Active hostel allocations retrieved successfully",
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
    "totalElements": 75
  }
}
```

---

**Get Allocations by Student**
```http
GET /api/v1/hostel-allocations/student/{studentUuid}
Authorization: Bearer {adminAccessToken | hostelManagerAccessToken | teacherAccessToken | studentAccessToken}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Hostel allocations retrieved successfully",
  "data": [
    {
      "uuid": "allocation-uuid-789",
      "studentUuid": "student-uuid-001",
      "studentName": "Alice Johnson",
      "roomNumber": "101",
      "hostelName": "Boys Hostel A",
      "allocatedAt": "2024-01-15T08:00:00",
      "releasedAt": null,
      "isActive": true,
      ...
    },
    {
      "uuid": "allocation-uuid-790",
      "roomNumber": "102",
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
GET /api/v1/hostel-allocations/student/{studentUuid}/active
Authorization: Bearer {adminAccessToken | hostelManagerAccessToken | teacherAccessToken | studentAccessToken}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Active hostel allocation retrieved successfully",
  "data": {
    "uuid": "allocation-uuid-789",
    "studentUuid": "student-uuid-001",
    "studentName": "Alice Johnson",
    "rollNumber": "10A001",
    "roomUuid": "room-uuid-101",
    "roomNumber": "101",
    "hostelUuid": "hostel-uuid-789",
    "hostelName": "Boys Hostel A",
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
  "message": "No active hostel allocation found for student with UUID: student-uuid-001"
}
```

---

**Get Allocations by Room**
```http
GET /api/v1/hostel-allocations/room/{roomUuid}?page=0&size=20
Authorization: Bearer {adminAccessToken | hostelManagerAccessToken | teacherAccessToken}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Hostel allocations retrieved successfully",
  "data": {
    "content": [
      {
        "uuid": "allocation-uuid-789",
        "studentUuid": "student-uuid-001",
        "studentName": "Alice Johnson",
        "rollNumber": "10A001",
        "roomUuid": "room-uuid-101",
        "roomNumber": "101",
        "isActive": true,
        ...
      },
      ...
    ],
    "totalElements": 4
  }
}
```

---

**Get Active Allocations by Room**
```http
GET /api/v1/hostel-allocations/room/{roomUuid}/active
Authorization: Bearer {adminAccessToken | hostelManagerAccessToken | teacherAccessToken}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Active hostel allocations retrieved successfully",
  "data": [
    {
      "uuid": "allocation-uuid-789",
      "studentUuid": "student-uuid-001",
      "studentName": "Alice Johnson",
      "rollNumber": "10A001",
      "isActive": true,
      ...
    },
    ...
  ]
}
```

---

**Get Allocations by Hostel**
```http
GET /api/v1/hostel-allocations/hostel/{hostelUuid}?page=0&size=20
Authorization: Bearer {adminAccessToken | hostelManagerAccessToken | teacherAccessToken}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Hostel allocations retrieved successfully",
  "data": {
    "content": [
      {
        "uuid": "allocation-uuid-789",
        "studentUuid": "student-uuid-001",
        "studentName": "Alice Johnson",
        "roomNumber": "101",
        "hostelUuid": "hostel-uuid-789",
        "hostelName": "Boys Hostel A",
        "isActive": true,
        ...
      },
      ...
    ],
    "totalElements": 50
  }
}
```

---

**Get Active Allocations by Hostel**
```http
GET /api/v1/hostel-allocations/hostel/{hostelUuid}/active
Authorization: Bearer {adminAccessToken | hostelManagerAccessToken | teacherAccessToken}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Active hostel allocations retrieved successfully",
  "data": [
    {
      "uuid": "allocation-uuid-789",
      "studentUuid": "student-uuid-001",
      "studentName": "Alice Johnson",
      "roomNumber": "101",
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
GET /api/v1/hostel-allocations/search?q=alice&page=0&size=20
Authorization: Bearer {adminAccessToken | hostelManagerAccessToken}

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
        "roomNumber": "101",
        "hostelName": "Boys Hostel A",
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
GET /api/v1/hostel-allocations/{allocationUuid}
Authorization: Bearer {adminAccessToken | hostelManagerAccessToken | teacherAccessToken | studentAccessToken}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Hostel allocation retrieved successfully",
  "data": {
    "uuid": "allocation-uuid-789",
    "studentUuid": "student-uuid-001",
    "studentName": "Alice Johnson",
    "rollNumber": "10A001",
    "roomUuid": "room-uuid-101",
    "roomNumber": "101",
    "hostelUuid": "hostel-uuid-789",
    "hostelName": "Boys Hostel A",
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
DELETE /api/v1/hostel-allocations/{allocationUuid}
Authorization: Bearer {adminAccessToken | hostelManagerAccessToken}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Hostel allocation deleted successfully",
  "data": null
}
```

---

### **Role 4: STUDENT - View Own Hostel**

#### **Flow 4.1: View My Hostel Allocation**

**Get My Active Hostel**
```http
GET /api/v1/hostel-allocations/student/{studentUuid}/active
Authorization: Bearer {studentAccessToken}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Active hostel allocation retrieved successfully",
  "data": {
    "uuid": "allocation-uuid-789",
    "studentUuid": "student-uuid-001",
    "studentName": "Alice Johnson",
    "rollNumber": "10A001",
    "roomUuid": "room-uuid-101",
    "roomNumber": "101",
    "hostelUuid": "hostel-uuid-789",
    "hostelName": "Boys Hostel A",
    "allocatedAt": "2024-01-15T08:00:00",
    "releasedAt": null,
    "isActive": true,
    ...
  }
}
```

---

**Get My Hostel History**
```http
GET /api/v1/hostel-allocations/student/{studentUuid}
Authorization: Bearer {studentAccessToken}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Hostel allocations retrieved successfully",
  "data": [
    {
      "uuid": "allocation-uuid-789",
      "roomNumber": "101",
      "hostelName": "Boys Hostel A",
      "allocatedAt": "2024-01-15T08:00:00",
      "releasedAt": null,
      "isActive": true,
      ...
    },
    {
      "uuid": "allocation-uuid-790",
      "roomNumber": "102",
      "hostelName": "Boys Hostel A",
      "allocatedAt": "2023-12-01T08:00:00",
      "releasedAt": "2024-01-14T10:00:00",
      "isActive": false,
      ...
    }
  ]
}
```

---

**Browse Available Hostels**
```http
GET /api/v1/hostels/all
Authorization: Bearer {studentAccessToken}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Hostels retrieved successfully",
  "data": [
    {
      "uuid": "hostel-uuid-789",
      "name": "Boys Hostel A",
      "type": "BOYS",
      "capacity": 200,
      "wardenName": "Michael Brown",
      ...
    },
    ...
  ]
}
```

---

### **Role 5: HOSTEL_WARDEN - View Assigned Hostels**

#### **Flow 5.1: View My Assigned Hostels**

**Get My Assigned Hostels**
```http
GET /api/v1/hostel-wardens/{wardenUuid}/hostels
Authorization: Bearer {wardenAccessToken}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Hostels retrieved successfully",
  "data": [
    {
      "uuid": "hostel-uuid-789",
      "name": "Boys Hostel A",
      "type": "BOYS",
      "capacity": 200,
      "wardenUuid": "hostel-warden-uuid-456",
      "wardenName": "Michael Brown",
      "collegeId": 1,
      ...
    }
  ]
}
```

---

**Get My Warden Profile**
```http
GET /api/v1/hostel-wardens/{wardenUuid}
Authorization: Bearer {wardenAccessToken}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Hostel warden retrieved successfully",
  "data": {
    "uuid": "hostel-warden-uuid-456",
    "name": "Michael Brown",
    "email": "michael.brown@college.edu",
    "designation": "Hostel Warden",
    "assignedHostelUuids": ["hostel-uuid-789"],
    "assignedHostelCount": 1,
    ...
  }
}
```

---

### **Role 6: COLLEGE_ADMIN / HOSTEL_MANAGER - Hostel Summary & Reporting**

#### **Flow 6.1: Get Hostel Summary**

**Get Hostel Statistics**
```http
GET /api/v1/hostel-allocations/summary
Authorization: Bearer {adminAccessToken | hostelManagerAccessToken}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Hostel summary retrieved successfully",
  "data": {
    "totalHostels": 5,
    "totalRooms": 100,
    "totalActiveAllocations": 250,
    "totalInactiveAllocations": 50,
    "totalStudents": 500,
    "totalStudentsWithHostel": 250,
    "totalStudentsWithoutHostel": 250,
    "totalCapacity": 400,
    "totalOccupied": 250,
    "totalAvailable": 150
  }
}
```

**What This Provides:**
- ✅ Total number of hostels in college
- ✅ Total number of rooms across all hostels
- ✅ Number of active hostel allocations
- ✅ Number of inactive/historical allocations
- ✅ Students currently staying in hostels
- ✅ Students without hostel accommodation
- ✅ Total capacity across all rooms
- ✅ Current occupancy
- ✅ Available spots

---

## Frontend Integration Guide

### **1. Hostel Manager Management Page**

**Component Structure:**
```
HostelManagerManagement/
  ├── HostelManagerList.tsx (List view with pagination)
  ├── CreateHostelManagerModal.tsx (Create form)
  ├── UpdateHostelManagerModal.tsx (Update form)
  └── HostelManagerCard.tsx (Individual manager card)
```

**API Integration:**
```typescript
// Fetch all hostel managers
const fetchHostelManagers = async (page: number, size: number) => {
  const response = await fetch(
    `/api/v1/hostel-managers?page=${page}&size=${size}`,
    {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    }
  );
  return response.json();
};

// Create hostel manager
const createHostelManager = async (data: CreateHostelManagerRequest) => {
  const response = await fetch('/api/v1/hostel-managers', {
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

### **2. Hostel Warden Management Page**

**Component Structure:**
```
HostelWardenManagement/
  ├── HostelWardenList.tsx (List view with pagination)
  ├── CreateHostelWardenModal.tsx (Create form)
  ├── UpdateHostelWardenModal.tsx (Update form)
  ├── HostelWardenCard.tsx (Individual warden card)
  └── AssignedHostelsList.tsx (Show hostels assigned to warden)
```

**API Integration:**
```typescript
// Fetch all hostel wardens
const fetchHostelWardens = async (page: number, size: number) => {
  const response = await fetch(
    `/api/v1/hostel-wardens?page=${page}&size=${size}`,
    {
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json'
      }
    }
  );
  return response.json();
};

// Get hostels assigned to warden
const getHostelsByWarden = async (wardenUuid: string) => {
  const response = await fetch(
    `/api/v1/hostel-wardens/${wardenUuid}/hostels`,
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

### **3. Hostel Management Page**

**Component Structure:**
```
HostelManagement/
  ├── HostelList.tsx (List view with filters)
  ├── CreateHostelModal.tsx (Create form with warden selection)
  ├── UpdateHostelModal.tsx (Update form)
  ├── HostelCard.tsx (Individual hostel card)
  └── HostelDetails.tsx (Hostel details with rooms and allocations)
```

**API Integration:**
```typescript
// Fetch all hostels (for dropdowns)
const fetchAllHostels = async () => {
  const response = await fetch('/api/v1/hostels/all', {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
  return response.json();
};

// Create hostel
const createHostel = async (data: CreateHostelRequest) => {
  const response = await fetch('/api/v1/hostels', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${token}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(data)
  });
  return response.json();
};

// Get hostels by type
const getHostelsByType = async (type: 'BOYS' | 'GIRLS', page: number) => {
  const response = await fetch(
    `/api/v1/hostels/type/${type}?page=${page}&size=20`,
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

### **4. Hostel Room Management Page**

**Component Structure:**
```
HostelRoomManagement/
  ├── RoomList.tsx (List view with filters)
  ├── CreateRoomModal.tsx (Create form with hostel selection)
  ├── UpdateRoomModal.tsx (Update form)
  ├── RoomCard.tsx (Individual room card with occupancy)
  └── RoomDetails.tsx (Room details with allocated students)
```

**API Integration:**
```typescript
// Fetch rooms by hostel
const fetchRoomsByHostel = async (hostelUuid: string) => {
  const response = await fetch(
    `/api/v1/hostel-rooms/hostel/${hostelUuid}/all`,
    {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    }
  );
  return response.json();
};

// Create room
const createRoom = async (data: CreateHostelRoomRequest) => {
  const response = await fetch('/api/v1/hostel-rooms', {
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

### **5. Hostel Allocation Management Page**

**Component Structure:**
```
HostelAllocationManagement/
  ├── AllocationList.tsx (List view with filters)
  ├── CreateAllocationModal.tsx (Create form with student/room selection)
  ├── UpdateAllocationModal.tsx (Update form)
  ├── AllocationCard.tsx (Individual allocation card)
  └── StudentHostelView.tsx (Student's hostel view)
```

**API Integration:**
```typescript
// Create allocation
const createAllocation = async (data: CreateHostelAllocationRequest) => {
  const response = await fetch('/api/v1/hostel-allocations', {
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
const getStudentActiveHostel = async (studentUuid: string) => {
  const response = await fetch(
    `/api/v1/hostel-allocations/student/${studentUuid}/active`,
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
    `/api/v1/hostel-allocations/${allocationUuid}/release`,
    {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`
      }
    }
  );
  return response.json();
};

// Get allocations by room
const getAllocationsByRoom = async (roomUuid: string) => {
  const response = await fetch(
    `/api/v1/hostel-allocations/room/${roomUuid}/active`,
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

### **6. Student Hostel Dashboard**

**Component Structure:**
```
StudentHostelDashboard/
  ├── MyHostelCard.tsx (Current active hostel allocation)
  ├── HostelHistory.tsx (Historical allocations)
  └── AvailableHostelsList.tsx (Browse available hostels)
```

**API Integration:**
```typescript
// Get student's active hostel
const getMyActiveHostel = async () => {
  const response = await fetch(
    `/api/v1/hostel-allocations/student/${studentUuid}/active`,
    {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    }
  );
  return response.json();
};

// Get student's hostel history
const getMyHostelHistory = async () => {
  const response = await fetch(
    `/api/v1/hostel-allocations/student/${studentUuid}`,
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

### **7. Hostel Warden Dashboard**

**Component Structure:**
```
HostelWardenDashboard/
  ├── MyAssignedHostels.tsx (List of assigned hostels)
  ├── HostelDetails.tsx (Details of assigned hostel)
  └── RoomOccupancyView.tsx (View room occupancy for assigned hostels)
```

**API Integration:**
```typescript
// Get warden's assigned hostels
const getMyAssignedHostels = async () => {
  const response = await fetch(
    `/api/v1/hostel-wardens/${wardenUuid}/hostels`,
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

### **8. Hostel Summary Dashboard**

**Component Structure:**
```
HostelSummaryDashboard/
  ├── SummaryCards.tsx (Statistics cards)
  ├── OccupancyChart.tsx (Visualization)
  └── HostelUtilizationChart.tsx (Hostel usage)
```

**API Integration:**
```typescript
// Get hostel summary
const getHostelSummary = async () => {
  const response = await fetch('/api/v1/hostel-allocations/summary', {
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
  return response.json();
};
```

---

## Common Use Cases

### **Use Case 1: Complete Hostel Setup Flow**

**Flow:**
1. Admin creates Hostel Manager account
2. Admin creates Hostel Warden accounts
3. Admin/Hostel Manager creates hostels (BOYS/GIRLS)
4. Admin/Hostel Manager assigns wardens to hostels
5. Admin/Hostel Manager creates rooms within hostels
6. Admin/Hostel Manager allocates students to rooms
7. System tracks occupancy and availability

**API Calls:**
```typescript
// 1. Create hostel manager
POST /api/v1/hostel-managers

// 2. Create hostel warden
POST /api/v1/hostel-wardens

// 3. Create hostel
POST /api/v1/hostels
{
  "name": "Boys Hostel A",
  "type": "BOYS",
  "capacity": 200,
  "wardenUuid": "hostel-warden-uuid-456"
}

// 4. Create rooms
POST /api/v1/hostel-rooms
{
  "hostelUuid": "hostel-uuid-789",
  "roomNumber": "101",
  "capacity": 4
}

// 5. Allocate student
POST /api/v1/hostel-allocations
{
  "studentUuid": "student-uuid-001",
  "roomUuid": "room-uuid-101"
}
```

---

### **Use Case 2: Assign Student to Hostel Room**

**Flow:**
1. Admin/Hostel Manager navigates to Hostel Allocation page
2. Clicks "Allocate Hostel"
3. Selects student from dropdown (or searches)
4. Selects hostel and room from dropdown
5. System validates room capacity
6. Submits form
7. System automatically releases any previous active allocation
8. New allocation created and displayed
9. Room occupancy updated

**API Calls:**
```typescript
POST /api/v1/hostel-allocations
{
  "studentUuid": "student-uuid-001",
  "roomUuid": "room-uuid-101"
}
```

---

### **Use Case 3: Change Student's Room**

**Flow:**
1. Admin/Hostel Manager views student's current allocation
2. Clicks "Update Allocation"
3. Selects new room
4. System validates new room has available capacity
5. Submits form
6. Allocation updated, old room capacity freed, new room capacity occupied

**API Calls:**
```typescript
PUT /api/v1/hostel-allocations/{allocationUuid}
{
  "roomUuid": "room-uuid-102"
}
```

---

### **Use Case 4: Release Student from Hostel**

**Flow:**
1. Admin/Hostel Manager views student's active allocation
2. Clicks "Release Hostel"
3. Confirms action
4. System sets releasedAt timestamp
5. Allocation becomes inactive
6. Room capacity freed
7. Student can now be allocated to a new room

**API Calls:**
```typescript
POST /api/v1/hostel-allocations/{allocationUuid}/release
```

---

### **Use Case 5: View Room Occupancy**

**Flow:**
1. Admin/Hostel Manager navigates to Rooms page
2. Clicks on a specific room
3. Views room details with current occupancy
4. Clicks "View Allocations" or "Active Allocations"
5. Sees all students allocated to that room

**API Calls:**
```typescript
GET /api/v1/hostel-allocations/room/{roomUuid}/active
```

---

### **Use Case 6: Assign Warden to Hostel**

**Flow:**
1. Admin/Hostel Manager navigates to Hostels page
2. Clicks on a hostel
3. Clicks "Assign Warden" or "Update Warden"
4. Selects warden from dropdown (only shows users with ROLE_HOSTEL_WARDEN)
5. Submits form
6. Warden assigned to hostel
7. Warden can now view assigned hostel details

**API Calls:**
```typescript
PUT /api/v1/hostels/{hostelUuid}
{
  "wardenUuid": "hostel-warden-uuid-456"
}
```

---

### **Use Case 7: Student Views Their Hostel**

**Flow:**
1. Student logs in
2. Navigates to "My Hostel" section
3. Views active hostel allocation (if any)
4. Sees hostel name, room number, roommates
5. Can view hostel history

**API Calls:**
```typescript
GET /api/v1/hostel-allocations/student/{studentUuid}/active
GET /api/v1/hostel-allocations/student/{studentUuid}
```

---

### **Use Case 8: Warden Views Assigned Hostels**

**Flow:**
1. Warden logs in
2. Navigates to "My Hostels" section
3. Views list of hostels assigned to them
4. Clicks on a hostel to see details
5. Can view rooms and allocations for assigned hostels

**API Calls:**
```typescript
GET /api/v1/hostel-wardens/{wardenUuid}/hostels
GET /api/v1/hostels/{hostelUuid}
GET /api/v1/hostel-rooms/hostel/{hostelUuid}/all
GET /api/v1/hostel-allocations/hostel/{hostelUuid}/active
```

---

## Error Handling

### **Common Error Responses**

**404 Not Found:**
```json
{
  "success": false,
  "status": 404,
  "message": "Hostel not found with UUID: hostel-uuid-789"
}
```

**409 Conflict:**
```json
{
  "success": false,
  "status": 409,
  "message": "Student already has an active hostel allocation"
}
```

```json
{
  "success": false,
  "status": 409,
  "message": "Room is at full capacity. Current occupancy: 4, Capacity: 4"
}
```

```json
{
  "success": false,
  "status": 409,
  "message": "Cannot delete warden assigned to 1 hostel(s). Please unassign from all hostels first."
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
      "field": "name",
      "message": "Hostel name is required"
    }
  ]
}
```

---

## Complete Setup Flow Summary

### **Initial Hostel Setup (Admin)**

1. **Create Hostel Manager Accounts**
   ```
   POST /api/v1/hostel-managers
   → Creates hostel manager with ROLE_HOSTEL_MANAGER
   → Hostel manager can now manage hostel operations
   ```

2. **Create Hostel Warden Accounts**
   ```
   POST /api/v1/hostel-wardens
   → Creates hostel warden with ROLE_HOSTEL_WARDEN
   → Warden can be assigned to hostels later
   ```

3. **Add Hostels**
   ```
   POST /api/v1/hostels
   → Create hostels (BOYS/GIRLS)
   → Set capacity
   → Optionally assign warden
   ```

4. **Add Rooms to Hostels**
   ```
   POST /api/v1/hostel-rooms
   → Create rooms within hostels
   → Set room numbers and capacity
   → Track room occupancy
   ```

5. **Hostel Manager Takes Over**
   ```
   → Hostel manager logs in
   → Can manage all hostel operations
   → Can allocate/release students
   → Can monitor statistics
   ```

---

## Conclusion

This document provides a complete guide for frontend developers to integrate the Hostel Management system with hostel manager and warden management capabilities. All endpoints are RESTful, follow consistent patterns, and include proper error handling and security measures.

### **Key Features:**
- ✅ **Hostel Manager Management:** Full CRUD operations for hostel manager accounts
- ✅ **Hostel Warden Management:** Full CRUD operations for hostel warden accounts with assignment tracking
- ✅ **Hostel Management:** Complete hostel catalog management with warden assignment
- ✅ **Room Management:** Comprehensive room management with occupancy tracking
- ✅ **Allocation Management:** Complete student hostel allocation system with capacity validation
- ✅ **Active/Inactive Tracking:** Automatic allocation lifecycle management
- ✅ **Statistics & Reporting:** Hostel analytics and monitoring
- ✅ **Multi-Role Support:** Admin, Hostel Manager, Hostel Warden, Teacher, Student roles
- ✅ **College Isolation:** Full tenant isolation for SaaS architecture

### **Role Hierarchy:**
```
SUPER_ADMIN / COLLEGE_ADMIN
    ↓
  Manage Hostel Managers & Wardens
    ↓
  HOSTEL_MANAGER
    ↓
  Manage Hostels, Rooms & Allocations
    ↓
  HOSTEL_WARDEN
    ↓
  View Assigned Hostels (Read-only)
    ↓
  TEACHER / STUDENT
    ↓
  View Hostels (Teacher)
  View Own Hostel (Student)
```

For any questions or clarifications, refer to the API documentation or contact the backend team.

