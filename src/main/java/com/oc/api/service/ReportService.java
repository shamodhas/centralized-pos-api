package com.oc.api.service;

import com.oc.api.repository.tenant.SaleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final SaleRepository saleRepository;

    public BigDecimal getDailyRevenue() {
        LocalDateTime start = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime end = start.plusDays(1).minusSeconds(1);
        BigDecimal revenue = saleRepository.getTotalSalesBetween(start, end);
        return revenue != null ? revenue : BigDecimal.ZERO;
    }

    public List<Object[]> getTopSellingProducts() {
        return saleRepository.getTopSellingProducts();
    }
}