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

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.io.FilenameUtils;

import com.synopsys.integration.blackduck.api.manual.view.BomMatchDeveloperView;
import com.synopsys.integration.blackduck.exception.BlackDuckIntegrationException;
import com.synopsys.integration.blackduck.http.BlackDuckRequestFactory;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.request.Request;

public class DeveloperScanService {
    public static final int DEFAULT_WAIT_INTERVAL_IN_SECONDS = 1;
    private static final String CONTENT_TYPE = "application/vnd.blackducksoftware.developer-scan-ld-1+json";
    private static final String HEADER_X_BD_MODE = "X-BD-MODE";
    private static final String HEADER_X_BD_PASSTHRU = "X-BD-PASSTHRU";
    private static final String HEADER_X_BD_SCAN_ID = "X-BD-SCAN-ID";
    private static final String HEADER_X_BD_DOCUMENT_COUNT = "X-BD-DOCUMENT-COUNT";
    private static final String HEADER_X_BD_SCAN_TYPE = "X-BD-SCAN-TYPE";
    private static final String FILE_NAME_BDIO_HEADER_JSONLD = "bdio-header.jsonld";

    private BlackDuckApiClient blackDuckApiClient;
    private BlackDuckRequestFactory blackDuckRequestFactory;
    private DeveloperScanWaiter developerScanWaiter;

    public DeveloperScanService(BlackDuckApiClient blackDuckApiClient, BlackDuckRequestFactory blackDuckRequestFactory, DeveloperScanWaiter developerScanWaiter) {
        this.blackDuckApiClient = blackDuckApiClient;
        this.blackDuckRequestFactory = blackDuckRequestFactory;
        this.developerScanWaiter = developerScanWaiter;
    }

    public List<BomMatchDeveloperView> performDeveloperScan(String scanType, File bdio2File, long timeoutInSeconds) throws IntegrationException, InterruptedException {
        return performDeveloperScan(scanType, bdio2File, timeoutInSeconds, DEFAULT_WAIT_INTERVAL_IN_SECONDS);
    }

    public List<BomMatchDeveloperView> performDeveloperScan(String scanType, File bdio2File, long timeoutInSeconds, int waitIntervalInSeconds) throws IntegrationException, InterruptedException {
        String absolutePath = bdio2File.getAbsolutePath();
        if (!bdio2File.isFile()) {
            throw new IllegalArgumentException(String.format("bdio file provided is not a file. Path: %s ", absolutePath));
        }
        if (!bdio2File.exists()) {
            throw new IllegalArgumentException(String.format("bdio file does not exist. Path: %s", absolutePath));
        }
        String fileExtension = FilenameUtils.getExtension(absolutePath);
        if (!"bdio".equals(fileExtension)) {
            throw new IllegalArgumentException(String.format("Unknown file extension. Cannot perform developer scan. Path: %s", absolutePath));
        }
        List<BdioContent> bdioContentList = new ArrayList<>();
        try (ZipFile zipFile = new ZipFile(bdio2File)) {
            Enumeration<? extends ZipEntry> entries = zipFile.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                bdioContentList.add(readEntryContent(zipFile, entry));
            }
        } catch (IOException ex) {
            throw new IntegrationException(String.format("Exception unzipping BDIO file. Path: %s", bdio2File), ex);
        }
        return uploadFilesAndWait(scanType, bdioContentList, timeoutInSeconds, waitIntervalInSeconds);
    }

    private BdioContent readEntryContent(ZipFile zipFile, ZipEntry entry) throws IntegrationException {
        String entryContent;
        byte[] buffer = new byte[1024];
        try (InputStream zipInputStream = zipFile.getInputStream(entry);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(byteArrayOutputStream)) {
            int length;
            while ((length = zipInputStream.read(buffer)) > 0) {
                bufferedOutputStream.write(buffer, 0, length);
            }
            bufferedOutputStream.flush();
            entryContent = new String(byteArrayOutputStream.toByteArray(), StandardCharsets.UTF_8);
        } catch (IOException ex) {
            throw new IntegrationException(String.format("Error reading entry %s", entry.getName()), ex);
        }
        return new BdioContent(entry.getName(), entryContent);
    }

    private List<BomMatchDeveloperView> uploadFilesAndWait(String scanType, List<BdioContent> bdioFiles, long timeoutInSeconds, int waitIntervalInSeconds) throws IntegrationException, InterruptedException {
        if (bdioFiles.isEmpty()) {
            throw new IllegalArgumentException("BDIO files cannot be empty.");
        }
        BdioContent header = bdioFiles.stream()
                                 .filter(content -> content.getFileName().equals(FILE_NAME_BDIO_HEADER_JSONLD))
                                 .findFirst()
                                 .orElseThrow(() -> new BlackDuckIntegrationException("Cannot find BDIO header file" + FILE_NAME_BDIO_HEADER_JSONLD + "."));

        List<BdioContent> remainingFiles = bdioFiles.stream()
                                               .filter(content -> !content.getFileName().equals(FILE_NAME_BDIO_HEADER_JSONLD))
                                               .collect(Collectors.toList());
        UUID scanId = UUID.randomUUID();
        int count = remainingFiles.size();
        startUpload(scanId, count, scanType, header);
        for (BdioContent content : remainingFiles) {
            uploadChunk(scanId, scanType, content);
        }

        return waitForUploadResults(scanId, timeoutInSeconds, waitIntervalInSeconds);
    }

    private void startUpload(UUID scanId, int count, String scanType, BdioContent header) throws IntegrationException {
        HttpUrl url = blackDuckApiClient.getUrl(BlackDuckApiClient.SCAN_DATA_PATH);
        Request request = blackDuckRequestFactory
                              .createCommonPostRequestBuilder(url, header.getContent())
                              .acceptMimeType(CONTENT_TYPE)
                              .addHeader("Content-type", CONTENT_TYPE)
                              .addHeader(HEADER_X_BD_MODE, "start")
                              .addHeader(HEADER_X_BD_PASSTHRU, "foo")
                              .addHeader(HEADER_X_BD_SCAN_ID, scanId.toString())
                              .addHeader(HEADER_X_BD_DOCUMENT_COUNT, String.valueOf(count))
                              .addHeader(HEADER_X_BD_SCAN_TYPE, scanType)
                              .build();

        blackDuckApiClient.execute(request);
    }

    private void uploadChunk(UUID scanId, String scanType, BdioContent bdioContent) throws IntegrationException {
        HttpUrl url = blackDuckApiClient.getUrl(BlackDuckApiClient.SCAN_DATA_PATH);
        Request request = blackDuckRequestFactory
                              .createCommonPostRequestBuilder(url, bdioContent.getContent())
                              .acceptMimeType(CONTENT_TYPE)
                              .addHeader("Content-type", CONTENT_TYPE)
                              .addHeader(HEADER_X_BD_MODE, "append")
                              .addHeader(HEADER_X_BD_PASSTHRU, "foo")
                              .addHeader(HEADER_X_BD_SCAN_ID, scanId.toString())
                              .addHeader(HEADER_X_BD_SCAN_TYPE, scanType)
                              .build();
        blackDuckApiClient.execute(request);
    }

    private List<BomMatchDeveloperView> waitForUploadResults(UUID scanId, long timeoutInSecond, int waitIntervalInSeconds) throws InterruptedException, IntegrationException {
        return developerScanWaiter.checkScanResult(scanId, timeoutInSecond, waitIntervalInSeconds);
    }

    private class BdioContent {
        private String fileName;
        private String content;

        public BdioContent(final String fileName, final String content) {
            this.fileName = fileName;
            this.content = content;
        }

        public String getFileName() {
            return fileName;
        }

        public String getContent() {
            return content;
        }
    }
}
