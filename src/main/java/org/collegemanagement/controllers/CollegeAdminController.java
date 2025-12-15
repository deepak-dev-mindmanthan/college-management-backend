package org.collegemanagement.controllers;


import org.collegemanagement.exception.ResourceNotFoundException;
import org.collegemanagement.security.jwt.TokenGenerator;
import org.collegemanagement.dto.*;
import org.collegemanagement.entity.*;
import org.collegemanagement.enums.FeeStatus;
import org.collegemanagement.enums.RoleType;
import org.collegemanagement.services.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@PreAuthorize("hasAnyRole('COLLEGE_ADMIN','SUPER_ADMIN')")
@RequestMapping("/api/v1/college-admin")
public class CollegeAdminController {

    private final UserManager userManager;
    private final CollegeService collegeService;
    private final RoleService roleService;
    private final TokenGenerator tokenGenerator;
    private final CourseService courseService;
    private final SubjectService subjectService;
    private final AttendanceService attendanceService;
    private final ExamResultService examResultService;
    private final ExamService examService;
    private final NotificationService notificationService;
    private final FeeService feeService;

    private Authentication currentAuth() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    private boolean isSuperAdmin(Authentication authentication) {
        if (authentication == null) return false;
        return authentication.getAuthorities().stream().anyMatch(a ->
                "ROLE_SUPER_ADMIN".equalsIgnoreCase(a.getAuthority()) || "SUPER_ADMIN".equalsIgnoreCase(a.getAuthority()));
    }

    private Long currentCollegeId(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) return null;
        if (authentication.getPrincipal() instanceof User user && user.getCollege() != null) {
            return user.getCollege().getId();
        }
        return null;
    }

    private void requireSameCollege(Long targetCollegeId) {
        Authentication auth = currentAuth();
        if (isSuperAdmin(auth)) {
            return;
        }
        Long currentCollege = currentCollegeId(auth);
        if (currentCollege == null || !currentCollege.equals(targetCollegeId)) {
            throw new AccessDeniedException("Access denied: cross-college access is not permitted.");
        }
    }

    private void requireSameCollege(User user) {
        if (user == null || user.getCollege() == null) {
            throw new AccessDeniedException("Access denied: user not assigned to a college.");
        }
        requireSameCollege(user.getCollege().getId());
    }

    private void requireSameCollege(Course course) {
        if (course == null || course.getCollege() == null) {
            throw new AccessDeniedException("Access denied: course not assigned to a college.");
        }
        requireSameCollege(course.getCollege().getId());
    }

    private void requireSameCollege(Subject subject) {
        if (subject == null || subject.getCourse() == null || subject.getCourse().getCollege() == null) {
            throw new AccessDeniedException("Access denied: subject not assigned to a college.");
        }
        requireSameCollege(subject.getCourse().getCollege().getId());
    }


    public CollegeAdminController(UserManager userManager, CollegeService collegeService, RoleService roleService, TokenGenerator tokenGenerator, CourseService courseService, SubjectService subjectService, AttendanceService attendanceService, ExamResultService examResultService, ExamService examService, NotificationService notificationService, FeeService feeService) {
        this.userManager = userManager;
        this.collegeService = collegeService;
        this.roleService = roleService;
        this.tokenGenerator = tokenGenerator;
        this.courseService = courseService;
        this.subjectService = subjectService;
        this.attendanceService = attendanceService;
        this.examResultService = examResultService;
        this.examService = examService;
        this.notificationService = notificationService;
        this.feeService = feeService;
    }

    @PostMapping("/teachers")
    public ResponseEntity<?> createTeacher(@RequestBody CreateTeacherOrStudentRequest request) {
        if (!collegeService.existsById(request.getCollegeId())) {
            return ResponseEntity.badRequest().body("No college found with id " + request.getCollegeId());
        }
        requireSameCollege(request.getCollegeId());
        User newTeacher = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(request.getPassword())
                .roles(roleService.getRoles(RoleType.ROLE_TEACHER))
                .college(collegeService.findById(request.getCollegeId()))
                .build();
        userManager.createUser(newTeacher);

        Collection<GrantedAuthority> authorities = newTeacher.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName().name().toUpperCase()))
                .collect(Collectors.toSet());
        Authentication authentication = UsernamePasswordAuthenticationToken.authenticated(newTeacher, request.getPassword(), authorities);
        return ResponseEntity.ok(tokenGenerator.createToken(authentication));
    }

    @GetMapping("/teachers/{collegeId}")
    public ResponseEntity<List<User>> getAllTeachers(@PathVariable Long collegeId) {
        requireSameCollege(collegeId);
        List<User> teachers = userManager.findByCollegeIdAndRoles(collegeId, roleService.getRoleByName(RoleType.ROLE_TEACHER));
        return ResponseEntity.ok(teachers);
    }

    @PutMapping("/teachers/{id}")
    public ResponseEntity<?> updateTeacher(@PathVariable Long id, @RequestBody CreateTeacherOrStudentRequest request) {
        User updateUser = User.builder()
                .id(id)
                .name(request.getName())
                .email(request.getEmail())
                .password(request.getPassword())
                .roles(roleService.getRoles(RoleType.ROLE_TEACHER))
                .build();
        requireSameCollege(userManager.getUserById(id).getCollege().getId());
        userManager.update(updateUser);
        return ResponseEntity.ok("Teacher updated successfully.");
    }

    @DeleteMapping("/teachers/{id}")
    public ResponseEntity<?> deleteTeacher(@PathVariable Long id) {
        if (!userManager.userExists(id)) {
            return ResponseEntity.badRequest().body("Teacher not found.");
        }
        requireSameCollege(userManager.getUserById(id).getCollege().getId());
        userManager.deleteUserById(id);
        return ResponseEntity.ok("Teacher deleted successfully.");
    }


    @PostMapping("/students")
    public ResponseEntity<?> createStudent(@RequestBody CreateTeacherOrStudentRequest request) {
        if (!collegeService.existsById(request.getCollegeId())) {
            return ResponseEntity.badRequest().body("No college found with id " + request.getCollegeId());
        }

        requireSameCollege(request.getCollegeId());

        User newStudent = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(request.getPassword())
                .roles(roleService.getRoles(RoleType.ROLE_STUDENT))
                .college(collegeService.findById(request.getCollegeId()))
                .build();

        userManager.createUser(newStudent);

        Collection<GrantedAuthority> authorities = newStudent.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName().name().toUpperCase()))
                .collect(Collectors.toSet());
        Authentication authentication = UsernamePasswordAuthenticationToken.authenticated(newStudent, request.getPassword(), authorities);
        return ResponseEntity.ok(tokenGenerator.createToken(authentication));
    }

    @GetMapping("/students/{collegeId}")
    public ResponseEntity<List<User>> getAllStudents(@PathVariable Long collegeId) {
        requireSameCollege(collegeId);
        List<User> students = userManager.findByCollegeIdAndRoles(collegeId, roleService.getRoleByName(RoleType.ROLE_STUDENT));
        return ResponseEntity.ok(students);
    }

    @DeleteMapping("/students/{id}")
    public ResponseEntity<?> deleteStudent(@PathVariable Long id) {
        if (!userManager.userExists(id)) {
            return ResponseEntity.badRequest().body("Student not found.");
        }
        requireSameCollege(userManager.getUserById(id).getCollege().getId());
        userManager.deleteUserById(id);
        return ResponseEntity.ok(Map.of("message","Student deleted successfully."));
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

        requireSameCollege(userManager.getUserById(id).getCollege().getId());
        userManager.update(updateUser);
        return ResponseEntity.ok(Map.of("message","Student updated successfully."));
    }

    // ACCOUNTANT MANAGEMENT
    @PostMapping("/accountants")
    public ResponseEntity<?> createAccountant(@RequestBody CreateTeacherOrStudentRequest request) {
        if (!collegeService.existsById(request.getCollegeId())) {
            throw new ResourceNotFoundException("No college found with id " + request.getCollegeId());
        }
        requireSameCollege(request.getCollegeId());
        User accountant = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password(request.getPassword())
                .roles(roleService.getRoles(RoleType.ROLE_ACCOUNTANT))
                .college(collegeService.findById(request.getCollegeId()))
                .build();
        userManager.createUser(accountant);
        return ResponseEntity.ok(Map.of("message","Accountant created successfully."));
    }

    @GetMapping("/accountants/{collegeId}")
    public ResponseEntity<List<User>> getAccountants(@PathVariable Long collegeId) {
        requireSameCollege(collegeId);
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

        requireSameCollege(userManager.getUserById(id).getCollege().getId());
        userManager.update(updateUser);
        return ResponseEntity.ok(Map.of("message","Accountant updated successfully."));
    }

    @DeleteMapping("/accountants/{id}")
    public ResponseEntity<?> deleteAccountant(@PathVariable Long id) {
        if (!userManager.userExists(id)) {
            return ResponseEntity.badRequest().body("Accountant not found.");
        }
        requireSameCollege(userManager.getUserById(id).getCollege().getId());
        userManager.deleteUserById(id);
        return ResponseEntity.ok("Accountant deleted successfully.");
    }

    @PostMapping("/courses")
    public ResponseEntity<?> createCourse(@RequestBody CreateCourseRequest request) {
        if (!collegeService.existsById(request.getCollegeId())) {
            return ResponseEntity.badRequest().body("College not found.");
        }

        College college = collegeService.findById(request.getCollegeId());
        requireSameCollege(college.getId());

        Course course = new Course();
        course.setName(request.getName());
        course.setDescription(request.getDescription());
        course.setCollege(college);

        courseService.createCourse(course);
        return ResponseEntity.ok("Course created successfully.");
    }

    @GetMapping("/courses")
    public ResponseEntity<List<Course>> getCourses(@RequestParam Long collegeId) {
        requireSameCollege(collegeId);
        List<Course> courses = courseService.findByCollegeId(collegeId);
        return ResponseEntity.ok(courses);
    }

    @PostMapping("/subjects")
    public ResponseEntity<?> createSubject(@RequestBody CreateSubjectRequest request) {

        if (!courseService.exitsByCourseId(request.getCourseId())) {
            return ResponseEntity.badRequest().body("Course not found with id:"+request.getCourseId());
        }

        Course course = courseService.findById(request.getCourseId());
        requireSameCollege(course);

        User teacher;
        if (request.getTeacherId() != null) {
            teacher = userManager.findById(request.getTeacherId());
            if (teacher==null) {
                return ResponseEntity.badRequest().body("Teacher not found.");
            }
        }
        else {
            return ResponseEntity.badRequest().body("Teacher id cannot be null");
        }

        Subject subject = Subject.builder()
                .name(request.getName())
                .code(request.getCode())
                .course(course)
                .teacher(teacher)
                .build();

        return ResponseEntity.ok(subjectService.createSubject(subject));
    }

    @GetMapping("/subjects")
    public ResponseEntity<List<Subject>> getSubjects(@RequestParam Long courseId) {
        List<Subject> subjects = subjectService.findByCourseId(courseId);
        if (!subjects.isEmpty()) {
            requireSameCollege(subjects.get(0).getCourse());
        }
        return ResponseEntity.ok(subjects);
    }

    @GetMapping("/attendance")
    public ResponseEntity<List<Attendance>> getAttendanceRecords(@RequestParam Long studentId) {
        User student = userManager.findById(studentId);
        requireSameCollege(student);
        return ResponseEntity.ok(attendanceService.findByStudentId(studentId));
    }



    @PostMapping("/exams")
    public ResponseEntity<?> createExam(@RequestBody CreateExamRequest request) {
        Subject subject = subjectService.findById(request.getSubjectId());
        requireSameCollege(subject);

        Exam exam = Exam.builder()
                .name(request.getName())
                .date(request.getDate())
                .subject(subject)
                .build();


        examService.createExam(exam);
        return ResponseEntity.ok("Exam scheduled successfully.");
    }

    @PostMapping("/exam-results")
    public ResponseEntity<?> addExamResult(@RequestBody ExamResultRequest request) {
        Exam exam = examService.findExamById(request.getExamId());
        User student = userManager.findById(request.getStudentId());
        requireSameCollege(exam.getSubject());
        requireSameCollege(student);

        ExamResult result = new ExamResult();
        result.setExam(exam);
        result.setStudent(student);
        result.setMarks(request.getMarks());

        examResultService.createExamResult(result);
        return ResponseEntity.ok("Result added successfully.");
    }

    @GetMapping("/exam-results")
    public ResponseEntity<List<ExamResult>> getResults(@RequestParam Long studentId) {
        User student = userManager.findById(studentId);
        requireSameCollege(student);
        return ResponseEntity.ok(examResultService.findByStudentId(studentId));
    }




    @PostMapping("/notifications")
    public ResponseEntity<?> sendNotification(@RequestBody NotificationRequest request) {
        User receiverUser = userManager.findById(request.getReceiverId());
        requireSameCollege(receiverUser);

        Notification notification =  Notification.builder()
                .title(request.getTitle())
                .message(request.getMessage())
                .receiver(receiverUser)
                .timestamp(LocalDateTime.now())
                .isRead(false)
                .build();


        return ResponseEntity.ok(notificationService.createNotification(notification));
    }

    @GetMapping("/notifications")
    public ResponseEntity<List<Notification>> getNotifications(@RequestParam Long receiverId) {
        User receiverUser = userManager.findById(receiverId);
        requireSameCollege(receiverUser);
        return ResponseEntity.ok(notificationService.getNotificationsByReceiverId(receiverId));
    }

    @PutMapping("/notifications/{id}/mark-read")
    public ResponseEntity<?> markNotificationAsRead(@PathVariable Long id) {
        Notification notification = notificationService.findById(id);
        notification.setRead(true);
        notificationService.updateNotification(notification);

        return ResponseEntity.ok("Notification marked as read.");
    }


    @PostMapping("/fees")
    public ResponseEntity<?> createFee(@RequestBody FeeRequest request) {
        User student = userManager.findById(request.getStudentId());
        requireSameCollege(student);
        Fee fee = Fee.builder()
                .student(student)
                .amount(request.getAmount())
                .dueDate(request.getDueDate())
                .status(request.getStatus())
                .build();

        feeService.save(fee);
        return ResponseEntity.ok("Fee record created successfully.");
    }

    @GetMapping("/fees")
    public ResponseEntity<List<Fee>> getFees(@RequestParam Long studentId) {
        User student = userManager.findById(studentId);
        requireSameCollege(student);
        return ResponseEntity.ok(feeService.findByStudentId(studentId));
    }

    @PutMapping("/fees/{id}/pay")
    public ResponseEntity<?> markFeeAsPaid(@PathVariable Long id) {
        Fee fee = feeService.findById(id);
        requireSameCollege(fee.getStudent());
        fee.setStatus(FeeStatus.PAID);
        feeService.save(fee);
        return ResponseEntity.ok("Fee marked as paid.");
    }



    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Long>> getCollegeAdminDashboard(@RequestParam Long collegeId) {
        requireSameCollege(collegeId);
        Map<String, Long> dashboardData = new HashMap<>();
        dashboardData.put("totalStudents", userManager.countByCollegeIdAndRole(collegeId, roleService.getRoleByName(RoleType.ROLE_STUDENT)));
        dashboardData.put("totalTeachers", userManager.countByCollegeIdAndRole(collegeId, roleService.getRoleByName(RoleType.ROLE_TEACHER)));
        dashboardData.put("totalAccountants", userManager.countByCollegeIdAndRole(collegeId, roleService.getRoleByName(RoleType.ROLE_ACCOUNTANT)));
        dashboardData.put("totalSubjects", subjectService.countByCollegeId(collegeId));

        return ResponseEntity.ok(dashboardData);
    }


}
