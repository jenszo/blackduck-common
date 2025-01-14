/*
 * blackduck-common
 *
 * Copyright (c) 2023 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.codelocation.signaturescanner.command;

import java.io.File;

import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;

public interface ScannerInstaller {
    File installOrUpdateScanner() throws BlackDuckIntegrationException;

}
