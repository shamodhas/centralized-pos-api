package com.oc.api.controller;

import com.oc.api.dto.SaleRequest;
import com.oc.api.model.tenant.Sale;
import com.oc.api.service.SaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
public class SaleController {

    private final SaleService saleService;

    @PostMapping("/checkout")
    public ResponseEntity<Sale> checkout(
            @RequestBody SaleRequest request,
            @RequestParam(required = false) String customerPhone) {
        Sale completedSale = saleService.processCheckout(request, customerPhone);
        return ResponseEntity.ok(completedSale);
    }
}