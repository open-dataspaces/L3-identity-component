/*
 * APIKeysEntity.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This entity defines the APIKeysEntity class, which represents an API key entity for application
 * authentication and authorization.
 *
 * Date: 2025/06/30
 */

package io.github.open_dataspaces.core.domain.entities;

import io.github.open_dataspaces.core.common.consts.ConstSqlQueries;
import io.github.open_dataspaces.core.domain.entities.base.AbstractBaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Entity class representing an API key record in the database.
 *
 * <p>This entity is mapped to the "api_keys" table and contains information about the API key, the
 * associated application name, and additional attributes.
 * </p>
 *
 * @author Y.Yamada
 * @since 2025-05-19
 */
@Entity
@Data
@EqualsAndHashCode(callSuper = true)    // Ensures equality and hash code are based on BaseEntity fields as well
@Table(schema = ConstSqlQueries.SCHEMA_AUTH, name = ConstSqlQueries.TABLE_APIKEYS)
public class APIKeysEntity extends AbstractBaseEntity {

    @Id
    @Column(name = ConstSqlQueries.COLUMN_APIKEYS_ID)
    private String id;

    @Column(name = ConstSqlQueries.COLUMN_APIKEYS_APIKEY)
    private String apiKey;

    @Column(name = ConstSqlQueries.COLUMN_APIKEYS_APPLICATION_NAME)
    private String applicationName;

    @Column(name = ConstSqlQueries.COLUMN_APIKEYS_IDP_REALM)
    private String idpRealm;

    @Column(name = ConstSqlQueries.COLUMN_APIKEYS_USECASE)
    private String usecase;
}