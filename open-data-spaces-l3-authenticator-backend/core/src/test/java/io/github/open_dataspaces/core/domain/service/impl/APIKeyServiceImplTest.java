/*
 * APIKeyServiceImplTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This file is part of the user authentication backend system.
 * Package: io.github.open_dataspaces.core.domain.service.impl
 * This package contains service implementation classes and their corresponding unit tests
 * for the user authentication backend domain logic.
 */

package io.github.open_dataspaces.core.domain.service.impl;

import io.github.open_dataspaces.core.domain.repository.interfaces.APIKeysRepository;
import io.github.open_dataspaces.core.domain.repository.interfaces.AuthorizationStoresRepository;
import io.github.open_dataspaces.core.domain.repository.interfaces.AuthorizationUCStoresRepository;
import io.github.open_dataspaces.core.domain.repository.interfaces.CidrsRepository;
import io.github.open_dataspaces.core.exception.ForbiddenException;
import io.github.open_dataspaces.core.exception.AbstractBaseException;
import io.github.open_dataspaces.core.exception.IllegalAuthDataException;
import io.github.open_dataspaces.core.exception.UnexpectedException;
import io.github.open_dataspaces.core.common.config.ODSProperties;
import io.github.open_dataspaces.core.common.consts.ConstError;
import io.github.open_dataspaces.core.common.utils.EntityUtils;
import io.github.open_dataspaces.core.domain.dto.APIKeyVerifyParam;
import io.github.open_dataspaces.core.domain.entities.APIKeysEntity;
import io.github.open_dataspaces.core.domain.entities.AuthorizationStoresEntity;
import io.github.open_dataspaces.core.domain.entities.AuthorizationUCStoresEntity;
import io.github.open_dataspaces.core.domain.entities.CidrsEntity;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.junit.jupiter.api.DisplayName;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for the APIKeyServiceImpl class.
 *
 * <p>This test class verifies the correct behavior of API key verification logic,
 * including CIDR and attribute-based checks, and covers both normal and edge cases.
 * It uses Mockito for mocking repository dependencies and JUnit 5 for assertions.
 * </p>
 *
 * @author btmurayamakohei
 * @since 2025/06/26
 */
@ExtendWith(MockitoExtension.class)
class APIKeyServiceImplTest {

    @InjectMocks
    private APIKeyServiceImpl apiKeyService;

    @Mock
    private ODSProperties odsProperties;

    @Mock
    private APIKeysRepository apiKeysRepository;

    @Mock
    private CidrsRepository cidrsRepository; // Mocking CidrsRepository for completeness

    @Mock
    private AuthorizationStoresRepository authorizationStoresRepository;

    @Mock
    private AuthorizationUCStoresRepository authorizationUCStoresRepository;

    // Common input parameters
    private final String commonApiKey = "api-key-123";
    private final String commonStorePurpose = "API_EXECUTION";
    private final String commonEnvironmentName = "env-name-123";
    private final String commonIdpRealm = "idp-realm-123";
    private final String commonStoreId = "store-id-123";
    private final String commonUsecase = "usecase-123";
    private final String commonUcStoreId = "uc-store-123";
    private final String commonUcStoreName = "uc-store-name-123";
    private final boolean commonDeletedFlag = false;
    private final LocalDate commonEffctiveStartDate = LocalDate.now(ZoneOffset.UTC).minusYears(1);
    private final LocalDate commonEffctiveEndDate = LocalDate.now(ZoneOffset.UTC).plusYears(1);

    /**
     * Test No.1
     * Verifies that verify returns true when CIDR check passes.
     */
    @Test
    void success_findCidrs() {
        String apiKey = "e387cf3a-4584-4352-8ed3-50df0f4045bd";
        String ip = "192.168.1.1";
        LocalDate currentDate = LocalDate.now(ZoneOffset.UTC);
        APIKeyVerifyParam param = new APIKeyVerifyParam(
                apiKey, ip, null
        );

        when(apiKeysRepository.findByApiKey(apiKey, currentDate)).thenReturn(List.of(new APIKeysEntity()));
        when(cidrsRepository.findCidrs(apiKey, ip, currentDate)).thenReturn(List.of(new CidrsEntity()));

        boolean result = apiKeyService.verify(param);

        assertTrue(result, "Should return true when the API key is valid");
        verify(apiKeysRepository, times(1)).findByApiKey(apiKey, currentDate);
        verify(cidrsRepository, times(1)).findCidrs(apiKey, ip, currentDate);
    }

    /**
     * Test No.2
     * Verifies that verify returns false when CIDR check returns an empty list.
     */
    @Test
    void failed_findCidrs_null() {
        String apiKey = "e387cf3a-4584-4352-8ed3-50df0f4045bd";
        String ip = "192.168.1.1";
        List<CidrsEntity> attributes = List.of();
        LocalDate currentDate = LocalDate.now(ZoneOffset.UTC);
        APIKeyVerifyParam param = new APIKeyVerifyParam(
                apiKey, ip, null
        );

        when(apiKeysRepository.findByApiKey(apiKey, currentDate)).thenReturn(List.of(new APIKeysEntity()));
        when(cidrsRepository.findCidrs(apiKey, ip, currentDate)).thenReturn(attributes);

        IllegalAuthDataException ex = assertThrows(
                IllegalAuthDataException.class, () -> apiKeyService.verify(param)
        );
        assertEquals(ConstError.ERR_403_IP_NOT_AUTHORIZED_FOR_KEY, ex.getMessage());

        verify(apiKeysRepository, times(1)).findByApiKey(apiKey, currentDate);
        verify(cidrsRepository, times(1)).findCidrs(apiKey, ip, currentDate);
    }

    /**
     * Test No.3
     * Verifies that verify returns false when CIDR check returns null.
     */
    @Test
    void failed_findCidrs_empty() {
        String apiKey = "e387cf3a-4584-4352-8ed3-50df0f4045bd";
        String ip = "192.168.1.1";
        LocalDate currentDate = LocalDate.now(ZoneOffset.UTC);
        APIKeyVerifyParam param = new APIKeyVerifyParam(
                apiKey, ip, null
        );

        when(apiKeysRepository.findByApiKey(apiKey, currentDate)).thenReturn(List.of(new APIKeysEntity()));
        when(cidrsRepository.findCidrs(apiKey, ip, currentDate)).thenReturn(null);

        IllegalAuthDataException ex = assertThrows(
                IllegalAuthDataException.class, () -> apiKeyService.verify(param)
        );
        assertEquals(ConstError.ERR_403_IP_NOT_AUTHORIZED_FOR_KEY, ex.getMessage());

        verify(apiKeysRepository, times(1)).findByApiKey(apiKey, currentDate);
        verify(cidrsRepository, times(1)).findCidrs(apiKey, ip, currentDate);
    }

    /**
     * Test No.4
     * Verifies that getIdpRealm returns the correct realm when the API key exists.
     */
    @Test
    void getIdpRealm_returnsIdpRealm_whenApiKeyExists() {
        String apiKey = "test-api-key";
        String expectedRealm = "test-realm";
        APIKeysEntity entity = new APIKeysEntity();
        LocalDate currentDate = LocalDate.now(ZoneOffset.UTC);
        entity.setIdpRealm(expectedRealm);

        when(apiKeysRepository.findByApiKey(apiKey, currentDate)).thenReturn(List.of(entity));

        String actualRealm = apiKeyService.getIdpRealm(apiKey);

        assertEquals(expectedRealm, actualRealm);
        verify(apiKeysRepository, times(1)).findByApiKey(apiKey, currentDate);
    }

    /**
     * Test No.5
     * Verifies that getIdpRealm throws an exception when the API key is not found.
     */
    @Test
    void getIdpRealm_throwsException_whenApiKeyNotFound() {
        String apiKey = "not-exist-api-key";
        LocalDate currentDate = LocalDate.now(ZoneOffset.UTC);
        when(apiKeysRepository.findByApiKey(apiKey, currentDate)).thenReturn(List.of());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                apiKeyService.getIdpRealm(apiKey)
        );
        assertEquals(ConstError.ERR_403_APIKEY_NOT_VALID, ex.getMessage());
    }

    /**
     * Verify behavior when the repository layer returns data.
     */
    @Test
    void verify_success() {
        String apiKey = "e387cf3a-4584-4352-8ed3-50df0f4045bd";
        LocalDate currentDate = LocalDate.now(ZoneOffset.UTC);

        when(apiKeysRepository.findByApiKey(apiKey, currentDate)).thenReturn(List.of(new APIKeysEntity()));

        boolean result = apiKeyService.verify(apiKey);

        assertTrue(result, "Should return true when the API key is valid");
        verify(apiKeysRepository, times(1)).findByApiKey(apiKey, currentDate);
    }

    /**
     * Verify behavior when the repository layer returns empty.
     */
    @Test
    void verify_returnEmpty() {
        String apiKey = "e387cf3a-4584-4352-8ed3-50df0f4045bd";
        LocalDate currentDate = LocalDate.now(ZoneOffset.UTC);

        when(apiKeysRepository.findByApiKey(apiKey, currentDate)).thenReturn(List.of());

        boolean result = apiKeyService.verify(apiKey);

        assertFalse(result, "Should return true when the API key is valid");
        verify(apiKeysRepository, times(1)).findByApiKey(apiKey, currentDate);
    }

    /**
     * Test verify method for handling an exception.
     */
    @ParameterizedTest
    @CsvSource({
        "IllegalArgumentException, BadParametersException, Invalid argument provided",
        "DataIntegrityViolationException, BadParametersException, foreign key",
        "RuntimeException, UnexpectedException, Some runtime exception"
    })
    @DisplayName("getOperator - Exception Handling")
    void verify_throwsException(
            String exceptionType,
            String thrownExceptionType,
            String exceptionMessage) {

        String apiKey = "e387cf3a-4584-4352-8ed3-50df0f4045bd";
        LocalDate currentDate = LocalDate.now(ZoneOffset.UTC);
        Exception exception = null;
        Class<? extends Throwable> clazz = null;
        switch (exceptionType) {
            case "IllegalArgumentException":
                exception = new IllegalArgumentException(exceptionMessage);
                clazz = IllegalArgumentException.class;
                break;
            case "DataIntegrityViolationException":
                exception = new DataIntegrityViolationException(exceptionMessage);
                clazz = DataIntegrityViolationException.class;
                break;
            case "RuntimeException":
                exception = new RuntimeException(exceptionMessage);
                clazz = RuntimeException.class;
                break;
            default:
                throw new IllegalArgumentException(String.format("Invalid exception type for test '%s'", exceptionType));
        }

        when(apiKeysRepository.findByApiKey(apiKey, currentDate))
                .thenThrow(exception);

        Throwable thrownException = assertThrows(clazz, () -> {
            apiKeyService.verify(apiKey);
        });

        assertEquals(thrownException.getClass(), clazz);
        if (thrownException instanceof AbstractBaseException) {
            AbstractBaseException thrownBaseException = (AbstractBaseException) thrownException;
            assertNotNull(thrownBaseException.getResponseMessage());
            assertNotNull(thrownBaseException.getLogMessage());
        } else {
            assertNotNull(thrownException.getMessage());
        }
    }

    /**
     * Verifies that getStoreId returns the correct store ID when the API key and store exist.
     */
    @Test
    @DisplayName("getStoreId - success")
    void testGetStoreId_success() throws UnexpectedException, ForbiddenException {
        APIKeysEntity apiKeysEntity = new APIKeysEntity();
        apiKeysEntity.setIdpRealm(commonIdpRealm);
        List<APIKeysEntity> apiKeysEntities = List.of(apiKeysEntity);

        AuthorizationStoresEntity authorizationStoresEntity = new AuthorizationStoresEntity();
        authorizationStoresEntity.setPdpStoreId(commonStoreId);
        authorizationStoresEntity.setDeletedFlag(commonDeletedFlag);
        authorizationStoresEntity.setEffectiveStartDate(commonEffctiveStartDate);
        authorizationStoresEntity.setEffectiveEndDate(commonEffctiveEndDate);
        Optional<AuthorizationStoresEntity> optionalEntity = Optional.of(authorizationStoresEntity);

        // Arrange
        when(apiKeysRepository.findByApiKey(anyString(), any()))
                .thenReturn(apiKeysEntities);
        when(odsProperties.getApplicationEnvName())
                .thenReturn(commonEnvironmentName);
        when(authorizationStoresRepository.findByEnvironmentNameAndIdpRealmAndPdpStorePurpose(anyString(), anyString(), anyString()))
                .thenReturn(optionalEntity);

        // Act
        String storeId = apiKeyService.getStoreId(commonApiKey, commonStorePurpose);

        // Assert
        verify(authorizationStoresRepository, times(1)).findByEnvironmentNameAndIdpRealmAndPdpStorePurpose(anyString(), anyString(), anyString());
        assertEquals(commonStoreId, storeId);
    }

    /**
     * Verifies that getStoreId throws UnexpectedException when the AuthorizationStore is not found.
     */
    @Test
    @DisplayName("getStoreId - not found AuthorizationStore - throws UnexpectedException")
    void testGetStoreId_notFoundAuthorizationStore_throwsUnexpectedException() throws UnexpectedException, ForbiddenException {
        APIKeysEntity apiKeysEntity = new APIKeysEntity();
        apiKeysEntity.setIdpRealm(commonIdpRealm);
        List<APIKeysEntity> apiKeysEntities = List.of(apiKeysEntity);

        Optional<AuthorizationStoresEntity> optionalEntity = Optional.empty();

        // Arrange
        when(apiKeysRepository.findByApiKey(anyString(), any()))
                .thenReturn(apiKeysEntities);
        when(odsProperties.getApplicationEnvName())
                .thenReturn(commonEnvironmentName);
        when(authorizationStoresRepository.findByEnvironmentNameAndIdpRealmAndPdpStorePurpose(anyString(), anyString(), anyString()))
                .thenReturn(optionalEntity);

        // Act & Assert
        UnexpectedException exception = assertThrows(UnexpectedException.class, () ->
                apiKeyService.getStoreId(commonApiKey, commonStorePurpose)
        );

        // Assert
        verify(authorizationStoresRepository, times(1)).findByEnvironmentNameAndIdpRealmAndPdpStorePurpose(anyString(), anyString(), anyString());
        assertEquals(exception.getResponseMessage(), ConstError.ERR_500);
        assertTrue(exception.getLogMessage().contains(
                String.format(ConstError.ERRLOG_500_DB_TABLE_INVALID, EntityUtils.getTableName(AuthorizationStoresEntity.class))
        ));
    }

    /**
     * Verifies that getStoreId throws UnexpectedException when the AuthorizationStore is not found.
     */
    @ParameterizedTest
    @CsvSource({
        "DELETED, VALID, VALID",     //　deletedFlag is true
        "ACTIVE, INVALID, VALID",     //　Edge case: effectiveStartDate is in the future
        "ACTIVE, VALID, INVALID"     //　Edge case: effectiveEndDate is in the past
    })
    @DisplayName("getStoreId - invalid storeId - throws ForbiddenException")
    void testGetStoreId_invalidStoreId_throwsForbiddenException(
            String argDeletedFlag,
            String argEffectiveStartDate,
            String argEffectiveEndDate
    ) throws UnexpectedException, ForbiddenException {
        // Determine test-specific values based on parameters
        boolean deletedFlag =
                "DELETED".equals(argDeletedFlag) ? true : commonDeletedFlag;
        LocalDate effectiveStartDate =
                "INVALID".equals(argEffectiveStartDate) ? LocalDate.now(ZoneOffset.UTC).plusDays(1) : commonEffctiveStartDate;
        LocalDate effectiveEndDate =
                "INVALID".equals(argEffectiveEndDate) ? LocalDate.now(ZoneOffset.UTC).minusDays(1) : commonEffctiveEndDate;

        APIKeysEntity apiKeysEntity = new APIKeysEntity();
        apiKeysEntity.setIdpRealm(commonIdpRealm);
        List<APIKeysEntity> apiKeysEntities = List.of(apiKeysEntity);

        AuthorizationStoresEntity authorizationStoresEntity = new AuthorizationStoresEntity();
        authorizationStoresEntity.setPdpStoreId(commonStoreId);
        authorizationStoresEntity.setDeletedFlag(deletedFlag);
        authorizationStoresEntity.setEffectiveStartDate(effectiveStartDate);
        authorizationStoresEntity.setEffectiveEndDate(effectiveEndDate);
        Optional<AuthorizationStoresEntity> optionalEntity = Optional.of(authorizationStoresEntity);

        // Arrange
        when(apiKeysRepository.findByApiKey(anyString(), any()))
                .thenReturn(apiKeysEntities);
        when(odsProperties.getApplicationEnvName())
                .thenReturn(commonEnvironmentName);
        when(authorizationStoresRepository.findByEnvironmentNameAndIdpRealmAndPdpStorePurpose(anyString(), anyString(), anyString()))
                .thenReturn(optionalEntity);

        // Act & Assert
        ForbiddenException exception = assertThrows(ForbiddenException.class, () ->
                apiKeyService.getStoreId(commonApiKey, commonStorePurpose)
        );

        // Assert
        verify(authorizationStoresRepository, times(1)).findByEnvironmentNameAndIdpRealmAndPdpStorePurpose(anyString(), anyString(), anyString());
        assertEquals(exception.getResponseMessage(), ConstError.ERR_403_APIKEY_NOT_VALID);
        assertTrue(exception.getLogMessage().contains(
                String.format(ConstError.ERRLOG_403_INVALID_AUTHORIZATION, commonIdpRealm)
        ));
    }

    /**
    * Verifies that getUsecaseStoreId returns the correct ucStoreId when the API key and store exist.
    */
    @Test
    @DisplayName("getUsecaseStoreId - success")
    void testGetUsecaseStoreId_success() throws UnexpectedException, ForbiddenException {
        APIKeysEntity apiKeysEntity = new APIKeysEntity();
        apiKeysEntity.setUsecase(commonUsecase);
        List<APIKeysEntity> apiKeysEntities = List.of(apiKeysEntity);

        AuthorizationUCStoresEntity authorizationUCStoresEntity = new AuthorizationUCStoresEntity();
        authorizationUCStoresEntity.setUsecase(commonUsecase);
        authorizationUCStoresEntity.setUcStoreId(commonUcStoreId);
        authorizationUCStoresEntity.setUcStoreName(commonUcStoreName);
        authorizationUCStoresEntity.setDeletedFlag(commonDeletedFlag);
        authorizationUCStoresEntity.setEffectiveStartDate(commonEffctiveStartDate);
        authorizationUCStoresEntity.setEffectiveEndDate(commonEffctiveEndDate);
        Optional<AuthorizationUCStoresEntity> optionalEntity = Optional.of(authorizationUCStoresEntity);

        when(apiKeysRepository.findByApiKey(anyString(), any()))
                .thenReturn(apiKeysEntities);
        when(authorizationUCStoresRepository.findByUsecase(anyString()))
                .thenReturn(optionalEntity);

        String ucStoreId = apiKeyService.getUsecaseStoreId(commonApiKey);

        verify(authorizationUCStoresRepository, times(1)).findByUsecase(anyString());
        assertEquals(commonUcStoreId, ucStoreId);
    }

    /**
     * Verifies that getUsecaseStoreId throws UnexpectedException when the AuthorizationStore is not found.
     */
    @Test
    @DisplayName("getUsecaseStoreId - not found AuthorizationStore - throws UnexpectedException")
    void testGetUsecaseStoreId_notFoundAuthorizationStore_throwsUnexpectedException() throws UnexpectedException, ForbiddenException {
        APIKeysEntity apiKeysEntity = new APIKeysEntity();
        apiKeysEntity.setUsecase(commonUsecase);
        List<APIKeysEntity> apiKeysEntities = List.of(apiKeysEntity);

        Optional<AuthorizationUCStoresEntity> optionalEntity = Optional.empty();

        when(apiKeysRepository.findByApiKey(anyString(), any()))
                .thenReturn(apiKeysEntities);
        when(authorizationUCStoresRepository.findByUsecase(anyString()))
                .thenReturn(optionalEntity);

        UnexpectedException exception = assertThrows(UnexpectedException.class, () ->
                apiKeyService.getUsecaseStoreId(commonApiKey)
        );

        verify(authorizationUCStoresRepository, times(1)).findByUsecase(anyString());
        assertEquals(exception.getResponseMessage(), ConstError.ERR_500);
    }

    /**
     * Verifies that getUsecaseStoreId throws UnexpectedException when the AuthorizationStore is not found.
     */
    @ParameterizedTest
    @CsvSource({
        "DELETED, VALID, VALID",     //　deletedFlag is true
        "ACTIVE, INVALID, VALID",     //　Edge case: effectiveStartDate is in the future
        "ACTIVE, VALID, INVALID"     //　Edge case: effectiveEndDate is in the past
    })
    @DisplayName("getUsecaseStoreId - invalid storeId - throws ForbiddenException")
    void testGetUsecaseStoreId_invalidStoreId_throwsForbiddenException(
            String argDeletedFlag,
            String argEffectiveStartDate,
            String argEffectiveEndDate
    ) throws UnexpectedException, ForbiddenException {
        // Determine test-specific values based on parameters
        boolean deletedFlag =
                "DELETED".equals(argDeletedFlag) ? true : commonDeletedFlag;
        LocalDate effectiveStartDate =
                "INVALID".equals(argEffectiveStartDate) ? LocalDate.now(ZoneOffset.UTC).plusDays(1) : commonEffctiveStartDate;
        LocalDate effectiveEndDate =
                "INVALID".equals(argEffectiveEndDate) ? LocalDate.now(ZoneOffset.UTC).minusDays(1) : commonEffctiveEndDate;

        APIKeysEntity apiKeysEntity = new APIKeysEntity();
        apiKeysEntity.setUsecase(commonUsecase);
        List<APIKeysEntity> apiKeysEntities = List.of(apiKeysEntity);

        AuthorizationUCStoresEntity authorizationUCStoresEntity = new AuthorizationUCStoresEntity();
        authorizationUCStoresEntity.setUsecase(commonUsecase);
        authorizationUCStoresEntity.setUcStoreId(commonUcStoreId);
        authorizationUCStoresEntity.setUcStoreName(commonUcStoreName);
        authorizationUCStoresEntity.setDeletedFlag(deletedFlag);
        authorizationUCStoresEntity.setEffectiveStartDate(effectiveStartDate);
        authorizationUCStoresEntity.setEffectiveEndDate(effectiveEndDate);
        Optional<AuthorizationUCStoresEntity> optionalEntity = Optional.of(authorizationUCStoresEntity);

        // Arrange
        when(apiKeysRepository.findByApiKey(anyString(), any()))
                .thenReturn(apiKeysEntities);
        when(authorizationUCStoresRepository.findByUsecase(anyString()))
                .thenReturn(optionalEntity);

        // Act & Assert
        ForbiddenException exception = assertThrows(ForbiddenException.class, () ->
                apiKeyService.getUsecaseStoreId(commonApiKey)
        );

        // Assert
        verify(authorizationUCStoresRepository, times(1)).findByUsecase(anyString());
        assertEquals(exception.getResponseMessage(), ConstError.ERR_403_APIKEY_NOT_VALID);
        assertTrue(exception.getLogMessage().contains(
                String.format(ConstError.ERRLOG_403_INVALID_AUTHORIZATION, commonUsecase)
        ));
    }

    /**
    * Verifies that getUsecaseStoreName returns the correct ucStoreName when the API key and store exist.
    */
    @Test
    @DisplayName("getUsecaseStoreName - success")
    void testGetUsecaseStoreName_success() throws UnexpectedException, ForbiddenException {
        APIKeysEntity apiKeysEntity = new APIKeysEntity();
        apiKeysEntity.setUsecase(commonUsecase);
        List<APIKeysEntity> apiKeysEntities = List.of(apiKeysEntity);

        AuthorizationUCStoresEntity authorizationUCStoresEntity = new AuthorizationUCStoresEntity();
        authorizationUCStoresEntity.setUsecase(commonUsecase);
        authorizationUCStoresEntity.setUcStoreId(commonUcStoreId);
        authorizationUCStoresEntity.setUcStoreName(commonUcStoreName);
        authorizationUCStoresEntity.setDeletedFlag(commonDeletedFlag);
        authorizationUCStoresEntity.setEffectiveStartDate(commonEffctiveStartDate);
        authorizationUCStoresEntity.setEffectiveEndDate(commonEffctiveEndDate);
        Optional<AuthorizationUCStoresEntity> optionalEntity = Optional.of(authorizationUCStoresEntity);

        when(apiKeysRepository.findByApiKey(anyString(), any()))
                .thenReturn(apiKeysEntities);
        when(authorizationUCStoresRepository.findByUsecase(anyString()))
                .thenReturn(optionalEntity);

        String ucStoreName = apiKeyService.getUsecaseStoreName(commonApiKey);

        verify(authorizationUCStoresRepository, times(1)).findByUsecase(anyString());
        assertEquals(commonUcStoreName, ucStoreName);
    }

    /**
     * Verifies that getUsecaseStoreName throws UnexpectedException when the AuthorizationStore is not found.
     */
    @Test
    @DisplayName("getUsecaseStoreName - not found AuthorizationStore - throws UnexpectedException")
    void testGetUsecaseStoreName_notFoundAuthorizationStore_throwsUnexpectedException() throws UnexpectedException, ForbiddenException {
        APIKeysEntity apiKeysEntity = new APIKeysEntity();
        apiKeysEntity.setUsecase(commonUsecase);
        List<APIKeysEntity> apiKeysEntities = List.of(apiKeysEntity);

        Optional<AuthorizationUCStoresEntity> optionalEntity = Optional.empty();

        when(apiKeysRepository.findByApiKey(anyString(), any()))
                .thenReturn(apiKeysEntities);
        when(authorizationUCStoresRepository.findByUsecase(anyString()))
                .thenReturn(optionalEntity);

        UnexpectedException exception = assertThrows(UnexpectedException.class, () ->
                apiKeyService.getUsecaseStoreId(commonApiKey)
        );

        verify(authorizationUCStoresRepository, times(1)).findByUsecase(anyString());
        assertEquals(exception.getResponseMessage(), ConstError.ERR_500);
    }

    /**
     * Verifies that getUsecaseStoreName throws UnexpectedException when the AuthorizationStoreName is not found.
     */
    @ParameterizedTest
    @CsvSource({
        "DELETED, VALID, VALID",     //　deletedFlag is true
        "ACTIVE, INVALID, VALID",     //　Edge case: effectiveStartDate is in the future
        "ACTIVE, VALID, INVALID"     //　Edge case: effectiveEndDate is in the past
    })
    @DisplayName("getUsecaseStoreName - invalid storeName - throws ForbiddenException")
    void testGetUsecaseStore_invalidStoreName_throwsForbiddenException(
            String argDeletedFlag,
            String argEffectiveStartDate,
            String argEffectiveEndDate
    ) throws UnexpectedException, ForbiddenException {
        // Determine test-specific values based on parameters
        boolean deletedFlag =
                "DELETED".equals(argDeletedFlag) ? true : commonDeletedFlag;
        LocalDate effectiveStartDate =
                "INVALID".equals(argEffectiveStartDate) ? LocalDate.now(ZoneOffset.UTC).plusDays(1) : commonEffctiveStartDate;
        LocalDate effectiveEndDate =
                "INVALID".equals(argEffectiveEndDate) ? LocalDate.now(ZoneOffset.UTC).minusDays(1) : commonEffctiveEndDate;

        APIKeysEntity apiKeysEntity = new APIKeysEntity();
        apiKeysEntity.setUsecase(commonUsecase);
        List<APIKeysEntity> apiKeysEntities = List.of(apiKeysEntity);

        AuthorizationUCStoresEntity authorizationUCStoresEntity = new AuthorizationUCStoresEntity();
        authorizationUCStoresEntity.setUsecase(commonUsecase);
        authorizationUCStoresEntity.setUcStoreId(commonUcStoreId);
        authorizationUCStoresEntity.setUcStoreName(commonUcStoreName);
        authorizationUCStoresEntity.setDeletedFlag(deletedFlag);
        authorizationUCStoresEntity.setEffectiveStartDate(effectiveStartDate);
        authorizationUCStoresEntity.setEffectiveEndDate(effectiveEndDate);
        Optional<AuthorizationUCStoresEntity> optionalEntity = Optional.of(authorizationUCStoresEntity);

        // Arrange
        when(apiKeysRepository.findByApiKey(anyString(), any()))
                .thenReturn(apiKeysEntities);
        when(authorizationUCStoresRepository.findByUsecase(anyString()))
                .thenReturn(optionalEntity);

        // Act & Assert
        ForbiddenException exception = assertThrows(ForbiddenException.class, () ->
                apiKeyService.getUsecaseStoreName(commonApiKey)
        );

        // Assert
        verify(authorizationUCStoresRepository, times(1)).findByUsecase(anyString());
        assertEquals(exception.getResponseMessage(), ConstError.ERR_403_APIKEY_NOT_VALID);
        assertTrue(exception.getLogMessage().contains(
                String.format(ConstError.ERRLOG_403_INVALID_AUTHORIZATION, commonUsecase)
        ));
    }
}