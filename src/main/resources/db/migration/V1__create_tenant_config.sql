CREATE TABLE tenant_config
(
    id          SERIAL PRIMARY KEY,
    tenant_id   VARCHAR(100) UNIQUE NOT NULL,
    tenant_name VARCHAR(150)        NOT NULL,
    db_url      VARCHAR(255)        NOT NULL,
    db_username VARCHAR(100)        NOT NULL,
    db_password VARCHAR(255)        NOT NULL,
    db_driver   VARCHAR(100)        NOT NULL,
    created_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);