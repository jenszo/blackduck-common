/**
 * blackduck-common
 *
 * Copyright (c) 2020 Synopsys, Inc.
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.synopsys.integration.blackduck.http;

import com.synopsys.integration.blackduck.api.generated.discovery.BlackDuckMediaTypeDiscovery;
import com.synopsys.integration.rest.HttpMethod;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.body.BodyContent;
import com.synopsys.integration.rest.request.Request;

import java.nio.charset.Charset;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.synopsys.integration.blackduck.http.RequestFactory.*;

public class BlackDuckRequestBuilder {
    private final BlackDuckMediaTypeDiscovery blackDuckMediaTypeDiscovery;
    private final Request.Builder requestBuilder;

    public BlackDuckRequestBuilder(BlackDuckMediaTypeDiscovery blackDuckMediaTypeDiscovery, Request.Builder requestBuilder) {
        this.blackDuckMediaTypeDiscovery = blackDuckMediaTypeDiscovery;
        this.requestBuilder = requestBuilder;
    }

    public Request build() {
        return requestBuilder.build();
    }

    public Request.Builder getRequestBuilder() {
        return requestBuilder;
    }

    public BlackDuckRequestBuilder url(HttpUrl url) {
        requestBuilder.url(url);
        if (null != url && (null == requestBuilder.getAcceptMimeType() || Request.DEFAULT_ACCEPT_MIME_TYPE.equals(requestBuilder.getAcceptMimeType()))) {
            String acceptMimeType = blackDuckMediaTypeDiscovery.determineMediaType(url);
            requestBuilder.acceptMimeType(acceptMimeType);
        }
        return this;
    }

    public BlackDuckRequestBuilder addLimit(int limit) {
        requestBuilder.addQueryParameter(LIMIT_PARAMETER, String.valueOf(limit));
        return this;
    }

    public BlackDuckRequestBuilder addOffset(int offset) {
        requestBuilder.addQueryParameter(OFFSET_PARAMETER, String.valueOf(offset));
        return this;
    }

    public BlackDuckRequestBuilder addBlackDuckQuery(Optional<BlackDuckQuery> blackDuckQuery) {
        if (blackDuckQuery.isPresent()) {
            requestBuilder.addQueryParameter(Q_PARAMETER, blackDuckQuery.get().getParameter());
        }
        return this;
    }

    public BlackDuckRequestBuilder addBlackDuckFilter(BlackDuckRequestFilter blackDuckRequestFilter) {
        if (blackDuckRequestFilter != null) {
            blackDuckRequestFilter.getFilterParameters().forEach(parameter -> {
                requestBuilder.addQueryParameter(FILTER_PARAMETER, parameter);
            });
        }
        return this;
    }

    public BlackDuckRequestBuilder method(HttpMethod method) {
        requestBuilder.method(method);
        return this;
    }

    public BlackDuckRequestBuilder acceptMimeType(String acceptHeader) {
        requestBuilder.acceptMimeType(acceptHeader);
        return this;
    }

    public BlackDuckRequestBuilder bodyEncoding(Charset bodyEncoding) {
        requestBuilder.bodyEncoding(bodyEncoding);
        return this;
    }

    public BlackDuckRequestBuilder queryParameters(Map<String, Set<String>> queryParameters) {
        requestBuilder.queryParameters(queryParameters);
        return this;
    }

    public BlackDuckRequestBuilder addQueryParameter(String key, String value) {
        requestBuilder.addQueryParameter(key, value);
        return this;
    }

    public BlackDuckRequestBuilder headers(Map<String, String> headers) {
        requestBuilder.headers(headers);
        return this;
    }

    public BlackDuckRequestBuilder addHeader(String key, String value) {
        requestBuilder.addHeader(key, value);
        return this;
    }

    public BlackDuckRequestBuilder bodyContent(BodyContent bodyContent) {
        requestBuilder.bodyContent(bodyContent);
        return this;
    }

    public HttpUrl getUrl() {
        return requestBuilder.getUrl();
    }

    public HttpMethod getMethod() {
        return requestBuilder.getMethod();
    }

    public String getAcceptMimeType() {
        return requestBuilder.getAcceptMimeType();
    }

    public Charset getBodyEncoding() {
        return requestBuilder.getBodyEncoding();
    }

    public Map<String, Set<String>> getQueryParameters() {
        return requestBuilder.getQueryParameters();
    }

    public Map<String, String> getHeaders() {
        return requestBuilder.getHeaders();
    }

    public BodyContent getBodyContent() {
        return requestBuilder.getBodyContent();
    }

}