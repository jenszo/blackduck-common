/*
 * blackduck-common
 *
 * Copyright (c) 2021 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.scan;

import java.io.File;
import java.util.List;

import com.synopsys.integration.blackduck.api.manual.view.DeveloperScanComponentResultView;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.HttpUrl;

public class RapidScanService extends AbstractScanService {
    public static final int DEFAULT_WAIT_INTERVAL_IN_SECONDS = 30;
    public static final String CONTENT_TYPE = "application/vnd.blackducksoftware.developer-scan-1-ld-2+json";
    private RapidScanWaiter rapidScanWaiter;

    public RapidScanService(ScanBdio2Reader bdio2Reader, ScanBdio2Uploader bdio2Uploader, RapidScanWaiter rapidScanWaiter) {
        super(bdio2Reader, bdio2Uploader);
        this.rapidScanWaiter = rapidScanWaiter;
    }

    public List<DeveloperScanComponentResultView> performScan(File bdio2File, long timeoutInSeconds) throws IntegrationException, InterruptedException {
        return performScan(bdio2File, timeoutInSeconds, DEFAULT_WAIT_INTERVAL_IN_SECONDS);
    }

    public List<DeveloperScanComponentResultView> performScan(File bdio2File, long timeoutInSeconds, int waitIntervalInSeconds) throws IntegrationException, InterruptedException {
        HttpUrl url = readContentAndUpload(bdio2File);
        return rapidScanWaiter.checkScanResult(url, timeoutInSeconds, waitIntervalInSeconds);
    }
}
