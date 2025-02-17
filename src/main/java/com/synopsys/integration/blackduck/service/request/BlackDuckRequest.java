/*
 * blackduck-common
 *
 * Copyright (c) 2023 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.service.request;

import com.synopsys.integration.blackduck.api.core.BlackDuckResponse;
import com.synopsys.integration.blackduck.api.core.response.UrlMultipleResponses;
import com.synopsys.integration.blackduck.api.core.response.UrlResponse;
import com.synopsys.integration.blackduck.api.core.response.UrlSingleResponse;
import com.synopsys.integration.blackduck.api.generated.discovery.BlackDuckMediaTypeDiscovery;
import com.synopsys.integration.blackduck.http.BlackDuckRequestBuilder;
import com.synopsys.integration.rest.HttpMethod;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.request.Request;

/**
 * The total picture of a Black Duck interaction. Intended to be an immutable,
 * valid packaging of an HTTP request to Black Duck and how to consider and
 * handle the response.
 */
public class BlackDuckRequest<T extends BlackDuckResponse, U extends UrlResponse<T>> {
    private static final PagingDefaultsEditor DEFAULT_PAGING_DEFAULTS_EDITOR = new PagingDefaultsEditor();
    private static final AcceptHeaderEditor DEFAULT_ACCEPT_HEADER_EDITOR = new AcceptHeaderEditor(new BlackDuckMediaTypeDiscovery());

    private final BlackDuckRequestBuilder blackDuckRequestBuilder;
    private final U urlResponse;

    public static <T extends BlackDuckResponse> BlackDuckRequest<T, UrlSingleResponse<T>> createSingleRequest(BlackDuckRequestBuilder blackDuckRequestBuilder, HttpUrl url, Class<T> responseClass) {
        return new BlackDuckSingleRequest(blackDuckRequestBuilder, new UrlSingleResponse(url, responseClass));
    }

    public static <T extends BlackDuckResponse> BlackDuckMultipleRequest<T> createMultipleRequest(BlackDuckRequestBuilder blackDuckRequestBuilder, HttpUrl url, Class<T> responseClass) {
        return new BlackDuckMultipleRequest(blackDuckRequestBuilder, new UrlMultipleResponses(url, responseClass));
    }

    public BlackDuckRequest(BlackDuckRequestBuilder blackDuckRequestBuilder, U urlResponse) {
        this(blackDuckRequestBuilder, urlResponse, DEFAULT_PAGING_DEFAULTS_EDITOR, DEFAULT_ACCEPT_HEADER_EDITOR);
    }

    public BlackDuckRequest(BlackDuckRequestBuilder blackDuckRequestBuilder, U urlResponse, PagingDefaultsEditor pagingDefaultsEditor, AcceptHeaderEditor acceptHeaderEditor) {
        this.blackDuckRequestBuilder = blackDuckRequestBuilder.url(urlResponse.getUrl());
        this.urlResponse = urlResponse;

        blackDuckRequestBuilder.apply(acceptHeaderEditor);

        if (HttpMethod.GET == blackDuckRequestBuilder.getMethod()) {
            blackDuckRequestBuilder.apply(pagingDefaultsEditor);
        }
    }

    public Request getRequest() {
        return blackDuckRequestBuilder.build();
    }

    public U getUrlResponse() {
        return urlResponse;
    }

    public HttpUrl getUrl() {
        return urlResponse.getUrl();
    }

    public Class<T> getResponseClass() {
        return urlResponse.getResponseClass();
    }

}
