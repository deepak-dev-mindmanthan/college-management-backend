package org.collegemanagement.controllers;


import org.collegemanagement.entity.academic.Course;
import org.collegemanagement.entity.academic.Subject;
import org.collegemanagement.entity.communication.Notification;
import org.collegemanagement.entity.exam.Exam;
import org.collegemanagement.entity.fees.StudentFee;
import org.collegemanagement.entity.user.User;
import org.collegemanagement.exception.ResourceNotFoundException;
import org.collegemanagement.dto.*;
import org.collegemanagement.enums.RoleType;
import org.collegemanagement.services.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@PreAuthorize("hasAnyRole('COLLEGE_ADMIN','SUPER_ADMIN')")
@RequestMapping("/api/v1/college-admin")
public class CollegeAdminController {

    private final UserManager userManager;
    private final CollegeService collegeService;
    private final RoleService roleService;
    private final CourseService courseService;
    private final SubjectService subjectService;
    private final ExamService examService;
    private final NotificationService notificationService;

    public CollegeAdminController(UserManager userManager,
                                  CollegeService collegeService,
                                  RoleService roleService,
                                  CourseService courseService,
                                  SubjectService subjectService,

                                  ExamService examService,
                                  NotificationService notificationService) {
        this.userManager = userManager;
        this.collegeService = collegeService;
        this.roleService = roleService;
        this.courseService = courseService;
        this.subjectService = subjectService;
        this.examService = examService;
        this.notificationService = notificationService;
    }


    @PreAuthorize("hasPermission(#request.collegeId, null, null)")
    @PostMapping("/teachers")
    public ResponseEntity<?> createTeacher(@RequestBody CreateTeacherOrStudentRequest request) {
        if (!collegeService.existsById(request.getCollegeId())) {
            return ResponseEntity.badRequest().body("No college found with id " + request.getCollegeId());
        }
        User newTeacher = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(request.getPassword())
                .roles(roleService.getRoles(RoleType.ROLE_TEACHER))
                .college(collegeService.findById(request.getCollegeId()))
                .build();
        userManager.createUser(newTeacher);
        return ResponseEntity.ok("teacher successfully created");
    }

    @PreAuthorize("hasPermission(#collegeId, null, null)")
    @GetMapping("/teachers/{collegeId}")
    public ResponseEntity<List<User>> getAllTeachers(@PathVariable Long collegeId) {
        List<User> teachers = userManager.findByCollegeIdAndRoles(collegeId, roleService.getRoleByName(RoleType.ROLE_TEACHER));
        return ResponseEntity.ok(teachers);
    }

    @PreAuthorize("hasPermission(#request.collegeId, null, null)")
    @PutMapping("/teachers/{id}")
    public ResponseEntity<?> updateTeacher(@PathVariable Long id, @RequestBody CreateTeacherOrStudentRequest request) {
        User updateUser = User.builder()
                .id(id)
                .name(request.getName())
                .email(request.getEmail())
                .password(request.getPassword())
                .roles(roleService.getRoles(RoleType.ROLE_TEACHER))
                .build();
        userManager.update(updateUser);
        return ResponseEntity.ok("Teacher updated successfully.");
    }

    @DeleteMapping("/teachers/{id}")
    public ResponseEntity<?> deleteTeacher(@PathVariable Long id) {
        if (!userManager.userExists(id)) {
            return ResponseEntity.badRequest().body("Teacher not found.");
        }
        userManager.deleteUserById(id);
        return ResponseEntity.ok("Teacher deleted successfully.");
    }


    @PreAuthorize("hasPermission(#request.collegeId, null, null)")
    @PostMapping("/students")
    public ResponseEntity<?> createStudent(@RequestBody CreateTeacherOrStudentRequest request) {
        if (!collegeService.existsById(request.getCollegeId())) {
            return ResponseEntity.badRequest().body("No college found with id " + request.getCollegeId());
        }

        User newStudent = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(request.getPassword())
                .roles(roleService.getRoles(RoleType.ROLE_STUDENT))
                .college(collegeService.findById(request.getCollegeId()))
                .build();

        userManager.createUser(newStudent);

        return ResponseEntity.ok("student successfully created");
    }

    @PreAuthorize("hasPermission(#collegeId, null, null)")
    @GetMapping("/students/{collegeId}")
    public ResponseEntity<List<User>> getAllStudents(@PathVariable Long collegeId) {
        List<User> students = userManager.findByCollegeIdAndRoles(collegeId, roleService.getRoleByName(RoleType.ROLE_STUDENT));
        return ResponseEntity.ok(students);
    }

    @DeleteMapping("/students/{id}")
    public ResponseEntity<?> deleteStudent(@PathVariable Long id) {
        if (!userManager.userExists(id)) {
            return ResponseEntity.badRequest().body("Student not found.");
        }
        userManager.deleteUserById(id);
        return ResponseEntity.ok(Map.of("message", "Student deleted successfully."));
    }


    @PutMapping("/students/{id}")
    public ResponseEntity<?> updateStudent(@PathVariable Long id, @RequestBody CreateTeacherOrStudentRequest request) {

        User updateUser = User.builder()
                .id(id)
                .name(request.getName())
                .email(request.getEmail())
                .password(request.getPassword())
                .roles(roleService.getRoles(RoleType.ROLE_STUDENT))
                .build();

        userManager.update(updateUser);
        return ResponseEntity.ok(Map.of("message", "Student updated successfully."));
    }

    // ACCOUNTANT MANAGEMENT
    @PostMapping("/accountants")
    public ResponseEntity<?> createAccountant(@RequestBody CreateTeacherOrStudentRequest request) {
        if (!collegeService.existsById(request.getCollegeId())) {
            throw new ResourceNotFoundException("No college found with id " + request.getCollegeId());
        }
        User accountant = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(request.getPassword())
                .roles(roleService.getRoles(RoleType.ROLE_ACCOUNTANT))
                .college(collegeService.findById(request.getCollegeId()))
                .build();
        userManager.createUser(accountant);
        return ResponseEntity.ok(Map.of("message", "Accountant created successfully."));
    }

    @GetMapping("/accountants/{collegeId}")
    public ResponseEntity<List<User>> getAccountants(@PathVariable Long collegeId) {
        List<User> accountants = userManager.findByCollegeIdAndRoles(collegeId, roleService.getRoleByName(RoleType.ROLE_ACCOUNTANT));
        return ResponseEntity.ok(accountants);
    }

    @PutMapping("/accountants/{id}")
    public ResponseEntity<?> updateAccountant(@PathVariable Long id, @RequestBody CreateTeacherOrStudentRequest request) {

        User updateUser = User.builder()
                .id(id)
                .name(request.getName())
                .email(request.getEmail())
                .password(request.getPassword())
                .roles(roleService.getRoles(RoleType.ROLE_ACCOUNTANT))
                .build();

        userManager.update(updateUser);
        return ResponseEntity.ok(Map.of("message", "Accountant updated successfully."));
    }

    @DeleteMapping("/accountants/{id}")
    public ResponseEntity<?> deleteAccountant(@PathVariable Long id) {
        if (!userManager.userExists(id)) {
            return ResponseEntity.badRequest().body("Accountant not found.");
        }
        userManager.deleteUserById(id);
        return ResponseEntity.ok(Map.of("message", "Accountant deleted successfully."));
    }

    @PostMapping("/courses")
    public ResponseEntity<?> createCourse(@RequestBody CreateCourseRequest request) {

        return ResponseEntity.ok("Course created successfully.");
    }

    @GetMapping("/courses")
    public ResponseEntity<List<Course>> getCourses(@RequestParam Long collegeId) {
        List<Course> courses = courseService.findByCollegeId(collegeId);
        return ResponseEntity.ok(courses);
    }

    @PostMapping("/subjects")
    public ResponseEntity<?> createSubject(@RequestBody CreateSubjectRequest request) {


        return ResponseEntity.ok(subjectService.createSubject(null));
    }

    @GetMapping("/subjects")
    public ResponseEntity<List<Subject>> getSubjects(@RequestParam Long courseId) {
        List<Subject> subjects = subjectService.findByCourseId(courseId);
        if (!subjects.isEmpty()) {
        }
        return ResponseEntity.ok(subjects);
    }

    @GetMapping("/attendance")
    public ResponseEntity<List<?>> getAttendanceRecords(@RequestParam Long studentId) {
        User student = userManager.findById(studentId);
        return ResponseEntity.ok(List.of());
    }


    @PostMapping("/exams")
    public ResponseEntity<?> createExam(@RequestBody CreateExamRequest request) {

        return ResponseEntity.ok("Exam scheduled successfully.");
    }

    @PostMapping("/exam-results")
    public ResponseEntity<?> addExamResult(@RequestBody ExamResultRequest request) {
        Exam exam = examService.findExamById(request.getExamId());
        User student = userManager.findById(request.getStudentId());


        return ResponseEntity.ok("Result added successfully.");
    }

    @GetMapping("/exam-results")
    public ResponseEntity<List<?>> getResults(@RequestParam Long studentId) {
       return ResponseEntity.ok(List.of());
    }


    @PostMapping("/notifications")
    public ResponseEntity<?> sendNotification(@RequestBody NotificationRequest request) {
        User receiverUser = userManager.findById(request.getReceiverId());
        //TODO: Implement this method
        return ResponseEntity.ok("");

    }

    @GetMapping("/notifications")
    public ResponseEntity<List<Notification>> getNotifications(@RequestParam Long receiverId) {
        User receiverUser = userManager.findById(receiverId);
        return ResponseEntity.ok(notificationService.getNotificationsByReceiverId(receiverId));
    }

    @PutMapping("/notifications/{id}/mark-read")
    public ResponseEntity<?> markNotificationAsRead(@PathVariable Long id) {

        return ResponseEntity.ok("Notification marked as read.");
    }


    @PostMapping("/fees")
    public ResponseEntity<?> createFee(@RequestBody FeeRequest request) {
        User student = userManager.findById(request.getStudentId());


        return ResponseEntity.ok("Fee record created successfully.");
    }

    @GetMapping("/fees")
    public ResponseEntity<List<StudentFee>> getFees(@RequestParam Long studentId) {
        User student = userManager.findById(studentId);
        return ResponseEntity.ok(null);
    }

    @PutMapping("/fees/{id}/pay")
    public ResponseEntity<?> markFeeAsPaid(@PathVariable Long id) {

        return ResponseEntity.ok("Fee marked as paid.");
    }


    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Long>> getCollegeAdminDashboard(@RequestParam Long collegeId) {
        Map<String, Long> dashboardData = new HashMap<>();
        dashboardData.put("totalStudents", userManager.countByCollegeIdAndRole(collegeId, roleService.getRoleByName(RoleType.ROLE_STUDENT)));
        dashboardData.put("totalTeachers", userManager.countByCollegeIdAndRole(collegeId, roleService.getRoleByName(RoleType.ROLE_TEACHER)));
        dashboardData.put("totalAccountants", userManager.countByCollegeIdAndRole(collegeId, roleService.getRoleByName(RoleType.ROLE_ACCOUNTANT)));
        dashboardData.put("totalSubjects", subjectService.countByCollegeId(collegeId));

        return ResponseEntity.ok(dashboardData);
    }


}
