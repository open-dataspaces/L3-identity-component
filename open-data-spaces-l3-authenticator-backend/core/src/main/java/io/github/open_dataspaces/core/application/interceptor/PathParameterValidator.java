/*
 * PathParameterValidator.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This HandlerInterceptor provides a concrete implementation for path parameter validation.
 *
 * Date: 2025/06/30
 */

package io.github.open_dataspaces.core.application.interceptor;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import io.github.open_dataspaces.core.common.consts.Const;
import io.github.open_dataspaces.core.common.consts.ConstError;
import io.github.open_dataspaces.core.common.utils.UUIDUtils;
import io.github.open_dataspaces.core.exception.ValidateException;

import io.micrometer.common.lang.NonNull;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Concrete interceptor for path parameter validation.
 */
@Component
public class PathParameterValidator implements HandlerInterceptor {

    @Autowired
    @Qualifier("pathParameterOperatorIdPaths")
    private List<String> pathParameterOperatorIdPaths;
    @Autowired
    @Qualifier("pathParameterPlantIdPaths")
    private List<String> pathParameterPlantIdPaths;
    @Autowired
    @Qualifier("pathParameterClientUuidPaths")
    private List<String> pathParameterClientUuidPaths;

    @Autowired
    @Qualifier("pathParameterClientIdPaths")
    private List<String> pathParameterClientIdPaths;

    /**
     * Checks for the path parameter validation.
     *
     * @param request the HTTP servlet request
     * @param response the HTTP servlet response
     * @param handler the handler object
     * @return true if the path parameter is valid, false otherwise
     * @throws ValidateException if the token is missing or invalid
     */
    @Override
    public boolean preHandle(@NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {
        // Get the path parameter from the request URI
        String uri = request.getRequestURI();
        String pathParameter = getLastPathParameter(uri);
        String requestUriPath = removePathParameter(uri, pathParameter);

        // If any of the paths are contained, perform validation
        for (String targetPath : pathParameterOperatorIdPaths) {
            if (getTargetPath(targetPath).equals(requestUriPath)) {
                return isOperatorIdValid(pathParameter);
            }
        }

        // If any of the paths are contained, perform validation
        for (String targetPath : pathParameterPlantIdPaths) {
            if (getTargetPath(targetPath).equals(requestUriPath)) {
                return isPlantIdValid(pathParameter);
            }
        }

        // If any of the paths are contained, perform validation
        for (String targetPath : pathParameterClientUuidPaths) {
            if (getTargetPath(targetPath).equals(requestUriPath)) {
                return isClientUuidValid(pathParameter);
            }
        }

        // If any of the paths are contained, perform validation
        for (String targetPath : pathParameterClientIdPaths) {
            if (getTargetPath(targetPath).equals(requestUriPath)) {
                return isClientIdValid(pathParameter);
            }
        }
        return true; // If the path does not match, no validation is needed
    }

    /**
     * Validates the operatorId path parameter in the request URI.
     *
     * @param request the HTTP servlet request
     * @return true if the operatorId is valid
     */
    private boolean isOperatorIdValid(String operatorId) {
        List<String> operatorsList = new ArrayList<String>();
        operatorsList.add(operatorId);
        if (UUIDUtils.getUUIDs(operatorsList).isEmpty()) {
            throw new ValidateException(String.format(ConstError.ERR_VALIDATION_INVALID_UUID, Const.API_PATH_PARAM_OPERATOR_ID));
        }
        return true;
    }

    /**
     * Validates the operatorId path parameter in the request URI.
     *
     * @param request the HTTP servlet request
     * @return true if the operatorId is valid
     */
    private boolean isPlantIdValid(String plantId) {
        List<String> plantsList = new ArrayList<String>();
        plantsList.add(plantId);
        if (UUIDUtils.getUUIDs(plantsList).isEmpty()) {
            throw new ValidateException(String.format(ConstError.ERR_VALIDATION_INVALID_UUID, Const.API_PATH_PARAM_PLANT_ID));
        }
        return true;
    }

    /**
     * Validates the clientUuid path parameter in the request URI.
     *
     * @param clientUuid the client UUID path parameter
     * @return true if the clientUuid is valid
     */
    private boolean isClientUuidValid(String clientUuid) {
        List<String> clientList = new ArrayList<String>();
        clientList.add(clientUuid);
        if (UUIDUtils.getUUIDs(clientList).isEmpty()) {
            throw new ValidateException(String.format(ConstError.ERR_VALIDATION_INVALID_UUID, Const.JSON_PROPERTY_CLIENT_UUID));
        }
        return true;
    }

    /**
     * Validates the clientId path parameter in the request URI.
     *
     * @param clientId the client ID path parameter
     * @return true if the clientId is valid
     */
    private boolean isClientIdValid(String clientId) {
        int length = clientId.length();
        if (length < Const.CLIENT_ID_LENGTH_MIN || length > Const.CLIENT_ID_LENGTH_MAX) {
            throw new ValidateException(String.format(ConstError.ERR_VALIDATION_INVALID_PATH_PARAMETER, Const.API_PATH_PARAM_CLIENT_ID));
        }

        if (!clientId.matches(Const.REGEX_CLIENT_ID)) {
            throw new ValidateException(String.format(ConstError.ERR_VALIDATION_INVALID_PATH_PARAMETER, Const.API_PATH_PARAM_CLIENT_ID));
        }
        return true;
    }

    /**
     * Constructs the target path by concatenating the provided path segments.
     *
     * @param argsPath the path segments to concatenate
     * @return the constructed target path
     */
    private String getTargetPath(String path) {
        // Split the URI by slashes and return the second segment
        return path.replaceFirst("\\{.+\\}$", "");
    }

    /**
     * Extracts the last path parameter from the request URI.
     *
     * @param uri the request URI
     * @return the last path parameter
     */
    private String getLastPathParameter(String uri) {
        if (uri.lastIndexOf("/") == uri.length() - 1) {
            throw new ValidateException(String.format(ConstError.ERR_VALIDATION_INVALID_PATH_PARAMETER, uri));
        }
        // Split the URI by slashes and return the last segment
        String[] pathSegments = uri.split("/");
        return pathSegments[pathSegments.length - 1];
    }

    /**
     * Extracts the last path parameter from the request URI.
     *
     * @param uri the request URI
     * @return the last path parameter
     */
    private String removePathParameter(String uri, String pathParameter) {
        return uri.replace("/" + pathParameter, "/");
    }
}
