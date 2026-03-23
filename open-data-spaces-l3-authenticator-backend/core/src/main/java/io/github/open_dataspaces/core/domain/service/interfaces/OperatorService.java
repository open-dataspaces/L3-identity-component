/*
 * OperatorService.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class defines the interface for operator information management logic.
 *
 * Date: 2025/06/30
 */

package io.github.open_dataspaces.core.domain.service.interfaces;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import io.github.open_dataspaces.core.domain.dto.OperatorResult;
import io.github.open_dataspaces.core.domain.dto.StateString;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service interface for operator information management operations.
 */
public interface OperatorService {

    /**
     * Adds a new operator with the specified parameters.
     *
     * @param operatorId the unique identifier for the operator
     * @param operatorName the name of the operator
     * @param operatorAddress the address of the operator
     * @param openOperatorId the open operator ID
     * @param globalOperatorId the global operator ID
     * @param effectiveStartDate the effective start date
     * @param effectiveEndDate the effective end date
     * @param creatorOperatorId the ID of the operator who is creating this new operator
     * @return the result of the add operation
     */
    @NonNull
    OperatorResult addOperator(
            String operatorId,
            String operatorName,
            String operatorAddress,
            String openOperatorId,
            String globalOperatorId,
            LocalDate effectiveStartDate,
            LocalDate effectiveEndDate,
            String creatorOperatorId);

    /**
     * Updates the operator information with the specified parameters.
     *
     * @param targetOperatorId the target operator ID
     * @param updatedAt the updated at timestamp
     * @param operatorName the name of the operator
     * @param operatorAddress the address of the operator
     * @param openOperatorId the open operator ID
     * @param globalOperatorId the global operator ID
     * @param updateOperatorId the update operator ID
     * @return the result of the update operation
     */
    @Nullable
    OperatorResult updateOperator(@NonNull String targetOperatorId, @NonNull LocalDateTime updatedAt,
            String operatorName, String operatorAddress, String openOperatorId, @NonNull StateString globalOperatorId,
            @NonNull String updateOperatorId);

    /**
     * Updates the operator information with the specified parameters.
     *
     * @param targetOperatorId the target operator ID
     * @param updatedAt the updated at timestamp
     * @param effectiveStartDate the effective start date
     * @param effectiveEndDate the effective end date
     * @param deletedFlag the deleted flag
     * @param updateOperatorId the update operator ID
     * @return the updated OperatorResult
     */
    @Nullable
    OperatorResult updateOperatorStatus(String targetOperatorId, LocalDateTime updatedAt,
            LocalDate effectiveStartDate, LocalDate effectiveEndDate, Boolean deletedFlag,
            String updateOperatorId);

    /**
     * Retrieves operator information based on the provided criteria.
     *
     * @param operatorId the operator ID to search for
     * @return the OperatorResponse containing the operator information
     */
    @Nullable
    OperatorResult getOperator(@NonNull String operatorId);

    /**
     * Searches for operators based on various criteria.

     * @param limit the maximum number of results to return
     * @param offset the starting point for results
     * @param orderByKey the key to order results by
     * @param orderByDirection the direction to order results (asc/desc)
     * @param operatorId the operator ID to filter by
     * @param operatorName the operator name to filter by
     * @param operatorAddress the operator address to filter by
     * @param openOperatorId the open operator ID to filter by
     * @param globalOperatorId the global operator ID to filter by
     * @param deletedFlag the deleted flag to filter by
     * @param effectiveDate the effective date to filter by
     * @return the list of matching operators
     */
    @NonNull
    List<OperatorResult> searchOperators(
            int limit,
            int offset,
            String orderByKey,
            String orderByDirection,
            String operatorId,
            String operatorName,
            String operatorAddress,
            String openOperatorId,
            StateString globalOperatorId,
            Boolean deletedFlag,
            LocalDate effectiveDate
    );

    /**
     * Adds a new operator with authorization check.

     * @param operatorId the unique identifier for the operator
     * @param operatorName the name of the operator
     * @param operatorAddress the address of the operator
     * @param openOperatorId the open operator ID
     * @param globalOperatorId the global operator ID
     * @param effectiveStartDate the effective start date
     * @param effectiveEndDate the effective end date
     * @param creatorOperatorId the ID of the operator who is creating this new operator
     * @param storeId the store ID for authorization
     * @return the result of the add operation
     */
    @NonNull
    OperatorResult addOperatorWithAuth(
            String operatorId,
            String operatorName,
            String operatorAddress,
            String openOperatorId,
            String globalOperatorId,
            java.time.LocalDate effectiveStartDate,
            java.time.LocalDate effectiveEndDate,
            String creatorOperatorId,
            String storeId);

    /**
     * Updates operator information with authorization check.
     *
     * @param targetOperatorId the target operator ID
     * @param updatedAt the updated at timestamp
     * @param operatorName the name of the operator
     * @param operatorAddress the address of the operator
     * @param openOperatorId the open operator ID
     * @param globalOperatorId the global operator ID
     * @param updateOperatorId the ID of the operator who is updating this operator
     * @param storeId the store ID for authorization
     * @return the result of the update operation
     */
    @Nullable
    OperatorResult updateOperatorWithAuth(
            @NonNull String targetOperatorId, @NonNull java.time.LocalDateTime updatedAt,
            @NonNull String operatorName, @NonNull String operatorAddress, @NonNull String openOperatorId, @NonNull StateString globalOperatorId,
            @NonNull String updateOperatorId,
            String storeId);

    /**
     * Updates operator status with authorization check.
     *
     * @param targetOperatorId the target operator ID
     * @param updatedAt the updated at timestamp
     * @param effectiveStartDate the effective start date
     * @param effectiveEndDate the effective end date
     * @param deletedFlag the deleted flag
     * @param updateOperatorId the ID of the operator who is updating this operator
     * @param storeId the store ID for authorization
     * @return the updated OperatorResult
     */
    @Nullable
    OperatorResult updateOperatorStatusWithAuth(
            String targetOperatorId, java.time.LocalDateTime updatedAt,
            java.time.LocalDate effectiveStartDate, java.time.LocalDate effectiveEndDate, Boolean deletedFlag,
            String updateOperatorId,
            String storeId);

    /**
     * getOperator with Authorization.
     *
     * @param operatorId the operator ID to search for
     * @param userId the user ID for authorization
     * @param storeId the store ID for authorization
     * @return the OperatorResponse containing the operator information
    */
    OperatorResult getOperatorWithAuth(String operatorId, String userId, String storeId);

    /**
     * searchOperators with Authorization.
     *
     * @param limit the maximum number of results to return
     * @param offset the starting point for results
     * @param orderByKey the key to order results by
     * @param orderByDirection the direction to order results (asc/desc)
     * @param operatorId the operator ID to filter by
     * @param operatorName the operator name to filter by
     * @param operatorAddress the operator address to filter by
     * @param openOperatorId the open operator ID to filter by
     * @param globalOperatorId the global operator ID to filter by
     * @param deletedFlag the deleted flag to filter by
     * @param effectiveDate the effective date to filter by
     * @param userId the user ID for authorization
     * @param storeId the store ID for authorization
     * @return the list of matching operators
    */
    List<OperatorResult> searchOperatorsWithAuth(
            int limit,
            int offset,
            String orderByKey,
            String orderByDirection,
            String operatorId,
            String operatorName,
            String operatorAddress,
            String openOperatorId,
            StateString globalOperatorId,
            Boolean deletedFlag,
            java.time.LocalDate effectiveDate,
            String userId,
            String storeId
    );

}
