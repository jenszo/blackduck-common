/*
 * blackduck-common
 *
 * Copyright (c) 2023 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.http.client;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.message.BasicNameValuePair;

import com.google.gson.Gson;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.credentials.Credentials;
import com.synopsys.integration.rest.proxy.ProxyInfo;
import com.synopsys.integration.rest.response.Response;
import com.synopsys.integration.rest.support.AuthenticationSupport;
import com.synopsys.integration.util.NameVersion;

public class CredentialsBlackDuckHttpClient extends DefaultBlackDuckHttpClient {
    private final Credentials credentials;
    private final CookieHeaderParser cookieHeaderParser;

    public CredentialsBlackDuckHttpClient(
        IntLogger logger, Gson gson, int timeout, boolean alwaysTrustServerCertificate, ProxyInfo proxyInfo, HttpUrl blackDuckUrl, NameVersion solutionDetails, AuthenticationSupport authenticationSupport, Credentials credentials,
        CookieHeaderParser cookieHeaderParser) {
        super(logger, gson, timeout, alwaysTrustServerCertificate, proxyInfo, blackDuckUrl, solutionDetails, authenticationSupport);
        this.credentials = credentials;
        this.cookieHeaderParser = cookieHeaderParser;

        if (credentials == null) {
            throw new IllegalArgumentException("Credentials cannot be null.");
        }
    }

    @Override
    public Response attemptAuthentication() throws IntegrationException {
        List<NameValuePair> bodyValues = new ArrayList<>();
        bodyValues.add(new BasicNameValuePair("j_username", credentials.getUsername().orElse(null)));
        bodyValues.add(new BasicNameValuePair("j_password", credentials.getPassword().orElse(null)));
        UrlEncodedFormEntity entity = new UrlEncodedFormEntity(bodyValues, StandardCharsets.UTF_8);

        return authenticationSupport.attemptAuthentication(this, getBlackDuckUrl(), "j_spring_security_check", entity);
    }

    @Override
    protected void completeAuthenticationRequest(HttpUriRequest request, Response response) {
        if (response.isStatusCodeSuccess()) {
            CloseableHttpResponse actualResponse = response.getActualResponse();
            Optional<String> token = cookieHeaderParser.parseBearerToken(actualResponse.getAllHeaders());
            authenticationSupport.addBearerToken(logger, request, this, token);
        }
    }

}
