package org.collegemanagement.controllers;

import org.collegemanagement.dto.AttendanceRequest;
import org.collegemanagement.entity.Attendance;
import org.collegemanagement.entity.Subject;
import org.collegemanagement.entity.User;
import org.collegemanagement.services.AttendanceService;
import org.collegemanagement.services.SubjectService;
import org.collegemanagement.services.UserManager;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/teacher")
public class TeacherController {

    private final SubjectService subjectService;
    private final UserManager userManager;
    private final AttendanceService attendanceService;

    public TeacherController(SubjectService subjectService, UserManager userManager, AttendanceService attendanceService) {
        this.subjectService = subjectService;
        this.userManager = userManager;
        this.attendanceService = attendanceService;
    }

    @PostMapping("/attendance")
    public ResponseEntity<?> markAttendance(@RequestBody AttendanceRequest request) {
        Subject subject = subjectService.findById(request.getSubjectId());
        User student = userManager.findById(request.getStudentId());

        if (!subject.getTeacher().getId().equals(request.getTeacherId())) {
            return ResponseEntity.status(403).body("Unauthorized: You can only mark attendance for your own subjects.");
        }

        Attendance attendance  = Attendance.builder()
                .subject(subject)
                .student(student)
                .date(LocalDate.now())
                .status(request.getStatus())
                .build();

        attendanceService.save(attendance);
        return ResponseEntity.ok("Attendance marked successfully.");
    }

    @GetMapping("/attendance")
    public ResponseEntity<List<Attendance>> getAttendanceBySubject(@RequestParam Long subjectId, @RequestParam LocalDate date) {
        return ResponseEntity.ok(attendanceService.findBySubjectIdAndDate(subjectId, date));
    }
}
