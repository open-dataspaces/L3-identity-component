/*
 * CidrsKey.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class defines the CidrsKey class, which serves as a composite key for identifying records by
 * CIDR and API key.
 *
 * Date: 2025/06/30
 */

package io.github.open_dataspaces.core.domain.entities;

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite key class for identifying records by CIDR and API key.
 *
 * <p>This class is used as a key in repositories or entities that require a combination of CIDR and
 * API key as a unique identifier.</p>
 */
public class CidrsKey implements Serializable {

    private String cidr;
    private String apiKey;

    /**
     * Default constructor.
     */
    public CidrsKey() {}

    /**
     * Constructs a CiderKey with the specified CIDR and API key.
     *
     * @param cidr the CIDR block
     * @param apiKey the API key
     */
    public CidrsKey(String cidr, String apiKey) {
        this.cidr = cidr;
        this.apiKey = apiKey;
    }

    /**
     * Gets the CIDR block.
     *
     * @return the CIDR block
     */
    public String getCidr() {
        return cidr;
    }

    /**
     * Sets the CIDR block.
     *
     * @param cidr the CIDR block to set
     */
    public void setCidr(String cidr) {
        this.cidr = cidr;
    }

    /**
     * Gets the API key.
     *
     * @return the API key
     */
    public String getApiKey() {
        return apiKey;
    }

    /**
     * Sets the API key.
     *
     * @param apiKey the API key to set
     */
    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * Returns the hash code for this composite key.
     *
     * @return the hash code
     */
    @Override
    public int hashCode() {
        return Objects.hash(cidr, apiKey);
    }

    /**
     * Compares this object to the specified object for equality.
     *
     * @param obj the object to compare
     * @return true if the objects are equal, false otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        CidrsKey that = (CidrsKey) obj;
        return Objects.equals(cidr, that.cidr) && Objects.equals(apiKey, that.apiKey);
    }
}
