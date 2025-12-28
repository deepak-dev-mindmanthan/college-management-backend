# Complete Exam Management Flow Documentation

## Overview
This document describes the complete API flow for Exam Management in the college management system, including exam creation, class and subject management, student marks entry, grade scale management, transcript generation, and comprehensive reporting for different roles (COLLEGE_ADMIN, TEACHER, STUDENT, PARENT).

---

## User Roles & Permissions

| Role | Exam Management | Class/Subject Setup | Marks Entry | Grade Scales | Transcript Generation | View Exams | View Results | View Transcripts |
|------|----------------|---------------------|-------------|--------------|----------------------|------------|--------------|------------------|
| **SUPER_ADMIN** | ✅ Full Access | ✅ Full Access | ✅ Yes | ✅ Full Access | ✅ Yes | ✅ Yes | ✅ Yes | ✅ Yes |
| **COLLEGE_ADMIN** | ✅ Full Access | ✅ Full Access | ✅ Yes | ✅ Full Access | ✅ Yes | ✅ Yes | ✅ Yes | ✅ Yes |
| **TEACHER** | ✅ Create/Update | ✅ Add Subjects | ✅ Yes (Assigned) | ❌ No | ✅ Generate | ✅ Yes | ✅ Yes | ✅ Yes |
| **STUDENT** | ❌ No | ❌ No | ❌ No | ❌ No | ❌ No | ✅ Yes (Read-only) | ✅ Yes (Own only) | ✅ Yes (Own only) |
| **PARENT** | ❌ No | ❌ No | ❌ No | ❌ No | ❌ No | ❌ No | ✅ Yes (Child only) | ✅ Yes (Child only) |

---

## Flow Diagram: Complete Exam Management Lifecycle

```
┌─────────────────────────────────────────────────────────────────┐
│         EXAM SETUP (COLLEGE_ADMIN / TEACHER)                    │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│  Step 1: POST /api/v1/exams/grade-scales                      │
│  - Create grade scales (A+, A, B+, B, C, etc.)                │
│  - Define min/max marks and grade points                      │
│  - College-specific grade scales                               │
│  - Required before entering marks                              │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│  Step 2: POST /api/v1/exams                                  │
│  - Create exam (Mid Term, Final, Unit Test, etc.)            │
│  - Link to academic year                                       │
│  - Set exam dates and type                                     │
│  - Optionally add classes during creation                      │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│  Step 3: POST /api/v1/exams/{examUuid}/classes               │
│  - Add classes to exam                                         │
│  - Validates class belongs to same academic year               │
│  - Can add multiple classes at once                            │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│  Step 4: POST /api/v1/exams/classes/{examClassUuid}/subjects │
│  - Add subjects to exam class                                  │
│  - Set max marks, pass marks, exam date                        │
│  - Optionally assign teacher for evaluation                    │
│  - Auto-assigns teacher from ClassSubjectTeacher if available  │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│          MARKS ENTRY (TEACHER / ADMIN)                          │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│  Step 5: POST /api/v1/exams/marks/bulk                        │
│  - Enter marks for multiple students at once                   │
│  - Validates student enrollment                                │
│  - Auto-calculates grades based on grade scales                │
│  - Sends notifications to students                             │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│  Step 6: (Optional) POST /api/v1/exams/subjects/{uuid}/      │
│           assign-teacher                                       │
│  - Assign/reassign teacher to evaluate exam subject            │
│  - Teacher gets access to enter marks                          │
│  - Can be done before or after marks entry                     │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│          RESULT GENERATION (ADMIN / TEACHER)                    │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│  Step 7: POST /api/v1/exams/transcripts/generate?            │
│           studentUuid={uuid}&academicYearUuid={uuid}          │
│  - Generate transcript for student and academic year           │
│  - Calculates CGPA from all exam marks                         │
│  - Determines result status (PASS/FAIL/PROMOTED)               │
│  - Includes all subject marks and grades                       │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│  Step 8: POST /api/v1/exams/transcripts/{uuid}/publish       │
│  - Publish transcript                                          │
│  - Sets result status and remarks                              │
│  - Records approver                                            │
│  - Sends notifications to student and parents                  │
│  - Makes transcript visible to students                        │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│          VIEWING RESULTS (STUDENTS / PARENTS)                   │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│  Step 9: GET /api/v1/exams/{examUuid}/students/{uuid}/result│
│  - View complete exam result                                   │
│  - See all subject marks, grades, percentage                   │
│  - View class rank (if calculated)                             │
│  - Check pass/fail status                                      │
└─────────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────────┐
│  Step 10: GET /api/v1/exams/transcripts/student/{uuid}       │
│  - View all transcripts for student                            │
│  - See academic history                                        │
│  - Access published transcripts only                           │
└─────────────────────────────────────────────────────────────────┘
```

---

## Detailed API Flows by User Role

### **Role 1: COLLEGE_ADMIN - Complete Exam Setup**

#### **Flow 1.1: Setup Grade Scales**

**Step 1: Create Grade Scales**
```http
POST /api/v1/exams/grade-scales
Authorization: Bearer {adminAccessToken}
Content-Type: application/json

Request Body:
{
  "grade": "A+",
  "minMarks": 90,
  "maxMarks": 100,
  "gradePoints": 10.00
}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Grade scale created successfully",
  "data": {
    "uuid": "grade-scale-uuid-1",
    "grade": "A+",
    "minMarks": 90,
    "maxMarks": 100,
    "gradePoints": 10.00,
    "collegeId": 1,
    "createdAt": "2024-01-15T10:00:00"
  }
}
```

**Step 2: Create Additional Grade Scales**
- Repeat for A (80-89, 9.0), B+ (70-79, 8.0), B (60-69, 7.0), C (50-59, 6.0), etc.
- Use: `POST /api/v1/exams/grade-scales` for each grade

**Step 3: Verify Grade Scales**
```http
GET /api/v1/exams/grade-scales/all
Authorization: Bearer {adminAccessToken}

Response (200 OK):
{
  "success": true,
  "data": [
    {
      "uuid": "grade-scale-uuid-1",
      "grade": "A+",
      "minMarks": 90,
      "maxMarks": 100,
      "gradePoints": 10.00
    },
    {
      "uuid": "grade-scale-uuid-2",
      "grade": "A",
      "minMarks": 80,
      "maxMarks": 89,
      "gradePoints": 9.00
    }
    // ... more grades
  ]
}
```

#### **Flow 1.2: Create Exam**

**Step 1: Create Exam**
```http
POST /api/v1/exams
Authorization: Bearer {adminAccessToken}
Content-Type: application/json

Request Body:
{
  "name": "Mid Term Examination 2024",
  "examType": "MIDTERM",
  "academicYearUuid": "academic-year-uuid-123",
  "startDate": "2024-03-01T09:00:00Z",
  "endDate": "2024-03-15T17:00:00Z",
  "classUuids": [
    "class-uuid-10A",
    "class-uuid-10B"
  ]
}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Exam created successfully",
  "data": {
    "uuid": "exam-uuid-123",
    "name": "Mid Term Examination 2024",
    "examType": "MIDTERM",
    "academicYearUuid": "academic-year-uuid-123",
    "academicYearName": "2024-25",
    "startDate": "2024-03-01T09:00:00Z",
    "endDate": "2024-03-15T17:00:00Z",
    "collegeId": 1,
    "examClasses": [
      {
        "uuid": "exam-class-uuid-1",
        "className": "10",
        "section": "A"
      },
      {
        "uuid": "exam-class-uuid-2",
        "className": "10",
        "section": "B"
      }
    ],
    "createdAt": "2024-01-15T10:00:00"
  }
}
```

#### **Flow 1.3: Add Subjects to Exam Classes**

**Step 1: Add Subject to Exam Class**
```http
POST /api/v1/exams/classes/{examClassUuid}/subjects
Authorization: Bearer {adminAccessToken}
Content-Type: application/json

Request Body:
{
  "subjectUuid": "subject-uuid-math",
  "maxMarks": 100,
  "passMarks": 35,
  "examDate": "2024-03-05",
  "assignedTeacherUuid": "teacher-uuid-123"  // Optional
}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Subject added to exam class successfully",
  "data": {
    "uuid": "exam-subject-uuid-1",
    "examClassUuid": "exam-class-uuid-1",
    "subjectUuid": "subject-uuid-math",
    "subjectName": "Mathematics",
    "subjectCode": "MATH101",
    "maxMarks": 100,
    "passMarks": 35,
    "examDate": "2024-03-05",
    "assignedTeacherUuid": "teacher-uuid-123",
    "assignedTeacherName": "Dr. John Smith",
    "totalStudents": 0,
    "studentsWithMarks": 0,
    "createdAt": "2024-01-15T10:30:00"
  }
}
```

**Step 2: Add More Subjects**
- Repeat for all subjects: Physics, Chemistry, English, etc.
- Use: `POST /api/v1/exams/classes/{examClassUuid}/subjects` for each subject

**Step 3: Verify Subjects Added**
```http
GET /api/v1/exams/classes/{examClassUuid}/subjects
Authorization: Bearer {adminAccessToken}

Response (200 OK):
{
  "success": true,
  "data": [
    {
      "uuid": "exam-subject-uuid-1",
      "subjectName": "Mathematics",
      "examDate": "2024-03-05",
      "maxMarks": 100
    },
    {
      "uuid": "exam-subject-uuid-2",
      "subjectName": "Physics",
      "examDate": "2024-03-07",
      "maxMarks": 100
    }
    // ... more subjects
  ]
}
```

---

### **Role 2: TEACHER - Marks Entry**

#### **Flow 2.1: Bulk Marks Entry**

**Step 1: Get Students List for Exam Subject**
```http
GET /api/v1/exams/subjects/{examSubjectUuid}
Authorization: Bearer {teacherAccessToken}

Response (200 OK):
{
  "success": true,
  "data": {
    "uuid": "exam-subject-uuid-1",
    "subjectName": "Mathematics",
    "maxMarks": 100,
    "passMarks": 35,
    "examDate": "2024-03-05",
    "totalStudents": 45,
    "studentsWithMarks": 0
  }
}
```

**Step 2: Bulk Enter Marks**
```http
POST /api/v1/exams/marks/bulk
Authorization: Bearer {teacherAccessToken}
Content-Type: application/json

Request Body:
{
  "examSubjectUuid": "exam-subject-uuid-1",
  "marks": [
    {
      "studentUuid": "student-uuid-1",
      "marksObtained": 85
    },
    {
      "studentUuid": "student-uuid-2",
      "marksObtained": 92
    },
    {
      "studentUuid": "student-uuid-3",
      "marksObtained": 78
    }
    // ... more students
  ]
}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Student marks updated successfully",
  "data": [
    {
      "uuid": "student-marks-uuid-1",
      "examSubjectUuid": "exam-subject-uuid-1",
      "examSubjectName": "Mathematics",
      "studentUuid": "student-uuid-1",
      "studentName": "John Doe",
      "rollNumber": "2024001",
      "marksObtained": 85,
      "maxMarks": 100,
      "passMarks": 35,
      "grade": "A",
      "gradePoints": 9.00,
      "isPassed": true
    }
    // ... more marks
  ]
}
```

**Note:** 
- System validates student enrollment automatically
- Auto-assigns grade based on grade scales
- Sends notifications to students
- Updates pass/fail status automatically

#### **Flow 2.2: Individual Marks Entry/Update**

**Step 1: Enter Single Student Marks**
```http
POST /api/v1/exams/marks
Authorization: Bearer {teacherAccessToken}
Content-Type: application/json

Request Body:
{
  "examSubjectUuid": "exam-subject-uuid-1",
  "studentUuid": "student-uuid-5",
  "marksObtained": 68
}

Response (200 OK):
{
  "success": true,
  "data": {
    "uuid": "student-marks-uuid-5",
    "marksObtained": 68,
    "grade": "B",
    "gradePoints": 7.00,
    "isPassed": true
  }
}
```

**Step 2: Update Marks if Needed**
```http
PUT /api/v1/exams/marks/{studentMarksUuid}
Authorization: Bearer {teacherAccessToken}
Content-Type: application/json

Request Body:
{
  "marksObtained": 72
}

Response (200 OK):
{
  "success": true,
  "data": {
    "uuid": "student-marks-uuid-5",
    "marksObtained": 72,
    "grade": "B+",
    "gradePoints": 8.00,
    "isPassed": true
  }
}
```

#### **Flow 2.3: View Marks by Exam Subject**

```http
GET /api/v1/exams/subjects/{examSubjectUuid}/marks
Authorization: Bearer {teacherAccessToken}

Response (200 OK):
{
  "success": true,
  "data": [
    {
      "uuid": "student-marks-uuid-1",
      "studentName": "John Doe",
      "rollNumber": "2024001",
      "marksObtained": 85,
      "grade": "A",
      "isPassed": true
    }
    // ... all students with marks
  ]
}
```

---

### **Role 3: COLLEGE_ADMIN / TEACHER - Transcript Generation**

#### **Flow 3.1: Generate Transcript**

**Step 1: Generate Transcript**
```http
POST /api/v1/exams/transcripts/generate?studentUuid={studentUuid}&academicYearUuid={academicYearUuid}
Authorization: Bearer {adminAccessToken}

Response (200 OK):
{
  "success": true,
  "status": 200,
  "message": "Transcript generated successfully",
  "data": {
    "uuid": "transcript-uuid-123",
    "studentUuid": "student-uuid-1",
    "studentName": "John Doe",
    "rollNumber": "2024001",
    "academicYearUuid": "academic-year-uuid-123",
    "academicYearName": "2024-25",
    "cgpa": 8.75,
    "totalCredits": 20,
    "resultStatus": "PASS",
    "published": false,
    "marks": [
      {
        "examSubjectName": "Mathematics",
        "marksObtained": 85,
        "maxMarks": 100,
        "grade": "A",
        "gradePoints": 9.00
      }
      // ... all subjects
    ],
    "createdAt": "2024-03-20T10:00:00"
  }
}
```

**Step 2: Review and Update Transcript (Optional)**
```http
PUT /api/v1/exams/transcripts/{transcriptUuid}
Authorization: Bearer {adminAccessToken}
Content-Type: application/json

Request Body:
{
  "resultStatus": "PROMOTED",
  "remarks": "Excellent performance. Promoted to next class."
}

Response (200 OK):
{
  "success": true,
  "data": {
    // Updated transcript with new status and remarks
  }
}
```

**Step 3: Publish Transcript**
```http
POST /api/v1/exams/transcripts/{transcriptUuid}/publish
Authorization: Bearer {adminAccessToken}
Content-Type: application/json

Request Body:
{
  "resultStatus": "PROMOTED",
  "remarks": "Excellent performance throughout the year."
}

Response (200 OK):
{
  "success": true,
  "data": {
    "uuid": "transcript-uuid-123",
    "published": true,
    "publishedAt": "2024-03-20T11:00:00Z",
    "approvedByUuid": "admin-uuid-123",
    "approvedByName": "College Admin",
    "resultStatus": "PROMOTED"
  }
}
```

**Note:**
- Publishing sends notifications to student and all parents
- Transcript becomes visible to students and parents
- Cannot be unpublished easily (audit trail maintained)

---

### **Role 4: STUDENT - Viewing Results**

#### **Flow 4.1: View Exam Results**

**Step 1: View All Exams**
```http
GET /api/v1/exams?page=0&size=20
Authorization: Bearer {studentAccessToken}

Response (200 OK):
{
  "success": true,
  "data": {
    "content": [
      {
        "uuid": "exam-uuid-123",
        "name": "Mid Term Examination 2024",
        "examType": "MIDTERM",
        "startDate": "2024-03-01T09:00:00Z",
        "endDate": "2024-03-15T17:00:00Z"
      }
      // ... more exams
    ],
    "totalElements": 5,
    "totalPages": 1
  }
}
```

**Step 2: View Specific Exam Result**
```http
GET /api/v1/exams/{examUuid}/students/{studentUuid}/result
Authorization: Bearer {studentAccessToken}

Response (200 OK):
{
  "success": true,
  "data": {
    "examUuid": "exam-uuid-123",
    "examName": "Mid Term Examination 2024",
    "studentUuid": "student-uuid-1",
    "studentName": "John Doe",
    "rollNumber": "2024001",
    "className": "10",
    "section": "A",
    "marks": [
      {
        "examSubjectName": "Mathematics",
        "marksObtained": 85,
        "maxMarks": 100,
        "grade": "A",
        "isPassed": true
      },
      {
        "examSubjectName": "Physics",
        "marksObtained": 78,
        "maxMarks": 100,
        "grade": "B+",
        "isPassed": true
      }
      // ... all subjects
    ],
    "totalMarks": 500,
    "obtainedMarks": 425,
    "percentage": 85.00,
    "overallGrade": "A",
    "overallGradePoints": 9.00,
    "isPassed": true,
    "rankInClass": 5
  }
}
```

#### **Flow 4.2: View Transcripts**

**Step 1: View All Transcripts**
```http
GET /api/v1/exams/transcripts/student/{studentUuid}
Authorization: Bearer {studentAccessToken}

Response (200 OK):
{
  "success": true,
  "data": [
    {
      "uuid": "transcript-uuid-123",
      "academicYearName": "2024-25",
      "cgpa": 8.75,
      "resultStatus": "PROMOTED",
      "published": true,
      "publishedAt": "2024-03-20T11:00:00Z"
    }
    // ... more transcripts
  ]
}
```

**Step 2: View Specific Transcript**
```http
GET /api/v1/exams/transcripts/{transcriptUuid}
Authorization: Bearer {studentAccessToken}

Response (200 OK):
{
  "success": true,
  "data": {
    "uuid": "transcript-uuid-123",
    "studentName": "John Doe",
    "rollNumber": "2024001",
    "academicYearName": "2024-25",
    "cgpa": 8.75,
    "totalCredits": 20,
    "resultStatus": "PROMOTED",
    "published": true,
    "marks": [
      // All subject marks with grades
    ],
    "remarks": "Excellent performance throughout the year."
  }
}
```

---

### **Role 5: PARENT - Viewing Child's Results**

#### **Flow 5.1: View Child's Results**

**Step 1: Get Child's Exam Results** (via student UUID)
```http
GET /api/v1/exams/{examUuid}/students/{childStudentUuid}/result
Authorization: Bearer {parentAccessToken}

Response (200 OK):
{
  "success": true,
  "data": {
    "examName": "Mid Term Examination 2024",
    "studentName": "John Doe",
    "rollNumber": "2024001",
    "percentage": 85.00,
    "isPassed": true,
    "marks": [
      // Subject-wise marks
    ]
  }
}
```

**Step 2: View Child's Transcripts**
```http
GET /api/v1/exams/transcripts/student/{childStudentUuid}
Authorization: Bearer {parentAccessToken}

Response (200 OK):
{
  "success": true,
  "data": [
    {
      "academicYearName": "2024-25",
      "cgpa": 8.75,
      "resultStatus": "PROMOTED",
      "published": true
    }
  ]
}
```

**Note:** Parents receive notifications when transcripts are published.

---

### **Role 6: COLLEGE_ADMIN - Reports & Analytics**

#### **Flow 6.1: Exam Summary**

```http
GET /api/v1/exams/{examUuid}/summary
Authorization: Bearer {adminAccessToken}

Response (200 OK):
{
  "success": true,
  "data": {
    "examUuid": "exam-uuid-123",
    "examName": "Mid Term Examination 2024",
    "examType": "MIDTERM",
    "academicYearName": "2024-25",
    "startDate": "2024-03-01T09:00:00Z",
    "endDate": "2024-03-15T17:00:00Z",
    "totalClasses": 5,
    "totalSubjects": 25,
    "totalStudents": 250,
    "studentsWithMarks": 240,
    "isCompleted": true
  }
}
```

#### **Flow 6.2: Class-wise Exam Summary**

```http
GET /api/v1/exams/{examUuid}/classes/summaries
Authorization: Bearer {adminAccessToken}

Response (200 OK):
{
  "success": true,
  "data": [
    {
      "examUuid": "exam-uuid-123",
      "examName": "Mid Term Examination 2024",
      "classUuid": "class-uuid-10A",
      "className": "10",
      "section": "A",
      "totalStudents": 45,
      "studentsWithMarks": 45,
      "totalSubjects": 5,
      "averagePercentage": 78.50,
      "passedStudents": 42,
      "failedStudents": 3,
      "passPercentage": 93.33
    }
    // ... more classes
  ]
}
```

#### **Flow 6.3: Class Exam Summary (Detailed)**

```http
GET /api/v1/exams/{examUuid}/classes/{classUuid}/summary
Authorization: Bearer {adminAccessToken}

Response (200 OK):
{
  "success": true,
  "data": {
    "className": "10",
    "section": "A",
    "totalStudents": 45,
    "averagePercentage": 78.50,
    "passedStudents": 42,
    "failedStudents": 3,
    "passPercentage": 93.33
  }
}
```

#### **Flow 6.4: View All Transcripts by Academic Year**

```http
GET /api/v1/exams/transcripts/academic-year/{academicYearUuid}?page=0&size=50
Authorization: Bearer {adminAccessToken}

Response (200 OK):
{
  "success": true,
  "data": {
    "content": [
      {
        "uuid": "transcript-uuid-1",
        "studentName": "John Doe",
        "rollNumber": "2024001",
        "cgpa": 8.75,
        "resultStatus": "PROMOTED",
        "published": true
      }
      // ... more transcripts
    ],
    "totalElements": 250,
    "totalPages": 5
  }
}
```

---

## Integration Scenarios for Frontend

### **Scenario 1: Complete Exam Setup Workflow**

```javascript
// Frontend Integration Example (React/Vue/Angular)

// 1. Setup Grade Scales
async function setupGradeScales() {
  const grades = [
    { grade: "A+", minMarks: 90, maxMarks: 100, gradePoints: 10.00 },
    { grade: "A", minMarks: 80, maxMarks: 89, gradePoints: 9.00 },
    { grade: "B+", minMarks: 70, maxMarks: 79, gradePoints: 8.00 },
    // ... more grades
  ];
  
  for (const grade of grades) {
    await api.post('/api/v1/exams/grade-scales', grade);
  }
}

// 2. Create Exam
async function createExam() {
  const exam = {
    name: "Mid Term Examination 2024",
    examType: "MIDTERM",
    academicYearUuid: selectedAcademicYear.uuid,
    startDate: "2024-03-01T09:00:00Z",
    endDate: "2024-03-15T17:00:00Z",
    classUuids: selectedClasses.map(c => c.uuid)
  };
  
  const response = await api.post('/api/v1/exams', exam);
  return response.data.data;
}

// 3. Add Subjects to Each Exam Class
async function addSubjectsToExam(examUuid, examClassUuid, subjects) {
  for (const subject of subjects) {
    await api.post(`/api/v1/exams/classes/${examClassUuid}/subjects`, {
      subjectUuid: subject.uuid,
      maxMarks: subject.maxMarks,
      passMarks: subject.passMarks,
      examDate: subject.examDate,
      assignedTeacherUuid: subject.teacherUuid // Optional
    });
  }
}
```

### **Scenario 2: Teacher Marks Entry Interface**

```javascript
// Frontend: Marks Entry Form

async function loadStudentsForExamSubject(examSubjectUuid) {
  // Get all students enrolled in the class
  const examSubject = await api.get(`/api/v1/exams/subjects/${examSubjectUuid}`);
  const examClassUuid = examSubject.data.data.examClassUuid;
  
  // Get class details
  const examClass = await api.get(`/api/v1/exams/classes/${examClassUuid}`);
  const classUuid = examClass.data.data.classUuid;
  
  // Get students from class enrollment
  const students = await api.get(`/api/v1/students/class/${classUuid}`);
  return students.data.data;
}

async function submitBulkMarks(examSubjectUuid, marksData) {
  const request = {
    examSubjectUuid: examSubjectUuid,
    marks: marksData.map(m => ({
      studentUuid: m.studentUuid,
      marksObtained: m.marks
    }))
  };
  
  const response = await api.post('/api/v1/exams/marks/bulk', request);
  
  // Show success message with grades calculated
  showNotification('Marks entered successfully. Grades auto-calculated.');
  return response.data.data;
}

// Real-time validation
function validateMarks(marksObtained, maxMarks) {
  if (marksObtained < 0 || marksObtained > maxMarks) {
    return {
      valid: false,
      message: `Marks must be between 0 and ${maxMarks}`
    };
  }
  return { valid: true };
}
```

### **Scenario 3: Student Dashboard - Results View**

```javascript
// Frontend: Student Results Dashboard

async function loadStudentExams(studentUuid) {
  // Get all exams for current academic year
  const activeYear = await api.get('/api/v1/academic-years/active');
  const exams = await api.get(
    `/api/v1/exams/academic-year/${activeYear.data.data.uuid}`
  );
  
  return exams.data.data.content;
}

async function loadExamResult(studentUuid, examUuid) {
  const result = await api.get(
    `/api/v1/exams/${examUuid}/students/${studentUuid}/result`
  );
  
  return result.data.data;
}

// Display result card
function ResultCard({ result }) {
  return (
    <div className="result-card">
      <h3>{result.examName}</h3>
      <div className="overall-score">
        <span>Percentage: {result.percentage}%</span>
        <span>Grade: {result.overallGrade}</span>
        <span>Status: {result.isPassed ? 'PASSED' : 'FAILED'}</span>
      </div>
      <SubjectMarksList marks={result.marks} />
    </div>
  );
}
```

### **Scenario 4: Parent Portal - Child's Results**

```javascript
// Frontend: Parent Dashboard

async function loadChildren(parentUuid) {
  // Get parent's children from parent-student relationships
  const parent = await api.get(`/api/v1/parents/${parentUuid}`);
  const children = await api.get(
    `/api/v1/parents/${parentUuid}/children`
  );
  
  return children.data.data;
}

async function loadChildResults(childStudentUuid) {
  const activeYear = await api.get('/api/v1/academic-years/active');
  
  // Get child's transcript for active year
  const transcript = await api.get(
    `/api/v1/exams/transcripts/student/${childStudentUuid}`
  );
  
  const publishedTranscript = transcript.data.data.find(
    t => t.published && t.academicYearUuid === activeYear.data.data.uuid
  );
  
  if (publishedTranscript) {
    const details = await api.get(
      `/api/v1/exams/transcripts/${publishedTranscript.uuid}`
    );
    return details.data.data;
  }
  
  return null;
}

// Parent notification handler
function handleResultNotification(notification) {
  if (notification.referenceType === 'RESULT') {
    // Show result published notification
    showNotification(`Results published for ${notification.content}`);
    // Navigate to results page
    navigateTo(`/results/${notification.referenceId}`);
  }
}
```

### **Scenario 5: Admin Analytics Dashboard**

```javascript
// Frontend: Admin Analytics

async function loadExamAnalytics(examUuid) {
  // Get exam summary
  const summary = await api.get(`/api/v1/exams/${examUuid}/summary`);
  
  // Get class-wise summaries
  const classSummaries = await api.get(
    `/api/v1/exams/${examUuid}/classes/summaries`
  );
  
  return {
    overall: summary.data.data,
    byClass: classSummaries.data.data
  };
}

// Display analytics
function ExamAnalyticsDashboard({ examUuid }) {
  const [analytics, setAnalytics] = useState(null);
  
  useEffect(() => {
    loadExamAnalytics(examUuid).then(setAnalytics);
  }, [examUuid]);
  
  if (!analytics) return <Loading />;
  
  return (
    <div>
      <SummaryCard data={analytics.overall} />
      <ClassPerformanceChart data={analytics.byClass} />
      <PassFailDistribution data={analytics.byClass} />
    </div>
  );
}
```

---

## API Endpoints Reference

### **Exam Management**
- `POST /api/v1/exams` - Create exam
- `PUT /api/v1/exams/{examUuid}` - Update exam
- `GET /api/v1/exams/{examUuid}` - Get exam details
- `GET /api/v1/exams` - List all exams (paginated)
- `GET /api/v1/exams/all` - List all exams (non-paginated)
- `GET /api/v1/exams/type/{examType}` - Get exams by type
- `GET /api/v1/exams/academic-year/{academicYearUuid}` - Get exams by academic year
- `GET /api/v1/exams/search?q={term}` - Search exams
- `GET /api/v1/exams/{examUuid}/summary` - Get exam summary
- `DELETE /api/v1/exams/{examUuid}` - Delete exam

### **Exam Class Management**
- `POST /api/v1/exams/{examUuid}/classes` - Add class to exam
- `DELETE /api/v1/exams/{examUuid}/classes/{examClassUuid}` - Remove class from exam
- `GET /api/v1/exams/classes/{examClassUuid}` - Get exam class details

### **Exam Subject Management**
- `POST /api/v1/exams/classes/{examClassUuid}/subjects` - Add subject to exam class
- `PUT /api/v1/exams/subjects/{examSubjectUuid}` - Update exam subject
- `DELETE /api/v1/exams/subjects/{examSubjectUuid}` - Remove subject from exam class
- `GET /api/v1/exams/subjects/{examSubjectUuid}` - Get exam subject details
- `GET /api/v1/exams/classes/{examClassUuid}/subjects` - Get all subjects for exam class
- `POST /api/v1/exams/subjects/{examSubjectUuid}/assign-teacher` - Assign teacher to exam subject

### **Student Marks Management**
- `POST /api/v1/exams/marks` - Create student marks (single)
- `PUT /api/v1/exams/marks/{studentMarksUuid}` - Update student marks
- `POST /api/v1/exams/marks/bulk` - Bulk create/update marks
- `GET /api/v1/exams/marks/{studentMarksUuid}` - Get student marks
- `GET /api/v1/exams/subjects/{examSubjectUuid}/marks` - Get all marks for exam subject
- `GET /api/v1/exams/{examUuid}/students/{studentUuid}/result` - Get student exam result
- `DELETE /api/v1/exams/marks/{studentMarksUuid}` - Delete student marks

### **Grade Scale Management**
- `POST /api/v1/exams/grade-scales` - Create grade scale
- `PUT /api/v1/exams/grade-scales/{gradeScaleUuid}` - Update grade scale
- `GET /api/v1/exams/grade-scales/{gradeScaleUuid}` - Get grade scale
- `GET /api/v1/exams/grade-scales` - List grade scales (paginated)
- `GET /api/v1/exams/grade-scales/all` - List all grade scales
- `DELETE /api/v1/exams/grade-scales/{gradeScaleUuid}` - Delete grade scale

### **Transcript Management**
- `POST /api/v1/exams/transcripts/generate?studentUuid={uuid}&academicYearUuid={uuid}` - Generate transcript
- `PUT /api/v1/exams/transcripts/{transcriptUuid}` - Update transcript
- `POST /api/v1/exams/transcripts/{transcriptUuid}/publish` - Publish transcript
- `POST /api/v1/exams/transcripts/{transcriptUuid}/unpublish` - Unpublish transcript
- `GET /api/v1/exams/transcripts/{transcriptUuid}` - Get transcript
- `GET /api/v1/exams/transcripts/student/{studentUuid}` - Get student transcripts
- `GET /api/v1/exams/transcripts/academic-year/{academicYearUuid}` - Get transcripts by academic year
- `GET /api/v1/exams/transcripts/academic-year/{academicYearUuid}/published` - Get published transcripts

### **Reports & Analytics**
- `GET /api/v1/exams/{examUuid}/classes/{classUuid}/summary` - Get class exam summary
- `GET /api/v1/exams/{examUuid}/classes/summaries` - Get all class summaries for exam

---

## Key Integration Points

### **1. Academic Year Integration**
- Exams must be linked to an academic year
- Frontend should fetch active academic year: `GET /api/v1/academic-years/active`
- Validate class belongs to same academic year

### **2. Class & Subject Integration**
- Classes and subjects come from Academic module
- Fetch classes: `GET /api/v1/classes` (filter by academic year)
- Fetch subjects: `GET /api/v1/subjects?classUuid={uuid}`
- Subjects must belong to the class being added to exam

### **3. Student Enrollment Validation**
- System automatically validates student enrollment
- Frontend can pre-fetch enrolled students: `GET /api/v1/students/class/{classUuid}`
- Marks entry will fail if student not enrolled (API handles validation)

### **4. Teacher Assignment Integration**
- Teachers come from Teacher module
- Fetch teachers: `GET /api/v1/teachers`
- Auto-assignment happens if teacher teaches the subject in that class
- Manual assignment: `POST /api/v1/exams/subjects/{uuid}/assign-teacher`

### **5. Notification Integration**
- Notifications sent automatically for:
  - Marks entry/update → Notifies student
  - Transcript publishing → Notifies student + all parents
- Frontend should poll or use WebSocket for notifications
- Endpoint: `GET /api/v1/notifications` (if notification API exists)

### **6. Grade Calculation**
- Grades auto-calculated based on marks and grade scales
- Frontend displays grade but doesn't calculate
- Grade scales must be setup before marks entry

---

## Frontend UI Component Recommendations

### **1. Exam Setup Wizard**
```
Step 1: Select Academic Year
Step 2: Enter Exam Details (name, type, dates)
Step 3: Select Classes
Step 4: Add Subjects (with marks, dates, teachers)
Step 5: Review & Create
```

### **2. Marks Entry Interface**
```
- Bulk entry table (student list with marks input)
- Real-time validation (marks range)
- Auto-grade display
- Save draft functionality
- Bulk submit with progress indicator
```

### **3. Results Dashboard**
```
- Exam cards with summary
- Subject-wise breakdown
- Visual grade indicators
- Download PDF option (if implemented)
- Share results option
```

### **4. Transcript View**
```
- Academic year tabs
- Subject-wise performance
- CGPA visualization
- Pass/Fail indicator
- Print-friendly layout
```

---

## Error Handling

### **Common Error Scenarios**

**1. Student Not Enrolled**
```json
{
  "success": false,
  "status": 409,
  "message": "Student 2024001 is not enrolled in class 10A for academic year 2024-25"
}
```

**2. Marks Out of Range**
```json
{
  "success": false,
  "status": 409,
  "message": "Marks obtained must be between 0 and 100"
}
```

**3. Exam Already Exists**
```json
{
  "success": false,
  "status": 409,
  "message": "Exam with name 'Mid Term Examination 2024' already exists for this academic year"
}
```

**4. Teacher Not Found**
```json
{
  "success": false,
  "status": 404,
  "message": "Teacher not found with UUID: teacher-uuid-123"
}
```

---

## Best Practices for Frontend Integration

1. **Caching Strategy**
   - Cache grade scales (rarely change)
   - Cache active academic year
   - Cache exam list with filters
   - Refresh marks data after entry

2. **Optimistic Updates**
   - Show grade immediately after marks entry
   - Update UI before API confirmation
   - Rollback on error

3. **Loading States**
   - Show loading during bulk operations
   - Progress indicators for bulk marks entry
   - Skeleton loaders for exam lists

4. **Data Validation**
   - Client-side validation before API call
   - Validate marks range (0 to maxMarks)
   - Validate dates (exam date within exam period)
   - Validate pass marks <= max marks

5. **User Feedback**
   - Success notifications after operations
   - Error messages with actionable steps
   - Confirmation dialogs for delete operations
   - Toast notifications for async operations

6. **Real-time Updates**
   - Poll for new notifications (or WebSocket)
   - Refresh exam summaries periodically
   - Update marks count in real-time

---

## Testing Scenarios

### **1. Complete Exam Lifecycle Test**
1. Create grade scales
2. Create exam with classes
3. Add subjects to exam classes
4. Enter marks for all students
5. Generate transcript
6. Publish transcript
7. Verify notifications sent
8. Verify results visible to students

### **2. Bulk Marks Entry Test**
1. Load students for exam subject
2. Enter marks for 50+ students
3. Verify all grades calculated
4. Verify all students notified
5. Check audit logs created

### **3. Error Handling Test**
1. Try entering marks for unenrolled student
2. Try entering marks > max marks
3. Try creating duplicate exam name
4. Verify appropriate error messages

---

## Security Considerations

1. **Role-Based Access Control**
   - All endpoints protected with `@PreAuthorize`
   - Teachers can only modify assigned subjects
   - Students can only view their own results
   - Parents can only view their children's results

2. **Tenant Isolation**
   - All operations automatically filtered by college
   - No cross-tenant data access possible
   - Validated at service level with `TenantAccessGuard`

3. **Audit Trail**
   - All operations logged with user, action, timestamp
   - Cannot delete audit logs
   - IP address tracking for security

---

This document provides a comprehensive guide for frontend developers to integrate the Exam Management module. All endpoints are production-ready with proper authentication, authorization, and tenant isolation.

