package com.oc.api.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
public class InvoiceResponse {
    private String tenantName;
    private String invoiceNumber;
    private LocalDateTime date;
    private String paymentMethod;
    private BigDecimal totalAmount;
    private BigDecimal paidAmount;
    private BigDecimal balance;
    private List<InvoiceItemDto> items;

    @Getter
    @Setter
    @Builder
    public static class InvoiceItemDto {
        private String productName;
        private Integer quantity;
        private BigDecimal unitPrice;
        private BigDecimal subTotal;
    }
}