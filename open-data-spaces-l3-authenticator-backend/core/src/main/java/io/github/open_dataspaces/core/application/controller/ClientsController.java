/*
 * ClientsController.java
 *
 * Copyright (c) 2025 NTT DATA Group Corporation All rights reserved.
 *
 * This controller handles clients information retrieval and registration requests.
 *
 * Date: 2025/11/30
 */

package io.github.open_dataspaces.core.application.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.github.open_dataspaces.core.common.consts.Const;
import io.github.open_dataspaces.core.common.consts.ConstError;
import io.github.open_dataspaces.core.common.consts.ConstPath;
import io.github.open_dataspaces.core.common.utils.ErrorDetails;
import io.github.open_dataspaces.core.domain.dto.APIResponse;
import io.github.open_dataspaces.core.domain.dto.IdpClientInfo;
import io.github.open_dataspaces.core.domain.dto.PostClientSecretResponse;
import io.github.open_dataspaces.core.domain.dto.PostClientsRequest;
import io.github.open_dataspaces.core.domain.dto.PostClientsResponse;
import io.github.open_dataspaces.core.domain.dto.PutClientsAuthCodeResponse;
import io.github.open_dataspaces.core.domain.dto.PutClientsClientCredentialsResponse;
import io.github.open_dataspaces.core.domain.dto.PutClientsRequest;
import io.github.open_dataspaces.core.domain.service.interfaces.ClientSecretService;
import io.github.open_dataspaces.core.domain.service.interfaces.IdentityProviderService;
import io.github.open_dataspaces.core.domain.service.interfaces.JWTVerifyService;
import io.github.open_dataspaces.core.exception.NotFoundException;
import io.github.open_dataspaces.core.exception.ValidateException;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Controller for handling clients and delegating Keycloak client creation to service layer.
 */
@RestController
@RequestMapping(ConstPath.AUTH_PATH)
public class ClientsController {
    private final JWTVerifyService jwtService;
    private final IdentityProviderService identityProviderService;
    private final ClientSecretService clientSecretService;

    /**
     * Constructs the controller.
     *
     * @param jwtService service to extract and validate JWT claims (operator_id / open_system_id)
     * @param identityProviderService abstraction over Keycloak operations
     * @param clientSecretService service for managing client secrets
     */
    public ClientsController(JWTVerifyService jwtService,
            IdentityProviderService identityProviderService,
            ClientSecretService clientSecretService) {
        this.jwtService = jwtService;
        this.identityProviderService = identityProviderService;
        this.clientSecretService = clientSecretService;
    }

    /**
     * Registers a new client in the identity provider.
     *
     * @param request raw servlet request (for headers such as API-Key & Authorization)
     * @param requestBody client creation payload
     * @param bindingResult Bean Validation result holder
     * @return 201 with client metadata; validation errors thrown as exceptions
     * @throws ValidateException when initial or flow-specific validation fails
     */
    @PostMapping(ConstPath.AUTH_CLIENTS_PATH_SHORT)
    public ResponseEntity<APIResponse<PostClientsResponse>> postClients(
            HttpServletRequest request,
            @Validated @RequestBody PostClientsRequest requestBody,
            BindingResult bindingResult) {

        String apiKey = request.getHeader(Const.HEADER_API_KEY);

        // Bean Validation errors -> throw ValidateException (handled globally)
        if (bindingResult.hasErrors()) {
            throw new ValidateException(ConstError.ERRLOG_400_INVALID_REQUEST,
                ErrorDetails.createErrorDetails(bindingResult));
        }

        // Validate API-Key access via JWT claims (operator_id / open_system_id)
        String operatorId = jwtService.getOperatorOrOpenSystemId(request);

        // Flow-specific validations similar to current implementation
        String flowType = requestBody.getFlowType();
        validateFlowSpecific(requestBody);

        List<String> redirectUris = Const.KEYCLOAK_SEND_VALUE_GRANT_TYPE_AUTHORIZATION_CODE.equals(flowType)
                ? requestBody.getRedirectUris()
                : null;

        // Delegate create to service/repository; exceptions are not caught here
        IdpClientInfo created = identityProviderService.registerClient(
                apiKey,
                requestBody.getClientId(),
                requestBody.getName(),
                requestBody.getDescription(),
                flowType,
                redirectUris,
                requestBody.getOperatorId(),
                requestBody.getOpenSystemId());

        // Persist the relation between created client and the API key used
        try {
            clientSecretService.save(created.getUuid(), apiKey, operatorId);
        } catch (Exception e) {
            // If failed to create client secret information, delete the client in the identity provider
            identityProviderService.deleteClient(apiKey, created.getUuid());
            throw e;
        }

        PostClientsResponse responseDto = new PostClientsResponse();
        responseDto.setClientUuid(created.getUuid());               //  uuid
        responseDto.setClientId(created.getClientId());
        responseDto.setEnabled(created.isEnabled());                // Keycloak enabled status
        responseDto.setName(created.getName());
        responseDto.setDescription(created.getDescription());   // from request
        responseDto.setFlowType(flowType);
        responseDto.setRedirectUris(created.getRedirectUris());
        responseDto.setOpenSystemId(requestBody.getOpenSystemId());
        responseDto.setOperatorId(requestBody.getOperatorId());

        APIResponse<PostClientsResponse> apiResponse =
                new APIResponse<>(request, HttpStatus.CREATED.value(), responseDto);
        return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
    }

    /**
     * Performs flow-specific validations not covered by Bean Validation annotations.
     *
     * @param req original request payload
     * @throws ValidateException on any rule violation
     */
    private void validateFlowSpecific(PostClientsRequest req) {
        String flowType = req.getFlowType();

        if (Const.KEYCLOAK_SEND_VALUE_GRANT_TYPE_AUTHORIZATION_CODE.equals(flowType)) {
            // redirect_uris is REQUIRED for authorization_code
            if (req.getRedirectUris() == null || req.getRedirectUris().isEmpty()) {
                throw new ValidateException(
                        ConstError.ERRLOG_400_INVALID_REQUEST,
                        ConstError.ERR_400_VALIDATION_REDIRECT_URIS_REQUIRED);
            }
        } else if (Const.KEYCLOAK_SEND_VALUE_GRANT_TYPE_CLIENT_CREDENTIALS.equals(flowType)) {
            // open_system_id is REQUIRED for client_credentials
            if (req.getOpenSystemId() == null || req.getOpenSystemId().isBlank()) {
                throw new ValidateException(
                        ConstError.ERRLOG_400_INVALID_REQUEST,
                        ConstError.ERR_VALIDATION_OPEN_SYSTEM_ID_REQUIRED);
            }
        }
    }

    /**
     * Retrieves the client secret for a given client ID.
     *
     * @param request servlet request (for headers / tracking)
     * @param clientUuid client identifier path parameter
     * @return 200 with secret
     */
    @PostMapping(ConstPath.AUTH_CLIENTS_SECRET_PATH_SHORT)
    public ResponseEntity<APIResponse<PostClientSecretResponse>> getClientSecret(
            HttpServletRequest request,
            @PathVariable(Const.JSON_PROPERTY_CLIENT_UUID) String clientUuid) {

        String apiKey = request.getHeader(Const.HEADER_API_KEY);

        // Ensure a link exists in tbl_client_secrets for the provided clientUuid
        if (!clientSecretService.existsById(clientUuid)) {
            throw new NotFoundException(
                    String.format(ConstError.ERR_404_RESOURCE_NOT_FOUND_MESSAGE, Const.JSON_PROPERTY_CLIENT_UUID));
        }

        String secret = identityProviderService.getClientSecret(apiKey, clientUuid);

        // Delete the link after successful secret retrieval
        clientSecretService.deleteById(clientUuid);

        PostClientSecretResponse body = new PostClientSecretResponse(secret);
        APIResponse<PostClientSecretResponse> apiResponse =
                new APIResponse<>(request, HttpStatus.OK.value(), body);
        return ResponseEntity.ok(apiResponse);
    }

    /**
     * Deletes a client in the identity provider.
     *
     * @param request servlet request (for headers / tracking)
     * @param clientId client identifier path parameter
     * @return 204 if deleted
     */
    @DeleteMapping(ConstPath.AUTH_CLIENTS_ID_PATH_SHORT)
    public ResponseEntity<Object> deleteClient(
            HttpServletRequest request,
            @PathVariable(Const.API_PATH_PARAM_CLIENT_ID) String clientId) {

        String apiKey = request.getHeader(Const.HEADER_API_KEY);
        String clientUuid = identityProviderService.getClientUuid(apiKey, clientId);

        identityProviderService.deleteClient(apiKey, clientUuid);
        // Cleanup link information if it exists (idempotent)
        clientSecretService.deleteById(clientUuid);

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    /**
     * Updates a client in the identity provider.
     *
     * @param request servlet request (for headers / tracking)
     * @param requestBody update payload
     * @return 200 with updated client metadata
     */
    @PutMapping(ConstPath.AUTH_CLIENTS_ID_PATH_SHORT)
    public ResponseEntity<APIResponse<?>> putClients(
            HttpServletRequest request,
            @PathVariable(Const.API_PATH_PARAM_CLIENT_ID) String clientId,
            @Validated @RequestBody PutClientsRequest requestBody,
            BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            throw new ValidateException(ConstError.ERRLOG_400_INVALID_REQUEST, ErrorDetails.createErrorDetails(bindingResult));
        }

        String apiKey = request.getHeader(Const.HEADER_API_KEY);
        String clientUuid = identityProviderService.getClientUuid(apiKey, clientId);

        String flowType = requestBody.getFlowType();
        String name = requestBody.getName();
        String description = requestBody.getDescription();
        String openSystemId = requestBody.getOpenSystemId();
        String operatorId = requestBody.getOperatorId();

        List<String> redirectUris = Const.KEYCLOAK_SEND_VALUE_GRANT_TYPE_AUTHORIZATION_CODE.equals(flowType)
                ? (requestBody.getRedirectUris() == null || requestBody.getRedirectUris().isEmpty()
                ? null
                : requestBody.getRedirectUris())
                : null;

        IdpClientInfo updated = identityProviderService.updateClient(
                apiKey,
                clientUuid,
                name,
                description,
                flowType,
                openSystemId,
                operatorId,
                redirectUris);

        Object body = putClientsResponse(flowType, name, description, openSystemId, operatorId, redirectUris, updated);

        APIResponse<Object> apiResponse = new APIResponse<>(request, HttpStatus.OK.value(), body);
        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    /**
     * Builds the response body DTO for {@link #putClients(HttpServletRequest, String, PutClientsRequest, BindingResult)}.
     *
     * <p>The response DTO type is selected by {@code flowType}:</p>
     * <ul>
     *   <li>{@code authorization_code} - {@link PutClientsAuthCodeResponse}</li>
     *   <li>{@code client_credentials} - {@link PutClientsClientCredentialsResponse}</li>
     * </ul>
     *
     * <p>Only fields explicitly provided in the request are included in the response.
     * For example, when {@code name} is {@code null} in the request, the response {@code name} is also {@code null}
     * (even if the client has a value in Keycloak). For {@code authorization_code} flow, {@code open_system_id}
     * and {@code operator_id} are not returned.</p>
     *
     * @param flowType flow type from request
     * @param name optional updated name from request
     * @param description optional updated description from request
     * @param openSystemId optional updated open_system_id from request (client_credentials)
     * @param operatorId optional updated operator_id from request (client_credentials)
     * @param redirectUris optional updated redirect_uris from request (authorization_code)
     * @param updated latest client representation returned from the identity provider
     * @return response DTO matching the flow type
     */
    private Object putClientsResponse(
            String flowType,
            String name,
            String description,
            String openSystemId,
            String operatorId,
            List<String> redirectUris,
            IdpClientInfo updated) {

        // Return only updated fields (fields omitted in request are not included in response)
        // When flow_type is authorization_code, open_system_id and operator_id are not returned.
        boolean isAuthorizationCode = Const.KEYCLOAK_SEND_VALUE_GRANT_TYPE_AUTHORIZATION_CODE.equals(flowType);
        if (isAuthorizationCode) {
            return new PutClientsAuthCodeResponse(
                    name != null ? updated.getName() : null,
                    description != null ? updated.getDescription() : null,
                    flowType,
                    redirectUris != null ? updated.getRedirectUris() : null);
        }

        return new PutClientsClientCredentialsResponse(
                name != null ? updated.getName() : null,
                description != null ? updated.getDescription() : null,
                flowType,
                openSystemId != null ? openSystemId : null,
                operatorId != null ? operatorId : null);
    }
}