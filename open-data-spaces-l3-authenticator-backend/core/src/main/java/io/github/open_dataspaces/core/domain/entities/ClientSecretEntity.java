/*
 * ClientSecretEntity.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * JPA entity for auth.tbl_client_secrets (client–API key relation).
 *
 * Date: 2025/11/30
 */

package io.github.open_dataspaces.core.domain.entities;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import io.github.open_dataspaces.core.common.consts.ConstSqlQueries;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * Entity for auth.tbl_client_secrets (client–API key link).
 *
 * <p>Maps to the table defined by {@link ConstSqlQueries#SCHEMA_AUTH} and
 * {@link ConstSqlQueries#TABLE_CLIENT_SECRETS}. Holds the one-time relation between
 * a Keycloak client UUID and the API key used at registration, plus audit fields.</p>
 *
 * <p>Fields:
 * - clientUuid: public client identifier (PK)
 * - apiKey: API key used to create the client
 * - createdAt: insertion timestamp
 * - createdUserId: operator/open-system ID from JWT</p>
 */
@Entity
@Data
@NoArgsConstructor
@ToString
@EqualsAndHashCode
@Table(schema = ConstSqlQueries.SCHEMA_AUTH, name = ConstSqlQueries.TABLE_CLIENT_SECRETS)
public class ClientSecretEntity {

    @Id
    @Column(name = ConstSqlQueries.COLUMN_CLIENT_SECRETS_CLIENT_UUID)
    private String clientUuid;

    @Column(name = ConstSqlQueries.COLUMN_APIKEYS_APIKEY)
    private String apiKey;

    @CreationTimestamp
    @Column(name = ConstSqlQueries.COLUMN_COMMON_CREATED_AT)
    private LocalDateTime createdAt;

    @Column(name = ConstSqlQueries.COLUMN_COMMON_CREATED_USER_ID)
    private String createdUserId;

    /**
     * Constructs a new {@code ClientSecretEntity} representing a one-time link
     * between a Keycloak client and the API key used to create it.
     *
     * <p>The {@code clientUuid} serves as the primary key and is expected to be
     * the public identifier returned at client registration. The {@code apiKey}
     * indicates which API key initiated the registration, and {@code createdUserId}
     * records the operator/open system identifier extracted from the JWT at the time
     * of creation.</p>
     *
     * @param clientUuid the public client UUID (primary key of this entity)
     * @param apiKey the API key associated with the client registration
     * @param createdUserId the initiator identifier (operator or open system) from JWT
     */
    public ClientSecretEntity(String clientUuid, String apiKey, String createdUserId) {
        this.clientUuid = clientUuid;
        this.apiKey = apiKey;
        this.createdUserId = createdUserId;
    }
}