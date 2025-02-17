/*
 * blackduck-common
 *
 * Copyright (c) 2023 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.codelocation;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.synopsys.integration.util.NameVersion;

public abstract class CodeLocationOutput {
    private final Result result;
    @Nullable
    private final NameVersion projectAndVersion;
    private final String codeLocationName;
    private final int expectedNotificationCount;
    private final String errorMessage;
    private final Exception exception;

    public CodeLocationOutput(Result result, @Nullable NameVersion projectAndVersion, String codeLocationName, int expectedNotificationCount, String errorMessage, Exception exception) {
        this.result = result;
        this.projectAndVersion = projectAndVersion;
        this.codeLocationName = codeLocationName;
        this.expectedNotificationCount = expectedNotificationCount;
        this.errorMessage = errorMessage;
        this.exception = exception;
    }

    public Result getResult() {
        return result;
    }

    public Optional<NameVersion> getProjectAndVersion() {
        return Optional.ofNullable(projectAndVersion);
    }

    public String getCodeLocationName() {
        return codeLocationName;
    }

    public int getExpectedNotificationCount() {
        return expectedNotificationCount;
    }

    public Optional<String> getErrorMessage() {
        return Optional.ofNullable(errorMessage);
    }

    public Optional<Exception> getException() {
        return Optional.ofNullable(exception);
    }

}
