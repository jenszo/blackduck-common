/*
 * blackduck-common
 *
 * Copyright (c) 2023 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.bdio2;

import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.synopsys.integration.blackduck.bdio2.model.BdioFileContent;
import com.synopsys.integration.blackduck.service.request.BlackDuckRequestBuilderEditor;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.exception.IntegrationRestException;
import com.synopsys.integration.rest.response.Response;

public class Bdio2RetryAwareStreamUploader {
    private static final List<Integer> NON_RETRYABLE_EXIT_CODES = Arrays.asList(401, 402, 403, 404, 500);
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Bdio2StreamUploader bdio2StreamUploader;

    public Bdio2RetryAwareStreamUploader(Bdio2StreamUploader bdio2StreamUploader) {
        this.bdio2StreamUploader = bdio2StreamUploader;
    }

    public Response start(BdioFileContent header, BlackDuckRequestBuilderEditor editor)
        throws RetriableBdioUploadException, IntegrationException {
        logger.trace("Executing BDIO upload start operation; non-retryable status codes: {}", NON_RETRYABLE_EXIT_CODES);
        try {
            return bdio2StreamUploader.start(header, editor);
        } catch (IntegrationRestException e) {
            return translateRetryableExceptions(e);
        }
    }

    public Response append(HttpUrl uploadUrl, int count, BdioFileContent content, BlackDuckRequestBuilderEditor editor)
        throws RetriableBdioUploadException, IntegrationException {
        logger.trace("Executing BDIO upload append operation");
        Response response = null;
        try {
            response = bdio2StreamUploader.append(uploadUrl, count, content, editor);
        } catch (IntegrationRestException e) {
            translateRetryableExceptions(e);
        }
        return response;
    }

    public Response finish(HttpUrl uploadUrl, int count, BlackDuckRequestBuilderEditor editor)
        throws RetriableBdioUploadException, IntegrationException {
        logger.trace("Executing BDIO upload finish operation");
        Response response = null;
        try {
            response = bdio2StreamUploader.finish(uploadUrl, count, editor);
        } catch (IntegrationRestException e) {
            translateRetryableExceptions(e);
        }
        return response;
    }

    public void onErrorThrowRetryableOrFailure(Response response) throws IntegrationException, RetriableBdioUploadException {
        if (!response.isStatusCodeSuccess()) {
            if (isRetryableExitCode(response.getStatusCode())) {
                logger.trace("Response status code {} is retryable", response.getStatusCode());
                throw new RetriableBdioUploadException();
            }
            logger.trace("Response status code {} is not retryable", response.getStatusCode());
            throw new IntegrationException(String.format("Bdio upload failed with non-retryable exit code: %d", response.getStatusCode()));
        }
        logger.trace("Response status code {} treated as success", response.getStatusCode());
    }

    private Response translateRetryableExceptions(final IntegrationRestException e) throws RetriableBdioUploadException, IntegrationRestException {
        if (isRetryableExitCode(e.getHttpStatusCode())) {
            logger.trace("Response status code {} in caught exception is retryable", e.getHttpStatusCode());
            throw new RetriableBdioUploadException();
        }
        throw e;
    }

    private boolean isRetryableExitCode(int exitCode) {
        if (NON_RETRYABLE_EXIT_CODES.contains(exitCode)) {
            return false;
        }
        return true;
    }
}
