package com.oc.api.model.master;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tenant_user_mapping")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TenantUserMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false,name = "tenant_id")
    private String tenantId;
}