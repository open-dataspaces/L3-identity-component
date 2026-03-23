/*
 * OperatorServiceImpl.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This service provides the implementation for operator information management logic.
 *
 * Date: 2025/06/30
 */

package io.github.open_dataspaces.core.domain.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Collections;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import io.github.open_dataspaces.core.common.config.ODSProperties;
import io.github.open_dataspaces.core.common.consts.Const;
import io.github.open_dataspaces.core.common.consts.ConstError;
import io.github.open_dataspaces.core.common.consts.ConstSqlQueries;
import io.github.open_dataspaces.core.common.consts.ConstPath;
import io.github.open_dataspaces.core.infrastructure.dto.Tuple;
import io.github.open_dataspaces.core.domain.dto.OperatorResult;
import io.github.open_dataspaces.core.domain.dto.StateString;
import io.github.open_dataspaces.core.domain.entities.OperatorEntity;
import io.github.open_dataspaces.core.common.utils.DateUtils;
import io.github.open_dataspaces.core.common.utils.SqlUtils;
import io.github.open_dataspaces.core.common.utils.UUIDUtils;
import io.github.open_dataspaces.core.domain.repository.interfaces.OperatorRepository;
import io.github.open_dataspaces.core.domain.service.interfaces.OperatorService;
import io.github.open_dataspaces.core.domain.service.interfaces.AuthorizationService;
import io.github.open_dataspaces.core.infrastructure.utils.TupleUtils;
import io.github.open_dataspaces.core.exception.BadParametersException;
import io.github.open_dataspaces.core.exception.ConflictException;
import io.github.open_dataspaces.core.exception.ForbiddenException;
import io.github.open_dataspaces.core.exception.IllegalAuthDataException;
import io.github.open_dataspaces.core.exception.NotFoundException;
import io.github.open_dataspaces.core.exception.OutOfServiceException;
import io.github.open_dataspaces.core.exception.UnexpectedException;

import jakarta.persistence.EntityExistsException;

/**
 * Service implementation for operator information management operations.
 *
 * <p>Provides logic to retrieve and update operator information.</p>
 */
@Service
public class OperatorServiceImpl implements OperatorService {

    // ODS application properties
    private final ODSProperties odsProperties;
    private final OperatorRepository operatorRepository;
    private final AuthorizationService authorizationService;

    /**
     * Constructs a new OperatorServiceImpl with the specified repository.
     *
     * @param operatorRepository the operator repository.
     */
    public OperatorServiceImpl(ODSProperties odsProperties, OperatorRepository operatorRepository,
            AuthorizationService authorizationService) {
        this.odsProperties = odsProperties;
        this.operatorRepository = operatorRepository;
        this.authorizationService = authorizationService;
    }

    /**
     * Adds a new operator with the provided details.
     *
     * @param operatorId the operator ID
     * @param operatorName the operator name
     * @param operatorAddress the operator address
     * @param openOperatorId the open operator ID
     * @param globalOperatorId the global operator ID
     * @param effectiveStartDate the effective start date
     * @param effectiveEndDate the effective end date
     * @param creatorOperatorId the creator operator ID
     * @return the created OperatorResult
     */
    @Transactional
    public @NonNull OperatorResult addOperator(
            String operatorId,
            String operatorName,
            String operatorAddress,
            String openOperatorId,
            String globalOperatorId,
            LocalDate effectiveStartDate,
            LocalDate effectiveEndDate,
            String creatorOperatorId) {

        // Validate operatorId
        List<String> operatorsList = new ArrayList<String>();
        if (!StringUtils.hasText(operatorId)
                || (operatorsList.add(operatorId) && UUIDUtils.getUUIDs(operatorsList).isEmpty())) {
            throw new UnexpectedException(ConstError.ERR_500_MESSAGE, String.format(ConstError.ERR_VALIDATION_INVALID_UUID, operatorId));
        }

        // Check for duplicate operatorId
        Optional<OperatorEntity> entities = operatorRepository.findById(operatorId);
        if (!entities.isEmpty()) {
            throw new ConflictException(String.format(
                    ConstError.ERR_409_CONFLICT, Const.JSON_PROPERTY_OPERATOR_ID, operatorId));
        }

        // Validate effectiveStartDate and effectiveEndDate
        if (effectiveStartDate == null) {
            effectiveStartDate = LocalDate.now(ZoneOffset.UTC);
        }
        if (effectiveEndDate == null) {
            effectiveEndDate = LocalDate.parse(odsProperties.getDefaultEffectiveEndDate());
        }
        if (effectiveStartDate.compareTo(effectiveEndDate) > 0) {
            throw new BadParametersException(ConstError.ERR_EFFECTIVE_DATE_START_AFTER_END);
        }

        // Create a new OperatorEntity with the provided parameters.
        OperatorEntity operatorEntity = new OperatorEntity(
                operatorId,
                operatorName,
                operatorAddress,
                openOperatorId,
                globalOperatorId,
                effectiveStartDate,
                effectiveEndDate,
                creatorOperatorId);

        // Add the operator to the database.
        try {
            // Check again for duplicate operator
            operatorEntity = operatorRepository.save(operatorEntity);
        } catch (EntityExistsException e) {
            throw new UnexpectedException(ConstError.ERR_500, e.getMessage(), e);
        } catch (IllegalArgumentException e) {
            throw new UnexpectedException(ConstError.ERR_500, e.getMessage(), e);
        } catch (Exception e) {
            throw new UnexpectedException(ConstError.ERR_500, e.getMessage(), e);
        }

        // Return the created OperatorEntity as an OperatorResult.
        return new OperatorResult(operatorEntity);
    }

    /**
     * Updates the operator information with the specified parameters.
     *
     * @param targetOperatorId the target operator ID
     * @param updatedAt the updated at timestamp
     * @param operatorName the operator name
     * @param operatorAddress the operator address
     * @param openOperatorId the open operator ID
     * @param globalOperatorId the global operator ID
     * @param updateOperatorId the update operator ID
     * @return the updated OperatorResult
     */
    @Override
    public @Nullable OperatorResult updateOperator(String targetOperatorId, LocalDateTime updatedAt,
            String operatorName, String operatorAddress, String openOperatorId, StateString globalOperatorId,
            String updateOperatorId) {
        // Validate targetOperatorId
        Optional<OperatorEntity> entities = operatorRepository.findById(targetOperatorId);
        if (entities.isEmpty()) {
            return null;
        }
        OperatorEntity currentOperatorEntity = entities.get();

        // Check if the operator is active and not deleted
        if (!DateUtils.isBetween(LocalDate.now(ZoneOffset.UTC), currentOperatorEntity.getEffectiveStartDate(), currentOperatorEntity.getEffectiveEndDate())
                || currentOperatorEntity.isDeletedFlag()) {
            throw new IllegalAuthDataException(String.format(ConstError.ERRLOG_403_FORBIDDEN_RESOURCE_DISABLED, targetOperatorId));
        }

        // Check for data update conflicts
        if (!DateUtils.equals(updatedAt, currentOperatorEntity.getUpdatedAt(), Const.ISO_8601_UTC_MILLISECOND_FORMAT)) {
            throw new ConflictException(ConstError.ERRLOG_409_CONFLICT,
                    String.format(ConstError.ERR_409_CONFLICT, Const.JSON_PROPERTY_UPDATED_AT, DateUtils.formatDateTime(updatedAt, Const.ISO_8601_UTC_MILLISECOND_FORMAT)));
        }

        // Update only if there are changes
        if (currentOperatorEntity.isUpdated(operatorName, operatorAddress, openOperatorId, globalOperatorId)) {

            if (operatorName != null) {
                currentOperatorEntity.setOperatorName(operatorName);
            }
            if (operatorAddress != null) {
                currentOperatorEntity.setOperatorAddress(operatorAddress);
            }
            if (openOperatorId != null) {
                currentOperatorEntity.setOpenOperatorId(openOperatorId);
            }
            if (globalOperatorId.isValueOrNull()) {
                currentOperatorEntity.setGlobalOperatorId(globalOperatorId.getValue());
            }
            currentOperatorEntity.setUpdatedUserId(updateOperatorId);

            // Save the updated operator information to the database.
            currentOperatorEntity = operatorRepository.saveAndFlush(currentOperatorEntity);
        }

        return new OperatorResult(currentOperatorEntity);
    }

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
    @Override
    public @Nullable OperatorResult updateOperatorStatus(String targetOperatorId, LocalDateTime updatedAt,
            LocalDate effectiveStartDate, LocalDate effectiveEndDate, Boolean deletedFlag,
            String updateOperatorId) {
        // Validate targetOperatorId
        Optional<OperatorEntity> entities = operatorRepository.findById(targetOperatorId);
        if (entities.isEmpty()) {
            return null;
        }
        OperatorEntity currentOperatorEntity = entities.get();

        // Check for data update conflicts
        if (!DateUtils.equals(updatedAt, currentOperatorEntity.getUpdatedAt(), Const.ISO_8601_UTC_MILLISECOND_FORMAT)) {
            throw new ConflictException(ConstError.ERRLOG_409_CONFLICT,
                    String.format(ConstError.ERR_409_CONFLICT, Const.JSON_PROPERTY_UPDATED_AT, DateUtils.formatDateTime(updatedAt, Const.ISO_8601_UTC_MILLISECOND_FORMAT)));
        }

        // Validate effectiveStartDate and effectiveEndDate
        if (effectiveStartDate != null) {
            LocalDate validEndDate = (effectiveEndDate != null) ? effectiveEndDate : currentOperatorEntity.getEffectiveEndDate();
            if (!DateUtils.isValidDateRange(effectiveStartDate, validEndDate)) {
                throw new BadParametersException(
                        String.format(ConstError.ERRLOG_400_EFFECTIVE_DATE_START_AFTER_END,
                                Const.JSON_PROPERTY_EFFECTIVE_START_DATE,
                                DateUtils.formatDate(effectiveStartDate, Const.DATE_FORMAT),
                                DateUtils.formatDate(validEndDate, Const.DATE_FORMAT))
                );
            }
        }
        if (effectiveEndDate != null && effectiveStartDate == null) {
            if (!DateUtils.isValidDateRange(currentOperatorEntity.getEffectiveStartDate(), effectiveEndDate)) {
                throw new BadParametersException(
                        String.format(ConstError.ERRLOG_400_EFFECTIVE_DATE_START_AFTER_END,
                                Const.JSON_PROPERTY_EFFECTIVE_END_DATE,
                                DateUtils.formatDate(currentOperatorEntity.getEffectiveStartDate(), Const.DATE_FORMAT),
                                DateUtils.formatDate(effectiveEndDate, Const.DATE_FORMAT))
                );
            }
        }

        // Update only if there are changes
        if (currentOperatorEntity.isStatusUpdated(effectiveStartDate, effectiveEndDate, deletedFlag)) {

            if (effectiveStartDate != null) {
                currentOperatorEntity.setEffectiveStartDate(effectiveStartDate);
            }
            if (effectiveEndDate != null) {
                currentOperatorEntity.setEffectiveEndDate(effectiveEndDate);
            }
            if (deletedFlag != null) {
                currentOperatorEntity.setDeletedFlag(deletedFlag);
            }
            currentOperatorEntity.setUpdatedUserId(updateOperatorId);

            // Save the updated operator information to the database.
            currentOperatorEntity = operatorRepository.saveAndFlush(currentOperatorEntity);
        }
        return new OperatorResult(currentOperatorEntity);
    }

    /**
     * Retrieves operator information based on the provided criteria.
     *
     * @param operatorId the operator ID to search for
     * @return the OperatorResponse containing the operator information
     */
    @Override
    public @Nullable OperatorResult getOperator(@NonNull String operatorId) {

        // Search by operatorId.
        Optional<OperatorEntity> operatorEntity = operatorRepository.findById(operatorId);

        if (operatorEntity.isEmpty()) {
            return null;
        }

        return new OperatorResult(operatorEntity.get());
    }

    /**
     * Searches for operators based on various criteria.
     *
     * @param limit the maximum number of results to return
     * @param offset the starting index for results
     * @param orderByKey the field to order by
     * @param orderByDirection the direction of the order (asc or desc)
     * @param operatorId the operator ID to filter by
     * @param operatorName the operator name to filter by
     * @param operatorAddress the operator address to filter by
     * @param openOperatorId the open operator ID to filter by
     * @param globalOperatorId the global operator ID to filter by
     * @param deletedFlag the deleted flag to filter by
     * @param effectiveDate the effective date to filter by
     * @return a list of matching operators
     */
    @Override
    public @NonNull List<OperatorResult> searchOperators(
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
    ) {
        // Check sort key and order validity
        Sort sort = SqlUtils.buildSort(OperatorEntity.class, orderByKey, orderByDirection, ConstSqlQueries.COLUMN_OPERATORS_OPERATOR_ID, Sort.Direction.ASC);

        // Add search criteria
        Specification<OperatorEntity> spec = SqlUtils.createSpecification();
        if (operatorId != null) {
            spec = SqlUtils.buildSpecification(spec, SqlUtils.SpecificationMethod.EQUALS, OperatorEntity.class, ConstSqlQueries.COLUMN_OPERATORS_OPERATOR_ID, operatorId);
        }
        if (operatorName != null) {
            spec = SqlUtils.buildSpecification(spec, SqlUtils.SpecificationMethod.LIKE, OperatorEntity.class, ConstSqlQueries.COLUMN_OPERATORS_OPERATOR_NAME, operatorName);
        }
        if (operatorAddress != null) {
            spec = SqlUtils.buildSpecification(spec, SqlUtils.SpecificationMethod.LIKE, OperatorEntity.class, ConstSqlQueries.COLUMN_OPERATORS_OPERATOR_ADDRESS, operatorAddress);
        }
        if (openOperatorId != null) {
            spec = SqlUtils.buildSpecification(spec, SqlUtils.SpecificationMethod.EQUALS, OperatorEntity.class, ConstSqlQueries.COLUMN_OPERATORS_OPEN_OPERATOR_ID, openOperatorId);
        }
        if (globalOperatorId.isValueOrNull()) {
            spec = SqlUtils.buildSpecification(spec, SqlUtils.SpecificationMethod.EQUALS, OperatorEntity.class, ConstSqlQueries.COLUMN_OPERATORS_GLOBAL_OPERATOR_ID, globalOperatorId.getValue());
        }
        if (deletedFlag != null) {
            spec = SqlUtils.buildSpecification(spec, SqlUtils.SpecificationMethod.EQUALS, OperatorEntity.class, ConstSqlQueries.COLUMN_COMMON_DELETED_FLAG, deletedFlag);
        }

        // Effective date range
        if (effectiveDate != null) {
            spec = SqlUtils.buildSpecification(spec, SqlUtils.SpecificationMethod.LESS_THAN_OR_EQUALS, OperatorEntity.class, ConstSqlQueries.COLUMN_COMMON_EFFECTIVE_START_DATE, effectiveDate);
            spec = SqlUtils.buildSpecification(spec, SqlUtils.SpecificationMethod.GREATER_THAN_OR_EQUALS, OperatorEntity.class, ConstSqlQueries.COLUMN_COMMON_EFFECTIVE_END_DATE, effectiveDate);
        }

        // Fetch operators with pagination
        // findWithLimitOffset is a custom method used to support limit and offset
        List<OperatorEntity> operators = operatorRepository.findWithLimitOffset(spec, offset, limit, sort);

        // Convert OperatorEntity to OperatorResult
        List<OperatorResult> result = new ArrayList<OperatorResult>();
        for (OperatorEntity operator : operators) {
            result.add(new OperatorResult(operator));
        }
        return result;
    }

    /**
     * Adds a new operator with authorization check.
    */
    @Override
    @Transactional
    public @NonNull OperatorResult addOperatorWithAuth(
            String operatorId,
            String operatorName,
            String operatorAddress,
            String openOperatorId,
            String globalOperatorId,
            LocalDate effectiveStartDate,
            LocalDate effectiveEndDate,
            String creatorOperatorId,
            String storeId) {
        String resource = ConstPath.TUPLE_OPERATOR_PATH_SHORT;
        String action = Const.AUTHZ_ACTION_CREATE;
        // Evaluate authorization
        boolean authorized;
        try {
            authorized = authorizationService.evaluate(storeId, creatorOperatorId, resource, action);
        } catch (Exception e) {
            // Preserve known exception types so ControllerAdvice can map them to proper HTTP status
            if (e instanceof BadParametersException
                    || e instanceof ConflictException
                    || e instanceof NotFoundException
                    || e instanceof OutOfServiceException
                    || e instanceof ForbiddenException) {
                throw (RuntimeException) e;
            }
            // Unknown exception -> wrap as UnexpectedException (500)
            throw new UnexpectedException(ConstError.ERR_500, e.getMessage(), e);
        }
        if (!authorized) {
            // Authorization failed
            throw new IllegalAuthDataException(
                    String.format(ConstError.ERR_403_AUTHORIZATION_FAILED, creatorOperatorId, resource, action));
        }

        // add operator
        OperatorResult serviceResult = addOperator(operatorId, operatorName, operatorAddress, openOperatorId, globalOperatorId, effectiveStartDate, effectiveEndDate, creatorOperatorId);

        // Write tuples to authorization store
        try {
            List<Tuple> tuples;
            boolean operatorAuthorization = odsProperties.isEnableOperatorPlantAuthorization();
            if (operatorAuthorization) {
                // Operator authorization is true
                tuples = TupleUtils.buildOperatorDefaultTuples(serviceResult.getOperatorId());
            } else {
                // Operator authorization is false
                tuples = TupleUtils.buildOperatorSuTuples(serviceResult.getOperatorId());
            }
            authorizationService.writeTuples(tuples, storeId);
        } catch (Exception e) {
            throw new UnexpectedException(ConstError.ERR_500, e.getMessage(), e);
        }

        return serviceResult;
    }

    /**
     * Updates operator information with authorization check.
    */
    @Override
    public @Nullable OperatorResult updateOperatorWithAuth(
            String targetOperatorId,
            LocalDateTime updatedAt,
            String operatorName,
            String operatorAddress,
            String openOperatorId,
            StateString globalOperatorId,
            String updateOperatorId,
            String storeId) {
        String resource = ConstPath.TUPLE_OPERATOR_PATH_WITH_ID_SHORT.replace("{operator_id}", targetOperatorId);
        String action = Const.AUTHZ_ACTION_UPDATE;
        boolean authorized;
        try {
            authorized = authorizationService.evaluate(storeId, updateOperatorId, resource, action);
        } catch (Exception e) {
            if (e instanceof BadParametersException
                    || e instanceof ConflictException
                    || e instanceof NotFoundException
                    || e instanceof OutOfServiceException
                    || e instanceof ForbiddenException) {
                throw (RuntimeException) e;
            }
            throw new UnexpectedException(ConstError.ERR_500, e.getMessage(), e);
        }
        if (!authorized) {
            // Authorization failed
            throw new IllegalAuthDataException(
                    String.format(ConstError.ERR_403_AUTHORIZATION_FAILED, updateOperatorId, resource, action));
        }
        return updateOperator(targetOperatorId, updatedAt, operatorName, operatorAddress, openOperatorId, globalOperatorId, updateOperatorId);
    }

    /**
     * Updates operator status with authorization check.
    */
    @Override
    public @Nullable OperatorResult updateOperatorStatusWithAuth(
            String targetOperatorId, LocalDateTime updatedAt,
            LocalDate effectiveStartDate, LocalDate effectiveEndDate, Boolean deletedFlag,
            String updateOperatorId,
            String storeId) {
        String resource = ConstPath.TUPLE_OPERATOR_PATH_WITH_ID_SHORT.replace("{operator_id}", targetOperatorId);
        String action = Const.AUTHZ_ACTION_STATUSUP;
        boolean authorized;
        try {
            authorized = authorizationService.evaluate(storeId, updateOperatorId, resource, action);
        } catch (Exception e) {
            if (e instanceof BadParametersException
                    || e instanceof ConflictException
                    || e instanceof NotFoundException
                    || e instanceof OutOfServiceException
                    || e instanceof ForbiddenException) {
                throw (RuntimeException) e;
            }
            throw new UnexpectedException(ConstError.ERR_500, e.getMessage(), e);
        }
        if (!authorized) {
            // Authorization failed
            throw new IllegalAuthDataException(
                    String.format(ConstError.ERR_403_AUTHORIZATION_FAILED, updateOperatorId, resource, action));
        }
        return updateOperatorStatus(targetOperatorId, updatedAt, effectiveStartDate, effectiveEndDate, deletedFlag, updateOperatorId);
    }

    /**
    * getOperator with Authorization.
     */
    @Override
    public OperatorResult getOperatorWithAuth(String operatorId, String userId, String storeId) {

        String resource = ConstPath.TUPLE_OPERATOR_PATH_WITH_ID_SHORT.replace("{operator_id}", operatorId);
        String action = Const.AUTHZ_ACTION_GET;
        boolean authorized;
        try {
            authorized = authorizationService.evaluate(storeId, userId, resource, action);
        } catch (Exception e) {
            if (e instanceof BadParametersException
                    || e instanceof ConflictException
                    || e instanceof NotFoundException
                    || e instanceof OutOfServiceException
                    || e instanceof ForbiddenException) {
                throw (RuntimeException) e;
            }
            throw new UnexpectedException(ConstError.ERR_500, e.getMessage(), e);
        }
        if (!authorized) {
            // Authorization failed
            throw new IllegalAuthDataException(
                    String.format(ConstError.ERR_403_AUTHORIZATION_FAILED, userId, resource, action));
        }
        return getOperator(operatorId);
    }

    /**
    * searchOperators with Authorization.
     */
    @Override
    public List<OperatorResult> searchOperatorsWithAuth(
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
            LocalDate effectiveDate,
            String userId,
            String storeId
    ) {
        // Parameters for authorization evaluation
        String action = Const.AUTHZ_ACTION_GET;

        // First, get the list of operators matching the search criteria
        List<OperatorResult> list = searchOperators(0, 0, orderByKey, orderByDirection,
                operatorId, operatorName, operatorAddress, openOperatorId, globalOperatorId, deletedFlag, effectiveDate);

        // If no operators found, return empty list
        if (list.isEmpty()) {
            return Collections.emptyList();
        }

        // Resources to evaluate
        List<Map<String, String>> resources = new ArrayList<>(list.size());
        for (OperatorResult op : list) {
            Map<String, String> m = new HashMap<>();
            m.put(Const.OPENFGA_EVALUATION_DEFAULT_KEY, Const.OPENFGA_EVALUATION_DEFAULT_RESOURCE_TYPE);
            m.put(Const.OPENFGA_EVALUATION_DEFAULT_ID, ConstPath.TUPLE_OPERATOR_PATH_WITH_ID_SHORT.replace("{operator_id}", op.getOperatorId()));
            resources.add(m);
        }

        // Evaluate authorization for all resources
        List<Boolean> decisions;
        try {
            decisions = authorizationService.evaluations(storeId, userId, action, resources);
        } catch (Exception e) {
            if (e instanceof BadParametersException
                    || e instanceof ConflictException
                    || e instanceof NotFoundException
                    || e instanceof OutOfServiceException
                    || e instanceof ForbiddenException) {
                throw (RuntimeException) e;
            }
            throw new UnexpectedException(ConstError.ERR_500, e.getMessage(), e);
        }

        // decisions count must match resources count
        if (decisions == null || decisions.size() != resources.size()) {
            throw new UnexpectedException(
                    ConstError.ERR_500,
                    String.format(ConstError.ERR_500_OPENFGA_FAILED_WITH_MESSAGE, String.format("Authorization decisions count mismatch. expected=%d, actual=%d", resources.size(), decisions == null ? 0 : decisions.size()))
            );
        }

        // Collect authorized operators
        List<OperatorResult> authorizedAccum = new ArrayList<>(list.size());
        for (int i = 0; i < list.size() && i < decisions.size(); i++) {
            if (Boolean.TRUE.equals(decisions.get(i))) {
                authorizedAccum.add(list.get(i));
            }
        }

        // Apply limit and offset
        if (authorizedAccum.isEmpty() || authorizedAccum.size() <= offset) {
            return Collections.emptyList();
        }

        // Calculate end index
        int end = limit > 0 ? Math.min(authorizedAccum.size(), offset + limit) : authorizedAccum.size();
        return new ArrayList<>(authorizedAccum.subList(offset, end));
    }
}