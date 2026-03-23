/*
 * AuthorizationUCStoresEntity.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This entity defines the AuthorizationUCStoresEntity class, which represents an authorization UC store entity for application
 * authentication and authorization.
 *
 * Date: 2025/12/31
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
import lombok.ToString;

/**
 * Entity class representing an API key record in the database.
 */
@Entity
@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)    // Ensures equality and hash code are based on BaseEntity fields as well
@Table(schema = ConstSqlQueries.SCHEMA_AUTH, name = ConstSqlQueries.TABLE_AUTHORIZATION_UC_STORES)
public class AuthorizationUCStoresEntity extends AbstractBaseEntity {

    @Id
    @Column(name = ConstSqlQueries.COLUMN_AUTHORIZATION_UC_STORES_ID)
    private String ucStoreId;

    @Column(name = ConstSqlQueries.COLUMN_AUTHORIZATION_STORES_UC_NAME)
    private String ucStoreName;

    @Column(name = ConstSqlQueries.COLUMN_AUTHORIZATION_STORES_USECASE)
    private String usecase;

}
