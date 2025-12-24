package org.collegemanagement.controllers;

import org.collegemanagement.entity.subscription.Subscription;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/pricing")
public class PricingController {
    @GetMapping
    public ResponseEntity<List<Subscription>> getSubscriptions() {
        return null;
    }
}

