/*
 * ErrorDetailsTest.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class contains unit tests for the ErrorDetails class.
 *
 * Date: 2025/06/30
 */

package io.github.open_dataspaces.core.common.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import java.util.Arrays;
import java.util.Set;

import org.junit.jupiter.api.Test;

import org.mockito.Mockito;

import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;

import io.github.open_dataspaces.core.domain.dto.APIJSONRequest;

import jakarta.validation.Path;
import jakarta.validation.ConstraintViolation;

/**
 * Unit tests for the {@link ErrorDetails} class.
 */
class ErrorDetailsTest {

    /**
     * (Case#1) Tests the createErrorDetails method with multiple validation errors.
     */
    @Test
    void testCreateErrorDetails_withMultipleErrors() {
        // Arrange
        BindingResult bindingResult = Mockito.mock(BindingResult.class);
        Mockito.when(bindingResult.getAllErrors()).thenReturn(Arrays.asList(
                new ObjectError("field1", "Error message 1"),
                new ObjectError("field2", "Error message 2"),
                new ObjectError("field3", "Error message 3")
        ));

        // Act
        String result = ErrorDetails.createErrorDetails(bindingResult);

        // Assert
        assertEquals("Error message 1; Error message 2; Error message 3", result, "Error messages should be concatenated with the delimiter");
    }

    /**
     * (Case#2) Tests the createErrorDetails method with no validation errors.
     */
    @Test
    void testCreateErrorDetails_withNoErrors() {
        // Arrange
        BindingResult bindingResult = Mockito.mock(BindingResult.class);
        Mockito.when(bindingResult.getAllErrors()).thenReturn(Arrays.asList());

        // Act
        String result = ErrorDetails.createErrorDetails(bindingResult);

        // Assert
        assertEquals("", result, "Result should be an empty string when there are no errors");
    }

    /**
     * (Case#3) Tests the createErrorDetails method with null BindingResult.
     */
    @Test
    void testCreateErrorDetails_withNullBindingResult() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> ErrorDetails.createErrorDetails((BindingResult) null), "Null BindingResult should throw NullPointerException");
    }

    /**
     * (Case#1) Tests the createErrorDetails method with multiple validation errors.
     */
    @Test
    void testCreateErrorDetails_withConstraintViolationMultipleErrors() {
        // Arrange
        @SuppressWarnings("unchecked")
        ConstraintViolation<APIJSONRequest> violation1 = Mockito.mock(ConstraintViolation.class);
        Path path1 = Mockito.mock(Path.class);
        Mockito.when(path1.toString()).thenReturn("field1");
        Mockito.when(violation1.getPropertyPath()).thenReturn(path1);
        Mockito.when(violation1.getMessage()).thenReturn("Error message 1");
        Mockito.when(violation1.getRootBeanClass()).thenReturn(APIJSONRequest.class);

        @SuppressWarnings("unchecked")
        ConstraintViolation<APIJSONRequest> violation2 = Mockito.mock(ConstraintViolation.class);
        Path path2 = Mockito.mock(Path.class);
        Mockito.when(path2.toString()).thenReturn("field2");
        Mockito.when(violation2.getPropertyPath()).thenReturn(path2);
        Mockito.when(violation2.getMessage()).thenReturn("Error message 2");
        Mockito.when(violation2.getRootBeanClass()).thenReturn(APIJSONRequest.class);

        @SuppressWarnings("unchecked")
        ConstraintViolation<APIJSONRequest> violation3 = Mockito.mock(ConstraintViolation.class);
        Path path3 = Mockito.mock(Path.class);
        Mockito.when(path3.toString()).thenReturn("field3");
        Mockito.when(violation3.getPropertyPath()).thenReturn(path3);
        Mockito.when(violation3.getMessage()).thenReturn("Error message 3");
        Mockito.when(violation3.getRootBeanClass()).thenReturn(APIJSONRequest.class);

        Set<ConstraintViolation<APIJSONRequest>> violations = Set.of(violation1, violation2, violation3);

        // Act
        String result = ErrorDetails.createErrorDetails(violations);

        // Assert
        assertTrue(result.contains("Error message 1"));
        assertTrue(result.contains("Error message 2"));
        assertTrue(result.contains("Error message 3"));
    }

    /**
     * (Case#2) Tests the createErrorDetails method with no validation errors.
     */
    @Test
    void testCreateErrorDetails_withConstraintViolationNoErrors() {
        // Arrange
        Set<ConstraintViolation<?>> violations = Set.of();

        // Act
        String result = ErrorDetails.createErrorDetails(violations);

        // Assert
        assertEquals("", result, "Result should be an empty string when there are no errors");
    }

    /**
     * (Case#3) Tests the createErrorDetails method with null BindingResult.
     */
    @Test
    void testCreateErrorDetails_withNullConstraintViolation() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> ErrorDetails.createErrorDetails((Set<? extends ConstraintViolation<?>>) null), "Null ConstraintViolation should throw NullPointerException");
    }

    /**
     * (Case#4) Tests that the constructor of ErrorDetails is private.
     */
    @Test
    void testPrivateConstructor() throws Exception {
        Constructor<ErrorDetails> constructor = ErrorDetails.class.getDeclaredConstructor();
        constructor.setAccessible(true);

        // Act & Assert
        InvocationTargetException exception = assertThrows(InvocationTargetException.class, constructor::newInstance);
        assertTrue(exception.getCause() instanceof UnsupportedOperationException, "Cause should be UnsupportedOperationException");
        assertEquals("Utility class cannot be instantiated.", exception.getCause().getMessage());
    }
}