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
import org.collegemanagement.dto.payment.CreatePaymentRequest;
import org.collegemanagement.dto.payment.PaymentResponse;
import org.collegemanagement.dto.payment.ProcessPaymentRequest;
import org.collegemanagement.enums.PaymentGateway;
import org.collegemanagement.enums.PaymentStatus;
import org.collegemanagement.services.PaymentService;
import org.collegemanagement.dto.PaymentSummary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
@AllArgsConstructor
@Tag(name = "Payment Management", description = "APIs for managing subscription payments")
public class PaymentController {

    private final PaymentService paymentService;

    @Operation(
            summary = "Create a new payment",
            description = "Creates a new payment record for an invoice. Requires COLLEGE_ADMIN or SUPER_ADMIN role."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Payment created successfully",
                    content = @Content(schema = @Schema(implementation = PaymentResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Invoice not found"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "Transaction ID already exists"
            )
    })
    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<PaymentResponse>> createPayment(
            @Valid @RequestBody CreatePaymentRequest request
    ) {
        PaymentResponse payment = paymentService.createPayment(request);
        return ResponseEntity.ok(ApiResponse.success(payment, "Payment created successfully"));
    }

    @Operation(
            summary = "Initiate and process payment",
            description = "Creates a payment record and automatically processes it through the payment gateway. " +
                    "Requires COLLEGE_ADMIN or SUPER_ADMIN role."
    )
    @ApiResponses(value = {
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "Payment initiated and processed successfully",
                    content = @Content(schema = @Schema(implementation = PaymentResponse.class))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "Invoice not found"
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "Transaction ID already exists"
            )
    })
    @PostMapping("/initiate")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<PaymentResponse>> initiatePayment(
            @Valid @RequestBody CreatePaymentRequest request
    ) {
        PaymentResponse payment = paymentService.initiatePayment(request);
        return ResponseEntity.ok(ApiResponse.success(payment, "Payment initiated and processed successfully"));
    }

    @Operation(
            summary = "Process payment",
            description = "Updates payment status (e.g., SUCCESS, FAILED). Requires COLLEGE_ADMIN or SUPER_ADMIN role."
    )
    @PutMapping("/{paymentUuid}/process")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<PaymentResponse>> processPayment(
            @Parameter(description = "UUID of the payment to process")
            @PathVariable String paymentUuid,
            @Valid @RequestBody ProcessPaymentRequest request
    ) {
        request.setPaymentUuid(paymentUuid);
        PaymentResponse payment = paymentService.processPayment(request);
        return ResponseEntity.ok(ApiResponse.success(payment, "Payment processed successfully"));
    }

    @Operation(
            summary = "Get payment by UUID",
            description = "Retrieves payment information by UUID. Requires COLLEGE_ADMIN or SUPER_ADMIN role."
    )
    @GetMapping("/{paymentUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPayment(
            @Parameter(description = "UUID of the payment")
            @PathVariable String paymentUuid
    ) {
        PaymentResponse payment = paymentService.getPaymentByUuid(paymentUuid);
        return ResponseEntity.ok(ApiResponse.success(payment, "Payment retrieved successfully"));
    }

    @Operation(
            summary = "Get payment by transaction ID",
            description = "Retrieves payment information by transaction ID. Requires COLLEGE_ADMIN or SUPER_ADMIN role."
    )
    @GetMapping("/transaction/{transactionId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentByTransactionId(
            @Parameter(description = "Transaction ID of the payment")
            @PathVariable String transactionId
    ) {
        PaymentResponse payment = paymentService.getPaymentByTransactionId(transactionId);
        return ResponseEntity.ok(ApiResponse.success(payment, "Payment retrieved successfully"));
    }

    @Operation(
            summary = "Get all payments",
            description = "Retrieves a paginated list of all payments. Requires COLLEGE_ADMIN or SUPER_ADMIN role."
    )
    @GetMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<Page<PaymentResponse>>> getAllPayments(
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "Sort field")
            @RequestParam(defaultValue = "paymentDate") String sortBy,
            @Parameter(description = "Sort direction")
            @RequestParam(defaultValue = "DESC") Sort.Direction direction
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<PaymentResponse> payments = paymentService.getAllPayments(pageable);
        return ResponseEntity.ok(ApiResponse.success(payments, "Payments retrieved successfully"));
    }

    @Operation(
            summary = "Get payments by invoice",
            description = "Retrieves all payments for a specific invoice. Requires COLLEGE_ADMIN or SUPER_ADMIN role."
    )
    @GetMapping("/invoice/{invoiceUuid}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<Page<PaymentResponse>>> getPaymentsByInvoice(
            @Parameter(description = "UUID of the invoice")
            @PathVariable String invoiceUuid,
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PaymentResponse> payments = paymentService.getPaymentsByInvoiceUuid(invoiceUuid, pageable);
        return ResponseEntity.ok(ApiResponse.success(payments, "Payments retrieved successfully"));
    }

    @Operation(
            summary = "Get payments by status",
            description = "Retrieves payments filtered by status. Requires COLLEGE_ADMIN or SUPER_ADMIN role."
    )
    @GetMapping("/status/{status}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<Page<PaymentResponse>>> getPaymentsByStatus(
            @Parameter(description = "Payment status")
            @PathVariable PaymentStatus status,
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PaymentResponse> payments = paymentService.getPaymentsByStatus(status, pageable);
        return ResponseEntity.ok(ApiResponse.success(payments, "Payments retrieved successfully"));
    }

    @Operation(
            summary = "Get payments by gateway",
            description = "Retrieves payments filtered by payment gateway. Requires COLLEGE_ADMIN or SUPER_ADMIN role."
    )
    @GetMapping("/gateway/{gateway}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<Page<PaymentResponse>>> getPaymentsByGateway(
            @Parameter(description = "Payment gateway")
            @PathVariable PaymentGateway gateway,
            @Parameter(description = "Page number (0-indexed)")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size")
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<PaymentResponse> payments = paymentService.getPaymentsByGateway(gateway, pageable);
        return ResponseEntity.ok(ApiResponse.success(payments, "Payments retrieved successfully"));
    }

    @Operation(
            summary = "Get payment summary",
            description = "Retrieves payment summary statistics. Requires COLLEGE_ADMIN or SUPER_ADMIN role."
    )
    @GetMapping("/summary")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'COLLEGE_ADMIN')")
    public ResponseEntity<ApiResponse<PaymentSummary>> getPaymentSummary() {
        PaymentSummary summary = paymentService.getPaymentSummary();
        return ResponseEntity.ok(ApiResponse.success(summary, "Payment summary retrieved successfully"));
    }
}

