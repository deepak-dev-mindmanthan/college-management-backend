package org.collegemanagement.controllers;


import org.collegemanagement.services.AttendanceService;
import org.collegemanagement.services.ExamResultService;
import org.collegemanagement.services.FeeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/student")
public class StudentController {

    private final AttendanceService attendanceService;
    private final FeeService feeService;
    private final ExamResultService examResultService;

    public StudentController(AttendanceService attendanceService, FeeService feeService, ExamResultService examResultService) {
        this.attendanceService = attendanceService;
        this.feeService = feeService;
        this.examResultService = examResultService;
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getStudentDashboard(@RequestParam Long studentId) {
        Map<String, Object> dashboardData = new HashMap<>();
        dashboardData.put("attendancePercentage", attendanceService.getAttendancePercentage(studentId));
        dashboardData.put("pendingFees", feeService.getPendingFees(studentId));
        dashboardData.put("examResults", examResultService.getStudentResults(studentId));

        return ResponseEntity.ok(dashboardData);
    }

}
