/*
 * PlantServiceImpl.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This service provides the implementation for plant information management logic.
 *
 * Date: 2025/06/30
 */

package io.github.open_dataspaces.core.domain.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.lang.NonNull;

import io.github.open_dataspaces.core.domain.repository.interfaces.OperatorRepository;
import io.github.open_dataspaces.core.domain.repository.interfaces.PlantRepository;
import io.github.open_dataspaces.core.domain.service.interfaces.PlantService;
import io.github.open_dataspaces.core.exception.BadParametersException;
import io.github.open_dataspaces.core.exception.ConflictException;
import io.github.open_dataspaces.core.exception.ForbiddenException;
import io.github.open_dataspaces.core.exception.IllegalAuthDataException;
import io.github.open_dataspaces.core.exception.NotFoundException;
import io.github.open_dataspaces.core.exception.OutOfServiceException;
import io.github.open_dataspaces.core.exception.UnexpectedException;
import io.github.open_dataspaces.core.infrastructure.dto.Tuple;
import io.github.open_dataspaces.core.infrastructure.utils.TupleUtils;
import io.github.open_dataspaces.core.common.config.ODSProperties;
import io.github.open_dataspaces.core.common.consts.Const;
import io.github.open_dataspaces.core.common.consts.ConstError;
import io.github.open_dataspaces.core.common.consts.ConstPath;
import io.github.open_dataspaces.core.common.consts.ConstSqlQueries;
import io.github.open_dataspaces.core.common.utils.UUIDUtils;
import io.github.open_dataspaces.core.domain.dto.PlantResult;
import io.github.open_dataspaces.core.domain.dto.StateString;
import io.github.open_dataspaces.core.domain.entities.OperatorEntity;
import io.github.open_dataspaces.core.common.utils.DateUtils;
import io.github.open_dataspaces.core.common.utils.SqlUtils;
import io.github.open_dataspaces.core.domain.entities.PlantEntity;
import io.github.open_dataspaces.core.domain.service.interfaces.AuthorizationService;

import jakarta.annotation.Nullable;

/**
 * Service implementation for plant information management operations.
 *
 * <p>Provides logic to retrieve and update plant information.</p>
 */
@Service
public class PlantServiceImpl implements PlantService {

    // ODS application properties
    private final ODSProperties odsProperties;

    private final PlantRepository plantRepository;

    private final OperatorRepository operatorRepository;

    private final AuthorizationService authorizationService;

    /**
     * Constructs a new PlantServiceImpl with the specified repository.
     *
     * @param plantRepository the plant repository
     */
    public PlantServiceImpl(ODSProperties odsProperties, PlantRepository plantRepository, OperatorRepository operatorRepository, AuthorizationService authorizationService) {
        this.odsProperties = odsProperties;
        this.plantRepository = plantRepository;
        this.operatorRepository = operatorRepository;
        this.authorizationService = authorizationService;
    }

    /**
     * Adds a new plant with the specified parameters.
     */
    @Transactional
    public @NonNull PlantResult addPlant(String operatorId,
            String plantName, String plantAddress, String openPlantId, String globalPlantId,
            LocalDate effectiveStartDate, LocalDate effectiveEndDate, String creatorOperatorId) {

        // Validate operatorId
        List<String> operatorsList = new ArrayList<String>();
        if (!StringUtils.hasText(operatorId)
                || (operatorsList.add(operatorId) && UUIDUtils.getUUIDs(operatorsList).isEmpty())) {
            throw new UnexpectedException(ConstError.ERR_500_MESSAGE, String.format(ConstError.ERR_VALIDATION_INVALID_UUID, operatorId));
        }

        // Generate a new plantId
        String plantId = UUID.randomUUID().toString();

        // Check for duplicate plantId
        Optional<PlantEntity> entities = plantRepository.findById(plantId);
        if (!entities.isEmpty()) {
            // This is highly unlikely due to the nature of UUIDs, but we check just in case.
            throw new UnexpectedException(
                    ConstError.ERR_500_MESSAGE,
                    String.format(ConstError.ERRLOG_500_FAILED_TO_GENERATE_UUID, Const.JSON_PROPERTY_PLANT_ID));
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

        // Validate that the operatorId exists
        Optional<OperatorEntity> operatorEntity = operatorRepository.findById(operatorId);
        if (operatorEntity.isEmpty()) {
            throw new BadParametersException(
                    String.format(ConstError.ERRLOG_400_INVALID_REQUEST_PARAMETER, Const.JSON_PROPERTY_OPERATOR_ID, operatorId));
        }

        // Create a new PlantEntity with the provided parameters.
        PlantEntity plantEntity = new PlantEntity(
                plantId,
                operatorId,
                plantName,
                plantAddress,
                openPlantId,
                globalPlantId,
                effectiveStartDate,
                effectiveEndDate,
                creatorOperatorId);

        // Add the operator to the database.
        plantEntity = plantRepository.save(plantEntity);

        // Return the created PlantEntity as a PlantEntityResult.
        return new PlantResult(plantEntity);
    }

    /**
     * Updates the operator information with the specified parameters.
     */
    @Override
    public @Nullable PlantResult updatePlant(String targetPlantId, LocalDateTime updatedAt,
            String plantName, String plantAddress, String openPlantId, StateString globalPlantId,
            String updateOperatorId) {
        // Validate targetOperatorId
        Optional<PlantEntity> entities = plantRepository.findById(targetPlantId);
        if (entities.isEmpty()) {
            return null;
        }
        PlantEntity currentPlantEntity = entities.get();

        // Check if the operator is active and not deleted
        if (!DateUtils.isBetween(LocalDate.now(ZoneOffset.UTC), currentPlantEntity.getEffectiveStartDate(), currentPlantEntity.getEffectiveEndDate())
                || currentPlantEntity.isDeletedFlag()) {
            throw new IllegalAuthDataException(String.format(ConstError.ERRLOG_403_FORBIDDEN_RESOURCE_DISABLED, targetPlantId));
        }

        // Check for data update conflicts
        if (!DateUtils.equals(updatedAt, currentPlantEntity.getUpdatedAt(), Const.ISO_8601_UTC_MILLISECOND_FORMAT)) {
            throw new ConflictException(ConstError.ERRLOG_409_CONFLICT,
                    String.format(ConstError.ERR_409_CONFLICT, Const.JSON_PROPERTY_UPDATED_AT, DateUtils.formatDateTime(updatedAt, Const.ISO_8601_UTC_MILLISECOND_FORMAT)));
        }

        // Update only if there are changes
        if (currentPlantEntity.isUpdated(plantName, plantAddress, openPlantId, globalPlantId)) {

            if (plantName != null) {
                currentPlantEntity.setPlantName(plantName);
            }
            if (plantAddress != null) {
                currentPlantEntity.setPlantAddress(plantAddress);
            }
            if (openPlantId != null) {
                currentPlantEntity.setOpenPlantId(openPlantId);
            }
            if (globalPlantId.isValueOrNull()) {
                currentPlantEntity.setGlobalPlantId(globalPlantId.getValue());
            }
            currentPlantEntity.setUpdatedUserId(updateOperatorId);

            // Save the updated plant information to the database.
            currentPlantEntity = plantRepository.saveAndFlush(currentPlantEntity);
        }

        return new PlantResult(currentPlantEntity);
    }

    /**
     * Updates the plant information with the specified parameters.
     */
    @Override
    public @Nullable PlantResult updatePlantStatus(String targetPlantId, LocalDateTime updatedAt,
            LocalDate effectiveStartDate, LocalDate effectiveEndDate, Boolean deletedFlag,
            String updateOperatorId) {
        // Validate targetPlantId
        Optional<PlantEntity> entities = plantRepository.findById(targetPlantId);
        if (entities.isEmpty()) {
            return null;
        }
        PlantEntity currentPlantEntity = entities.get();

        // Check for data update conflicts
        if (!DateUtils.equals(updatedAt, currentPlantEntity.getUpdatedAt(), Const.ISO_8601_UTC_MILLISECOND_FORMAT)) {
            throw new ConflictException(ConstError.ERRLOG_409_CONFLICT,
                    String.format(ConstError.ERR_409_CONFLICT, Const.JSON_PROPERTY_UPDATED_AT, DateUtils.formatDateTime(updatedAt, Const.ISO_8601_UTC_MILLISECOND_FORMAT)));
        }

        // Validate effectiveStartDate and effectiveEndDate
        if (effectiveStartDate != null) {
            LocalDate validEndDate = (effectiveEndDate != null) ? effectiveEndDate : currentPlantEntity.getEffectiveEndDate();
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
            if (!DateUtils.isValidDateRange(currentPlantEntity.getEffectiveStartDate(), effectiveEndDate)) {
                throw new BadParametersException(
                        String.format(ConstError.ERRLOG_400_EFFECTIVE_DATE_START_AFTER_END,
                                Const.JSON_PROPERTY_EFFECTIVE_END_DATE,
                                DateUtils.formatDate(currentPlantEntity.getEffectiveStartDate(), Const.DATE_FORMAT),
                                DateUtils.formatDate(effectiveEndDate, Const.DATE_FORMAT))
                );
            }
        }

        // Update only if there are changes
        if (currentPlantEntity.isStatusUpdated(effectiveStartDate, effectiveEndDate, deletedFlag)) {

            if (effectiveStartDate != null) {
                currentPlantEntity.setEffectiveStartDate(effectiveStartDate);
            }
            if (effectiveEndDate != null) {
                currentPlantEntity.setEffectiveEndDate(effectiveEndDate);
            }
            if (deletedFlag != null) {
                currentPlantEntity.setDeletedFlag(deletedFlag);
            }
            currentPlantEntity.setUpdatedUserId(updateOperatorId);

            // Save the updated operator information to the database.
            currentPlantEntity = plantRepository.saveAndFlush(currentPlantEntity);
        }
        return new PlantResult(currentPlantEntity);
    }

    /**
     * Retrieves plant information by plantId.
     *
     * <p>This method fetches a PlantEntity from the repository using the given plantId and maps it to a PlantResult DTO.
     * If no entity is found, null is returned.</p>
     *
     * @param plantId the unique identifier of the plant
     * @return PlantResult DTO containing plant information, or null if not found
     */
    @Override
    public @Nullable PlantResult getPlant(String plantId) {
        // Search by plantId
        Optional<PlantEntity> plantEntity = plantRepository.findById(plantId);
        if (plantEntity.isEmpty()) {
            return null;
        }

        return new PlantResult(plantEntity.get());
    }

    /**
     * Searches for plants based on various criteria.
     *
     * @param limit           the maximum number of results to return (page size)
     * @param offset          the starting index of the results (page offset)
     * @param orderByKey      the field name to sort by
     * @param orderByDirection the sort direction ("ASC" or "DESC")
     * @param operatorId      the operator identifier to filter by
     * @param plantId         the plant identifier to filter by
     * @param plantName       the plant name to filter by (partial match)
     * @param plantAddress    the plant address to filter by (partial match)
     * @param deletedFlag     the deletion flag to filter by (true: disabled, false: enabled)
     * @param effectiveDate   the effective date to filter by
     * @return a list of PlantResult objects matching the search criteria
     */
    @Override
    public @NonNull List<PlantResult> searchPlants(
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
            LocalDate effectiveDate
    ) {
        // Check sort key and order validity
        Sort sort = SqlUtils.buildSort(PlantEntity.class, orderByKey, orderByDirection, ConstSqlQueries.COLUMN_PLANTS_PLANT_ID, Sort.Direction.ASC);

        // Build search specification
        Specification<PlantEntity> spec = SqlUtils.createSpecification();
        if (operatorId != null) {
            spec = SqlUtils.buildSpecification(spec, SqlUtils.SpecificationMethod.EQUALS, PlantEntity.class, ConstSqlQueries.COLUMN_PLANTS_OPERATOR_ID, operatorId);
        }
        if (plantId != null) {
            spec = SqlUtils.buildSpecification(spec, SqlUtils.SpecificationMethod.EQUALS, PlantEntity.class, ConstSqlQueries.COLUMN_PLANTS_PLANT_ID, plantId);
        }
        if (openPlantId != null) {
            spec = SqlUtils.buildSpecification(spec, SqlUtils.SpecificationMethod.EQUALS, PlantEntity.class, ConstSqlQueries.COLUMN_PLANTS_OPEN_PLANT_ID, openPlantId);
        }
        if (globalPlantId.isValueOrNull()) {
            spec = SqlUtils.buildSpecification(spec, SqlUtils.SpecificationMethod.EQUALS, PlantEntity.class, ConstSqlQueries.COLUMN_PLANTS_GLOBAL_PLANT_ID, globalPlantId.getValue());
        }
        if (plantName != null) {
            spec = SqlUtils.buildSpecification(spec, SqlUtils.SpecificationMethod.LIKE, PlantEntity.class, ConstSqlQueries.COLUMN_PLANTS_PLANT_NAME, plantName);
        }
        if (plantAddress != null) {
            spec = SqlUtils.buildSpecification(spec, SqlUtils.SpecificationMethod.LIKE, PlantEntity.class, ConstSqlQueries.COLUMN_PLANTS_PLANT_ADDRESS, plantAddress);
        }
        if (deletedFlag != null) {
            spec = SqlUtils.buildSpecification(spec, SqlUtils.SpecificationMethod.EQUALS, PlantEntity.class, ConstSqlQueries.COLUMN_PLANTS_DELETED_FLAG, deletedFlag);
        }

        // Effective date range
        if (effectiveDate != null) {
            spec = SqlUtils.buildSpecification(spec, SqlUtils.SpecificationMethod.LESS_THAN_OR_EQUALS, PlantEntity.class, ConstSqlQueries.COLUMN_COMMON_EFFECTIVE_START_DATE, effectiveDate);
            spec = SqlUtils.buildSpecification(spec, SqlUtils.SpecificationMethod.GREATER_THAN_OR_EQUALS, PlantEntity.class, ConstSqlQueries.COLUMN_COMMON_EFFECTIVE_END_DATE, effectiveDate);
        }

        // Fetch plants with pagination
        List<PlantEntity> plants = plantRepository.findWithLimitOffset(spec, offset, limit, sort);

        // Convert PlantEntity to PlantResult
        List<PlantResult> result = new ArrayList<>();
        for (PlantEntity plant : plants) {
            result.add(new PlantResult(plant));
        }
        return result;
    }

    /**
     * Adds a new plant with authorization check.
    */
    @Transactional
    public @NonNull PlantResult addPlantWithAuth(
            String operatorId,
            String plantName,
            String plantAddress,
            String openPlantId,
            String globalPlantId,
            LocalDate effectiveStartDate,
            LocalDate effectiveEndDate,
            String creatorOperatorId,
            String storeId
    ) {
        String resource = ConstPath.TUPLE_OPERATOR_PLANT_PATH_SHORT.replace("{operator_id}", operatorId);
        String action = Const.AUTHZ_ACTION_CREATE;
        boolean authorized;

        // Authorization check
        try {
            authorized = authorizationService.evaluate(storeId, creatorOperatorId, resource, action);
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
                    String.format(ConstError.ERR_403_AUTHORIZATION_FAILED, creatorOperatorId, resource, action));
        }

        //add plant
        PlantResult serviceResult = addPlant(operatorId, plantName, plantAddress, openPlantId, globalPlantId, effectiveStartDate, effectiveEndDate, creatorOperatorId);

        // Determine operator type by checking authorization
        // Check operator type in order: superuser > managing operator > user operator
        boolean isSuperuser = false;
        boolean isManagingOperator = false;

        try {
            // First check if operatorId is a superuser
            String checkAction = Const.TUPLE_REL_MEMBER;
            isSuperuser = authorizationService.evaluate(storeId, Const.OPENFGA_EVALUATION_DEFAULT_USER_TYPE, operatorId, Const.TUPLE_REL_ADMIN, Const.TUPLE_REL_SU_GROUP_NAME, checkAction);

            // If not superuser, check if managing operator
            if (!isSuperuser) {
                isManagingOperator = authorizationService.evaluate(storeId, Const.OPENFGA_EVALUATION_DEFAULT_USER_TYPE, operatorId, Const.TUPLE_REL_ADMIN, Const.TUPLE_REL_MANAGERS_GROUP_NAME, checkAction);
            }
        } catch (BadParametersException | ConflictException | NotFoundException
                 | OutOfServiceException | ForbiddenException e) {
            // Rethrow known exceptions
            throw (RuntimeException) e;
        } catch (Exception e) {
            // Wrap and throw unexpected exceptions
            throw new UnexpectedException(ConstError.ERR_500, e.getMessage(), e);
        }

        // Write tuples to authorization store for plant relationships
        try {
            List<Tuple> tuples;
            if (isSuperuser) {
                // Use superuser operator tuple builder
                tuples = TupleUtils.buildPlantDefaultTuplesForSuOperator(operatorId, serviceResult.getPlantId());
            } else if (isManagingOperator) {
                // Use managing operator tuple builder
                tuples = TupleUtils.buildPlantDefaultTuplesForManagingOperator(operatorId, serviceResult.getPlantId());
            } else {
                // Use user operator tuple builder
                tuples = TupleUtils.buildPlantDefaultTuplesForUserOperator(operatorId, serviceResult.getPlantId());
            }
            authorizationService.writeTuples(tuples, storeId);
        } catch (Exception e) {
            throw new UnexpectedException(ConstError.ERR_500, e.getMessage(), e);
        }

        return serviceResult;
    }

    /**
     * Updates plant information with authorization check.
    */
    @Transactional
    public @Nullable PlantResult updatePlantWithAuth(
            String targetPlantId,
            LocalDateTime updatedAt,
            String plantName,
            String plantAddress,
            String openPlantId,
            StateString globalPlantId,
            String updateOperatorId,
            String storeId
    ) {
        String resource = ConstPath.TUPLE_OPERATOR_PLANT_PATH_WITH_ID_SHORT.replace("{plant_id}", targetPlantId);
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

        return updatePlant(targetPlantId, updatedAt, plantName, plantAddress, openPlantId, globalPlantId, updateOperatorId);
    }

    /**
     * Updates plant status with authorization check.
    */
    @Transactional
    public @Nullable PlantResult updatePlantStatusWithAuth(
            String targetPlantId,
            LocalDateTime updatedAt,
            LocalDate effectiveStartDate,
            LocalDate effectiveEndDate,
            Boolean deletedFlag,
            String updateOperatorId,
            String storeId
    ) {
        String resource = ConstPath.TUPLE_OPERATOR_PLANT_PATH_WITH_ID_SHORT.replace("{plant_id}", targetPlantId);
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
        return updatePlantStatus(targetPlantId, updatedAt, effectiveStartDate, effectiveEndDate, deletedFlag, updateOperatorId);
    }

    /**
     * getPlant with Authorization.
     *
     * @param plantId the plant identifier
     * @param userId the user performing the request
     * @param storeId the store id (from API key)
     * @return the PlantResult when authorized
     */
    @Override
    public PlantResult getPlantWithAuth(String plantId, String userId, String storeId) {

        String resource = ConstPath.TUPLE_OPERATOR_PLANT_PATH_WITH_ID_SHORT.replace("{plant_id}", plantId);
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
        return getPlant(plantId);
    }

    /**
    * searchPlants with Authorization.
    */
    @Override
    public List<PlantResult> searchPlantsWithAuth(
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
            LocalDate effectiveDate,
            String userId,
            String storeId
    ) {
        String action = Const.AUTHZ_ACTION_GET;

        // Fetch all plants matching the search criteria without pagination
        List<PlantResult> list = searchPlants(0, 0, orderByKey, orderByDirection,
                operatorId, plantId, openPlantId, globalPlantId, plantName, plantAddress, deletedFlag, effectiveDate);

        // If no plants found, return empty list
        if (list.isEmpty()) {
            return java.util.Collections.emptyList();
        }

        // Resources to evaluate
        List<Map<String, String>> resources = new ArrayList<>(list.size());
        for (PlantResult pl : list) {
            Map<String, String> m = new HashMap<>();
            m.put(Const.OPENFGA_EVALUATION_DEFAULT_KEY, Const.OPENFGA_EVALUATION_DEFAULT_RESOURCE_TYPE);
            m.put(Const.OPENFGA_EVALUATION_DEFAULT_ID, ConstPath.TUPLE_OPERATOR_PLANT_PATH_WITH_ID_SHORT.replace("{plant_id}", pl.getPlantId()));
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

        // Strict mode: decisions count must match resources count
        if (decisions == null || decisions.size() != resources.size()) {
            throw new UnexpectedException(
                    ConstError.ERR_500,
                    String.format(ConstError.ERR_500_OPENFGA_FAILED_WITH_MESSAGE, String.format("Authorization decisions count mismatch. expected=%d, actual=%d", resources.size(), decisions == null ? 0 : decisions.size()))
            );
        }

        // Collect authorized plants
        List<PlantResult> authorizedAccum = new ArrayList<>(list.size());
        for (int i = 0; i < list.size() && i < decisions.size(); i++) {
            if (Boolean.TRUE.equals(decisions.get(i))) {
                authorizedAccum.add(list.get(i));
            }
        }

        // Apply limit and offset
        if (authorizedAccum.isEmpty() || authorizedAccum.size() <= offset) {
            return java.util.Collections.emptyList();
        }

        // Calculate end index
        int end = limit > 0 ? Math.min(authorizedAccum.size(), offset + limit) : authorizedAccum.size();
        return new ArrayList<>(authorizedAccum.subList(offset, end));
    }
}
