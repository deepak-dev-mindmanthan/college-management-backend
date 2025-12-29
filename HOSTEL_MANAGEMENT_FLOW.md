# Complete Hostel Management Flow Documentation

## Overview
This document describes the complete API flow for Hostel Management in the college management system, including hostel manager management, hostel warden management, hostel setup, room management, student hostel allocation, and comprehensive hostel reporting for different roles (COLLEGE_ADMIN, HOSTEL_MANAGER, HOSTEL_WARDEN, TEACHER, STUDENT).

**Production-Ready Features:**
- ✅ **Notification System**: Automatic in-app notifications for students and parents on allocation/release events
- ✅ **Audit Logging**: Complete audit trail for all operations (CREATE, UPDATE, DELETE) with user tracking
- ✅ **Bidirectional Relationships**: Proper entity relationships (Student ↔ HostelAllocation, User ↔ Hostel) for data consistency
- ✅ **Real-time Updates**: WebSocket/polling support for notifications
- ✅ **Comprehensive Error Handling**: Detailed error responses with proper HTTP status codes
- ✅ **Role-Based Security**: Full RBAC implementation with college isolation

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
│  - ✅ AUDIT LOG: Creates audit log entry                        │
│  - ✅ NOTIFICATION: Sends notification to student                │
│  - ✅ NOTIFICATION: Sends notification to parents                │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│              ACTIVE HOSTEL STAY PERIOD                           │
│  - Student has active hostel allocation                         │
│  - Student receives notification (in-app)                       │
│  - Parents receive notification (in-app)                        │
│  - Can view their hostel details                                │
│  - System tracks allocation history                             │
│  - Hostel Manager can monitor all allocations                   │
│  - Warden can view assigned hostels                             │
│  - All operations are audit logged                              │
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
│  - ✅ AUDIT LOG: Creates audit log entry                        │
│  - ✅ NOTIFICATION: Sends notification to student                │
│  - ✅ NOTIFICATION: Sends notification to parents                │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│      MONITORING & STATISTICS (ADMIN/HOSTEL_MANAGER)              │
│  - View active allocations                                      │
│  - Track hostel occupancy                                       │
│  - Hostel summary statistics                                    │
│  - Manage hostel manager and warden accounts                    │
│  - View audit logs for all operations                           │
│  - Monitor notification delivery                                 │
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
- ✅ **AUDIT LOG**: Creates audit log entry (CREATE action, HOSTEL_MANAGER entity type)

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
- ✅ **AUDIT LOG**: Creates audit log entry (CREATE action, HOSTEL_WARDEN entity type)

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
- ✅ **AUDIT LOG**: Creates audit log entry (CREATE action, HOSTEL entity type)

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
- ✅ **AUDIT LOG**: Creates audit log entry (CREATE action, HOSTEL_ROOM entity type)

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
- ✅ **AUDIT LOG**: Creates audit log entry (CREATE action, HOSTEL_ALLOCATION entity type)
- ✅ **NOTIFICATION**: Sends in-app notification to student with allocation details
- ✅ **NOTIFICATION**: Sends in-app notification to all parents of the student

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
- ✅ **AUDIT LOG**: Creates audit log entry (UPDATE action, HOSTEL_ALLOCATION entity type)
- ✅ **NOTIFICATION**: Sends in-app notification to student about release
- ✅ **NOTIFICATION**: Sends in-app notification to all parents of the student

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

## System Integration Features

### **1. Notification System Integration**

The Hostel Management module is fully integrated with the system-wide notification service. Notifications are automatically sent to relevant users when key events occur.

#### **Notification Triggers:**

1. **Student Hostel Allocation Created:**
   - **Recipients**: Student, All Parents
   - **Type**: IN_APP
   - **Title**: "Hostel Allocation: [Hostel Name]"
   - **Content**: "You have been allocated to [Hostel Name], Room [Room Number]."
   - **Action URL**: `/hostel/allocations/{allocationUuid}`
   - **Priority**: 5 (Medium)

2. **Student Hostel Allocation Released:**
   - **Recipients**: Student, All Parents
   - **Type**: IN_APP
   - **Title**: "Hostel Allocation Released: [Hostel Name]"
   - **Content**: "Your hostel allocation has been released from [Hostel Name], Room [Room Number]."
   - **Action URL**: `/hostel/allocations/{allocationUuid}`
   - **Priority**: 5 (Medium)

#### **Notification Reference Type:**
- `HOSTEL_ALLOCATION` - Used to link notifications to hostel allocation entities

#### **Frontend Notification Handling:**
```typescript
// Example: Listen for notifications
const notifications = useNotifications(); // Custom hook

useEffect(() => {
  const hostelNotifications = notifications.filter(
    n => n.referenceType === 'HOSTEL_ALLOCATION'
  );
  
  hostelNotifications.forEach(notification => {
    if (!notification.isRead) {
      showToast(notification.title, notification.content);
      // Navigate to allocation details on click
      if (notification.actionUrl) {
        navigate(notification.actionUrl);
      }
    }
  });
}, [notifications]);
```

---

### **2. Audit Logging System Integration**

All Hostel Management operations are automatically logged in the audit system for compliance, tracking, and security purposes.

#### **Audit Log Entity Types:**

- `HOSTEL` - Hostel creation, updates, deletions
- `HOSTEL_ROOM` - Room creation, updates, deletions
- `HOSTEL_ALLOCATION` - Allocation creation, updates, releases, deletions
- `HOSTEL_WARDEN` - Warden account creation, updates, deletions
- `HOSTEL_MANAGER` - Manager account creation, updates, deletions

#### **Audit Actions Tracked:**

- `CREATE` - When new entities are created
- `UPDATE` - When entities are modified
- `DELETE` - When entities are removed

#### **Audit Log Information Captured:**

- User who performed the action
- Timestamp of the action
- Entity type and ID
- Descriptive message
- IP address (if available)
- College context

#### **Example Audit Log Entry:**
```json
{
  "id": 12345,
  "userId": 789,
  "userName": "John Admin",
  "action": "CREATE",
  "entityType": "HOSTEL_ALLOCATION",
  "entityId": 456,
  "description": "Allocated student Alice Johnson to room 101 in Boys Hostel A",
  "ipAddress": "192.168.1.100",
  "createdAt": "2024-01-15T10:30:00Z"
}
```

#### **Frontend Audit Log Viewing:**
```typescript
// Example: View audit logs for a hostel allocation
const fetchAuditLogs = async (allocationUuid: string) => {
  const response = await fetch(
    `/api/v1/audit-logs?entityType=HOSTEL_ALLOCATION&entityId=${allocationId}`,
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
  
  const result = await response.json();
  
  if (result.success) {
    // Show success message
    showSuccessToast('Hostel allocation released successfully');
    
    // Notifications are automatically sent by backend
    await refreshNotifications();
    
    // Show notification preview
    showNotificationPreview({
      title: 'Notification Sent',
      message: 'Student and parents have been notified about the release'
    });
  }
  
  return result;
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
  ├── AvailableHostelsList.tsx (Browse available hostels)
  └── NotificationBadge.tsx (Shows unread hostel notifications)
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

// Get hostel-related notifications
const getHostelNotifications = async () => {
  const response = await fetch(
    `/api/v1/notifications?referenceType=HOSTEL_ALLOCATION&isRead=false`,
    {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    }
  );
  return response.json();
};

// Mark notification as read when user views allocation
const markNotificationAsRead = async (notificationId: string) => {
  await fetch(`/api/v1/notifications/${notificationId}/read`, {
    method: 'PUT',
    headers: {
      'Authorization': `Bearer ${token}`
    }
  });
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
// ✅ Audit log created (CREATE, HOSTEL_MANAGER)

// 2. Create hostel warden
POST /api/v1/hostel-wardens
// ✅ Audit log created (CREATE, HOSTEL_WARDEN)

// 3. Create hostel
POST /api/v1/hostels
{
  "name": "Boys Hostel A",
  "type": "BOYS",
  "capacity": 200,
  "wardenUuid": "hostel-warden-uuid-456"
}
// ✅ Audit log created (CREATE, HOSTEL)

// 4. Create rooms
POST /api/v1/hostel-rooms
{
  "hostelUuid": "hostel-uuid-789",
  "roomNumber": "101",
  "capacity": 4
}
// ✅ Audit log created (CREATE, HOSTEL_ROOM)

// 5. Allocate student
POST /api/v1/hostel-allocations
{
  "studentUuid": "student-uuid-001",
  "roomUuid": "room-uuid-101"
}
// ✅ Audit log created (CREATE, HOSTEL_ALLOCATION)
// ✅ Notification sent to student
// ✅ Notification sent to all parents
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
10. ✅ **Audit log created** (CREATE action, HOSTEL_ALLOCATION entity)
11. ✅ **Notification sent to student** (in-app notification)
12. ✅ **Notification sent to all parents** (in-app notification)
13. Frontend shows success message
14. Frontend can poll for new notifications or use WebSocket

**API Calls:**
```typescript
POST /api/v1/hostel-allocations
{
  "studentUuid": "student-uuid-001",
  "roomUuid": "room-uuid-101"
}

// What Happens:
// ✅ Allocation created
// ✅ Audit log entry created
// ✅ Notification sent to student
// ✅ Notification sent to parents
// ✅ Frontend receives success response
// ✅ Frontend can poll for new notifications or use WebSocket
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
7. ✅ **Audit log created** (UPDATE action, HOSTEL_ALLOCATION entity)
8. ✅ **Notification sent to student** (in-app notification about release)
9. ✅ **Notification sent to all parents** (in-app notification about release)
10. Student can now be allocated to a new room
11. Frontend shows success message

**API Calls:**
```typescript
POST /api/v1/hostel-allocations/{allocationUuid}/release

// What Happens:
// ✅ Allocation released
// ✅ Audit log entry created (UPDATE action)
// ✅ Notification sent to student
// ✅ Notification sent to parents
// ✅ Room capacity freed
// ✅ Frontend receives success response
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
2. Sees notification badge (if unread notifications exist)
3. Clicks notification to view allocation details OR
4. Navigates to "My Hostel" section
5. Views active hostel allocation (if any)
6. Sees hostel name, room number, roommates
7. Can view hostel history
8. Can view audit log timeline (if permissions allow)

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

## System Integration Features

### **1. Notification System Integration**

The Hostel Management module is fully integrated with the system-wide notification service. Notifications are automatically sent to relevant users when key events occur.

#### **Notification Triggers:**

1. **Student Hostel Allocation Created:**
   - **Recipients**: Student, All Parents
   - **Type**: IN_APP
   - **Title**: "Hostel Allocation: [Hostel Name]"
   - **Content**: "You have been allocated to [Hostel Name], Room [Room Number]."
   - **Action URL**: `/hostel/allocations/{allocationUuid}`
   - **Priority**: 5 (Medium)
   - **Reference Type**: `HOSTEL_ALLOCATION`

2. **Student Hostel Allocation Released:**
   - **Recipients**: Student, All Parents
   - **Type**: IN_APP
   - **Title**: "Hostel Allocation Released: [Hostel Name]"
   - **Content**: "Your hostel allocation has been released from [Hostel Name], Room [Room Number]."
   - **Action URL**: `/hostel/allocations/{allocationUuid}`
   - **Priority**: 5 (Medium)
   - **Reference Type**: `HOSTEL_ALLOCATION`

#### **Notification Flow Example:**
```
Admin creates allocation
    ↓
Backend creates allocation
    ↓
Backend sends notification to Student
    ↓
Backend sends notification to Parent 1
    ↓
Backend sends notification to Parent 2
    ↓
All notifications appear in user's notification center
    ↓
User clicks notification
    ↓
Frontend navigates to allocation details page
    ↓
Notification marked as read
```

---

### **2. Audit Logging System Integration**

All Hostel Management operations are automatically logged in the audit system for compliance, tracking, and security purposes.

#### **Audit Log Entity Types:**

- `HOSTEL` - Hostel creation, updates, deletions
- `HOSTEL_ROOM` - Room creation, updates, deletions
- `HOSTEL_ALLOCATION` - Allocation creation, updates, releases, deletions
- `HOSTEL_WARDEN` - Warden account creation, updates, deletions
- `HOSTEL_MANAGER` - Manager account creation, updates, deletions

#### **Audit Actions Tracked:**

- `CREATE` - When new entities are created
- `UPDATE` - When entities are modified (including releases)
- `DELETE` - When entities are removed

#### **Audit Log Information Captured:**

- User who performed the action
- Timestamp of the action
- Entity type and ID
- Descriptive message
- IP address (if available)
- College context

#### **Example Audit Log Entry:**
```json
{
  "id": 12345,
  "userId": 789,
  "userName": "John Admin",
  "action": "CREATE",
  "entityType": "HOSTEL_ALLOCATION",
  "entityId": 456,
  "description": "Allocated student Alice Johnson to room 101 in Boys Hostel A",
  "ipAddress": "192.168.1.100",
  "collegeId": 1,
  "createdAt": "2024-01-15T10:30:00Z"
}
```

---

## Frontend Notification Integration Patterns

### **Real-time Notification Handling**

**Option 1: Polling (Recommended for MVP)**
```typescript
// Poll for new notifications every 30 seconds
useEffect(() => {
  const interval = setInterval(async () => {
    const response = await fetch('/api/v1/notifications?isRead=false', {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });
    const data = await response.json();
    updateNotificationState(data.data);
  }, 30000);
  
  return () => clearInterval(interval);
}, [token]);
```

**Option 2: WebSocket (Recommended for Production)**
```typescript
// Connect to WebSocket for real-time notifications
useEffect(() => {
  const ws = new WebSocket(`wss://api.example.com/notifications?token=${token}`);
  
  ws.onmessage = (event) => {
    const notification = JSON.parse(event.data);
    if (notification.referenceType === 'HOSTEL_ALLOCATION') {
      showNotificationToast(notification);
      updateNotificationBadge();
    }
  };
  
  return () => ws.close();
}, [token]);
```

### **Notification UI Components**

```typescript
// Notification Badge Component
const NotificationBadge = () => {
  const { unreadCount } = useNotifications();
  
  return (
    <Badge count={unreadCount} showZero={false}>
      <BellIcon />
    </Badge>
  );
};

// Notification Center Component
const NotificationCenter = () => {
  const { notifications, markAsRead } = useNotifications();
  const navigate = useNavigate();
  
  const handleNotificationClick = async (notification) => {
    await markAsRead(notification.id);
    if (notification.actionUrl) {
      navigate(notification.actionUrl);
    }
  };
  
  return (
    <NotificationList>
      {notifications
        .filter(n => n.referenceType === 'HOSTEL_ALLOCATION')
        .map(notification => (
          <NotificationItem
            key={notification.id}
            notification={notification}
            onClick={() => handleNotificationClick(notification)}
          />
        ))}
    </NotificationList>
  );
};
```

### **Notification Toast Integration**

```typescript
// Show toast when allocation is created
const showAllocationNotification = (allocation) => {
  showToast({
    type: 'success',
    title: 'Hostel Allocated',
    message: `${allocation.studentName} allocated to ${allocation.hostelName}, Room ${allocation.roomNumber}`,
    duration: 5000,
    action: {
      label: 'View Details',
      onClick: () => navigate(`/hostel/allocations/${allocation.uuid}`)
    }
  });
};
```

---

## Frontend Audit Log Integration

### **Viewing Audit Logs**

```typescript
// Fetch audit logs for a specific hostel allocation
const fetchAllocationAuditLogs = async (allocationUuid: string) => {
  const allocation = await getHostelAllocation(allocationUuid);
  
  const response = await fetch(
    `/api/v1/audit-logs?entityType=HOSTEL_ALLOCATION&entityId=${allocation.id}`,
    {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    }
  );
  
  return response.json();
};

// Display audit log timeline
const AuditLogTimeline = ({ allocationUuid }) => {
  const [logs, setLogs] = useState([]);
  
  useEffect(() => {
    fetchAllocationAuditLogs(allocationUuid).then(setLogs);
  }, [allocationUuid]);
  
  return (
    <Timeline>
      {logs.map(log => (
        <TimelineItem key={log.id}>
          <TimelineIcon action={log.action} />
          <TimelineContent>
            <User>{log.userName}</User>
            <Action>{log.action}</Action>
            <Description>{log.description}</Description>
            <Timestamp>{formatDate(log.createdAt)}</Timestamp>
          </TimelineContent>
        </TimelineItem>
      ))}
    </Timeline>
  );
};
```

---

## User Interaction Flows with Notifications

### **Flow 1: Student Receives Hostel Allocation Notification**

**Scenario:** Admin allocates a student to a hostel room.

**User Journey:**
1. Admin creates hostel allocation via API
2. Backend automatically sends notifications to:
   - Student's account
   - All parents linked to the student
3. Student logs into their dashboard
4. Student sees notification badge with count of unread notifications
5. Student clicks on notification
6. Notification shows: "You have been allocated to Boys Hostel A, Room 101"
7. Student clicks "View Details" button
8. Frontend navigates to `/hostel/allocations/{allocationUuid}`
9. Student views full allocation details
10. Notification is marked as read
11. Student can now see their hostel allocation in "My Hostel" section

**Frontend Implementation:**
```typescript
// Notification Component
const HostelAllocationNotification = ({ notification }) => {
  const navigate = useNavigate();
  
  const handleClick = async () => {
    // Mark as read
    await markNotificationAsRead(notification.id);
    
    // Navigate to allocation details
    if (notification.actionUrl) {
      navigate(notification.actionUrl);
    }
  };
  
  return (
    <NotificationCard onClick={handleClick}>
      <NotificationIcon type="hostel" />
      <NotificationContent>
        <Title>{notification.title}</Title>
        <Message>{notification.content}</Message>
        <Timestamp>{formatDate(notification.createdAt)}</Timestamp>
      </NotificationContent>
      {!notification.isRead && <UnreadBadge />}
    </NotificationCard>
  );
};
```

---

### **Flow 2: Parent Receives Hostel Allocation Notification**

**Scenario:** Parent receives notification when their child is allocated to hostel.

**User Journey:**
1. Parent logs into parent portal
2. Parent sees notification in notification center
3. Notification shows: "Your child Alice Johnson (10A001) has been allocated to Boys Hostel A, Room 101"
4. Parent clicks notification
5. Frontend navigates to allocation details page
6. Parent can view:
   - Hostel name and details
   - Room number and capacity
   - Allocation date
   - Warden contact information
7. Parent can share this information or contact warden if needed
8. Notification is marked as read

---

### **Flow 3: Student Receives Hostel Release Notification**

**Scenario:** Student's hostel allocation is released by admin.

**User Journey:**
1. Admin releases hostel allocation
2. Backend sends notifications to student and parents
3. Student receives notification: "Your hostel allocation has been released from Boys Hostel A, Room 101"
4. Student views notification and allocation details
5. Student can see the release date and reason (if provided)
6. Student's "My Hostel" section now shows "No Active Allocation"
7. Student can view historical allocations in "Hostel History"

---

## Production-Ready Features

### **✅ System Integrations:**
- **Audit Logging**: All operations are logged with user, timestamp, and action details
- **Notification System**: Automatic notifications sent to students and parents
- **Entity Relationships**: Bidirectional relationships for data consistency
- **Error Handling**: Comprehensive error responses with proper HTTP status codes
- **Security**: Role-based access control with `@PreAuthorize` annotations
- **College Isolation**: Full tenant isolation using `TenantAccessGuard`

### **✅ Frontend Integration Checklist:**

- [ ] Implement notification polling or WebSocket connection
- [ ] Create notification badge component showing unread count
- [ ] Build notification center/dropdown for viewing notifications
- [ ] Add notification toast/popup for real-time alerts
- [ ] Implement notification click handlers to navigate to allocation details
- [ ] Add "Mark as Read" functionality
- [ ] Create audit log viewer component (for admin/manager roles)
- [ ] Implement notification preferences (if applicable)
- [ ] Add notification sound/vibration (mobile apps)
- [ ] Handle notification deep links for mobile apps

### **✅ User Experience Enhancements:**

1. **Notification Badge**: Show unread notification count in header
2. **Notification Center**: Dropdown/modal showing all notifications
3. **Real-time Updates**: Use WebSocket or polling for live notifications
4. **Toast Notifications**: Show temporary success/error messages
5. **Deep Linking**: Navigate directly to allocation details from notification
6. **Notification History**: Allow users to view past notifications
7. **Filter Notifications**: Filter by type (HOSTEL_ALLOCATION, etc.)
8. **Mark All as Read**: Bulk action for notifications

---

## Conclusion

This document provides a complete guide for frontend developers to integrate the Hostel Management system with hostel manager and warden management capabilities. All endpoints are RESTful, follow consistent patterns, and include proper error handling and security measures.

### **Key Features:**
- ✅ **Hostel Manager Management:** Full CRUD operations for hostel manager accounts with audit logging
- ✅ **Hostel Warden Management:** Full CRUD operations for hostel warden accounts with assignment tracking and audit logging
- ✅ **Hostel Management:** Complete hostel catalog management with warden assignment and audit logging
- ✅ **Room Management:** Comprehensive room management with occupancy tracking and audit logging
- ✅ **Allocation Management:** Complete student hostel allocation system with capacity validation, notifications, and audit logging
- ✅ **Active/Inactive Tracking:** Automatic allocation lifecycle management
- ✅ **Statistics & Reporting:** Hostel analytics and monitoring
- ✅ **Multi-Role Support:** Admin, Hostel Manager, Hostel Warden, Teacher, Student roles
- ✅ **College Isolation:** Full tenant isolation for SaaS architecture
- ✅ **Notification System:** Automatic in-app notifications for students and parents on allocation/release
- ✅ **Audit Logging:** Complete audit trail for all operations (CREATE, UPDATE, DELETE)
- ✅ **Bidirectional Relationships:** Proper entity relationships for data consistency

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

---

## API Response Examples with System Features

### **Allocation Creation Response:**
```json
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

**Note:** Notifications and audit logs are created automatically by the backend. The frontend should poll for notifications or use WebSocket to receive them in real-time.

---

## Frontend Implementation Best Practices

### **1. Notification Handling**

**Recommended Approach:**
```typescript
// Custom hook for notifications
const useHostelNotifications = () => {
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);
  
  useEffect(() => {
    // Initial fetch
    fetchNotifications();
    
    // Poll every 30 seconds
    const interval = setInterval(fetchNotifications, 30000);
    
    return () => clearInterval(interval);
  }, []);
  
  const fetchNotifications = async () => {
    const response = await fetch(
      '/api/v1/notifications?referenceType=HOSTEL_ALLOCATION&isRead=false',
      {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      }
    );
    const data = await response.json();
    setNotifications(data.data);
    setUnreadCount(data.data.length);
  };
  
  return { notifications, unreadCount, refresh: fetchNotifications };
};
```

### **2. Error Handling**

```typescript
// Comprehensive error handling
const handleAllocationError = (error: ApiError) => {
  switch (error.status) {
    case 409:
      if (error.message.includes('already has an active')) {
        showErrorToast('Student already has an active allocation');
      } else if (error.message.includes('full capacity')) {
        showErrorToast('Room is at full capacity. Please select another room.');
      }
      break;
    case 404:
      showErrorToast('Resource not found. Please refresh and try again.');
      break;
    case 403:
      showErrorToast('You do not have permission to perform this action.');
      break;
    default:
      showErrorToast('An error occurred. Please try again.');
  }
};
```

### **3. Optimistic UI Updates**

```typescript
// Update UI immediately, then sync with server
const createAllocationOptimistic = async (data) => {
  // Show loading state
  setLoading(true);
  
  // Optimistically add to list
  const tempAllocation = {
    ...data,
    uuid: 'temp-' + Date.now(),
    isActive: true,
    allocatedAt: new Date().toISOString()
  };
  setAllocations(prev => [tempAllocation, ...prev]);
  
  try {
    // Make API call
    const result = await createAllocation(data);
    
    // Replace temp with real data
    setAllocations(prev => 
      prev.map(a => a.uuid === tempAllocation.uuid ? result.data : a)
    );
    
    // Show success and refresh notifications
    showSuccessToast('Allocation created successfully');
    await refreshNotifications();
  } catch (error) {
    // Remove temp on error
    setAllocations(prev => 
      prev.filter(a => a.uuid !== tempAllocation.uuid)
    );
    handleAllocationError(error);
  } finally {
    setLoading(false);
  }
};
```

---

For any questions or clarifications, refer to the API documentation or contact the backend team.

