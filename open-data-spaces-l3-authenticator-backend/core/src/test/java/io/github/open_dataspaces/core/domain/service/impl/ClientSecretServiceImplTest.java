/*
 * ClientSecretServiceImplTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This package contains unit tests for the ClientSecretServiceImpl class,
 *
 * Date: 2025/12/31
 */

package io.github.open_dataspaces.core.domain.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.mockito.ArgumentMatchers.anyString;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.open_dataspaces.core.domain.repository.interfaces.ClientSecretRepository;
import io.github.open_dataspaces.core.exception.BadParametersException;
import io.github.open_dataspaces.core.exception.ConflictException;

/**
 * Unit tests for ClientSecretServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
public class ClientSecretServiceImplTest {

    @Mock
    private ClientSecretRepository clientSecretRepository;
    @InjectMocks
    private ClientSecretServiceImpl clientSecretService;

    // common data
    private final String commonClientUuid = "test-client-id";
    private final String commonApiKey = "commonApiKey";
    private final String commonCreatedUserId = "commonCreatedUserId";

    @BeforeEach
    void setUp() throws Exception {
        clientSecretService = new ClientSecretServiceImpl(clientSecretRepository);
    }

    @ParameterizedTest
    @CsvSource({
        // Result
        "true",
        "false"
    })
    @DisplayName("existsById - Success Cases")
    void testExistsById_success(String argResult) {
        // Arrange
        boolean expected = argResult.equals("true");
        when(clientSecretRepository.existsByClientUuid(anyString())).thenReturn(expected);

        // Act
        boolean response = clientSecretService.existsById(commonClientUuid);

        // Assert
        assertNotNull(response);
        assertEquals(response, expected);
    }

    @ParameterizedTest
    @CsvSource({
        // Exception
        "IllegalArgumentException",
        "BadParametersException",
        "RuntimeException"
    })
    @DisplayName("existsById throws exception when repository fails")
    void testExistsById_throwsException(String exceptionClassName) throws IllegalArgumentException {
        // Determine exception, expected status matcher, and expected HTTP status code
        Class<? extends Exception> clazz = null;
        switch (exceptionClassName) {
            case "BadParametersException":
                clazz = BadParametersException.class;
                break;
            case "IllegalArgumentException":
                clazz = IllegalArgumentException.class;
                break;
            case "RuntimeException":
                clazz = RuntimeException.class;
                break;
            default:
                // Unexpected exception
                throw new IllegalArgumentException("Unknown exceptionClassName: " + exceptionClassName);
        }
        when(clientSecretRepository.existsByClientUuid(anyString())).thenThrow(clazz);

        // Act & Assert
        assertThrows(clazz, () -> {
            clientSecretService.existsById(commonClientUuid);
        });
    }

    @Test
    @DisplayName("deleteById - Success Case")
    void testDeleteById_success() {
        // Arrange
        // No specific arrangement needed as we are testing successful execution

        // Act & Assert
        try {
            clientSecretService.deleteById(commonClientUuid);
        } catch (Exception e) {
            // If any exception is thrown, the test should fail
            assertEquals(true, false, "Exception was thrown during deleteById: " + e.getMessage());
        }
    }

    @ParameterizedTest
    @CsvSource({
        // Exception
        "IllegalArgumentException",
        "BadParametersException",
        "RuntimeException"
    })
    @DisplayName("deleteById throws exception when repository fails")
    void testDeleteById_throwsException(String exceptionClassName) throws IllegalArgumentException {
        // Determine exception, expected status matcher, and expected HTTP status code
        Class<? extends Exception> clazz = null;
        switch (exceptionClassName) {
            case "BadParametersException":
                clazz = BadParametersException.class;
                break;
            case "IllegalArgumentException":
                clazz = IllegalArgumentException.class;
                break;
            case "RuntimeException":
                clazz = RuntimeException.class;
                break;
            default:
                // Unexpected exception
                throw new IllegalArgumentException("Unknown exceptionClassName: " + exceptionClassName);
        }
        doThrow(clazz).when(clientSecretRepository).deleteByClientUuid(anyString());

        // Act & Assert
        assertThrows(clazz, () -> {
            clientSecretService.deleteById(commonClientUuid);
        });
    }

    /**
     * Test save method for success cases.
     */
    @Test
    @DisplayName("save - Success")
    void testSave_success() {
        doNothing().when(clientSecretRepository).save(commonClientUuid, commonApiKey, commonCreatedUserId);
        clientSecretService.save(commonClientUuid, commonApiKey, commonCreatedUserId);
        // Unlike other unit tests, there is no return value, so verification with assertions is not possible.
        verify(clientSecretRepository).save(commonClientUuid, commonApiKey, commonCreatedUserId);
    }

    /**
     * Test save method for handling EntityExistsException and IllegalArgumentException.
     */
    @ParameterizedTest
    @CsvSource({
        "IllegalArgumentException, Invalid argument provided",
        "BadParametersException, bad param",
        "ConflictException, conflict error",
        "RuntimeException, Some runtime exception"
    })
    @DisplayName("save - Exception Handling")
    void testSave_throwsException(
            String exceptionType,
            String exceptionMessage) {

        // Arrange
        Exception exception = null;
        Class<? extends Throwable> clazz = null;
        switch (exceptionType) {
            case "IllegalArgumentException":
                exception = new IllegalArgumentException(exceptionMessage);
                clazz = IllegalArgumentException.class;
                break;
            case "BadParametersException":
                exception = new BadParametersException(exceptionMessage);
                clazz = BadParametersException.class;
                break;
            case "ConflictException":
                exception = new ConflictException(exceptionMessage, exceptionMessage);
                clazz = ConflictException.class;
                break;
            case "RuntimeException":
                exception = new RuntimeException(exceptionMessage);
                clazz = RuntimeException.class;
                break;
            default:
                throw new IllegalArgumentException(String.format("Invalid exception type for test '%s'", exceptionType));
        }

        // Act & Assert
        doThrow(exception).when(clientSecretRepository)
            .save(commonClientUuid, commonApiKey, commonCreatedUserId);
        Throwable thrownException = assertThrows(clazz, () -> {
            clientSecretService.save(commonClientUuid, commonApiKey, commonCreatedUserId);
        });

        assertEquals(thrownException.getClass(), clazz);
        assertNotNull(thrownException.getMessage());
    }
}