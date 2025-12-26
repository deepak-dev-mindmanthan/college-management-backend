# Complete Student Admission & Enrollment Flow

## Overview
This document describes the complete API flow for student admission applications, approval process, student creation, and enrollment management in the college management system.

---

## Flow Diagram

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
│  - Auto-generates application number (APP-YYYYMMDD-XXXXX)      │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│  Step 2: PUT /api/v1/admissions/{uuid}                         │
│  - Update application details (DRAFT only)                     │
│  - Add/edit student information, documents                     │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│  Step 3: POST /api/v1/admissions/{uuid}/submit                 │
│  - Submit application                                           │
│  - Status: DRAFT → SUBMITTED                                    │
│  - Sets submittedAt timestamp                                  │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│  Step 4: POST /api/v1/admissions/{uuid}/verify                 │
│  - Verify application (Admin action)                           │
│  - Status: SUBMITTED → VERIFIED                                │
│  - Admin reviews documents and details                         │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│  Step 5: POST /api/v1/admissions/{uuid}/approve                │
│  - Approve application and create Student                      │
│  - Status: VERIFIED → APPROVED                                 │
│  - Creates User account                                        │
│  - Creates Student record                                      │
│  - Optionally creates Enrollment                               │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    STUDENT MANAGEMENT                            │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│  Step 6: POST /api/v1/students/{uuid}/enrollments              │
│  - Create enrollment for student                               │
│  - Links to AcademicYear and ClassRoom                         │
│  - Status: ACTIVE                                               │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│  Step 7: POST /api/v1/students/{uuid}/parents                  │
│  - Assign parent(s) to student                                 │
│  - Sets relationship type (FATHER, MOTHER, GUARDIAN, etc.)     │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│                    ONGOING STUDENT MANAGEMENT                    │
│  - View student details                                         │
│  - Update student information                                   │
│  - Manage enrollments                                           │
│  - Track attendance, fees, exams, etc.                         │
└─────────────────────────────────────────────────────────────────┘
```

---

## Detailed API Flow

### **Phase 1: Create Admission Application**

#### **1.1 Create Admission Application (DRAFT)**

```http
POST /api/v1/admissions
Authorization: Bearer {accessToken}
Content-Type: application/json

Request Body:
{
  "studentName": "John Doe",
  "dob": "2010-05-15",
  "gender": "MALE",
  "email": "john.doe@example.com",
  "phone": "+1234567890",
  "classUuid": "class-uuid-here",
  "previousSchool": "ABC Elementary School",
  "documentsJson": "{\"birthCertificate\": \"url1\", \"marksheet\": \"url2\"}"
}

Response:
{
  "success": true,
  "status": 200,
  "message": "Admission application created successfully",
  "data": {
    "uuid": "admission-uuid-here",
    "applicationNo": "APP-20240115-A1B2C3D4",
    "studentName": "John Doe",
    "dob": "2010-05-15",
    "gender": "MALE",
    "email": "john.doe@example.com",
    "phone": "+1234567890",
    "classUuid": "class-uuid-here",
    "className": "Grade 10",
    "section": "A",
    "previousSchool": "ABC Elementary School",
    "documentsJson": "{\"birthCertificate\": \"url1\", \"marksheet\": \"url2\"}",
    "status": "DRAFT",
    "submittedAt": null,
    "collegeId": 1,
    "createdAt": "2024-01-15T10:00:00",
    "updatedAt": "2024-01-15T10:00:00"
  }
}
```

**What Happens:**
- ✅ Creates `AdmissionApplication` entity in database
- ✅ Auto-generates unique application number (APP-YYYYMMDD-XXXXX format)
- ✅ Sets status to `DRAFT`
- ✅ Links to college (tenant isolation)
- ✅ Optionally links to `ClassRoom` if classUuid provided

**Database State:**
- `admission_applications` table: 1 new record (status: DRAFT)

**Frontend Integration:**
```javascript
// Frontend: Create Admission Form
const createAdmission = async (formData) => {
  const response = await fetch('/api/v1/admissions', {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${accessToken}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      studentName: formData.studentName,
      dob: formData.dob,
      gender: formData.gender,
      email: formData.email,
      phone: formData.phone,
      classUuid: formData.selectedClass,
      previousSchool: formData.previousSchool,
      documentsJson: JSON.stringify(formData.documents)
    })
  });
  
  const result = await response.json();
  // Store admission UUID for later operations
  localStorage.setItem('currentAdmissionUuid', result.data.uuid);
  return result;
};
```

**UI Flow:**
1. Admin navigates to "Admissions" → "New Application"
2. Fill in student information form
3. Upload documents (store URLs in documentsJson)
4. Optionally select class
5. Click "Save Draft" → Creates application with DRAFT status
6. Show application number to user

---

#### **1.2 Update Admission Application (DRAFT Only)**

```http
PUT /api/v1/admissions/{admissionUuid}
Authorization: Bearer {accessToken}
Content-Type: application/json

Request Body:
{
  "studentName": "John Doe Updated",
  "phone": "+1234567891",
  "documentsJson": "{\"birthCertificate\": \"url1\", \"marksheet\": \"url2\", \"photo\": \"url3\"}"
}

Response:
{
  "success": true,
  "status": 200,
  "message": "Admission application updated successfully",
  "data": {
    "uuid": "admission-uuid-here",
    "applicationNo": "APP-20240115-A1B2C3D4",
    "studentName": "John Doe Updated",
    "status": "DRAFT",
    ...
  }
}
```

**What Happens:**
- ✅ Updates admission application fields
- ✅ Only allowed if status is `DRAFT`
- ✅ Returns updated application

**Frontend Integration:**
```javascript
// Frontend: Update Draft Application
const updateAdmission = async (admissionUuid, updates) => {
  const response = await fetch(`/api/v1/admissions/${admissionUuid}`, {
    method: 'PUT',
    headers: {
      'Authorization': `Bearer ${accessToken}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify(updates)
  });
  
  return await response.json();
};
```

**UI Flow:**
1. Edit button enabled only for DRAFT applications
2. Form pre-populated with existing data
3. User makes changes and saves
4. Show success message

---

### **Phase 2: Submit Application**

#### **2.1 Submit Admission Application**

```http
POST /api/v1/admissions/{admissionUuid}/submit
Authorization: Bearer {accessToken}

Response:
{
  "success": true,
  "status": 200,
  "message": "Admission application submitted successfully",
  "data": {
    "uuid": "admission-uuid-here",
    "applicationNo": "APP-20240115-A1B2C3D4",
    "status": "SUBMITTED",
    "submittedAt": "2024-01-15T11:00:00Z",
    ...
  }
}
```

**What Happens:**
- ✅ Updates status: `DRAFT` → `SUBMITTED`
- ✅ Sets `submittedAt` timestamp
- ✅ Application can no longer be edited (only DRAFT can be updated)
- ✅ Application is now ready for admin review

**Database State:**
- `admission_applications` table: Status updated to SUBMITTED
- `submittedAt` field populated

**Frontend Integration:**
```javascript
// Frontend: Submit Application
const submitAdmission = async (admissionUuid) => {
  if (!confirm('Are you sure you want to submit this application? You will not be able to edit it afterwards.')) {
    return;
  }
  
  const response = await fetch(`/api/v1/admissions/${admissionUuid}/submit`, {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${accessToken}`
    }
  });
  
  const result = await response.json();
  // Update UI to show SUBMITTED status
  updateApplicationStatus(admissionUuid, 'SUBMITTED');
  return result;
};
```

**UI Flow:**
1. Show "Submit Application" button on DRAFT applications
2. Confirmation dialog before submission
3. After submission:
   - Disable edit button
   - Show status badge: "SUBMITTED"
   - Show submitted date
   - Display message: "Application submitted. Awaiting verification."

---

### **Phase 3: Verify Application**

#### **3.1 Verify Admission Application (Admin Action)**

```http
POST /api/v1/admissions/{admissionUuid}/verify
Authorization: Bearer {accessToken}

Response:
{
  "success": true,
  "status": 200,
  "message": "Admission application verified successfully",
  "data": {
    "uuid": "admission-uuid-here",
    "applicationNo": "APP-20240115-A1B2C3D4",
    "status": "VERIFIED",
    ...
  }
}
```

**What Happens:**
- ✅ Updates status: `SUBMITTED` → `VERIFIED`
- ✅ Admin has reviewed documents and details
- ✅ Application is ready for approval

**Database State:**
- `admission_applications` table: Status updated to VERIFIED

**Frontend Integration:**
```javascript
// Frontend: Verify Application (Admin Only)
const verifyAdmission = async (admissionUuid) => {
  const response = await fetch(`/api/v1/admissions/${admissionUuid}/verify`, {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${accessToken}`
    }
  });
  
  const result = await response.json();
  // Update UI to show VERIFIED status
  updateApplicationStatus(admissionUuid, 'VERIFIED');
  return result;
};
```

**UI Flow:**
1. Admin views submitted applications
2. Opens application details
3. Reviews documents and information
4. Click "Verify Application" button
5. Status changes to VERIFIED
6. "Approve" button becomes available

---

### **Phase 4: Approve Application & Create Student**

#### **4.1 Approve Admission Application**

```http
POST /api/v1/admissions/{admissionUuid}/approve
Authorization: Bearer {accessToken}
Content-Type: application/json

Request Body:
{
  "email": "john.doe@college.edu",
  "password": "SecurePass123!",
  "rollNumber": "2024-001",
  "registrationNumber": "REG-2024-001",
  "admissionDate": "2024-01-20T00:00:00Z",
  "bloodGroup": "O+",
  "address": "123 Main St, City, State",
  "academicYearUuid": "academic-year-uuid-here"
}

Response:
{
  "success": true,
  "status": 200,
  "message": "Admission application approved and student created successfully",
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
    ...
  }
}
```

**What Happens:**
1. ✅ Updates admission application status: `VERIFIED` → `APPROVED`
2. ✅ Creates `User` entity with `ROLE_STUDENT` role
3. ✅ Creates `Student` entity with provided details
4. ✅ If `appliedClass` exists and `academicYearUuid` provided (or active year exists):
   - Creates `StudentEnrollment` record
   - Links student to class and academic year
   - Sets enrollment status to `ACTIVE`
5. ✅ Returns `StudentResponse` with created student details

**Database State:**
- `admission_applications` table: Status updated to APPROVED
- `users` table: 1 new record (student user)
- `students` table: 1 new record
- `student_enrollments` table: 1 new record (if class/year provided)

**Frontend Integration:**
```javascript
// Frontend: Approve Application
const approveAdmission = async (admissionUuid, approvalData) => {
  const response = await fetch(`/api/v1/admissions/${admissionUuid}/approve`, {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${accessToken}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      email: approvalData.email,
      password: approvalData.password,
      rollNumber: approvalData.rollNumber,
      registrationNumber: approvalData.registrationNumber,
      admissionDate: approvalData.admissionDate,
      bloodGroup: approvalData.bloodGroup,
      address: approvalData.address,
      academicYearUuid: approvalData.academicYearUuid // Optional
    })
  });
  
  const result = await response.json();
  // Navigate to student details page
  navigate(`/students/${result.data.uuid}`);
  return result;
};
```

**UI Flow:**
1. Admin opens VERIFIED application
2. Click "Approve Application" button
3. Modal/form appears with fields:
   - Email (required)
   - Password (required)
   - Roll Number (required)
   - Registration Number (required)
   - Admission Date (required)
   - Blood Group (optional)
   - Address (optional)
   - Academic Year (optional - defaults to active year)
4. Submit approval
5. Success message: "Application approved. Student created successfully."
6. Redirect to student details page
7. Show student information with enrollment details (if created)

---

### **Phase 5: Reject Application (Alternative Path)**

#### **5.1 Reject Admission Application**

```http
POST /api/v1/admissions/{admissionUuid}/reject
Authorization: Bearer {accessToken}

Response:
{
  "success": true,
  "status": 200,
  "message": "Admission application rejected successfully",
  "data": {
    "uuid": "admission-uuid-here",
    "applicationNo": "APP-20240115-A1B2C3D4",
    "status": "REJECTED",
    ...
  }
}
```

**What Happens:**
- ✅ Updates status: Any status → `REJECTED`
- ✅ Application is closed
- ✅ No student record created
- ✅ Cannot be approved after rejection

**Frontend Integration:**
```javascript
// Frontend: Reject Application
const rejectAdmission = async (admissionUuid, reason) => {
  if (!confirm('Are you sure you want to reject this application?')) {
    return;
  }
  
  const response = await fetch(`/api/v1/admissions/${admissionUuid}/reject`, {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${accessToken}`
    }
  });
  
  const result = await response.json();
  // Update UI to show REJECTED status
  updateApplicationStatus(admissionUuid, 'REJECTED');
  return result;
};
```

**UI Flow:**
1. Admin can reject at SUBMITTED or VERIFIED status
2. Confirmation dialog with optional reason field
3. Status changes to REJECTED
4. Show rejection message/badge
5. Application moved to "Rejected" list

---

### **Phase 6: Student Enrollment Management**

#### **6.1 Create Enrollment (If Not Created During Approval)**

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

**What Happens:**
- ✅ Creates `StudentEnrollment` record
- ✅ Links student to academic year and class
- ✅ Sets enrollment status (ACTIVE by default)
- ✅ Validates that student is not already enrolled for the academic year

**Frontend Integration:**
```javascript
// Frontend: Create Enrollment
const createEnrollment = async (studentUuid, enrollmentData) => {
  const response = await fetch(`/api/v1/students/${studentUuid}/enrollments`, {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${accessToken}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      academicYearUuid: enrollmentData.academicYearUuid,
      classUuid: enrollmentData.classUuid,
      rollNumber: enrollmentData.rollNumber,
      status: 'ACTIVE'
    })
  });
  
  return await response.json();
};
```

**UI Flow:**
1. Navigate to student details page
2. Click "Add Enrollment" button
3. Select Academic Year (dropdown)
4. Select Class (dropdown filtered by academic year)
5. Enter roll number (optional)
6. Submit
7. Enrollment appears in student's enrollment list

---

#### **6.2 Update Enrollment Status**

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

**What Happens:**
- ✅ Updates enrollment status
- ✅ Valid statuses: ACTIVE, PROMOTED, DROPPED, COMPLETED
- ✅ Used for tracking student progression

**Frontend Integration:**
```javascript
// Frontend: Update Enrollment Status
const updateEnrollmentStatus = async (studentUuid, enrollmentUuid, newStatus) => {
  const response = await fetch(
    `/api/v1/students/${studentUuid}/enrollments/${enrollmentUuid}/status?status=${newStatus}`,
    {
      method: 'PUT',
      headers: {
        'Authorization': `Bearer ${accessToken}`
      }
    }
  );
  
  return await response.json();
};
```

**UI Flow:**
1. View student enrollments list
2. Click "Change Status" on an enrollment
3. Select new status from dropdown
4. Confirm
5. Status badge updates

---

### **Phase 7: Assign Parents to Student**

#### **7.1 Assign Parent to Student**

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

**What Happens:**
- ✅ Creates `ParentStudent` relationship
- ✅ Links parent to student with relation type
- ✅ Valid relations: FATHER, MOTHER, GUARDIAN, STEPFATHER, STEPMOTHER, OTHER

**Frontend Integration:**
```javascript
// Frontend: Assign Parent
const assignParent = async (studentUuid, parentUuid, relation) => {
  const response = await fetch(`/api/v1/students/${studentUuid}/parents`, {
    method: 'POST',
    headers: {
      'Authorization': `Bearer ${accessToken}`,
      'Content-Type': 'application/json'
    },
    body: JSON.stringify({
      parentUuid: parentUuid,
      relation: relation
    })
  });
  
  return await response.json();
};
```

**UI Flow:**
1. Navigate to student details page
2. Click "Add Parent" button
3. Search/select parent (or create new parent first)
4. Select relationship type
5. Submit
6. Parent appears in student's parent list

---

## Complete Flow Summary

| Step | Endpoint | Purpose | Status Change |
|------|----------|---------|---------------|
| 1 | `POST /api/v1/admissions` | Create admission application | Status: DRAFT |
| 2 | `PUT /api/v1/admissions/{uuid}` | Update application (DRAFT only) | - |
| 3 | `POST /api/v1/admissions/{uuid}/submit` | Submit application | DRAFT → SUBMITTED |
| 4 | `POST /api/v1/admissions/{uuid}/verify` | Verify application (Admin) | SUBMITTED → VERIFIED |
| 5 | `POST /api/v1/admissions/{uuid}/approve` | Approve & create student | VERIFIED → APPROVED, Student created |
| 6 | `POST /api/v1/students/{uuid}/enrollments` | Create enrollment | Enrollment: ACTIVE |
| 7 | `POST /api/v1/students/{uuid}/parents` | Assign parent(s) | Parent linked |

---

## Admission Status Flow

```
DRAFT → SUBMITTED → VERIFIED → APPROVED
                         │
                         └──→ REJECTED (can reject at SUBMITTED or VERIFIED)

States:
- DRAFT: Can be edited/deleted
- SUBMITTED: Awaiting admin review, cannot edit
- VERIFIED: Documents verified, ready for approval
- APPROVED: Student created, application closed
- REJECTED: Application closed, no student created
```

---

## Frontend Integration Guide

### **1. Admission Management Page**

**UI Components Needed:**
- Admission list table with filters (status, class, search)
- Create new application button
- Status badges (DRAFT, SUBMITTED, VERIFIED, APPROVED, REJECTED)
- Action buttons per status:
  - DRAFT: Edit, Submit, Delete
  - SUBMITTED: View, Verify, Reject
  - VERIFIED: View, Approve, Reject
  - APPROVED: View (link to student)
  - REJECTED: View

**Example React Component Structure:**
```jsx
// AdmissionList.jsx
const AdmissionList = () => {
  const [admissions, setAdmissions] = useState([]);
  const [statusFilter, setStatusFilter] = useState('ALL');
  
  // Fetch admissions
  useEffect(() => {
    const fetchAdmissions = async () => {
      const url = statusFilter === 'ALL' 
        ? '/api/v1/admissions'
        : `/api/v1/admissions/status/${statusFilter}`;
      const response = await api.get(url);
      setAdmissions(response.data.data.content);
    };
    fetchAdmissions();
  }, [statusFilter]);
  
  return (
    <div>
      <Filters statusFilter={statusFilter} onStatusChange={setStatusFilter} />
      <AdmissionTable 
        admissions={admissions}
        onEdit={handleEdit}
        onSubmit={handleSubmit}
        onVerify={handleVerify}
        onApprove={handleApprove}
        onReject={handleReject}
      />
    </div>
  );
};
```

---

### **2. Create/Edit Admission Form**

**Form Fields:**
- Student Name (required)
- Date of Birth (required)
- Gender (required, dropdown: MALE, FEMALE, OTHER)
- Email (optional)
- Phone (optional)
- Class (optional, dropdown)
- Previous School (optional)
- Documents Upload (store URLs, save as JSON)

**Form Validation:**
- Required fields must be filled
- Email format validation
- Date validation (DOB should be reasonable)
- Phone format validation (optional)

**Example Form:**
```jsx
// AdmissionForm.jsx
const AdmissionForm = ({ admissionUuid, onSave }) => {
  const [formData, setFormData] = useState({
    studentName: '',
    dob: '',
    gender: '',
    email: '',
    phone: '',
    classUuid: '',
    previousSchool: '',
    documents: []
  });
  
  const handleSubmit = async (e) => {
    e.preventDefault();
    if (admissionUuid) {
      // Update existing
      await updateAdmission(admissionUuid, formData);
    } else {
      // Create new
      const result = await createAdmission(formData);
      admissionUuid = result.data.uuid;
    }
    onSave(admissionUuid);
  };
  
  return (
    <form onSubmit={handleSubmit}>
      {/* Form fields */}
    </form>
  );
};
```

---

### **3. Approval Modal/Form**

**Fields:**
- Email (required, pre-filled from admission if available)
- Password (required, generate or manual)
- Roll Number (required, auto-suggest or manual)
- Registration Number (required, auto-suggest or manual)
- Admission Date (required, date picker, default: today)
- Blood Group (optional, dropdown)
- Address (optional, text area)
- Academic Year (optional, dropdown, default: active year)

**Example Modal:**
```jsx
// ApprovalModal.jsx
const ApprovalModal = ({ admission, onApprove, onClose }) => {
  const [approvalData, setApprovalData] = useState({
    email: admission.email || '',
    password: generatePassword(),
    rollNumber: generateRollNumber(),
    registrationNumber: generateRegistrationNumber(),
    admissionDate: new Date().toISOString(),
    bloodGroup: '',
    address: '',
    academicYearUuid: ''
  });
  
  const handleApprove = async () => {
    const result = await approveAdmission(admission.uuid, approvalData);
    onApprove(result.data);
  };
  
  return (
    <Modal onClose={onClose}>
      <h2>Approve Application: {admission.applicationNo}</h2>
      <form>
        {/* Approval form fields */}
        <button onClick={handleApprove}>Approve & Create Student</button>
      </form>
    </Modal>
  );
};
```

---

### **4. Student Details Page**

**Sections:**
1. **Student Information**
   - Basic details (name, email, roll number, etc.)
   - Edit button

2. **Enrollments Section**
   - List of enrollments (current and historical)
   - Add Enrollment button
   - Status badges per enrollment

3. **Parents Section**
   - List of assigned parents
   - Add Parent button
   - Relationship type badges

4. **Quick Actions**
   - View Attendance
   - View Fees
   - View Exam Results
   - View Documents

**Example Component:**
```jsx
// StudentDetails.jsx
const StudentDetails = ({ studentUuid }) => {
  const [student, setStudent] = useState(null);
  
  useEffect(() => {
    const fetchStudent = async () => {
      const response = await api.get(`/api/v1/students/${studentUuid}/details`);
      setStudent(response.data.data);
    };
    fetchStudent();
  }, [studentUuid]);
  
  return (
    <div>
      <StudentInfo student={student} />
      <EnrollmentsList enrollments={student?.enrollments} />
      <ParentsList parents={student?.parents} />
      <QuickActions studentUuid={studentUuid} />
    </div>
  );
};
```

---

## API Endpoints Reference

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

### **Student Endpoints**

| Method | Endpoint | Description | Roles |
|--------|----------|-------------|-------|
| GET | `/api/v1/students/{uuid}` | Get student details | COLLEGE_ADMIN, SUPER_ADMIN, TEACHER, STUDENT |
| GET | `/api/v1/students/{uuid}/details` | Get full student details | COLLEGE_ADMIN, SUPER_ADMIN, TEACHER, STUDENT |
| POST | `/api/v1/students/{uuid}/enrollments` | Create enrollment | COLLEGE_ADMIN, SUPER_ADMIN |
| PUT | `/api/v1/students/{uuid}/enrollments/{enrollmentUuid}/status` | Update enrollment status | COLLEGE_ADMIN, SUPER_ADMIN |
| POST | `/api/v1/students/{uuid}/parents` | Assign parent | COLLEGE_ADMIN, SUPER_ADMIN |
| DELETE | `/api/v1/students/{uuid}/parents/{parentUuid}` | Remove parent | COLLEGE_ADMIN, SUPER_ADMIN |

---

## Error Handling

Common error scenarios:

1. **409 Conflict:**
   - Email already exists during approval
   - Roll/Registration number already exists
   - Parent already assigned
   - Enrollment already exists for academic year

2. **404 Not Found:**
   - Admission application not found
   - Student not found
   - Class/Academic Year not found

3. **403 Forbidden:**
   - Wrong college (tenant isolation)
   - Insufficient role

4. **400 Bad Request:**
   - Trying to update non-DRAFT application
   - Invalid status transition
   - Validation errors

**Frontend Error Handling:**
```javascript
try {
  const result = await approveAdmission(admissionUuid, approvalData);
  showSuccess('Student created successfully');
  navigate(`/students/${result.data.uuid}`);
} catch (error) {
  if (error.response?.status === 409) {
    showError('Conflict: ' + error.response.data.message);
  } else if (error.response?.status === 404) {
    showError('Resource not found');
  } else {
    showError('An error occurred: ' + error.message);
  }
}
```

---

## Security & Isolation

- ✅ **College Isolation:** All queries filter by `collegeId` (tenant isolation)
- ✅ **Role-Based Access:** 
  - `COLLEGE_ADMIN` and `SUPER_ADMIN` can manage admissions
  - `TEACHER` can view student details
  - `STUDENT` can view own details
- ✅ **JWT Authentication:** All protected endpoints require valid JWT token
- ✅ **Tenant Context:** Automatically set from authenticated user's college
- ✅ **Status Guards:** Business rules enforce valid status transitions

---

## Frontend State Management Recommendations

### **Recommended State Structure:**

```javascript
// Admission State
{
  admissions: {
    items: [],
    filters: {
      status: 'ALL',
      classUuid: null,
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
  currentAdmission: {
    data: null,
    loading: false,
    error: null
  }
}

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
    loading: false,
    error: null
  }
}
```

---

## Future Enhancements

1. **Bulk Operations:**
   - Bulk approve/reject applications
   - Bulk enrollment creation

2. **Email Notifications:**
   - Send email when application submitted
   - Send email when application approved/rejected
   - Send credentials to student when approved

3. **Document Management:**
   - Direct file upload integration
   - Document verification workflow

4. **Application Tracking:**
   - Real-time status updates
   - Application history/audit log

5. **Advanced Filters:**
   - Filter by date range
   - Filter by multiple criteria

6. **Reports:**
   - Admission statistics reports
   - Enrollment reports
   - Student progression reports

