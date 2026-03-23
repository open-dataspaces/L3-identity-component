/*
 * IPForAPIKeyValidator.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This class is an interceptor for API key validation with IP address checking.
 *
 * Date: 2025/06/30
 */

package io.github.open_dataspaces.core.application.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import io.github.open_dataspaces.core.common.config.SpringProperties;
import io.github.open_dataspaces.core.common.consts.Const;
import io.github.open_dataspaces.core.common.consts.ConstError;
import io.github.open_dataspaces.core.domain.dto.APIKeyVerifyParam;
import io.github.open_dataspaces.core.domain.service.interfaces.APIKeyService;
import io.github.open_dataspaces.core.exception.IllegalAuthDataException;

/**
 * Interceptor for API key validation with IP address checking.
 *
 * <p>This class implements API key validation logic that also checks the client IP address.</p>
 */
@Component
public class IPForAPIKeyValidator extends AbstractAPIKeyValidator {

    private SpringProperties springProperties;

    private final APIKeyService apiKeyService;
    private static final Logger LOGGER = LoggerFactory.getLogger(IPForAPIKeyValidator.class);

    /**
     * Constructor for IPForAPIKeyValidator.
     *
     * @param apiKeyService the service for verifying API keys
     */
    public IPForAPIKeyValidator(SpringProperties springProperties, APIKeyService apiKeyService) {
        this.springProperties = springProperties;
        this.apiKeyService = apiKeyService;
    }

    /**
     * Validates the API key and checks the client IP address.
     *
     * @param request the HTTP servlet request
     * @param response the HTTP servlet response
     * @param apiKey the API key extracted from the request header
     * @return true if the API key and IP are valid, false otherwise
     * @throws IllegalAuthDataException if the API key or IP is missing or invalid
     */
    @Override
    public boolean validateAPIKey(HttpServletRequest request, HttpServletResponse response,
            String apiKey) {
        String ip = getIP(request);
        try {
            if (!StringUtils.hasText(ip)) {
                throw new IllegalAuthDataException(ConstError.ERRLOG_403_IP_NOT_PROVIDED,
                        ConstError.ERR_403_IP_NOT_PROVIDED);
            }
            if (!StringUtils.hasText(apiKey)) {
                throw new IllegalAuthDataException(ConstError.ERRLOG_403_APIKEY_NOT_PROVIDED,
                        ConstError.ERR_403_APIKEY_NOT_PROVIDED);
            }
            if (!apiKeyService.verify(new APIKeyVerifyParam(apiKey, ip, null))) {
                throw new IllegalAuthDataException(ConstError.ERRLOG_403_IP_NOT_AUTHORIZED_FOR_KEY,
                        ConstError.ERR_403_IP_NOT_AUTHORIZED_FOR_KEY);
            }
        } catch (Exception e) {
            logValidationResult(ip, apiKey, false);
            throw e;
        }
        logValidationResult(ip, apiKey, true);
        return true;
    }

    /**
     * Retrieves the client IP address from the request, considering proxy headers.
     *
     * @param request the HTTP servlet request
     * @return the client IP address, or null if not available
     */
    private String getIP(HttpServletRequest request) {
        // In local environment, return the remote address directly
        if (Const.SPRING_PROFILES_ACTIVE_LOCAL.equalsIgnoreCase(springProperties.getProfiles().getActive())) {
            return request.getRemoteAddr();
        }

        String xff = request.getHeader("X-Forwarded-For");
        if (!StringUtils.hasText(xff)) {
            return null;
        }
        // If there are multiple IPs, extract the appropriate one based on proxy position
        if (!xff.contains(",")) {
            return null;
        }
        // Split the XFF header and get the correct IP
        String[] ips = xff.split(",\s*");
        if (ips.length < Const.PROXY_IP_POS) {
            return null;
        }
        return ips[ips.length - Const.PROXY_IP_POS];
    }

    /**
     * Logs the IP address and API key for debugging purposes.
     *
     * @param requestIpAddress the IP address of the request
     * @param requestApiKey the API key from the request
     * @param isRequestResult indicates request result is success or failure
     */
    private void logValidationResult(String requestIpAddress, String requestApiKey, boolean isRequestResult) {
        String dumpMessage = String.format(Const.DUMP_IP_FOR_APIKEY, isRequestResult,
                LocalDateTime.now(ZoneOffset.UTC)
                .format(DateTimeFormatter.ofPattern(Const.ISO_8601_UTC_FORMAT)),
                requestIpAddress, requestApiKey);
        LOGGER.info(dumpMessage);
    }
}
