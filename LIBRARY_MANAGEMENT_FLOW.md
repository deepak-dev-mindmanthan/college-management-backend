# Complete Library Management Flow Documentation

## Overview
This document describes the complete API flow for Library Management in the college management system, including book management, issue/return operations, and user interactions for different roles (COLLEGE_ADMIN, TEACHER, STUDENT).

---

## User Roles & Permissions

| Role | Book Management | Issue Books | Return Books | View Own Books | View All Books | View Statistics |
|------|----------------|-------------|--------------|----------------|----------------|-----------------|
| **SUPER_ADMIN** | ✅ Full Access | ✅ Yes | ✅ Yes | ✅ Yes | ✅ Yes | ✅ Yes |
| **COLLEGE_ADMIN** | ✅ Full Access | ✅ Yes | ✅ Yes | ✅ Yes | ✅ Yes | ✅ Yes |
| **TEACHER** | ❌ No | ✅ Yes | ✅ Yes | ✅ Yes | ✅ Yes | ✅ Yes |
| **STUDENT** | ❌ No | ❌ No | ❌ No | ✅ Yes | ✅ Yes (Read-only) | ❌ No |

---

## Flow Diagram: Complete Library Management Lifecycle

```
┌─────────────────────────────────────────────────────────────────┐
│              LIBRARY SETUP (COLLEGE_ADMIN)                       │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│  Step 1: POST /api/v1/library/books                            │
│  - Add books to library catalog                                │
│  - Set total copies, category, ISBN                             │
│  - Initially all copies available                               │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│              BOOK DISCOVERY (ALL USERS)                          │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│  Step 2: GET /api/v1/library/books                             │
│  - Browse all books                                             │
│  - Search by title/author/ISBN/category                         │
│  - Filter by category or availability                           │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│              ISSUE BOOK (ADMIN/TEACHER)                          │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│  Step 3: POST /api/v1/library/issues                           │
│  - Issue book to student/staff                                  │
│  - Set due date                                                 │
│  - Book availability decreases                                  │
│  - Status: ISSUED                                               │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│              ACTIVE BORROWING PERIOD                             │
│  - User has book                                                │
│  - Can view their borrowed books                                │
│  - System tracks due date                                       │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│              RETURN BOOK (ADMIN/TEACHER)                         │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│  Step 4: POST /api/v1/library/issues/{uuid}/return             │
│  - Return book                                                  │
│  - Auto-calculate fine if overdue                               │
│  - Book availability increases                                  │
│  - Status: ISSUED → RETURNED                                    │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│              MONITORING & STATISTICS                             │
│  - View overdue books                                           │
│  - Track fines                                                  │
│  - Library summary statistics                                   │
└─────────────────────────────────────────────────────────────────┘
```

---

## Detailed API Flows by User Role

### **Role 1: COLLEGE_ADMIN - Library Management**

#### **Flow 1.1: Add New Books to Library**

**Step 1: Create Book**
```http
POST /api/v1/library/books
Authorization: Bearer {adminAccessToken}
Content-Type: application/json

Request Body:
{
  "isbn": "978-0-123456-78-9",
  "title": "Introduction to Computer Science",
  "author": "John Smith",
  "publisher": "Tech Publishers",
  "category": "Science",
  "totalCopies": 10
}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Book created successfully",
  "data": {
    "uuid": "book-uuid-123",
    "isbn": "978-0-123456-78-9",
    "title": "Introduction to Computer Science",
    "author": "John Smith",
    "publisher": "Tech Publishers",
    "category": "Science",
    "totalCopies": 10,
    "availableCopies": 10,
    "collegeId": 1,
    "createdAt": "2024-01-15T10:30:00",
    "updatedAt": "2024-01-15T10:30:00"
  }
}
```

**What Happens:**
- ✅ Book added to library catalog
- ✅ Initially all copies are available (`availableCopies = totalCopies`)
- ✅ ISBN validated for uniqueness within college
- ✅ Book linked to college (tenant isolation)

---

**Step 2: Update Book Information**
```http
PUT /api/v1/library/books/{bookUuid}
Authorization: Bearer {adminAccessToken}
Content-Type: application/json

Request Body:
{
  "title": "Advanced Computer Science",
  "category": "Advanced Science",
  "totalCopies": 15
}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Book updated successfully",
  "data": {
    "uuid": "book-uuid-123",
    "title": "Advanced Computer Science",
    "category": "Advanced Science",
    "totalCopies": 15,
    "availableCopies": 13,  // 2 already issued, so 13 available
    ...
  }
}
```

**What Happens:**
- ✅ Book details updated
- ✅ Total copies can be increased (if no active issues exceed new total)
- ✅ Available copies recalculated automatically

---

**Step 3: Delete Book (if no active issues)**
```http
DELETE /api/v1/library/books/{bookUuid}
Authorization: Bearer {adminAccessToken}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Book deleted successfully",
  "data": null
}
```

**Error Response (409 Conflict):**
```json
{
  "success": false,
  "status": 409,
  "message": "Cannot delete book. There are 3 active issues. Please return all books first."
}
```

---

#### **Flow 1.2: Issue Books to Users**

**Step 1: Search for User (Student/Staff)**
```http
GET /api/v1/students?q=john
Authorization: Bearer {adminAccessToken}

# Or search teachers
GET /api/v1/teachers?q=john
Authorization: Bearer {adminAccessToken}
```

**Step 2: Check Book Availability**
```http
GET /api/v1/library/books/{bookUuid}
Authorization: Bearer {adminAccessToken}

Response:
{
  "success": true,
  "data": {
    "uuid": "book-uuid-123",
    "title": "Introduction to Computer Science",
    "availableCopies": 5,  // 5 copies available
    ...
  }
}
```

**Step 3: Issue Book to User**
```http
POST /api/v1/library/issues
Authorization: Bearer {adminAccessToken}
Content-Type: application/json

Request Body:
{
  "bookUuid": "book-uuid-123",
  "userUuid": "student-uuid-456",
  "dueDate": "2024-02-15"  // 30 days from now
}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Book issued successfully",
  "data": {
    "uuid": "issue-uuid-789",
    "bookUuid": "book-uuid-123",
    "bookTitle": "Introduction to Computer Science",
    "bookAuthor": "John Smith",
    "issuedToUserUuid": "student-uuid-456",
    "issuedToUserName": "John Doe",
    "issuedToUserEmail": "john.doe@college.edu",
    "issuedByUserUuid": "admin-uuid-001",
    "issuedByUserName": "Library Admin",
    "issueDate": "2024-01-15",
    "dueDate": "2024-02-15",
    "status": "ISSUED",
    "fineAmount": null,
    "createdAt": "2024-01-15T11:00:00"
  }
}
```

**What Happens:**
- ✅ Book issued to user
- ✅ `availableCopies` decreases by 1
- ✅ Issue record created with status `ISSUED`
- ✅ Validates user belongs to same college
- ✅ Validates book availability
- ✅ Prevents duplicate active issues for same book

**Error Scenarios:**
```json
// No copies available
{
  "success": false,
  "status": 409,
  "message": "No copies available for this book"
}

// User already has this book
{
  "success": false,
  "status": 409,
  "message": "User already has an active issue for this book"
}

// User from different college
{
  "success": false,
  "status": 409,
  "message": "User does not belong to a college"
}
```

---

#### **Flow 1.3: Return Books**

**Step 1: View Active Issues**
```http
GET /api/v1/library/issues?status=ISSUED&page=0&size=20
Authorization: Bearer {adminAccessToken}
```

**Step 2: Return Book**
```http
POST /api/v1/library/issues/{issueUuid}/return
Authorization: Bearer {adminAccessToken}
Content-Type: application/json

Request Body (Optional - fine calculated automatically):
{
  "fineAmount": 50.00,  // Optional: manual fine override
  "remarks": "Book returned in good condition"
}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Book returned successfully",
  "data": {
    "uuid": "issue-uuid-789",
    "bookTitle": "Introduction to Computer Science",
    "issuedToUserName": "John Doe",
    "issueDate": "2024-01-15",
    "dueDate": "2024-02-15",
    "returnDate": "2024-02-20",  // Returned 5 days late
    "status": "RETURNED",
    "fineAmount": 50.00,  // 5 days × 10 per day
    ...
  }
}
```

**What Happens:**
- ✅ Book returned
- ✅ `availableCopies` increases by 1
- ✅ Status changes: `ISSUED` → `RETURNED`
- ✅ Fine calculated automatically if overdue (10 currency units per day)
- ✅ Return date set to current date

**Fine Calculation:**
- If `dueDate < returnDate`: Fine = (days overdue) × 10
- If returned on time: `fineAmount = 0` or `null`

---

#### **Flow 1.4: Monitor Library Statistics**

**View Library Summary**
```http
GET /api/v1/library/summary
Authorization: Bearer {adminAccessToken}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Library summary retrieved successfully",
  "data": {
    "totalBooks": 150,
    "totalCopies": 500,
    "availableCopies": 320,
    "issuedBooks": 180,
    "overdueBooks": 12,
    "totalFines": 1500.00,
    "pendingFines": 350.00
  }
}
```

**View Overdue Books**
```http
GET /api/v1/library/issues/overdue?page=0&size=20
Authorization: Bearer {adminAccessToken}

Response:
{
  "success": true,
  "data": {
    "content": [
      {
        "uuid": "issue-uuid-001",
        "bookTitle": "Mathematics Fundamentals",
        "issuedToUserName": "Alice Johnson",
        "dueDate": "2024-01-10",
        "status": "ISSUED",
        // Due date passed, book is overdue
      },
      ...
    ],
    "totalElements": 12,
    "totalPages": 1
  }
}
```

---

### **Role 2: TEACHER - Issue & Return Books**

#### **Flow 2.1: Issue Book to Student**

**Step 1: Search for Available Books**
```http
GET /api/v1/library/books/available?page=0&size=20
Authorization: Bearer {teacherAccessToken}

Response:
{
  "success": true,
  "data": {
    "content": [
      {
        "uuid": "book-uuid-123",
        "title": "Introduction to Computer Science",
        "availableCopies": 5,
        ...
      },
      ...
    ]
  }
}
```

**Step 2: Search Books by Category**
```http
GET /api/v1/library/books/category/Science?page=0&size=20
Authorization: Bearer {teacherAccessToken}
```

**Step 3: Issue Book**
```http
POST /api/v1/library/issues
Authorization: Bearer {teacherAccessToken}
Content-Type: application/json

Request Body:
{
  "bookUuid": "book-uuid-123",
  "userUuid": "student-uuid-456",
  "dueDate": "2024-02-15"
}

Response:
{
  "success": true,
  "message": "Book issued successfully",
  "data": { ... }
}
```

**Step 4: Return Book**
```http
POST /api/v1/library/issues/{issueUuid}/return
Authorization: Bearer {teacherAccessToken}
```

---

### **Role 3: STUDENT - View & Track Books**

#### **Flow 3.1: Browse Library Catalog**

**Step 1: View All Books**
```http
GET /api/v1/library/books?page=0&size=20&sortBy=title&direction=ASC
Authorization: Bearer {studentAccessToken}

Response:
{
  "success": true,
  "data": {
    "content": [
      {
        "uuid": "book-uuid-123",
        "title": "Introduction to Computer Science",
        "author": "John Smith",
        "category": "Science",
        "availableCopies": 5,
        "totalCopies": 10
      },
      ...
    ],
    "totalElements": 150,
    "totalPages": 8
  }
}
```

**Step 2: Search Books**
```http
GET /api/v1/library/books/search?q=computer&page=0&size=20
Authorization: Bearer {studentAccessToken}
```

**Search Capabilities:**
- Search by book title
- Search by author name
- Search by ISBN
- Search by category

**Response:**
```json
{
  "success": true,
  "data": {
    "content": [
      {
        "uuid": "book-uuid-123",
        "title": "Introduction to Computer Science",
        "author": "John Smith",
        "availableCopies": 5
      },
      {
        "uuid": "book-uuid-124",
        "title": "Computer Networks",
        "author": "Jane Doe",
        "availableCopies": 3
      }
    ]
  }
}
```

**Step 3: Filter by Category**
```http
GET /api/v1/library/books/category/Science?page=0&size=20
Authorization: Bearer {studentAccessToken}
```

---

#### **Flow 3.2: View Own Borrowed Books**

**Step 1: Get All My Issues**
```http
GET /api/v1/library/users/{myUserUuid}/issues?page=0&size=20
Authorization: Bearer {studentAccessToken}

Response:
{
  "success": true,
  "data": {
    "content": [
      {
        "uuid": "issue-uuid-001",
        "bookTitle": "Introduction to Computer Science",
        "bookAuthor": "John Smith",
        "issueDate": "2024-01-15",
        "dueDate": "2024-02-15",
        "status": "ISSUED",
        "fineAmount": null
      },
      {
        "uuid": "issue-uuid-002",
        "bookTitle": "Mathematics Fundamentals",
        "issueDate": "2024-01-01",
        "dueDate": "2024-01-31",
        "returnDate": "2024-02-05",
        "status": "RETURNED",
        "fineAmount": 50.00  // Overdue by 5 days
      }
    ],
    "totalElements": 5
  }
}
```

**Step 2: Get Only Active (Currently Borrowed) Books**
```http
GET /api/v1/library/users/{myUserUuid}/issues/active?page=0&size=20
Authorization: Bearer {studentAccessToken}

Response:
{
  "success": true,
  "data": {
    "content": [
      {
        "uuid": "issue-uuid-001",
        "bookTitle": "Introduction to Computer Science",
        "dueDate": "2024-02-15",
        "status": "ISSUED",
        // Only books with status ISSUED
      }
    ]
  }
}
```

**Step 3: Check for Overdue Books**
```http
GET /api/v1/library/users/{myUserUuid}/issues/overdue?page=0&size=20
Authorization: Bearer {studentAccessToken}

Response:
{
  "success": true,
  "data": {
    "content": [
      {
        "uuid": "issue-uuid-003",
        "bookTitle": "Physics Principles",
        "dueDate": "2024-01-10",  // Past due date
        "status": "ISSUED",
        // Fine will be calculated when returned
      }
    ]
  }
}
```

**Step 4: Calculate Fine for Overdue Book**
```http
GET /api/v1/library/issues/{issueUuid}/fine
Authorization: Bearer {studentAccessToken}

Response:
{
  "success": true,
  "data": 50.00  // 5 days overdue × 10 per day
}
```

---

## Complete User Journey Examples

### **Journey 1: Student Discovers and Tracks Books**

```
1. Student logs in → Gets JWT token
   POST /api/v1/auth/login

2. Browse library catalog
   GET /api/v1/library/books?page=0&size=20

3. Search for specific book
   GET /api/v1/library/books/search?q=mathematics

4. Filter by category
   GET /api/v1/library/books/category/Science

5. View book details
   GET /api/v1/library/books/{bookUuid}

6. Check own borrowed books
   GET /api/v1/library/users/{userUuid}/issues/active

7. Check for overdue books
   GET /api/v1/library/users/{userUuid}/issues/overdue

8. Calculate fine if any
   GET /api/v1/library/issues/{issueUuid}/fine
```

---

### **Journey 2: Admin Manages Complete Book Lifecycle**

```
1. Admin logs in
   POST /api/v1/auth/login

2. Add new books to library
   POST /api/v1/library/books
   {
     "title": "New Book",
     "author": "Author Name",
     "totalCopies": 10
   }

3. View all books
   GET /api/v1/library/books

4. Update book information
   PUT /api/v1/library/books/{bookUuid}

5. Issue book to student
   POST /api/v1/library/issues
   {
     "bookUuid": "...",
     "userUuid": "...",
     "dueDate": "2024-02-15"
   }

6. Monitor library statistics
   GET /api/v1/library/summary

7. View overdue books
   GET /api/v1/library/issues/overdue

8. Return book (with auto fine calculation)
   POST /api/v1/library/issues/{issueUuid}/return

9. Delete book (if no active issues)
   DELETE /api/v1/library/books/{bookUuid}
```

---

### **Journey 3: Teacher Issues and Returns Books**

```
1. Teacher logs in
   POST /api/v1/auth/login

2. Search available books
   GET /api/v1/library/books/available?q=physics

3. Issue book to student
   POST /api/v1/library/issues
   {
     "bookUuid": "...",
     "userUuid": "student-uuid",
     "dueDate": "2024-02-15"
   }

4. View all issues
   GET /api/v1/library/issues?status=ISSUED

5. Return book
   POST /api/v1/library/issues/{issueUuid}/return
```

---

## Status Flow Diagrams

### **Book Issue Status Flow**
```
ISSUED → RETURNED
   │
   └─→ OVERDUE (if due date passed, but still not returned)
```

### **Book Availability Flow**
```
Total Copies: 10
     │
     ├─→ Available: 10 (initially)
     │
     ├─→ Issue Book: Available: 9
     │
     ├─→ Issue Another: Available: 8
     │
     ├─→ Return Book: Available: 9
     │
     └─→ Return All: Available: 10
```

---

## Error Handling & Validation

### **Common Error Responses**

**1. Book Not Found (404)**
```json
{
  "success": false,
  "status": 404,
  "message": "Book not found with UUID: invalid-uuid"
}
```

**2. No Copies Available (409)**
```json
{
  "success": false,
  "status": 409,
  "message": "No copies available for this book"
}
```

**3. Duplicate Issue (409)**
```json
{
  "success": false,
  "status": 409,
  "message": "User already has an active issue for this book"
}
```

**4. Invalid Due Date (409)**
```json
{
  "success": false,
  "status": 409,
  "message": "Due date cannot be in the past"
}
```

**5. Already Returned (409)**
```json
{
  "success": false,
  "status": 409,
  "message": "Book has already been returned"
}
```

**6. Access Denied (403)**
```json
{
  "success": false,
  "status": 403,
  "message": "Access denied"
}
// Student trying to view another student's books
```

**7. Cannot Delete (409)**
```json
{
  "success": false,
  "status": 409,
  "message": "Cannot delete book. There are 3 active issues. Please return all books first."
}
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

// Step 3: Use token for all library API calls
const headers = {
  'Authorization': `Bearer ${accessToken}`,
  'Content-Type': 'application/json'
};
```

---

### **2. Book Listing Component**

```javascript
// Fetch all books with pagination
async function fetchBooks(page = 0, size = 20, searchTerm = '') {
  const url = searchTerm 
    ? `/api/v1/library/books/search?q=${searchTerm}&page=${page}&size=${size}`
    : `/api/v1/library/books?page=${page}&size=${size}`;
  
  const response = await fetch(url, {
    headers: {
      'Authorization': `Bearer ${getAccessToken()}`
    }
  });
  
  const { data } = await response.json();
  return data; // Contains content[], totalElements, totalPages
}
```

---

### **3. Issue Book Component (Admin/Teacher)**

```javascript
async function issueBook(bookUuid, userUuid, dueDate) {
  const response = await fetch('/api/v1/library/issues', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${getAccessToken()}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      bookUuid,
      userUuid,
      dueDate // Format: 'YYYY-MM-DD'
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

### **4. Return Book Component**

```javascript
async function returnBook(issueUuid, fineAmount = null) {
  const body = {};
  if (fineAmount) {
    body.fineAmount = fineAmount;
  }
  
  const response = await fetch(`/api/v1/library/issues/${issueUuid}/return`, {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${getAccessToken()}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(body)
  });
  
  return await response.json();
}
```

---

### **5. View My Books (Student)**

```javascript
async function getMyBooks(userUuid) {
  const response = await fetch(
    `/api/v1/library/users/${userUuid}/issues/active?page=0&size=100`,
    {
      headers: {
        'Authorization': `Bearer ${getAccessToken()}`
      }
    }
  );
  
  const { data } = await response.json();
  return data.content;
}

async function getOverdueBooks(userUuid) {
  const response = await fetch(
    `/api/v1/library/users/${userUuid}/issues/overdue`,
    {
      headers: {
        'Authorization': `Bearer ${getAccessToken()}`
      }
    }
  );
  
  const { data } = await response.json();
  return data.content;
}
```

---

### **6. Library Dashboard (Admin)**

```javascript
async function getLibrarySummary() {
  const response = await fetch('/api/v1/library/summary', {
    headers: {
      'Authorization': `Bearer ${getAccessToken()}`
    }
  });
  
  const { data } = await response.json();
  return data; // { totalBooks, totalCopies, availableCopies, issuedBooks, overdueBooks, totalFines, pendingFines }
}
```

---

## UI/UX Recommendations

### **1. Book Search & Filter**
- **Search Bar:** Real-time search as user types (debounced)
- **Category Filter:** Dropdown/buttons for quick category filtering
- **Availability Toggle:** Show only available books checkbox
- **Sort Options:** Sort by title, author, or recently added

### **2. Book Cards Display**
```
┌─────────────────────────────┐
│ [Book Cover/Icon]           │
│                             │
│ Introduction to Computer    │
│ Science                     │
│                             │
│ Author: John Smith          │
│ Category: Science           │
│                             │
│ Available: 5 / 10 copies    │
│ [Available] [Issue] [View]  │
└─────────────────────────────┘
```

### **3. Issue Book Modal**
```
Issue Book
──────────
Book: Introduction to Computer Science
Student: [Search/Select Student]
Due Date: [Date Picker - min: today]

[Cancel] [Issue Book]
```

### **4. My Books Dashboard (Student)**
```
My Borrowed Books
─────────────────
┌─────────────────────────────────────┐
│ ✓ Introduction to Computer Science │
│ Due: Feb 15, 2024                  │
│ Status: Active                     │
│ [View Details]                     │
└─────────────────────────────────────┘

┌─────────────────────────────────────┐
│ ⚠ Mathematics Fundamentals         │
│ Due: Jan 10, 2024 (OVERDUE)        │
│ Fine: ₹50.00                       │
│ [Calculate Fine] [Pay Fine]        │
└─────────────────────────────────────┘
```

### **5. Library Statistics Dashboard (Admin)**
```
Library Statistics
──────────────────
Total Books:        150
Total Copies:       500
Available Copies:   320
Issued Books:       180
Overdue Books:      12 ⚠
Total Fines:        ₹1,500.00
Pending Fines:      ₹350.00
```

---

## Security & Isolation Notes

- ✅ **College Isolation:** All queries automatically filter by `collegeId`
- ✅ **Role-Based Access:** Permissions enforced at service layer
- ✅ **JWT Authentication:** All endpoints require valid token
- ✅ **Tenant Context:** Automatically set from authenticated user's college
- ✅ **User Validation:** Users can only view their own books (except admins/teachers)

---

## Fine Calculation Rules

- **Fine Rate:** 10 currency units per day (configurable)
- **Calculation:** `(returnDate - dueDate) × 10` if overdue
- **Auto-calculation:** Automatic on return if no manual fine specified
- **Manual Override:** Admin can set custom fine amount

---

## Future Enhancements

1. **Reservation System:** Allow students to reserve books
2. **Renewal Feature:** Allow extending due dates
3. **Fine Payment Integration:** Link with payment module
4. **Email Notifications:** Send reminders for due dates
5. **Barcode/QR Code:** Scan books for quick issue/return
6. **Book Recommendations:** Suggest books based on history
7. **Digital Books:** Support for e-books and digital resources
8. **Book Reviews:** Allow students to rate and review books

---

## Testing Scenarios

### **Test Case 1: Complete Book Lifecycle**
1. ✅ Create book with 10 copies
2. ✅ Issue 3 copies → Verify available: 7
3. ✅ Return 1 copy → Verify available: 8
4. ✅ Update total copies to 15 → Verify available: 13
5. ✅ Delete book (when all returned)

### **Test Case 2: Overdue Fine Calculation**
1. ✅ Issue book with due date: 2024-01-15
2. ✅ Return on 2024-01-20 (5 days late)
3. ✅ Verify fine: 50.00 (5 × 10)

### **Test Case 3: Duplicate Issue Prevention**
1. ✅ Issue book to user
2. ✅ Try to issue same book to same user again
3. ✅ Verify error: "User already has an active issue"

### **Test Case 4: College Isolation**
1. ✅ Login as College A admin
2. ✅ Try to access College B's books
3. ✅ Verify 404 or empty results (tenant isolation)

---

## API Endpoint Summary

| Endpoint | Method | Role | Purpose |
|----------|--------|------|---------|
| `/api/v1/library/books` | GET | All | Get all books (paginated) |
| `/api/v1/library/books` | POST | Admin | Create book |
| `/api/v1/library/books/{uuid}` | GET | All | Get book by UUID |
| `/api/v1/library/books/{uuid}` | PUT | Admin | Update book |
| `/api/v1/library/books/{uuid}` | DELETE | Admin | Delete book |
| `/api/v1/library/books/search` | GET | All | Search books |
| `/api/v1/library/books/category/{cat}` | GET | All | Get books by category |
| `/api/v1/library/books/available` | GET | All | Get available books |
| `/api/v1/library/issues` | POST | Admin/Teacher | Issue book |
| `/api/v1/library/issues` | GET | Admin/Teacher | Get all issues |
| `/api/v1/library/issues/{uuid}` | GET | All | Get issue by UUID |
| `/api/v1/library/issues/{uuid}/return` | POST | Admin/Teacher | Return book |
| `/api/v1/library/issues/{uuid}/status` | PUT | Admin | Update issue status |
| `/api/v1/library/issues/{uuid}/fine` | GET | All | Calculate fine |
| `/api/v1/library/issues/status/{status}` | GET | Admin/Teacher | Get issues by status |
| `/api/v1/library/issues/overdue` | GET | Admin/Teacher | Get overdue books |
| `/api/v1/library/users/{uuid}/issues` | GET | All* | Get user's issues |
| `/api/v1/library/users/{uuid}/issues/active` | GET | All* | Get active issues |
| `/api/v1/library/users/{uuid}/issues/overdue` | GET | All* | Get overdue issues |
| `/api/v1/library/summary` | GET | Admin/Teacher | Get library statistics |

*All authenticated users can view their own issues; Admin/Teacher can view any user's issues.

---

## Conclusion

This document provides a complete guide for frontend developers to integrate the Library Management system. All endpoints are RESTful, follow consistent patterns, and include proper error handling and security measures.

For any questions or clarifications, refer to the API documentation or contact the backend team.

