/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.codelocation.bdio2legacy;

import java.io.IOException;
import java.util.concurrent.Callable;

import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.codelocation.upload.UploadOutput;
import com.synopsys.integration.blackduck.codelocation.upload.UploadTarget;
import com.synopsys.integration.blackduck.http.BlackDuckRequestBuilderFactory;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.request.Request;
import com.synopsys.integration.rest.response.Response;
import com.synopsys.integration.util.NameVersion;

public class UploadBdio2Callable implements Callable<UploadOutput> {
    private final BlackDuckApiClient blackDuckApiClient;
    private final ApiDiscovery apiDiscovery;
    private final BlackDuckRequestBuilderFactory blackDuckRequestBuilderFactory;
    private final UploadTarget uploadTarget;
    private final NameVersion projectAndVersion;
    private final String codeLocationName;

    public UploadBdio2Callable(BlackDuckApiClient blackDuckApiClient, ApiDiscovery apiDiscovery, BlackDuckRequestBuilderFactory blackDuckRequestBuilderFactory, UploadTarget uploadTarget) {
        this.blackDuckApiClient = blackDuckApiClient;
        this.apiDiscovery = apiDiscovery;
        this.blackDuckRequestBuilderFactory = blackDuckRequestBuilderFactory;
        this.uploadTarget = uploadTarget;
        this.projectAndVersion = uploadTarget.getProjectAndVersion();
        this.codeLocationName = uploadTarget.getCodeLocationName();
    }

    @Override
    public UploadOutput call() {
        try {
            HttpUrl url = apiDiscovery.metaSingleResponse(BlackDuckApiClient.SCAN_DATA_PATH).getUrl();
            Request request = blackDuckRequestBuilderFactory
                                  .createBlackDuckRequestBuilder()
                                  .postFile(uploadTarget.getUploadFile())
                                  .acceptMimeType(uploadTarget.getMediaType())
                                  .url(url)
                                  .build();
            try (Response response = blackDuckApiClient.execute(request)) {
                String responseString = response.getContentString();
                return UploadOutput.SUCCESS(projectAndVersion, codeLocationName, responseString);
            } catch (IOException e) {
                return UploadOutput.FAILURE(projectAndVersion, codeLocationName, e.getMessage(), e);
            }
        } catch (Exception e) {
            String errorMessage = String.format("Failed to upload file: %s because %s", uploadTarget.getUploadFile().getAbsolutePath(), e.getMessage());
            return UploadOutput.FAILURE(projectAndVersion, codeLocationName, errorMessage, e);
        }
    }

}
