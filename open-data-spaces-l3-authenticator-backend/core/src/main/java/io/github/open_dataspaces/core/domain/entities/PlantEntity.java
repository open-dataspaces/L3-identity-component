/*
 * PlantEntity.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This entity is a JPA entity representing plant information.
 *
 * Date: 2025/06/30
 */

package io.github.open_dataspaces.core.domain.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Objects;

import org.springframework.lang.NonNull;

import io.github.open_dataspaces.core.common.consts.ConstSqlQueries;
import io.github.open_dataspaces.core.domain.dto.StateString;
import io.github.open_dataspaces.core.domain.entities.base.AbstractBaseEntity;

/**
 * Entity class representing a plant record in the database.
 *
 * <p>This entity is mapped to the "plants" table and contains information about the plant, such as
 * plantId, operatorId, plant name, address, and related IDs. It also manages audit fields like
 * creation and update timestamps and user IDs.</p>
 *
 * <p>This class provides constructors and getter/setter methods for all fields.</p>
 */
@Entity
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Table(schema = ConstSqlQueries.SCHEMA_AUTH, name = ConstSqlQueries.TABLE_PLANTS)
public class PlantEntity extends AbstractBaseEntity {

    @Id
    @Column(name = ConstSqlQueries.COLUMN_PLANTS_PLANT_ID)
    private String plantId;

    @Column(name = ConstSqlQueries.COLUMN_PLANTS_OPERATOR_ID)
    private String operatorId;

    @Column(name = ConstSqlQueries.COLUMN_PLANTS_PLANT_NAME)
    private String plantName;

    @Column(name = ConstSqlQueries.COLUMN_PLANTS_PLANT_ADDRESS)
    private String plantAddress;

    @Column(name = ConstSqlQueries.COLUMN_PLANTS_OPEN_PLANT_ID)
    private String openPlantId;

    @Column(name = ConstSqlQueries.COLUMN_PLANTS_GLOBAL_PLANT_ID)
    private String globalPlantId;

    /**
     * Constructs a new {@code PlantEntity} with the specified operator information.
     *
     * @param plantId the unique identifier for the plant
     * @param operatorId the operatorId to which the plant belongs
     * @param plantName the name of the plant
     * @param plantAddress the address of the plant
     * @param openPlantId the openPlantId
     * @param globalPlantId the globalPlantId
     * @param effectiveStartDate the effective start date
     * @param effectiveEndDate the effective end date
     * @param creatorOperatorId the ID of the operator who is creating this new plant
     */
    public PlantEntity(String plantId, String operatorId, String plantName, String plantAddress,
            String openPlantId, String globalPlantId, LocalDate effectiveStartDate, LocalDate effectiveEndDate,
            String creatorOperatorId) {
        super();
        this.setPlantId(plantId);
        this.setOperatorId(operatorId);
        this.setPlantName(plantName);
        this.setPlantAddress(plantAddress);
        this.setOpenPlantId(openPlantId);
        this.setGlobalPlantId(globalPlantId);
        this.setEffectiveStartDate(effectiveStartDate);
        this.setEffectiveEndDate(effectiveEndDate);
        this.setCreatedUserId(creatorOperatorId);
        this.setUpdatedUserId(creatorOperatorId);
    }

    /**
     * Updates the fields of this {@code OperatorEntity} with the specified values.
     *
     * @param plantName the new plant name
     * @param plantAddress the new plant address
     * @param openPlantId the new open plant ID
     * @param globalPlantId the new global plant ID
     * @return true if any field was updated, false otherwise
     */
    public boolean isUpdated(
            @NonNull String  plantName,
            @NonNull String  plantAddress,
            @NonNull String  openPlantId,
            @NonNull StateString  globalPlantId) {
        if (plantName != null && !Objects.equals(this.plantName, plantName)) {
            return true;
        }
        if (plantAddress != null && !Objects.equals(this.plantAddress, plantAddress)) {
            return true;
        }
        if (openPlantId != null && !Objects.equals(this.openPlantId, openPlantId)) {
            return true;
        }
        if (globalPlantId.isValueOrNull() && !Objects.equals(this.globalPlantId, globalPlantId.getValue())) {
            return true;
        }
        return false;
    }
}