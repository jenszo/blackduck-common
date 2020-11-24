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
package com.synopsys.integration.blackduck.developermode;

import java.util.List;
import java.util.UUID;

import com.synopsys.integration.blackduck.api.core.BlackDuckPath;
import com.synopsys.integration.blackduck.api.core.response.BlackDuckPathMultipleResponses;
import com.synopsys.integration.blackduck.api.manual.view.BomMatchDeveloperView;
import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.wait.WaitJob;

public class DeveloperScanWaiter {
    private IntLogger logger;
    private BlackDuckApiClient blackDuckApiClient;

    public DeveloperScanWaiter(final IntLogger logger, final BlackDuckApiClient blackDuckApiClient) {
        this.logger = logger;
        this.blackDuckApiClient = blackDuckApiClient;
    }

    public List<BomMatchDeveloperView> checkScanResult(UUID scanId, long timeoutInSeconds, int waitIntervalInSeconds) throws IntegrationException, InterruptedException {
        BlackDuckPath apiPath = new BlackDuckPath(String.format("/api/scans/%s/developer-result", scanId.toString()));
        BlackDuckPathMultipleResponses<BomMatchDeveloperView> multipleResponses = new BlackDuckPathMultipleResponses<>(apiPath, BomMatchDeveloperView.class);
        DeveloperScanWaitJobTask waitTask = new DeveloperScanWaitJobTask(logger, blackDuckApiClient, apiPath);
        // if a timeout of 0 is provided and the timeout check is done too quickly, w/o a do/while, no check will be performed
        // regardless of the timeout provided, we always want to check at least once
        boolean allCompleted = waitTask.isComplete();

        // waitInterval needs to be less than the timeout
        if (waitIntervalInSeconds > timeoutInSeconds) {
            waitIntervalInSeconds = (int) timeoutInSeconds;
        }

        if (!allCompleted) {
            WaitJob waitJob = WaitJob.create(logger, timeoutInSeconds, System.currentTimeMillis(), waitIntervalInSeconds, waitTask);
            allCompleted = waitJob.waitFor();
        }

        if (!allCompleted) {
            throw new BlackDuckIntegrationException("Error getting developer scan result. Timeout may have occurred.");
        }
        return blackDuckApiClient.getAllResponses(multipleResponses);
    }
}
