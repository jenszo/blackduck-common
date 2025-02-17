/*
 * blackduck-common
 *
 * Copyright (c) 2023 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.http.client.cache;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.collections4.map.LRUMap;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.HttpClientBuilder;

import com.google.gson.Gson;
import com.synopsys.integration.blackduck.api.core.BlackDuckResponse;
import com.synopsys.integration.blackduck.api.core.response.UrlResponse;
import com.synopsys.integration.blackduck.http.client.BlackDuckHttpClient;
import com.synopsys.integration.blackduck.service.request.BlackDuckRequest;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.HttpMethod;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.proxy.ProxyInfo;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.response.ErrorResponse;
import com.synopsys.integration.rest.response.Response;

public class CachingHttpClient implements BlackDuckHttpClient {
    private final BlackDuckHttpClient blackDuckHttpClient;
    private final Map<Request, Response> cache;

    public CachingHttpClient(BlackDuckHttpClient blackDuckHttpClient) {
        this.blackDuckHttpClient = blackDuckHttpClient;
        this.cache = Collections.synchronizedMap(new LRUMap<>(1000));
    }

    public void emptyCache() {
        cache.clear();
    }

    @Override
    public <T extends BlackDuckResponse, U extends UrlResponse<T>> Response execute(BlackDuckRequest<T, U> blackDuckRequest) throws IntegrationException {
        Request request = blackDuckRequest.getRequest();
        if (HttpMethod.GET == request.getMethod() && cache.containsKey(request)) {
            return cache.get(request);
        }
        Response response = blackDuckHttpClient.execute(blackDuckRequest);

        // the usage of the response will determine whether or not it is cached, because we can only cache responses IFF they are retrieved by string content
        CacheableResponse cacheableResponse = new CacheableResponse(request, response, cache);
        return cacheableResponse;
    }

    @Override
    public Response attemptAuthentication() throws IntegrationException {
        return blackDuckHttpClient.attemptAuthentication();
    }

    @Override
    public boolean isAlreadyAuthenticated(HttpUriRequest request) {
        return blackDuckHttpClient.isAlreadyAuthenticated(request);
    }

    @Override
    public Optional<ErrorResponse> extractErrorResponse(String responseContent) {
        return blackDuckHttpClient.extractErrorResponse(responseContent);
    }

    @Override
    public void handleErrorResponse(HttpUriRequest request, Response response) {
        blackDuckHttpClient.handleErrorResponse(request, response);
    }

    @Override
    public void throwExceptionForError(Response response) throws IntegrationException {
        blackDuckHttpClient.throwExceptionForError(response);
    }

    @Override
    public HttpUrl getBlackDuckUrl() {
        return blackDuckHttpClient.getBlackDuckUrl();
    }

    @Override
    public String getUserAgentString() {
        return blackDuckHttpClient.getUserAgentString();
    }

    @Override
    public HttpClientBuilder getHttpClientBuilder() {
        return blackDuckHttpClient.getHttpClientBuilder();
    }

    @Override
    public int getTimeoutInSeconds() {
        return blackDuckHttpClient.getTimeoutInSeconds();
    }

    @Override
    public boolean isAlwaysTrustServerCertificate() {
        return blackDuckHttpClient.isAlwaysTrustServerCertificate();
    }

    @Override
    public ProxyInfo getProxyInfo() {
        return blackDuckHttpClient.getProxyInfo();
    }

    @Override
    public IntLogger getLogger() {
        return blackDuckHttpClient.getLogger();
    }

    @Override
    public Gson getGson() {
        return blackDuckHttpClient.getGson();
    }

}
