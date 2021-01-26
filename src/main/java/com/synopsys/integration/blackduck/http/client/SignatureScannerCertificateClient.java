/**
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
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
package com.synopsys.integration.blackduck.http.client;

import java.security.cert.Certificate;

import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.BasicHttpContext;

import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.client.IntHttpClient;
import com.synopsys.integration.rest.proxy.ProxyInfo;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.response.Response;

public class SignatureScannerCertificateClient extends IntHttpClient {
    public static final String PEER_CERTIFICATES = "PEER_CERTIFICATES";

    private Certificate serverCertificate;

    public SignatureScannerCertificateClient(BlackDuckHttpClient blackDuckHttpClient) {
        super(blackDuckHttpClient.getLogger(), blackDuckHttpClient.getTimeoutInSeconds(), blackDuckHttpClient.isAlwaysTrustServerCertificate(), blackDuckHttpClient.getProxyInfo());
    }

    public SignatureScannerCertificateClient(IntLogger logger, int timeoutInSeconds, boolean alwaysTrustServerCertificate, ProxyInfo proxyInfo) {
        super(logger, timeoutInSeconds, alwaysTrustServerCertificate, proxyInfo);
    }

    @Override
    protected void addToHttpClientBuilder(HttpClientBuilder httpClientBuilder, RequestConfig.Builder defaultRequestConfigBuilder) {
        super.addToHttpClientBuilder(httpClientBuilder, defaultRequestConfigBuilder);
        httpClientBuilder.addInterceptorLast(new BlackDuckCertificateInterceptor());
        httpClientBuilder.setConnectionReuseStrategy((httpResponse, httpContext) -> true);
    }

    @Override
    public Response execute(Request request) throws IntegrationException {
        BasicHttpContext httpContext = new BasicHttpContext();
        Response response = super.execute(request, httpContext);

        Certificate[] peerCertificates = (Certificate[]) httpContext.getAttribute(PEER_CERTIFICATES);
        if (null != peerCertificates) {
            serverCertificate = peerCertificates[0];
        }

        return response;
    }

    public Certificate getServerCertificate() {
        return serverCertificate;
    }

}
