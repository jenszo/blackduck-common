package com.synopsys.integration.blackduck.bdio2;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.synopsys.integration.blackduck.bdio2.model.BdioFileContent;
import com.synopsys.integration.blackduck.service.request.BlackDuckRequestBuilderEditor;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.exception.IntegrationRestException;

class Bdio2RetryAwareStreamUploaderTest {

    @Test
    void testStartRetriable() throws IntegrationException {
        BlackDuckRequestBuilderEditor editor = Mockito.mock(BlackDuckRequestBuilderEditor.class);
        BdioFileContent bdioFileContent = Mockito.mock(BdioFileContent.class);
        Bdio2RetryAwareStreamUploader bdio2RetryAwareStreamUploader = mockBdio2RetryAwareStreamUploaderThrows512OnStart(editor, bdioFileContent);
        try {
            bdio2RetryAwareStreamUploader.start(bdioFileContent, editor);
            Assertions.fail("Expected RetriableBdioUploadException");
        } catch (RetriableBdioUploadException e) {
            // expected
        }
    }

    @Test
    void testAppendRetriable() throws IntegrationException {
        HttpUrl httpUrl = Mockito.mock(HttpUrl.class);
        BdioFileContent bdioFileContent = Mockito.mock(BdioFileContent.class);
        BlackDuckRequestBuilderEditor editor = Mockito.mock(BlackDuckRequestBuilderEditor.class);
        Bdio2RetryAwareStreamUploader bdio2RetryAwareStreamUploader = mockBdio2RetryAwareStreamUploaderThrows512OnAppend(httpUrl, bdioFileContent, editor);
        try {
            bdio2RetryAwareStreamUploader.append(httpUrl, 1, bdioFileContent, editor);
            Assertions.fail("Expected RetriableBdioUploadException");
        } catch (RetriableBdioUploadException e) {
            // expected
        }
    }

    @Test
    void testFinishRetriable() throws IntegrationException {
        HttpUrl httpUrl = Mockito.mock(HttpUrl.class);
        //BdioFileContent bdioFileContent = Mockito.mock(BdioFileContent.class);
        BlackDuckRequestBuilderEditor editor = Mockito.mock(BlackDuckRequestBuilderEditor.class);
        Bdio2RetryAwareStreamUploader bdio2RetryAwareStreamUploader = mockBdio2RetryAwareStreamUploaderThrows512OnFinish(httpUrl, editor);
        try {
            bdio2RetryAwareStreamUploader.finish(httpUrl, 1, editor);
            Assertions.fail("Expected RetriableBdioUploadException");
        } catch (RetriableBdioUploadException e) {
            // expected
        }
    }

    @Test
    void testStartNonRetriable() throws IntegrationException, RetriableBdioUploadException {
        BdioFileContent bdioFileContent = Mockito.mock(BdioFileContent.class);
        BlackDuckRequestBuilderEditor editor = Mockito.mock(BlackDuckRequestBuilderEditor.class);
        Bdio2RetryAwareStreamUploader bdio2RetryAwareStreamUploader = mockBdio2RetryAwareStreamUploaderThrow404OnStart(bdioFileContent, editor);
        try {
            bdio2RetryAwareStreamUploader.start(bdioFileContent, editor);
            Assertions.fail("Expected RetriableBdioUploadException");
        } catch (IntegrationException e) {
            // expected
        }
    }

    @NotNull
    private Bdio2RetryAwareStreamUploader mockBdio2RetryAwareStreamUploaderThrow404OnStart(BdioFileContent bdioFileContent, BlackDuckRequestBuilderEditor editor)
        throws IntegrationException {
        Bdio2StreamUploader bdio2StreamUploader = Mockito.mock(Bdio2StreamUploader.class);
        Bdio2RetryAwareStreamUploader bdio2RetryAwareStreamUploader = new Bdio2RetryAwareStreamUploader(bdio2StreamUploader);
        IntegrationRestException exception404 = Mockito.mock(IntegrationRestException.class);
        Mockito.when(bdio2StreamUploader.start(bdioFileContent, editor)).thenThrow(exception404);
        Mockito.when(exception404.getHttpStatusCode()).thenReturn(404);
        return bdio2RetryAwareStreamUploader;
    }

    @NotNull
    private Bdio2RetryAwareStreamUploader mockBdio2RetryAwareStreamUploaderThrows512OnStart(BlackDuckRequestBuilderEditor editor, BdioFileContent bdioFileContent)
        throws IntegrationException {
        Bdio2StreamUploader bdio2StreamUploader = Mockito.mock(Bdio2StreamUploader.class);
        IntegrationRestException exception512 = Mockito.mock(IntegrationRestException.class);
        Mockito.when(exception512.getHttpStatusCode()).thenReturn(512);
        Mockito.when(bdio2StreamUploader.start(bdioFileContent, editor)).thenThrow(exception512);
        return new Bdio2RetryAwareStreamUploader(bdio2StreamUploader);
    }

    @NotNull
    private Bdio2RetryAwareStreamUploader mockBdio2RetryAwareStreamUploaderThrows512OnAppend(HttpUrl httpUrl, BdioFileContent bdioFileContent, BlackDuckRequestBuilderEditor editor)
        throws IntegrationException {
        Bdio2StreamUploader bdio2StreamUploader = Mockito.mock(Bdio2StreamUploader.class);
        IntegrationRestException exception512 = Mockito.mock(IntegrationRestException.class);
        Mockito.when(bdio2StreamUploader.append(httpUrl, 1, bdioFileContent, editor)).thenThrow(exception512);
        Mockito.when(exception512.getHttpStatusCode()).thenReturn(512);
        return new Bdio2RetryAwareStreamUploader(bdio2StreamUploader);
    }

    @NotNull
    private Bdio2RetryAwareStreamUploader mockBdio2RetryAwareStreamUploaderThrows512OnFinish(HttpUrl httpUrl, BlackDuckRequestBuilderEditor editor)
        throws IntegrationException {
        Bdio2StreamUploader bdio2StreamUploader = Mockito.mock(Bdio2StreamUploader.class);
        IntegrationRestException exception512 = Mockito.mock(IntegrationRestException.class);
        Mockito.when(bdio2StreamUploader.finish(httpUrl, 1, editor)).thenThrow(exception512);
        Mockito.when(exception512.getHttpStatusCode()).thenReturn(512);
        return new Bdio2RetryAwareStreamUploader(bdio2StreamUploader);
    }
}
