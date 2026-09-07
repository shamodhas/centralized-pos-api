package com.oc.api.repository.master;

import com.oc.api.model.master.TenantUserMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TenantUserMappingRepository extends JpaRepository<TenantUserMapping, Long> {
    Optional<TenantUserMapping> findByEmail(String email);

    boolean existsByEmail(String email);
}