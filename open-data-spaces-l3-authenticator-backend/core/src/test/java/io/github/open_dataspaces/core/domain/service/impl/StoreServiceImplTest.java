/*
 * StoreServiceImplTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class is a unit test for StoreServiceImpl.
 *
 * Date: 2026/02/17
 */

package io.github.open_dataspaces.core.domain.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.open_dataspaces.core.common.consts.ConstError;
import io.github.open_dataspaces.core.common.consts.ConstPath;
import io.github.open_dataspaces.core.common.enums.EnumStorePurpose;
import io.github.open_dataspaces.core.domain.repository.interfaces.AuthorizationStoresRepository;
import io.github.open_dataspaces.core.domain.repository.interfaces.AuthorizationUCStoresRepository;
import io.github.open_dataspaces.core.domain.service.interfaces.APIKeyService;
import io.github.open_dataspaces.core.domain.service.interfaces.AuthorizationService;
import io.github.open_dataspaces.core.exception.BadParametersException;
import io.github.open_dataspaces.core.exception.ConflictException;
import io.github.open_dataspaces.core.exception.NotFoundException;
import io.github.open_dataspaces.core.infrastructure.dto.CreateStoreResponse;

@ExtendWith(MockitoExtension.class)
class StoreServiceImplTest {

    @InjectMocks
    private StoreServiceImpl storeServiceImpl;
    @Mock
    private AuthorizationService authorizationService;
    @Mock
    private AuthorizationStoresRepository authorizationStoresRepository;
    @Mock
    private AuthorizationUCStoresRepository authorizationUCStoresRepository;
    @Mock
    private APIKeyService apiKeyService;
    @Mock
    private ObjectMapper objectMapper;

    private final String commonApiKey = "api-key-123";
    private final String commonAdminStoreId = "admin-store-123";
    private final String commonRealm = "test-realm";
    private final String commonStoreId = "store-123";
    private final String commonRequestBody = "{\"key\": \"value\"}";
    private final String commonNotFoundErrMsg = "Resource not found";
    private final String commonBadParametersErrMsg = "Invalid parameters";
    private final CreateStoreResponse commonCreateStoreResponse = new CreateStoreResponse(
            commonStoreId,
            "store-name-123",
            "2026-01-01T01:18:14.261Z",
            "2026-01-01T01:18:14.261Z");

    @Test
    @DisplayName("registerStore - Success")
    void registerStore_success() throws Exception {
        ResponseEntity<Object> createResult = ResponseEntity.ok(commonCreateStoreResponse);

        when(authorizationService.forward(ConstPath.AUTHORIZATION_STORES_PATH, HttpMethod.POST, commonRequestBody)).thenReturn(createResult);
        when(objectMapper.convertValue(createResult.getBody(), CreateStoreResponse.class)).thenReturn(commonCreateStoreResponse);
        when(apiKeyService.getIdpRealm(commonApiKey)).thenReturn(commonRealm);
        when(apiKeyService.getStoreId(commonApiKey, EnumStorePurpose.REALM_STORE_BINDING.name())).thenReturn(commonAdminStoreId);

        ResponseEntity<Object> result = storeServiceImpl.registerStore(commonApiKey, commonRequestBody);

        assertEquals(createResult, result);
        verify(authorizationService).writeTuples(anyList(), eq(commonAdminStoreId));
    }

    @Test
    @DisplayName("registerStore - Fails when writeTuples throws exception")
    void registerStore_writeTuplesFailure() throws Exception {
        ResponseEntity<Object> createResult = ResponseEntity.ok(commonCreateStoreResponse);

        when(authorizationService.forward(ConstPath.AUTHORIZATION_STORES_PATH, HttpMethod.POST, commonRequestBody)).thenReturn(createResult);
        when(objectMapper.convertValue(createResult.getBody(), CreateStoreResponse.class)).thenReturn(commonCreateStoreResponse);
        when(apiKeyService.getIdpRealm(commonApiKey)).thenReturn(commonRealm);
        when(apiKeyService.getStoreId(commonApiKey, EnumStorePurpose.REALM_STORE_BINDING.name())).thenReturn(commonAdminStoreId);
        doThrow(new NotFoundException(commonNotFoundErrMsg)).when(authorizationService).writeTuples(anyList(), eq(commonAdminStoreId));

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            storeServiceImpl.registerStore(commonApiKey, commonRequestBody);
        });

        assertTrue(exception.getMessage().contains(commonNotFoundErrMsg));
        assertEquals(0, exception.getSuppressed().length);
    }

    @Test
    @DisplayName("registerStore - Fails when store deletion during rollback fails after writeTuples exception")
    void registerStore_rollbackStoreDeleteFailure() throws Exception {
        ResponseEntity<Object> createResult = ResponseEntity.ok(commonCreateStoreResponse);

        when(authorizationService.forward(ConstPath.AUTHORIZATION_STORES_PATH, HttpMethod.POST, commonRequestBody)).thenReturn(createResult);
        when(objectMapper.convertValue(createResult.getBody(), CreateStoreResponse.class)).thenReturn(commonCreateStoreResponse);
        when(apiKeyService.getIdpRealm(commonApiKey)).thenReturn(commonRealm);
        when(apiKeyService.getStoreId(commonApiKey, EnumStorePurpose.REALM_STORE_BINDING.name())).thenReturn(commonAdminStoreId);
        doThrow(new NotFoundException(commonNotFoundErrMsg)).when(authorizationService).writeTuples(anyList(), eq(commonAdminStoreId));
        when(authorizationService.deleteStore(commonStoreId)).thenThrow(new BadParametersException(commonBadParametersErrMsg));

        NotFoundException exception = assertThrows(NotFoundException.class, () -> {
            storeServiceImpl.registerStore(commonApiKey, commonRequestBody);
        });

        assertEquals(commonNotFoundErrMsg, exception.getMessage());
        assertEquals(commonBadParametersErrMsg, exception.getSuppressed()[0].getMessage());
    }

    @Test
    @DisplayName("deleteStore - Success")
    void deleteStore_success() throws Exception {
        when(authorizationStoresRepository.existsById(commonStoreId)).thenReturn(false);
        when(authorizationUCStoresRepository.existsById(commonStoreId)).thenReturn(false);
        when(authorizationService.deleteStore(commonStoreId)).thenReturn(ResponseEntity.noContent().build());
        when(apiKeyService.getIdpRealm(commonApiKey)).thenReturn(commonRealm);
        when(apiKeyService.getStoreId(commonApiKey, EnumStorePurpose.REALM_STORE_BINDING.name())).thenReturn(commonAdminStoreId);

        storeServiceImpl.deleteStore(commonApiKey, commonStoreId);

        verify(authorizationService).deleteStore(commonStoreId);
        verify(authorizationService).deleteTuples(anyList(), eq(commonAdminStoreId));
    }

    @ParameterizedTest
    @CsvSource(value = {
        "true, false",
        "false, true",
        "true, true",
    })
    @DisplayName("deleteStore - Throws ConflictException for non-deletable stores")
    void deleteStore_conflictException(boolean existsInStores, boolean existsInUCStores) throws Exception {
        lenient().when(authorizationStoresRepository.existsById(commonStoreId)).thenReturn(existsInStores);
        lenient().when(authorizationUCStoresRepository.existsById(commonStoreId)).thenReturn(existsInUCStores);

        ConflictException exception = assertThrows(ConflictException.class, () -> {
            storeServiceImpl.deleteStore(commonApiKey, commonStoreId);
        });

        assertEquals(String.format(ConstError.ERRLOG_409_STORE_CANNOT_BE_DELETED, commonStoreId), exception.getMessage());
    }
}
