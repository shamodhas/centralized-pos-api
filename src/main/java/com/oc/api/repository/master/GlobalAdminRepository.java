package com.oc.api.repository.master;

import com.oc.api.model.master.GlobalAdmin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GlobalAdminRepository extends JpaRepository<GlobalAdmin, Long> {
    Optional<GlobalAdmin> findByUsername(String username);
}