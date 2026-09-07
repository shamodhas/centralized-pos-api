package com.oc.api.repository.tenant;

import com.oc.api.model.tenant.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {

    @Query("SELECT SUM(s.totalAmount) FROM Sale s WHERE s.createdAt BETWEEN :start AND :end")
    BigDecimal getTotalSalesBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT si.productId, si.productName, SUM(si.quantity) as totalQty " +
            "FROM SaleItem si GROUP BY si.productId, si.productName ORDER BY totalQty DESC")
    List<Object[]> getTopSellingProducts();
}