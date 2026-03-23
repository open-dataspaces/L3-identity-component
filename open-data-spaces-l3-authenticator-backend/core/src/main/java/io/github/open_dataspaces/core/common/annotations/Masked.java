/*
 * Masked.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * Annotation to indicate that a field should be masked.
 *
 * Date: 2025/08/12
 */

package io.github.open_dataspaces.core.common.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;

/**
 * Annotation to indicate that a field should be masked.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Masked {
    /**
     * Length of the unmasked prefix.
     */
    int unmaskedPrefixLength() default 0;   // Length of the unmasked prefix

    /**
     * Length of the unmasked suffix.
     */
    int unmaskedSuffixLength() default 0;   // Length of the unmasked suffix
}
