package com.oc.api.model.master;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import java.io.Serializable;

@Entity
@Table(name = "tenant_config")
@Getter
@Setter
public class TenantConfig implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "tenant_id")
    private String tenantId;

    @Column(name = "db_url", nullable = false)
    private String dbUrl;

    @Column(name = "tenant_name", unique = true, nullable = false)
    private String tenantName;

    @Column(name = "db_username", nullable = false)
    private String dbUsername;

    @Column(name = "db_password", nullable = false)
    private String dbPassword;

    @Column(name = "db_driver", nullable = false)
    private String dbDriver;
}