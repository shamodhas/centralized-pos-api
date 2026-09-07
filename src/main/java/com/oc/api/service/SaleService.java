package com.oc.api.service;

import com.oc.api.dto.SaleRequest;
import com.oc.api.model.tenant.Product;
import com.oc.api.model.tenant.Sale;
import com.oc.api.model.tenant.SaleItem;
import com.oc.api.repository.tenant.ProductRepository;
import com.oc.api.repository.tenant.SaleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SaleService {

    private final SaleRepository saleRepository;
    private final ProductRepository productRepository;
    private final CustomerService customerService; // Customer service එකතු කරන ලදී

    @Transactional
    public Sale processCheckout(SaleRequest request, String customerPhone) {
        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Sale items cannot be empty");
        }

        Sale sale = new Sale();
        sale.setInvoiceNumber("INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        sale.setPaymentMethod(request.getPaymentMethod() != null ? request.getPaymentMethod() : "CASH");

        BigDecimal totalAmount = BigDecimal.ZERO;
        java.util.List<SaleItem> saleItems = new ArrayList<>();

        for (SaleRequest.SaleItemRequest itemReq : request.getItems()) {
            Product product = productRepository.findById(itemReq.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found with ID: " + itemReq.getProductId()));

            if (product.getStock() < itemReq.getQuantity()) {
                throw new IllegalStateException("Insufficient stock for product: " + product.getName());
            }

            product.setStock(product.getStock() - itemReq.getQuantity());
            productRepository.save(product);

            BigDecimal subTotal = product.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()));
            totalAmount = totalAmount.add(subTotal);

            SaleItem saleItem = new SaleItem();
            saleItem.setSale(sale);
            saleItem.setProductId(product.getId());
            saleItem.setProductName(product.getName());
            saleItem.setQuantity(itemReq.getQuantity());
            saleItem.setUnitPrice(product.getPrice());
            saleItem.setSubTotal(subTotal);

            saleItems.add(saleItem);
        }

        sale.setTotalAmount(totalAmount);
        sale.setPaidAmount(request.getPaidAmount());

        if (request.getPaidAmount().compareTo(totalAmount) < 0) {
            throw new IllegalArgumentException("Paid amount is less than total amount");
        }

        sale.setBalance(request.getPaidAmount().subtract(totalAmount));
        sale.setItems(saleItems);

        Sale savedSale = saleRepository.save(sale);

        if (customerPhone != null && !customerPhone.isBlank()) {
            customerService.addLoyaltyPoints(customerPhone, totalAmount);
        }

        return savedSale;
    }
}