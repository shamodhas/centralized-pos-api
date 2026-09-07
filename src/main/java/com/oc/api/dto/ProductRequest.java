package com.oc.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductRequest {

    @NotBlank(message = "SKU is required")
    private String sku;

    @NotBlank(message = "Product name is required")
    private String name;

    private String category;

    @NotNull(message = "Price is required")
    private BigDecimal price;

    @NotNull(message = "Initial stock is required")
    private Integer stock;

    private Integer lowStockThreshold;
}