/*
 * blackduck-common
 *
 * Copyright (c) 2023 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.codelocation.upload;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.blackduck.codelocation.CodeLocationOutput;
import com.synopsys.integration.blackduck.codelocation.Result;
import com.synopsys.integration.util.NameVersion;

public class UploadOutput extends CodeLocationOutput {
    private final String response;
    private final String scanId;

    private UploadOutput(@Nullable NameVersion projectAndVersion, String codeLocationName, Result result, String response, String errorMessage, Exception exception, String scanId) {
        super(result, projectAndVersion, codeLocationName, 1, errorMessage, exception);
        this.response = response;
        this.scanId = scanId;
    }
    
    public static UploadOutput SUCCESS(@Nullable NameVersion projectAndVersion, String codeLocationName, String response) {
        return new UploadOutput(projectAndVersion, codeLocationName, Result.SUCCESS, response, null, null, null);
    }

    public static UploadOutput SUCCESS(@Nullable NameVersion projectAndVersion, String codeLocationName, String response, String scanId) {
        return new UploadOutput(projectAndVersion, codeLocationName, Result.SUCCESS, response, null, null, scanId);
    }

    public static UploadOutput FAILURE(@Nullable NameVersion projectAndVersion, String codeLocationName, String errorMessage, Exception exception) {
        return new UploadOutput(projectAndVersion, codeLocationName, Result.FAILURE, null, errorMessage, exception, null);
    }

    public static UploadOutput FAILURE(@Nullable NameVersion projectAndVersion, String codeLocationName, String response, String errorMessage, Exception exception) {
        return new UploadOutput(projectAndVersion, codeLocationName, Result.FAILURE, response, errorMessage, exception, null);
    }

    public Optional<String> getResponse() {
        return Optional.ofNullable(response);
    }

	public Optional<String> getScanId() {
		return Optional.ofNullable(scanId);
	}

}
