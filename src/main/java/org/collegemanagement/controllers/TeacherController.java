package org.collegemanagement.controllers;

import org.collegemanagement.dto.AttendanceRequest;
import org.collegemanagement.entity.academic.Subject;
import org.collegemanagement.entity.user.User;
import org.collegemanagement.services.AttendanceService;
import org.collegemanagement.services.SubjectService;
import org.collegemanagement.services.UserManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/teacher")
public class TeacherController {

    private final SubjectService subjectService;
    private final UserManager userManager;

    public TeacherController(SubjectService subjectService, UserManager userManager) {
        this.subjectService = subjectService;
        this.userManager = userManager;
    }

    @PostMapping("/attendance")
    public ResponseEntity<?> markAttendance(@RequestBody AttendanceRequest request) {

        return ResponseEntity.ok("Attendance marked successfully.");
    }

    @GetMapping("/attendance")
    public ResponseEntity<List<?>> getAttendanceBySubject(@RequestParam Long subjectId, @RequestParam LocalDate date) {
        return ResponseEntity.ok(List.of());
    }
}
