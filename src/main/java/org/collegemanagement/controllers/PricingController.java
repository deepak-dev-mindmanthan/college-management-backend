package org.collegemanagement.controllers;

import org.collegemanagement.dto.PlanPriceDto;
import org.collegemanagement.services.PlanPriceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/pricing")
public class PricingController {

    private final PlanPriceService planPriceService;

    public PricingController(PlanPriceService planPriceService) {
        this.planPriceService = planPriceService;
    }

    @GetMapping
    public ResponseEntity<List<PlanPriceDto>> listActivePlanPrices() {
        List<PlanPriceDto> prices = planPriceService.listActive()
                .stream()
                .map(PlanPriceDto::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(prices);
    }
}

