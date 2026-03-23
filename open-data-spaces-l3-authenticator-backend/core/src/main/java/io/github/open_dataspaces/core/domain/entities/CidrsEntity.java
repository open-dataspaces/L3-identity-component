/*
 * CidrsEntity.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This entity defines the CidrsEntity class, which represents a database entity for storing CIDR
 * blocks associated with API keys.
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

import jakarta.persistence.IdClass;

/**
 * Entity class representing a record in the "cidrs" table.
 *
 * <p>This entity uses a composite primary key consisting of a CIDR block and an API key. It is used to
 * associate specific CIDR blocks with API keys for access control.</p>
 */
@Entity
@Data
@EqualsAndHashCode(callSuper = true)    // Ensures equality and hash code are based on BaseEntity fields as well
@Table(schema = ConstSqlQueries.SCHEMA_AUTH, name = ConstSqlQueries.TABLE_CIDRS)
@IdClass(CidrsKey.class)
public class CidrsEntity extends AbstractBaseEntity {

    @Id
    @Column(name = ConstSqlQueries.COLUMN_CIDRS_CIDR)
    private String cidr;

    @Id
    @Column(name = ConstSqlQueries.COLUMN_CIDRS_APIKEY)
    private String apiKey;

}
