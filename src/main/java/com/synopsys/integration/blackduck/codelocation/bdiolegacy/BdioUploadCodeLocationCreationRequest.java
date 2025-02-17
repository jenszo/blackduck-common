/*
 * blackduck-common
 *
 * Copyright (c) 2023 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.codelocation.bdiolegacy;

import com.synopsys.integration.blackduck.codelocation.CodeLocationCreationRequest;
import com.synopsys.integration.blackduck.codelocation.upload.UploadBatch;
import com.synopsys.integration.blackduck.codelocation.upload.UploadBatchOutput;
import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;

public class BdioUploadCodeLocationCreationRequest extends CodeLocationCreationRequest<UploadBatchOutput> {
    private final UploadBatchRunner uploadBatchRunner;
    private final UploadBatch uploadBatch;

    public BdioUploadCodeLocationCreationRequest(UploadBatchRunner uploadBatchRunner, UploadBatch uploadBatch) {
        this.uploadBatchRunner = uploadBatchRunner;
        this.uploadBatch = uploadBatch;
    }

    @Override
    public UploadBatchOutput executeRequest() throws BlackDuckIntegrationException {
        return uploadBatchRunner.executeUploads(uploadBatch);
    }

}
