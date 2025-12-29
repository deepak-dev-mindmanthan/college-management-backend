package org.collegemanagement.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.collegemanagement.api.response.ApiResponse;
import org.collegemanagement.dto.leave.ApproveLeaveRequestRequest;
import org.collegemanagement.dto.leave.CreateLeaveRequestRequest;
import org.collegemanagement.dto.leave.LeaveRequestResponse;
import org.collegemanagement.dto.leave.UpdateLeaveRequestRequest;
import org.collegemanagement.enums.LeaveOwnerType;
import org.collegemanagement.enums.LeaveStatus;
import org.collegemanagement.enums.LeaveType;
import org.collegemanagement.services.LeaveRequestService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/leave-requests")
@AllArgsConstructor
@Tag(name = "Leave Request Management", description = "APIs for managing leave requests (student and staff)")
public class LeaveRequestController {

    private final LeaveRequestService leaveRequestService;

    @Operation(
            summary = "Create a leave request",
            description = "Creates a new leave request. Accessible by all authenticated users."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Leave request created successfully",
                    content = @Content(schema = @Schema(implementation = LeaveRequestResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "User not found"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "Overlapping leave request exists"
            )
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER', 'STUDENT', 'PARENT')")
    public ResponseEntity<ApiResponse<LeaveRequestResponse>> createLeaveRequest(
            @Valid @RequestBody CreateLeaveRequestRequest request
    ) {
        LeaveRequestResponse leaveRequest = leaveRequestService.createLeaveRequest(request);
        return ResponseEntity.ok(ApiResponse.success(leaveRequest, "Leave request created successfully"));
    }

    @Operation(
            summary = "Update leave request",
            description = "Updates a leave request. Only PENDING requests can be updated. Accessible by the requester or admins."
    )
    @PutMapping("/{leaveRequestUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER', 'STUDENT', 'PARENT')")
    public ResponseEntity<ApiResponse<LeaveRequestResponse>> updateLeaveRequest(
            @Parameter(description = "UUID of the leave request to update")
            @PathVariable String leaveRequestUuid,
            @Valid @RequestBody UpdateLeaveRequestRequest request
    ) {
        LeaveRequestResponse leaveRequest = leaveRequestService.updateLeaveRequest(leaveRequestUuid, request);
        return ResponseEntity.ok(ApiResponse.success(leaveRequest, "Leave request updated successfully"));
    }

    @Operation(
            summary = "Approve or reject leave request",
            description = "Approves or rejects a leave request. Only PENDING requests can be approved/rejected. Requires COLLEGE_ADMIN or SUPER_ADMIN role."
    )
    @PutMapping("/{leaveRequestUuid}/approve-reject")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<LeaveRequestResponse>> approveOrRejectLeaveRequest(
            @Parameter(description = "UUID of the leave request")
            @PathVariable String leaveRequestUuid,
            @Valid @RequestBody ApproveLeaveRequestRequest request
    ) {
        LeaveRequestResponse leaveRequest = leaveRequestService.approveOrRejectLeaveRequest(leaveRequestUuid, request);
        return ResponseEntity.ok(ApiResponse.success(leaveRequest, "Leave request " + request.getStatus() + " successfully"));
    }

    @Operation(
            summary = "Cancel leave request",
            description = "Cancels a leave request. Only PENDING or APPROVED requests can be cancelled. Accessible by the requester or admins."
    )
    @PutMapping("/{leaveRequestUuid}/cancel")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER', 'STUDENT', 'PARENT')")
    public ResponseEntity<ApiResponse<Void>> cancelLeaveRequest(
            @Parameter(description = "UUID of the leave request to cancel")
            @PathVariable String leaveRequestUuid
    ) {
        leaveRequestService.cancelLeaveRequest(leaveRequestUuid);
        return ResponseEntity.ok(ApiResponse.success(null, "Leave request cancelled successfully"));
    }

    @Operation(
            summary = "Get leave request by UUID",
            description = "Retrieves a leave request by UUID. Accessible by appropriate roles."
    )
    @GetMapping("/{leaveRequestUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER', 'STUDENT', 'PARENT')")
    public ResponseEntity<ApiResponse<LeaveRequestResponse>> getLeaveRequest(
            @Parameter(description = "UUID of the leave request")
            @PathVariable String leaveRequestUuid
    ) {
        LeaveRequestResponse leaveRequest = leaveRequestService.getLeaveRequestByUuid(leaveRequestUuid);
        return ResponseEntity.ok(ApiResponse.success(leaveRequest, "Leave request retrieved successfully"));
    }

    @Operation(
            summary = "Get all leave requests",
            description = "Retrieves all leave requests with pagination. Requires COLLEGE_ADMIN, SUPER_ADMIN, or TEACHER role."
    )
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<Page<LeaveRequestResponse>>> getAllLeaveRequests(
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "startDate") String sortBy,
            @Parameter(description = "Sort direction (ASC or DESC)")
            @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<LeaveRequestResponse> leaveRequests = leaveRequestService.getAllLeaveRequests(pageable);
        return ResponseEntity.ok(ApiResponse.success(leaveRequests, "Leave requests retrieved successfully"));
    }

    @Operation(
            summary = "Get leave requests by user",
            description = "Retrieves all leave requests for a specific user. Accessible by appropriate roles."
    )
    @GetMapping("/user/{userUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER', 'STUDENT', 'PARENT')")
    public ResponseEntity<ApiResponse<Page<LeaveRequestResponse>>> getLeaveRequestsByUser(
            @Parameter(description = "UUID of the user")
            @PathVariable String userUuid,
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "startDate") String sortBy,
            @Parameter(description = "Sort direction (ASC or DESC)")
            @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<LeaveRequestResponse> leaveRequests = leaveRequestService.getLeaveRequestsByUser(userUuid, pageable);
        return ResponseEntity.ok(ApiResponse.success(leaveRequests, "Leave requests retrieved successfully"));
    }

    @Operation(
            summary = "Get leave requests by owner type",
            description = "Retrieves leave requests filtered by owner type (STUDENT or STAFF). Requires COLLEGE_ADMIN, SUPER_ADMIN, or TEACHER role."
    )
    @GetMapping("/owner-type/{ownerType}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<Page<LeaveRequestResponse>>> getLeaveRequestsByOwnerType(
            @Parameter(description = "Owner type (STUDENT or STAFF)")
            @PathVariable LeaveOwnerType ownerType,
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "startDate") String sortBy,
            @Parameter(description = "Sort direction (ASC or DESC)")
            @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<LeaveRequestResponse> leaveRequests = leaveRequestService.getLeaveRequestsByOwnerType(ownerType, pageable);
        return ResponseEntity.ok(ApiResponse.success(leaveRequests, "Leave requests retrieved successfully"));
    }

    @Operation(
            summary = "Get leave requests by status",
            description = "Retrieves leave requests filtered by status. Requires COLLEGE_ADMIN, SUPER_ADMIN, or TEACHER role."
    )
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<Page<LeaveRequestResponse>>> getLeaveRequestsByStatus(
            @Parameter(description = "Leave status")
            @PathVariable LeaveStatus status,
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "startDate") String sortBy,
            @Parameter(description = "Sort direction (ASC or DESC)")
            @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<LeaveRequestResponse> leaveRequests = leaveRequestService.getLeaveRequestsByStatus(status, pageable);
        return ResponseEntity.ok(ApiResponse.success(leaveRequests, "Leave requests retrieved successfully"));
    }

    @Operation(
            summary = "Get leave requests by user and status",
            description = "Retrieves leave requests for a user filtered by status. Accessible by appropriate roles."
    )
    @GetMapping("/user/{userUuid}/status/{status}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER', 'STUDENT', 'PARENT')")
    public ResponseEntity<ApiResponse<Page<LeaveRequestResponse>>> getLeaveRequestsByUserAndStatus(
            @Parameter(description = "UUID of the user")
            @PathVariable String userUuid,
            @Parameter(description = "Leave status")
            @PathVariable LeaveStatus status,
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "startDate") String sortBy,
            @Parameter(description = "Sort direction (ASC or DESC)")
            @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<LeaveRequestResponse> leaveRequests = leaveRequestService.getLeaveRequestsByUserAndStatus(
                userUuid, status, pageable);
        return ResponseEntity.ok(ApiResponse.success(leaveRequests, "Leave requests retrieved successfully"));
    }

    @Operation(
            summary = "Get leave requests by date range",
            description = "Retrieves leave requests within a date range. Requires COLLEGE_ADMIN, SUPER_ADMIN, or TEACHER role."
    )
    @GetMapping("/date-range")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<Page<LeaveRequestResponse>>> getLeaveRequestsByDateRange(
            @Parameter(description = "Start date (yyyy-MM-dd)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @Parameter(description = "End date (yyyy-MM-dd)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "startDate") String sortBy,
            @Parameter(description = "Sort direction (ASC or DESC)")
            @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<LeaveRequestResponse> leaveRequests = leaveRequestService.getLeaveRequestsByDateRange(
                startDate, endDate, pageable);
        return ResponseEntity.ok(ApiResponse.success(leaveRequests, "Leave requests retrieved successfully"));
    }

    @Operation(
            summary = "Get leave requests by leave type",
            description = "Retrieves leave requests filtered by leave type. Requires COLLEGE_ADMIN, SUPER_ADMIN, or TEACHER role."
    )
    @GetMapping("/leave-type/{leaveType}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN', 'TEACHER')")
    public ResponseEntity<ApiResponse<Page<LeaveRequestResponse>>> getLeaveRequestsByLeaveType(
            @Parameter(description = "Leave type")
            @PathVariable LeaveType leaveType,
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "startDate") String sortBy,
            @Parameter(description = "Sort direction (ASC or DESC)")
            @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase("ASC") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<LeaveRequestResponse> leaveRequests = leaveRequestService.getLeaveRequestsByLeaveType(leaveType, pageable);
        return ResponseEntity.ok(ApiResponse.success(leaveRequests, "Leave requests retrieved successfully"));
    }
}

