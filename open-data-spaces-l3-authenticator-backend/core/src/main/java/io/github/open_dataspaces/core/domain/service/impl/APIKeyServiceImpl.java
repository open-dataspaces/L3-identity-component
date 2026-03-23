/*
 * APIKeyServiceImpl.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This service provides the implementation for API key verification logic.
 *
 * Date: 2025/06/30
 */

package io.github.open_dataspaces.core.domain.service.impl;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.lang.NonNull;

import io.github.open_dataspaces.core.common.config.ODSProperties;
import io.github.open_dataspaces.core.common.consts.ConstError;
import io.github.open_dataspaces.core.common.utils.DateUtils;
import io.github.open_dataspaces.core.common.utils.EntityUtils;
import io.github.open_dataspaces.core.domain.dto.APIKeyVerifyParam;
import io.github.open_dataspaces.core.domain.repository.interfaces.APIKeysRepository;
import io.github.open_dataspaces.core.domain.repository.interfaces.AuthorizationStoresRepository;
import io.github.open_dataspaces.core.domain.repository.interfaces.AuthorizationUCStoresRepository;
import io.github.open_dataspaces.core.domain.entities.APIKeysEntity;
import io.github.open_dataspaces.core.domain.entities.AuthorizationStoresEntity;
import io.github.open_dataspaces.core.domain.entities.AuthorizationUCStoresEntity;
import io.github.open_dataspaces.core.domain.entities.CidrsEntity;
import io.github.open_dataspaces.core.domain.repository.interfaces.CidrsRepository;
import io.github.open_dataspaces.core.domain.service.interfaces.APIKeyService;
import io.github.open_dataspaces.core.exception.ForbiddenException;
import io.github.open_dataspaces.core.exception.IllegalAuthDataException;
import io.github.open_dataspaces.core.exception.UnexpectedException;

/**
 * Service implementation for API key verification.
 *
 * <p>Provides logic to verify API keys and their associated IP addresses and attributes.</p>
 */
@Service
public class APIKeyServiceImpl implements APIKeyService {

    private final ODSProperties odsProperties;
    private final APIKeysRepository apiKeysRepository;
    private final CidrsRepository cidrsRepository;
    private final AuthorizationStoresRepository authorizationStoresRepository;
    private final AuthorizationUCStoresRepository authorizationUCStoresRepository;

    /**
     * Constructs a new APIKeyServiceImpl with the specified repositories.
     *
     * @param apiKeysRepository the API keys repository
     * @param cidrsRepository the CIDRs repository
     */
    public APIKeyServiceImpl(
            ODSProperties odsProperties,
            APIKeysRepository apiKeysRepository,
            CidrsRepository cidrsRepository,
            AuthorizationStoresRepository authorizationStoresRepository,
            AuthorizationUCStoresRepository authorizationUCStoresRepository) {
        this.odsProperties = odsProperties;
        this.apiKeysRepository = apiKeysRepository;
        this.cidrsRepository = cidrsRepository;
        this.authorizationStoresRepository = authorizationStoresRepository;
        this.authorizationUCStoresRepository = authorizationUCStoresRepository;
    }

    /**
     * Verifies the API key based on the provided parameters.
     * <ul>
     * <li>If an IP address is specified, checks if it is within the allowed CIDR range for the API
     * key.</li>
     * <li>If no IP address is specified, checks if the API key matches the specified
     * attributes.</li>
     * </ul>
     *
     * @param param the API key verification parameters
     * @return true if the API key is valid for the given parameters, false otherwise
     */
    @Override
    public boolean verify(@NonNull APIKeyVerifyParam param) {
        // Current date for effective date checks
        LocalDate currentDate = LocalDate.now(ZoneOffset.UTC);

        // Check if the API key is valid
        List<APIKeysEntity> apiKeysEntities = apiKeysRepository.findByApiKey(param.getApiKey(), currentDate);
        if (apiKeysEntities == null || apiKeysEntities.isEmpty()) {
            throw new IllegalAuthDataException(ConstError.ERRLOG_403_APIKEY_NOT_VALID,
                    ConstError.ERR_403_APIKEY_NOT_VALID);
        }

        // API key
        if (StringUtils.hasText(param.getIp())) {
            List<CidrsEntity> cidrsEntities =
                    cidrsRepository.findCidrs(param.getApiKey(), param.getIp(), currentDate);
            // If no CIDR is found for the given API key and IP address, throw an exception
            if (cidrsEntities == null || cidrsEntities.isEmpty()) {
                throw new IllegalAuthDataException(
                        String.format(ConstError.ERRLOG_403_IP_NOT_AUTHORIZED_FOR_KEY),
                        ConstError.ERR_403_IP_NOT_AUTHORIZED_FOR_KEY);
            }
        }

        return true; // If no IP address is specified, assume the API key is valid
    }

    /**
     * Verifies the API key based on the provided parameters.
     * <ul>
     * Search for the specified API key in the TBL_API_KEYS table,
     * and return true if it exists; return false if it does not exist.
     * </ul>
     *
     * @param verifyApikey the API key verification parameters
     * @return true if the API key is valid for the given parameters, false otherwise
     */
    @Override
    public boolean verify(@NonNull String verifyApikey) {
        // Current date for effective date checks
        LocalDate currentDate = LocalDate.now(ZoneOffset.UTC);
        List<APIKeysEntity> apiKeysEntities = apiKeysRepository.findByApiKey(verifyApikey, currentDate);
        if (apiKeysEntities.isEmpty()) {
            return false;
        }
        return true;
    }

    /**
     * Retrieves the IDP realm associated with the given API key.
     *
     * @param apiKey the API key to look up
     * @return the IDP realm associated with the API key, or null if not found
     */
    @Override
    public String getIdpRealm(@NonNull String apiKey) {
        // Current date for effective date checks
        LocalDate currentDate = LocalDate.now(ZoneOffset.UTC);

        // Implementation logic to retrieve the IDP realm for the given API key
        List<APIKeysEntity> apiKeysEntities = apiKeysRepository.findByApiKey(apiKey, currentDate);
        if (apiKeysEntities == null || apiKeysEntities.isEmpty()) {
            throw new IllegalArgumentException(ConstError.ERR_403_APIKEY_NOT_VALID);
        }
        // Return the IDP realm from the found API key entity
        return apiKeysEntities.get(0).getIdpRealm();
    }

    /**
     * Retrieves the store ID associated with the given API key and store purpose.
     */
    @Override
    public String getStoreId(String apiKey, String storePurpose) throws UnexpectedException, ForbiddenException {
        // Retrieve the store ID associated with the given API key.
        String idpRealm = getIdpRealm(apiKey);

        // Find the authorization store entity by environment name, IDP realm, and store purpose
        Optional<AuthorizationStoresEntity> optionalEntity =
                authorizationStoresRepository.findByEnvironmentNameAndIdpRealmAndPdpStorePurpose(
                odsProperties.getApplicationEnvName(), idpRealm, storePurpose);
        if (optionalEntity.isEmpty()) {
            // If no store is found for the given IDP realm, throw an exception
            throw new UnexpectedException(
                    String.format(ConstError.ERRLOG_500_DB_TABLE_INVALID, EntityUtils.getTableName(AuthorizationStoresEntity.class)),
                    ConstError.ERR_500
            );
        }
        AuthorizationStoresEntity authorizationStoresEntity = optionalEntity.get();

        // Validate effective dates and deletion flag
        if (!DateUtils.isBetween(LocalDate.now(ZoneOffset.UTC), authorizationStoresEntity.getEffectiveStartDate(), authorizationStoresEntity.getEffectiveEndDate())
                || authorizationStoresEntity.isDeletedFlag()) {
            throw new ForbiddenException(
                    String.format(ConstError.ERRLOG_403_INVALID_AUTHORIZATION, idpRealm),
                    ConstError.ERR_403_APIKEY_NOT_VALID);
        }

        return authorizationStoresEntity.getPdpStoreId();
    }

    /**
     * Retrieves the usecase associated with the given API key.
     *
     * @param apiKey the API key to look up
     * @return the usecase associated with the API key, or null if not found
     */
    @Override
    public String getUsecase(@NonNull String apiKey) {
        // Current date for effective date checks
        LocalDate currentDate = LocalDate.now(ZoneOffset.UTC);

        // Implementation logic to retrieve the IDP realm for the given API key
        List<APIKeysEntity> apiKeysEntities = apiKeysRepository.findByApiKey(apiKey, currentDate);
        if (apiKeysEntities == null || apiKeysEntities.isEmpty()) {
            throw new IllegalArgumentException(ConstError.ERR_403_APIKEY_NOT_VALID);
        }
        // Return the IDP realm from the found API key entity
        return apiKeysEntities.get(0).getUsecase();
    }

    /**
     * Retrieves the use case store name associated with the given API key.
     */
    @Override
    public String getUsecaseStoreId(String apiKey) throws UnexpectedException, ForbiddenException {
        // Retrieve the store ID associated with the given API key.
        String usecase = getUsecase(apiKey);
        // Find the authorization store entity by environment name and IDP realm
        Optional<AuthorizationUCStoresEntity> optionalEntity =
                authorizationUCStoresRepository.findByUsecase(usecase);
        // If no store is found for the given IDP realm, throw an exception
        if (optionalEntity.isEmpty()) {
            throw new UnexpectedException(
                String.format(ConstError.ERRLOG_500_DB_TABLE_INVALID, EntityUtils.getTableName(AuthorizationUCStoresEntity.class)),
                ConstError.ERR_500
            );
        }
        AuthorizationUCStoresEntity authorizationUCStoresEntity = optionalEntity.get();

        // Validate effective dates and deletion flag
        if (!DateUtils.isBetween(LocalDate.now(ZoneOffset.UTC), authorizationUCStoresEntity.getEffectiveStartDate(), authorizationUCStoresEntity.getEffectiveEndDate())
                || authorizationUCStoresEntity.isDeletedFlag()) {
            throw new ForbiddenException(
                String.format(ConstError.ERRLOG_403_INVALID_AUTHORIZATION, usecase),
                ConstError.ERR_403_APIKEY_NOT_VALID);
        }

        return authorizationUCStoresEntity.getUcStoreId();
    }

    /**
     * Retrieves the usecase store name associated with the given API key.
     */
    @Override
    public String getUsecaseStoreName(String apiKey) throws UnexpectedException, ForbiddenException {
        String usecase = getUsecase(apiKey);

        // Find the authorization store entity by environment name and IDP realm
        Optional<AuthorizationUCStoresEntity> optionalEntity =
                authorizationUCStoresRepository.findByUsecase(usecase);
        if (optionalEntity.isEmpty()) {
            throw new UnexpectedException(
                String.format(ConstError.ERRLOG_500_DB_TABLE_INVALID, EntityUtils.getTableName(AuthorizationUCStoresEntity.class)),
                ConstError.ERR_500
            );
        }
        AuthorizationUCStoresEntity authorizationUCStoresEntity = optionalEntity.get();

        // Validate effective dates and deletion flag
        if (!DateUtils.isBetween(LocalDate.now(ZoneOffset.UTC), authorizationUCStoresEntity.getEffectiveStartDate(), authorizationUCStoresEntity.getEffectiveEndDate())
                || authorizationUCStoresEntity.isDeletedFlag()) {
            throw new ForbiddenException(
                String.format(ConstError.ERRLOG_403_INVALID_AUTHORIZATION, usecase),
                ConstError.ERR_403_APIKEY_NOT_VALID);
        }

        return authorizationUCStoresEntity.getUcStoreName();
    }
}
