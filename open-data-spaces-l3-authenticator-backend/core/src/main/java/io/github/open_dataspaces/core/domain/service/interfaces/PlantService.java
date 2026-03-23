/*
 * PlantService.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class defines the interface for plant information management logic.
 *
 * Date: 2025/06/30
 */

package io.github.open_dataspaces.core.domain.service.interfaces;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import io.github.open_dataspaces.core.domain.dto.PlantResult;
import io.github.open_dataspaces.core.domain.dto.StateString;

/**
 * Service interface for plant information management operations.
 */
public interface PlantService {

    /**
     * Adds a new plant with the specified parameters.
     *
     * @param operatorId the operator ID to which the plant belongs
     * @param plantName the name of the plant
     * @param plantAddress the address of the plant
     * @param openPlantId the open plant ID
     * @param globalPlantId the global plant ID
     * @param effectiveStartDate the effective start date
     * @param effectiveEndDate the effective end date
     * @param creatorOperatorId the ID of the operator who is creating this new plant
     * @return the result of the add operation
     */
    @NonNull
    PlantResult addPlant(@NonNull String operatorId,
            @NonNull String plantName, @NonNull String plantAddress, @NonNull String openPlantId, String globalPlantId,
            @NonNull LocalDate effectiveStartDate, @NonNull LocalDate effectiveEndDate, @NonNull String creatorOperatorId);

    /**
     * Updates the operator information with the specified parameters.
     *
     * @param targetPlantId the target plant ID
     * @param updatedAt the updated at timestamp
     * @param plantName the plant name
     * @param plantAddress the plant address
     * @param openPlantId the open plant ID
     * @param globalPlantId the global plant ID
     * @param updateOperatorId the update operator ID
     * @return the result of the update operation
     */
    @Nullable
    PlantResult updatePlant(@NonNull String targetPlantId, @NonNull LocalDateTime updatedAt,
            String plantName, String plantAddress, String openPlantId, StateString globalPlantId,
            @NonNull String updateOperatorId);

    /**
     * Updates the plant information with the specified parameters.
     *
     * @param targetPlantId the target plant ID
     * @param updatedAt the updated at timestamp
     * @param effectiveStartDate the effective start date
     * @param effectiveEndDate the effective end date
     * @param deletedFlag the deleted flag
     * @param updateOperatorId the update operator ID
     * @return the updated PlantResult
     */
    @Nullable
    PlantResult updatePlantStatus(@NonNull String targetPlantId, @NonNull LocalDateTime updatedAt,
            LocalDate effectiveStartDate, LocalDate effectiveEndDate, Boolean deletedFlag,
            @NonNull String updateOperatorId);

    /**
     * Retrieves plant information based on the provided criteria.
     *
     * @param plantId the plant ID to search for
     * @return the PlantResult containing the plant information
     */
    @Nullable
    PlantResult getPlant(@NonNull String plantId);

    /**
     * Searches for plants based on various criteria.
     *
     * @param limit           The maximum number of records to retrieve.
     * @param offset          The starting position of records to retrieve.
     * @param orderByKey      The key to sort the results by.
     * @param orderByDirection The direction of sorting (asc/desc).
     * @param operatorId      The operator ID to filter by.
     * @param plantId         The plant ID to filter by.
     * @param plantName       The plant name to filter by.
     * @param plantAddress    The plant address to filter by.
     * @param deletedFlag     The deleted flag to filter by.
     * @param effectiveDate   The effective date to filter by.
     * @return                A list of PlantResult objects matching the criteria.
     */
    @NonNull
    List<PlantResult> searchPlants(
            int limit,
            int offset,
            String orderByKey,
            String orderByDirection,
            String operatorId,
            String plantId,
            String openPlantId,
            StateString  globalPlantId,
            String plantName,
            String plantAddress,
            Boolean deletedFlag,
            LocalDate effectiveDate
    );

    /**
     * Adds a new plant with authorization check (WithAuth).
     *
     * @param operatorId the operator ID to which the plant belongs
     * @param plantName the name of the plant
     * @param plantAddress the address of the plant
     * @param openPlantId the open plant ID
     * @param globalPlantId the global plant ID
     * @param effectiveStartDate the effective start date
     * @param effectiveEndDate the effective end date
     * @param creatorOperatorId the ID of the operator who is creating this new plant
     * @param storeId the store ID for authorization
     * @return the result of the add operation
     */
    @NonNull
    PlantResult addPlantWithAuth(
            String operatorId,
            String plantName,
            String plantAddress,
            String openPlantId,
            String globalPlantId,
            LocalDate effectiveStartDate,
            LocalDate effectiveEndDate,
            String creatorOperatorId,
            String storeId
    );

    /**
    * Updates plant information with authorization check (WithAuth).
    *
    * @param targetPlantId the target plant ID
    * @param updatedAt the updated at timestamp
    * @param plantName the plant name
    * @param plantAddress the plant address
    * @param openPlantId the open plant ID
    * @param globalPlantId the global plant ID
    * @param updateOperatorId the update operator ID
    * @param storeId the store ID for authorization
    * @return the result of the update operation
    */
    @Nullable
    PlantResult updatePlantWithAuth(
            String targetPlantId,
            LocalDateTime updatedAt,
            String plantName,
            String plantAddress,
            String openPlantId,
            StateString globalPlantId,
            String updateOperatorId,
            String storeId
    );

    /**
     * Updates plant status with authorization check (WithAuth).
     *
     * @param targetPlantId the target plant ID
     * @param updatedAt the updated at timestamp
     * @param effectiveStartDate the effective start date
     * @param effectiveEndDate the effective end date
     * @param deletedFlag the deleted flag
     * @param updateOperatorId the update operator ID
     * @param storeId the store ID for authorization
     * @return the updated PlantResult
     */
    @Nullable
    PlantResult updatePlantStatusWithAuth(
            String targetPlantId,
            LocalDateTime updatedAt,
            LocalDate effectiveStartDate,
            LocalDate effectiveEndDate,
            Boolean deletedFlag,
            String updateOperatorId,
            String storeId
    );

    /**
     * getPlant with Authorization.
     *
     * @param plantId the plant ID to search for
     * @param userId the user ID for authorization
     * @param storeId the store ID for authorization
     * @return the PlantResult containing the plant information
    */
    PlantResult getPlantWithAuth(String plantId, String userId, String storeId);

    /**
     * searchPlants with Authorization.
     *
     * @param limit The maximum number of records to retrieve.
     * @param offset The starting position of records to retrieve.
     * @param orderByKey The key to sort the results by.
     * @param orderByDirection The direction of sorting (asc/desc).
     * @param operatorId The operator ID to filter by.
     * @param plantId The plant ID to filter by.
     * @param plantName The plant name to filter by.
     * @param plantAddress The plant address to filter by.
     * @param deletedFlag The deleted flag to filter by.
     * @param effectiveDate The effective date to filter by.
     * @param userId the user ID for authorization
     * @param storeId the store ID for authorization
     * @return A list of PlantResult objects matching the criteria.
     */
    List<PlantResult> searchPlantsWithAuth(
            int limit,
            int offset,
            String orderByKey,
            String orderByDirection,
            String operatorId,
            String plantId,
            String openPlantId,
            StateString globalPlantId,
            String plantName,
            String plantAddress,
            Boolean deletedFlag,
            java.time.LocalDate effectiveDate,
            String userId,
            String storeId
    );
}
