package org.collegemanagement.controllers;

/**
 * Controller for handling payment gateway webhooks.
 * 
 * TODO: Integrate payment gateway webhooks
 * - Implement Razorpay webhook signature verification
 * - Implement Stripe webhook signature verification
 * - Process payment status updates
 * - Handle refund webhooks
 */

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.collegemanagement.api.response.ApiResponse;
import org.collegemanagement.services.PaymentWebhookService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments/webhooks")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payment Webhooks", description = "Webhook endpoints for payment gateway callbacks")
public class PaymentWebhookController {

    private final PaymentWebhookService webhookService;

    @Operation(
            summary = "Razorpay webhook endpoint",
            description = "Receives webhook events from Razorpay. Public endpoint."
    )
    @PostMapping("/razorpay")
    public ResponseEntity<ApiResponse<String>> razorpay(
            @RequestBody String payload,
            @RequestHeader("X-Razorpay-Signature") String signature
    ) {
        log.info("Razorpay webhook received");
        webhookService.handleRazorpay(payload, signature);
        return ResponseEntity.ok(ApiResponse.success("OK", "Razorpay webhook processed"));
    }

    @Operation(
            summary = "Stripe webhook endpoint",
            description = "Receives webhook events from Stripe. Public endpoint."
    )
    @PostMapping("/stripe")
    public ResponseEntity<ApiResponse<String>> stripe(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String signature
    ) {
        log.info("Stripe webhook received");
        webhookService.handleStripe(payload, signature);
        return ResponseEntity.ok(ApiResponse.success("OK", "Stripe webhook processed"));
    }
}
