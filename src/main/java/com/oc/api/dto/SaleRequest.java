package com.oc.api.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class SaleRequest {
    private String paymentMethod;
    private BigDecimal paidAmount;
    private List<SaleItemRequest> items;

    @Getter
    @Setter
    public static class SaleItemRequest {
        private Long productId;
        private Integer quantity;
    }
}