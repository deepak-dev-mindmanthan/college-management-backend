# Complete Attendance Management Flow Documentation

## Overview
This document describes the complete API flow for Attendance Management in the college management system, including session creation, marking attendance, viewing records, and generating attendance summaries for different roles (COLLEGE_ADMIN, TEACHER, STUDENT).

---

## User Roles & Permissions

| Role | Create Session | Mark Attendance | Update Record | View Own Records | View All Records | View Summaries | Delete Session |
|------|----------------|-----------------|---------------|------------------|------------------|----------------|----------------|
| **SUPER_ADMIN** | ✅ Yes | ✅ Yes | ✅ Yes | ✅ Yes | ✅ Yes | ✅ Yes | ✅ Yes |
| **COLLEGE_ADMIN** | ✅ Yes | ✅ Yes | ✅ Yes | ✅ Yes | ✅ Yes | ✅ Yes | ✅ Yes |
| **TEACHER** | ✅ Yes | ✅ Yes | ✅ Yes | ✅ Yes | ✅ Yes | ✅ Yes | ❌ No |
| **STUDENT** | ❌ No | ❌ No | ❌ No | ✅ Yes | ❌ No | ✅ Yes (Own only) | ❌ No |

---

## Flow Diagram: Complete Attendance Management Lifecycle

```
┌─────────────────────────────────────────────────────────────────┐
│         ATTENDANCE SESSION SETUP (ADMIN/TEACHER)                 │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│  Step 1: POST /api/v1/attendance/sessions                      │
│  - Create attendance session for a class                        │
│  - Set date, session type (DAY/PERIOD)                          │
│  - Session created but empty (no records yet)                   │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│              MARK ATTENDANCE (ADMIN/TEACHER)                     │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│  Step 2: POST /api/v1/attendance/mark                          │
│  - Mark attendance for multiple students                        │
│  - Set status: PRESENT/ABSENT/LATE                             │
│  - Creates or updates attendance records                        │
│  - Validates students are enrolled in class                     │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│              VIEW ATTENDANCE RECORDS (ALL ROLES)                 │
│  - View records by session                                      │
│  - View records by student                                      │
│  - View records by date range                                   │
│  - Generate attendance summaries                                │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│              UPDATE ATTENDANCE (ADMIN/TEACHER)                   │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│  Step 3: PUT /api/v1/attendance/records/{uuid}                 │
│  - Update attendance record status                              │
│  - Change PRESENT → ABSENT or vice versa                        │
│  - Update LATE status                                           │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│              ATTENDANCE ANALYSIS & REPORTS                       │
│  - Student attendance summary                                   │
│  - Class attendance summary                                     │
│  - Date range analytics                                         │
└─────────────────────────────────────────────────────────────────┘
```

---

## Detailed API Flows by User Role

### **Role 1: COLLEGE_ADMIN/TEACHER - Attendance Management**

#### **Flow 1.1: Create Attendance Session**

**Step 1: Create Attendance Session**
```http
POST /api/v1/attendance/sessions
Authorization: Bearer {adminAccessToken}
Content-Type: application/json

Request Body:
{
  "classUuid": "class-uuid-123",
  "date": "2024-01-15",
  "sessionType": "DAY"
}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Attendance session created successfully",
  "data": {
    "uuid": "session-uuid-456",
    "classUuid": "class-uuid-123",
    "className": "Grade 10",
    "section": "A",
    "date": "2024-01-15",
    "sessionType": "DAY",
    "totalStudents": 0,
    "presentCount": 0,
    "absentCount": 0,
    "lateCount": 0,
    "collegeId": 1,
    "createdAt": "2024-01-15T08:00:00",
    "updatedAt": "2024-01-15T08:00:00"
  }
}
```

**What Happens:**
- ✅ Attendance session created for specific class and date
- ✅ Session type can be `DAY` (daily attendance) or `PERIOD` (period-wise)
- ✅ Initially empty (no attendance records yet)
- ✅ Validates class exists and belongs to college
- ✅ Prevents duplicate sessions (same class, date, and session type)

**Error Response (409 Conflict):**
```json
{
  "success": false,
  "status": 409,
  "message": "Attendance session already exists for class Grade 10 on 2024-01-15 with session type DAY"
}
```

---

#### **Flow 1.2: Mark Attendance (Bulk)**

**Step 1: Get Enrolled Students for Class**
```http
GET /api/v1/students?classUuid=class-uuid-123
Authorization: Bearer {adminAccessToken}

# This returns all active students enrolled in the class
```

**Step 2: Mark Attendance for Multiple Students**
```http
POST /api/v1/attendance/mark
Authorization: Bearer {adminAccessToken}
Content-Type: application/json

Request Body:
{
  "sessionUuid": "session-uuid-456",
  "records": [
    {
      "studentUuid": "student-uuid-001",
      "status": "PRESENT"
    },
    {
      "studentUuid": "student-uuid-002",
      "status": "PRESENT"
    },
    {
      "studentUuid": "student-uuid-003",
      "status": "ABSENT"
    },
    {
      "studentUuid": "student-uuid-004",
      "status": "LATE"
    }
  ]
}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Attendance marked successfully",
  "data": {
    "uuid": "session-uuid-456",
    "classUuid": "class-uuid-123",
    "className": "Grade 10",
    "section": "A",
    "date": "2024-01-15",
    "sessionType": "DAY",
    "totalStudents": 4,
    "presentCount": 2,
    "absentCount": 1,
    "lateCount": 1,
    "collegeId": 1,
    "createdAt": "2024-01-15T08:00:00",
    "updatedAt": "2024-01-15T08:30:00"
  }
}
```

**What Happens:**
- ✅ Creates or updates attendance records for all students in the request
- ✅ Validates each student is enrolled in the class
- ✅ Sets attendance status (PRESENT/ABSENT/LATE)
- ✅ Updates session statistics (present/absent/late counts)
- ✅ Can mark attendance in bulk for efficiency

**Attendance Status Options:**
- `PRESENT`: Student is present
- `ABSENT`: Student is absent
- `LATE`: Student is late

**Error Scenarios:**
```json
// Student not enrolled in class
{
  "success": false,
  "status": 409,
  "message": "Student John Doe is not enrolled in class Grade 10"
}

// Session not found
{
  "success": false,
  "status": 404,
  "message": "Attendance session not found with UUID: invalid-uuid"
}
```

---

#### **Flow 1.3: Update Individual Attendance Record**

**Update Attendance Status**
```http
PUT /api/v1/attendance/records/{recordUuid}
Authorization: Bearer {adminAccessToken}
Content-Type: application/json

Request Body:
{
  "status": "PRESENT"  // Change from ABSENT to PRESENT
}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Attendance record updated successfully",
  "data": {
    "uuid": "record-uuid-789",
    "sessionUuid": "session-uuid-456",
    "sessionDate": "2024-01-15",
    "sessionType": "DAY",
    "studentUuid": "student-uuid-003",
    "studentName": "John Doe",
    "rollNumber": "10A001",
    "classUuid": "class-uuid-123",
    "className": "Grade 10",
    "status": "PRESENT",
    "createdAt": "2024-01-15T08:30:00",
    "updatedAt": "2024-01-15T09:00:00"
  }
}
```

**What Happens:**
- ✅ Updates attendance status for a specific record
- ✅ Useful for corrections (e.g., marking absent student as present later)
- ✅ Updates session statistics automatically

---

#### **Flow 1.4: View Attendance Sessions**

**Get All Sessions for a Class**
```http
GET /api/v1/attendance/classes/{classUuid}/sessions?page=0&size=20&sortBy=date&direction=DESC
Authorization: Bearer {adminAccessToken}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Attendance sessions retrieved successfully",
  "data": {
    "content": [
      {
        "uuid": "session-uuid-456",
        "classUuid": "class-uuid-123",
        "className": "Grade 10",
        "section": "A",
        "date": "2024-01-15",
        "sessionType": "DAY",
        "totalStudents": 30,
        "presentCount": 28,
        "absentCount": 1,
        "lateCount": 1,
        ...
      },
      ...
    ],
    "totalElements": 50,
    "totalPages": 3
  }
}
```

**Get Sessions by Date Range**
```http
GET /api/v1/attendance/classes/{classUuid}/sessions/range?startDate=2024-01-01&endDate=2024-01-31
Authorization: Bearer {adminAccessToken}

Response (200 OK):
{
  "success": true,
  "data": [
    {
      "uuid": "session-uuid-456",
      "date": "2024-01-15",
      "presentCount": 28,
      "absentCount": 1,
      "lateCount": 1,
      ...
    },
    ...
  ]
}
```

---

#### **Flow 1.5: View Attendance Records**

**Get All Records for a Session**
```http
GET /api/v1/attendance/sessions/{sessionUuid}/records
Authorization: Bearer {adminAccessToken}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Attendance records retrieved successfully",
  "data": [
    {
      "uuid": "record-uuid-001",
      "sessionUuid": "session-uuid-456",
      "sessionDate": "2024-01-15",
      "sessionType": "DAY",
      "studentUuid": "student-uuid-001",
      "studentName": "Alice Johnson",
      "rollNumber": "10A001",
      "classUuid": "class-uuid-123",
      "className": "Grade 10",
      "status": "PRESENT",
      "createdAt": "2024-01-15T08:30:00",
      "updatedAt": "2024-01-15T08:30:00"
    },
    {
      "uuid": "record-uuid-002",
      "studentName": "Bob Smith",
      "rollNumber": "10A002",
      "status": "ABSENT",
      ...
    },
    ...
  ]
}
```

---

#### **Flow 1.6: Generate Class Attendance Summary**

**Get Class Summary for Date Range**
```http
GET /api/v1/attendance/classes/{classUuid}/summary?startDate=2024-01-01&endDate=2024-01-31
Authorization: Bearer {adminAccessToken}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Class attendance summary retrieved successfully",
  "data": {
    "classUuid": "class-uuid-123",
    "className": "Grade 10",
    "section": "A",
    "startDate": "2024-01-01",
    "endDate": "2024-01-31",
    "totalStudents": 30,
    "totalSessions": 22,
    "totalPresent": 600,
    "totalAbsent": 40,
    "totalLate": 20,
    "averageAttendancePercentage": 90.91
  }
}
```

**What Happens:**
- ✅ Calculates total sessions in date range
- ✅ Aggregates present/absent/late counts across all sessions
- ✅ Calculates average attendance percentage
- ✅ Useful for generating monthly/term reports

---

### **Role 2: STUDENT - View Own Attendance**

#### **Flow 2.1: View My Attendance Records**

**Get All My Attendance Records**
```http
GET /api/v1/attendance/students/{studentUuid}/records?page=0&size=20&sortBy=sessionDate&direction=DESC
Authorization: Bearer {studentAccessToken}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Attendance records retrieved successfully",
  "data": {
    "content": [
      {
        "uuid": "record-uuid-001",
        "sessionUuid": "session-uuid-456",
        "sessionDate": "2024-01-15",
        "sessionType": "DAY",
        "studentUuid": "student-uuid-001",
        "studentName": "Alice Johnson",
        "rollNumber": "10A001",
        "classUuid": "class-uuid-123",
        "className": "Grade 10",
        "status": "PRESENT",
        "createdAt": "2024-01-15T08:30:00",
        "updatedAt": "2024-01-15T08:30:00"
      },
      {
        "sessionDate": "2024-01-14",
        "status": "ABSENT",
        ...
      },
      ...
    ],
    "totalElements": 45,
    "totalPages": 3
  }
}
```

**Get My Records for Date Range**
```http
GET /api/v1/attendance/students/{studentUuid}/records/range?startDate=2024-01-01&endDate=2024-01-31
Authorization: Bearer {studentAccessToken}

Response (200 OK):
{
  "success": true,
  "data": [
    {
      "sessionDate": "2024-01-15",
      "status": "PRESENT",
      ...
    },
    ...
  ]
}
```

---

#### **Flow 2.2: View My Attendance Summary**

**Get My Attendance Summary**
```http
GET /api/v1/attendance/students/{studentUuid}/summary?startDate=2024-01-01&endDate=2024-01-31
Authorization: Bearer {studentAccessToken}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Attendance summary retrieved successfully",
  "data": {
    "studentUuid": "student-uuid-001",
    "studentName": "Alice Johnson",
    "rollNumber": "10A001",
    "classUuid": "class-uuid-123",
    "className": "Grade 10",
    "startDate": "2024-01-01",
    "endDate": "2024-01-31",
    "totalDays": 22,
    "presentDays": 20,
    "absentDays": 1,
    "lateDays": 1,
    "attendancePercentage": 90.91
  }
}
```

**What Happens:**
- ✅ Calculates total attendance days in date range
- ✅ Counts present/absent/late days
- ✅ Calculates attendance percentage
- ✅ Useful for students to track their own attendance

---

## Complete User Journey Examples

### **Journey 1: Teacher Marks Daily Attendance**

```
1. Teacher logs in → Gets JWT token
   POST /api/v1/auth/login

2. Select class and date for attendance
   (Frontend: Show class list, date picker)

3. Create attendance session (if not exists)
   POST /api/v1/attendance/sessions
   {
     "classUuid": "...",
     "date": "2024-01-15",
     "sessionType": "DAY"
   }

4. Get enrolled students for the class
   GET /api/v1/students?classUuid=...

5. Mark attendance for all students
   POST /api/v1/attendance/mark
   {
     "sessionUuid": "...",
     "records": [
       { "studentUuid": "...", "status": "PRESENT" },
       { "studentUuid": "...", "status": "ABSENT" },
       ...
     ]
   }

6. View session summary (optional)
   GET /api/v1/attendance/sessions/{sessionUuid}

7. Generate monthly class summary
   GET /api/v1/attendance/classes/{classUuid}/summary?startDate=2024-01-01&endDate=2024-01-31
```

---

### **Journey 2: Student Views Own Attendance**

```
1. Student logs in
   POST /api/v1/auth/login

2. View my attendance records
   GET /api/v1/attendance/students/{myStudentUuid}/records?page=0&size=20

3. Filter by date range
   GET /api/v1/attendance/students/{myStudentUuid}/records/range?startDate=2024-01-01&endDate=2024-01-31

4. View my attendance summary
   GET /api/v1/attendance/students/{myStudentUuid}/summary?startDate=2024-01-01&endDate=2024-01-31

5. Check attendance percentage
   (Display percentage from summary response)
```

---

### **Journey 3: Admin Manages Attendance**

```
1. Admin logs in
   POST /api/v1/auth/login

2. View all attendance sessions for a class
   GET /api/v1/attendance/classes/{classUuid}/sessions?page=0&size=20

3. View specific session records
   GET /api/v1/attendance/sessions/{sessionUuid}/records

4. Update incorrect attendance record
   PUT /api/v1/attendance/records/{recordUuid}
   {
     "status": "PRESENT"  // Correct from ABSENT
   }

5. Generate class attendance report
   GET /api/v1/attendance/classes/{classUuid}/summary?startDate=2024-01-01&endDate=2024-01-31

6. View all sessions by date
   GET /api/v1/attendance/sessions/date/2024-01-15

7. Delete incorrect session (if needed)
   DELETE /api/v1/attendance/sessions/{sessionUuid}
```

---

## Status Flow Diagrams

### **Attendance Status Values**
```
PRESENT  → Student is present
ABSENT   → Student is absent
LATE     → Student is late

Note: Status can be updated between these values
```

### **Attendance Session Lifecycle**
```
Session Created (empty)
    │
    ▼
Attendance Marked (records added)
    │
    ▼
Session Complete (with statistics)
    │
    ├─→ Can update individual records
    │
    └─→ Can delete session (admin only)
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

// Step 3: Use token for all attendance API calls
const headers = {
  'Authorization': `Bearer ${accessToken}`,
  'Content-Type': 'application/json'
};
```

---

### **2. Create Attendance Session Component**

```javascript
async function createAttendanceSession(classUuid, date, sessionType) {
  const response = await fetch('/api/v1/attendance/sessions', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${getAccessToken()}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      classUuid,
      date, // Format: 'YYYY-MM-DD'
      sessionType // 'DAY' or 'PERIOD'
    })
  });
  
  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message);
  }
  
  return await response.json();
}
```

---

### **3. Mark Attendance Component (Bulk)**

```javascript
async function markAttendance(sessionUuid, attendanceRecords) {
  // attendanceRecords: [{ studentUuid, status }, ...]
  const response = await fetch('/api/v1/attendance/mark', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${getAccessToken()}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      sessionUuid,
      records: attendanceRecords
    })
  });
  
  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message);
  }
  
  return await response.json();
}

// Example usage:
const records = [
  { studentUuid: 'student-001', status: 'PRESENT' },
  { studentUuid: 'student-002', status: 'PRESENT' },
  { studentUuid: 'student-003', status: 'ABSENT' },
  { studentUuid: 'student-004', status: 'LATE' }
];

await markAttendance('session-uuid-456', records);
```

---

### **4. View Attendance Sessions Component**

```javascript
async function getSessionsByClass(classUuid, page = 0, size = 20) {
  const response = await fetch(
    `/api/v1/attendance/classes/${classUuid}/sessions?page=${page}&size=${size}&sortBy=date&direction=DESC`,
    {
      headers: {
        'Authorization': `Bearer ${getAccessToken()}`
      }
    }
  );
  
  const { data } = await response.json();
  return data; // Contains content[], totalElements, totalPages
}

async function getSessionsByDateRange(classUuid, startDate, endDate) {
  const response = await fetch(
    `/api/v1/attendance/classes/${classUuid}/sessions/range?startDate=${startDate}&endDate=${endDate}`,
    {
      headers: {
        'Authorization': `Bearer ${getAccessToken()}`
      }
    }
  );
  
  const { data } = await response.json();
  return data; // Array of sessions
}
```

---

### **5. View Attendance Records Component**

```javascript
async function getRecordsBySession(sessionUuid) {
  const response = await fetch(
    `/api/v1/attendance/sessions/${sessionUuid}/records`,
    {
      headers: {
        'Authorization': `Bearer ${getAccessToken()}`
      }
    }
  );
  
  const { data } = await response.json();
  return data; // Array of attendance records
}

async function getRecordsByStudent(studentUuid, page = 0, size = 20) {
  const response = await fetch(
    `/api/v1/attendance/students/${studentUuid}/records?page=${page}&size=${size}&sortBy=sessionDate&direction=DESC`,
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

### **6. Update Attendance Record Component**

```javascript
async function updateAttendanceRecord(recordUuid, status) {
  const response = await fetch(`/api/v1/attendance/records/${recordUuid}`, {
    method: 'PUT',
    headers: {
      'Authorization': `Bearer ${getAccessToken()}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      status // 'PRESENT', 'ABSENT', or 'LATE'
    })
  });
  
  if (!response.ok) {
    const error = await response.json();
    throw new Error(error.message);
  }
  
  return await response.json();
}
```

---

### **7. Attendance Summary Components**

```javascript
async function getStudentAttendanceSummary(studentUuid, startDate, endDate) {
  const response = await fetch(
    `/api/v1/attendance/students/${studentUuid}/summary?startDate=${startDate}&endDate=${endDate}`,
    {
      headers: {
        'Authorization': `Bearer ${getAccessToken()}`
      }
    }
  );
  
  const { data } = await response.json();
  return data; // { totalDays, presentDays, absentDays, lateDays, attendancePercentage, ... }
}

async function getClassAttendanceSummary(classUuid, startDate, endDate) {
  const response = await fetch(
    `/api/v1/attendance/classes/${classUuid}/summary?startDate=${startDate}&endDate=${endDate}`,
    {
      headers: {
        'Authorization': `Bearer ${getAccessToken()}`
      }
    }
  );
  
  const { data } = await response.json();
  return data; // { totalStudents, totalSessions, totalPresent, averageAttendancePercentage, ... }
}
```

---

## UI/UX Recommendations

### **1. Mark Attendance Page (Teacher/Admin)**

```
┌──────────────────────────────────────────────────────────┐
│  Mark Attendance - Grade 10 Section A                    │
│  Date: [2024-01-15]  Session Type: [DAY ▼]              │
├──────────────────────────────────────────────────────────┤
│                                                          │
│  [Create Session] [Load Existing Session]                │
│                                                          │
│  ┌────────────────────────────────────────────────────┐ │
│  │ Roll No │ Student Name      │ Status               │ │
│  ├────────────────────────────────────────────────────┤ │
│  │ 10A001  │ Alice Johnson     │ [●Present] [○Absent] │ │
│  │ 10A002  │ Bob Smith         │ [●Present] [○Absent] │ │
│  │ 10A003  │ Charlie Brown     │ [○Present] [●Absent] │ │
│  │ 10A004  │ Diana Prince      │ [○Present] [○Absent] │ │
│  │         │                   │ [●Late]              │ │
│  │ ...     │ ...               │ ...                  │ │
│  └────────────────────────────────────────────────────┘ │
│                                                          │
│  Summary: Present: 28  Absent: 1  Late: 1  Total: 30    │
│                                                          │
│  [Cancel] [Save Attendance]                              │
└──────────────────────────────────────────────────────────┘
```

**Features:**
- Bulk selection: "Mark All Present" button
- Quick toggle buttons for PRESENT/ABSENT/LATE
- Real-time summary statistics
- Auto-save draft functionality
- Validation: Ensure all students are marked

---

### **2. Attendance Records List (Student View)**

```
┌──────────────────────────────────────────────────────────┐
│  My Attendance Records                                    │
│  Date Range: [2024-01-01] to [2024-01-31] [Apply]        │
├──────────────────────────────────────────────────────────┤
│                                                          │
│  Summary Card:                                           │
│  ┌─────────┬─────────┬─────────┬──────────────┐        │
│  │ Present │ Absent  │  Late   │  Percentage  │        │
│  │   20    │    1    │    1    │    90.91%    │        │
│  └─────────┴─────────┴─────────┴──────────────┘        │
│                                                          │
│  ┌────────────────────────────────────────────────────┐ │
│  │ Date       │ Status   │ Class                      │ │
│  ├────────────────────────────────────────────────────┤ │
│  │ 2024-01-15 │ ✓ Present│ Grade 10 Section A        │ │
│  │ 2024-01-14 │ ✗ Absent │ Grade 10 Section A        │ │
│  │ 2024-01-13 │ ⏱ Late   │ Grade 10 Section A        │ │
│  │ 2024-01-12 │ ✓ Present│ Grade 10 Section A        │ │
│  │ ...        │ ...      │ ...                        │ │
│  └────────────────────────────────────────────────────┘ │
│                                                          │
│  [< Previous] [1] [2] [3] [Next >]                      │
└──────────────────────────────────────────────────────────┘
```

**Features:**
- Color-coded status indicators (Green: Present, Red: Absent, Yellow: Late)
- Date range filter
- Pagination
- Summary card showing overall statistics
- Export to PDF option

---

### **3. Class Attendance Dashboard (Admin/Teacher)**

```
┌──────────────────────────────────────────────────────────┐
│  Class Attendance - Grade 10 Section A                   │
│  [Select Date Range] [Generate Report] [Export]          │
├──────────────────────────────────────────────────────────┤
│                                                          │
│  Overall Statistics:                                     │
│  ┌──────────────────────────────────────────────────┐   │
│  │ Total Students: 30                               │   │
│  │ Total Sessions: 22                               │   │
│  │ Average Attendance: 90.91%                       │   │
│  │ Total Present: 600                               │   │
│  │ Total Absent: 40                                 │   │
│  │ Total Late: 20                                   │   │
│  └──────────────────────────────────────────────────┘   │
│                                                          │
│  Attendance Calendar View:                               │
│  ┌──────────────────────────────────────────────────┐   │
│  │  Jan 2024                                        │   │
│  │  Mon Tue Wed Thu Fri Sat Sun                     │   │
│  │   1   2   3   4   5   6   7                      │   │
│  │  [90%][92%][88%][95%][91%]                       │   │
│  │   8   9  10  11  12  13  14                      │   │
│  │  [93%][89%][94%][92%][90%]                       │   │
│  │  ...                                              │   │
│  └──────────────────────────────────────────────────┘   │
│                                                          │
│  Student-wise Attendance:                                │
│  ┌──────────────────────────────────────────────────┐   │
│  │ Roll No │ Name           │ Present │ %           │   │
│  ├──────────────────────────────────────────────────┤   │
│  │ 10A001  │ Alice Johnson  │   21/22 │ 95.45%      │   │
│  │ 10A002  │ Bob Smith      │   20/22 │ 90.91%      │   │
│  │ 10A003  │ Charlie Brown  │   19/22 │ 86.36% ⚠   │   │
│  │ ...     │ ...            │   ...   │ ...         │   │
│  └──────────────────────────────────────────────────┘   │
└──────────────────────────────────────────────────────────┘
```

**Features:**
- Calendar view with color coding (green: >90%, yellow: 75-90%, red: <75%)
- Student-wise attendance table
- Sorting and filtering options
- Export functionality (PDF, Excel)
- Alert for students with low attendance (<75%)

---

### **4. Attendance Session List**

```
┌──────────────────────────────────────────────────────────┐
│  Attendance Sessions - Grade 10 Section A                │
│  [Filter by Date Range] [Create New Session]             │
├──────────────────────────────────────────────────────────┤
│                                                          │
│  ┌────────────────────────────────────────────────────┐ │
│  │ Date       │ Type │ Present │ Absent │ Late │ Total │ │
│  ├────────────────────────────────────────────────────┤ │
│  │ 2024-01-15 │ DAY  │   28    │   1    │  1   │  30   │ │
│  │            │      │ [View] [Edit] [Delete]         │ │
│  ├────────────────────────────────────────────────────┤ │
│  │ 2024-01-14 │ DAY  │   29    │   0    │  1   │  30   │ │
│  │            │      │ [View] [Edit] [Delete]         │ │
│  ├────────────────────────────────────────────────────┤ │
│  │ 2024-01-13 │ PERIOD│  28    │   1    │  1   │  30   │ │
│  │            │      │ [View] [Edit] [Delete]         │ │
│  │ ...        │ ...  │   ...   │  ...  │ ... │  ...  │ │
│  └────────────────────────────────────────────────────┘ │
│                                                          │
│  [< Previous] [1] [2] [3] [Next >]                      │
└──────────────────────────────────────────────────────────┘
```

---

## Error Handling & Validation

### **Common Error Responses**

**1. Session Already Exists (409)**
```json
{
  "success": false,
  "status": 409,
  "message": "Attendance session already exists for class Grade 10 on 2024-01-15 with session type DAY"
}
```

**2. Student Not Enrolled (409)**
```json
{
  "success": false,
  "status": 409,
  "message": "Student John Doe is not enrolled in class Grade 10"
}
```

**3. Session Not Found (404)**
```json
{
  "success": false,
  "status": 404,
  "message": "Attendance session not found with UUID: invalid-uuid"
}
```

**4. Invalid Date Range (409)**
```json
{
  "success": false,
  "status": 409,
  "message": "Start date cannot be after end date"
}
```

**5. Access Denied (403)**
```json
{
  "success": false,
  "status": 403,
  "message": "Access denied"
}
// Student trying to view another student's attendance or unauthorized access
```

**6. Class Not Found (404)**
```json
{
  "success": false,
  "status": 404,
  "message": "Class not found with UUID: invalid-uuid"
}
```

---

## Security & Isolation Notes

- ✅ **College Isolation:** All queries automatically filter by `collegeId`
- ✅ **Role-Based Access:** Permissions enforced at service layer
- ✅ **JWT Authentication:** All endpoints require valid token
- ✅ **Tenant Context:** Automatically set from authenticated user's college
- ✅ **Student Validation:** Students can only view their own attendance records
- ✅ **Enrollment Validation:** Only enrolled students can have attendance marked

---

## Session Type Explanation

### **DAY Session Type**
- Used for daily attendance
- One session per class per day
- Typically marked once per day (e.g., morning attendance)

### **PERIOD Session Type**
- Used for period-wise attendance
- Multiple sessions per class per day (one per period)
- Useful for tracking attendance in each subject/period

**Example:**
```
Class: Grade 10 Section A
Date: 2024-01-15

Session 1: DAY (overall daily attendance)
Session 2: PERIOD (Math period)
Session 3: PERIOD (Science period)
Session 4: PERIOD (English period)
...
```

---

## API Endpoint Summary

| Endpoint | Method | Role | Purpose |
|----------|--------|------|---------|
| `/api/v1/attendance/sessions` | POST | Admin/Teacher | Create attendance session |
| `/api/v1/attendance/sessions/{uuid}` | GET | All | Get session by UUID |
| `/api/v1/attendance/sessions/{uuid}` | DELETE | Admin | Delete session |
| `/api/v1/attendance/sessions/date/{date}` | GET | Admin/Teacher | Get all sessions for a date |
| `/api/v1/attendance/sessions/range` | GET | Admin/Teacher | Get sessions by date range |
| `/api/v1/attendance/classes/{uuid}/sessions` | GET | All | Get sessions for a class |
| `/api/v1/attendance/classes/{uuid}/sessions/range` | GET | All | Get sessions for class by date range |
| `/api/v1/attendance/classes/{uuid}/summary` | GET | Admin/Teacher | Get class attendance summary |
| `/api/v1/attendance/mark` | POST | Admin/Teacher | Mark attendance (bulk) |
| `/api/v1/attendance/records/{uuid}` | GET | All | Get record by UUID |
| `/api/v1/attendance/records/{uuid}` | PUT | Admin/Teacher | Update attendance record |
| `/api/v1/attendance/sessions/{uuid}/records` | GET | All | Get all records for a session |
| `/api/v1/attendance/students/{uuid}/records` | GET | All* | Get student's records |
| `/api/v1/attendance/students/{uuid}/records/range` | GET | All* | Get student's records by date range |
| `/api/v1/attendance/students/{uuid}/summary` | GET | All* | Get student attendance summary |

*All authenticated users can view their own records; Admin/Teacher can view any student's records.

---

## Future Enhancements

1. **Bulk Session Creation:** Create sessions for multiple days/classes at once
2. **Attendance Alerts:** Send notifications for low attendance or absent students
3. **Parent Portal Integration:** Allow parents to view their child's attendance
4. **QR Code Attendance:** Scan QR codes for quick attendance marking
5. **Biometric Integration:** Fingerprint/face recognition for attendance
6. **Automated Reminders:** Email/SMS reminders for absent students
7. **Attendance Reports:** Generate PDF/Excel reports with charts and graphs
8. **Holiday Management:** Mark holidays and exclude from attendance calculations
9. **Leave Management Integration:** Link with leave requests to mark as present
10. **Mobile App Support:** Mobile-optimized views for teachers to mark attendance on-the-go

---

## Testing Scenarios

### **Test Case 1: Complete Attendance Flow**
1. ✅ Create attendance session for a class
2. ✅ Mark attendance for multiple students (bulk)
3. ✅ View session with statistics
4. ✅ Update individual attendance record
5. ✅ View attendance records by student
6. ✅ Generate attendance summary

### **Test Case 2: Date Range Queries**
1. ✅ Get sessions for a class within date range
2. ✅ Get student records within date range
3. ✅ Verify summary calculations for date range
4. ✅ Test with invalid date ranges (startDate > endDate)

### **Test Case 3: College Isolation**
1. ✅ Login as College A admin
2. ✅ Try to access College B's attendance sessions
3. ✅ Verify 404 or empty results (tenant isolation)

### **Test Case 4: Student Enrollment Validation**
1. ✅ Try to mark attendance for non-enrolled student
2. ✅ Verify error: "Student is not enrolled in class"

### **Test Case 5: Duplicate Session Prevention**
1. ✅ Create session for class, date, and session type
2. ✅ Try to create duplicate session
3. ✅ Verify error: "Attendance session already exists"

---

## Conclusion

This document provides a complete guide for frontend developers to integrate the Attendance Management system. All endpoints are RESTful, follow consistent patterns, and include proper error handling and security measures.

**Key Features:**
- ✅ Support for DAY and PERIOD-based attendance
- ✅ Bulk attendance marking for efficiency
- ✅ Comprehensive attendance summaries and statistics
- ✅ Date range queries for flexible reporting
- ✅ Role-based access control
- ✅ College-level isolation

For any questions or clarifications, refer to the API documentation or contact the backend team.

