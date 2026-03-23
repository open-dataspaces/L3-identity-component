/*
 * OperatorEntity.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This entity is a JPA entity representing operator information.
 *
 * Date: 2025/06/30
 */

package io.github.open_dataspaces.core.domain.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.Column;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.Data;

import java.time.LocalDate;
import java.util.Objects;

import org.springframework.lang.NonNull;

import io.github.open_dataspaces.core.common.consts.ConstSqlQueries;
import io.github.open_dataspaces.core.domain.dto.StateString;
import io.github.open_dataspaces.core.domain.entities.base.AbstractBaseEntity;

/**
 * Entity class representing an operator record in the database.
 *
 * <p>This entity is mapped to the "operators" table and contains information about the operator, such
 * as operatorId, name, address, and related IDs. It also manages audit fields like creation and
 * update timestamps and user IDs.</p>
 *
 * <p>This class provides constructors and getter/setter methods for all fields.</p>
 *
 * @author btmurayamakohei
 * @since 2025-05-26
 */
@Entity
@Data
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Table(schema = ConstSqlQueries.SCHEMA_AUTH, name = ConstSqlQueries.TABLE_OPERATORS)
public class OperatorEntity extends AbstractBaseEntity {

    @Id
    @Column(name = ConstSqlQueries.COLUMN_OPERATORS_OPERATOR_ID)
    private String operatorId;

    @Column(name = ConstSqlQueries.COLUMN_OPERATORS_OPERATOR_NAME)
    private String operatorName;

    @Column(name = ConstSqlQueries.COLUMN_OPERATORS_OPERATOR_ADDRESS)
    private String operatorAddress;

    @Column(name = ConstSqlQueries.COLUMN_OPERATORS_OPEN_OPERATOR_ID)
    private String openOperatorId;

    @Column(name = ConstSqlQueries.COLUMN_OPERATORS_GLOBAL_OPERATOR_ID)
    private String globalOperatorId;

    /**
     * Constructs a new {@code OperatorEntity} with the specified operator information.
     *
     * @param operatorId the operatorId
     * @param operatorName the operator name
     * @param operatorAddress the operator address
     * @param openOperatorId the openOperatorId
     * @param globalOperatorId the globalOperatorId
     * @param effectiveStartDate the effective start date
     * @param effectiveEndDate the effective end date
     * @param creatorOperatorId the ID of the operator who created this record
     */
    public OperatorEntity(String operatorId, String operatorName, String operatorAddress,
            String openOperatorId, String globalOperatorId, LocalDate effectiveStartDate, LocalDate effectiveEndDate,
            String creatorOperatorId) {
        super();
        this.setOperatorId(operatorId);
        this.setOperatorName(operatorName);
        this.setOperatorAddress(operatorAddress);
        this.setOpenOperatorId(openOperatorId);
        this.setGlobalOperatorId(globalOperatorId);
        this.setEffectiveStartDate(effectiveStartDate);
        this.setEffectiveEndDate(effectiveEndDate);
        this.setCreatedUserId(creatorOperatorId);
        this.setUpdatedUserId(creatorOperatorId);
    }

    /**
     * Updates the fields of this {@code OperatorEntity} with the specified values.
     *
     * @param operatorName the new operator name
     * @param operatorAddress the new operator address
     * @param openOperatorId the new open operator ID
     * @param globalOperatorId the new global operator ID
     * @return true if any field was updated, false otherwise
     */
    public boolean isUpdated(
            @NonNull String operatorName,
            @NonNull String operatorAddress,
            @NonNull String openOperatorId,
            @NonNull StateString globalOperatorId) {
        if (operatorName != null && !Objects.equals(this.operatorName, operatorName)) {
            return true;
        }
        if (operatorAddress != null && !Objects.equals(this.operatorAddress, operatorAddress)) {
            return true;
        }
        if (openOperatorId != null && !Objects.equals(this.openOperatorId, openOperatorId)) {
            return true;
        }
        if (globalOperatorId.isValueOrNull() && !Objects.equals(this.globalOperatorId, globalOperatorId.getValue())) {
            return true;
        }
        return false;
    }

}
