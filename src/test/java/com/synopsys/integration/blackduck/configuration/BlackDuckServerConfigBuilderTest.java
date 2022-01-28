package com.synopsys.integration.blackduck.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.synopsys.integration.blackduck.TimingExtension;
import com.synopsys.integration.blackduck.http.client.CookieHeaderParser;
import com.synopsys.integration.blackduck.service.BlackDuckServicesFactory;
import com.synopsys.integration.builder.BuilderProperties;
import com.synopsys.integration.builder.BuilderPropertyKey;
import com.synopsys.integration.builder.BuilderStatus;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;
import com.synopsys.integration.log.LogLevel;
import com.synopsys.integration.log.PrintStreamIntLogger;
import com.synopsys.integration.rest.HttpUrl;
import com.synopsys.integration.rest.credentials.Credentials;
import com.synopsys.integration.rest.credentials.CredentialsBuilder;
import com.synopsys.integration.rest.proxy.ProxyInfo;
import com.synopsys.integration.rest.proxy.ProxyInfoBuilder;
import com.synopsys.integration.rest.support.AuthenticationSupport;
import com.synopsys.integration.util.IntEnvironmentVariables;
import com.synopsys.integration.util.NameVersion;
import com.synopsys.integration.util.NoThreadExecutorService;

@ExtendWith(TimingExtension.class)
public class BlackDuckServerConfigBuilderTest {
    private static final String GOOD_URL = "https://blackduck-common.com";
    private static final String NAME_VERSION_NAME = "name";
    private static final String NAME_VERSION_VERSION = "version";
    private static final String USERNAME = "username";
    private static final String PASSWORD = "password";
    private static final String KEY = "property-key";
    private static final String VALUE = "property-value";
    public static final int TIMEOUT = 60;
    public static final String API_TOKEN = "my_api_token";

    @Test
    void testSettingFromPropertiesMapWithMixed() {
        Map<String, String> properties = new HashMap<>();
        properties.put("BLACKDUCK_URL", "test url");
        properties.put("blackduck.username", "user");
        properties.put("BLACKDUCK_PASSWORD", "password");

        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = new BlackDuckServerConfigBuilder();
        assertNull(blackDuckServerConfigBuilder.getUrl());
        assertNull(blackDuckServerConfigBuilder.getUsername());
        assertNull(blackDuckServerConfigBuilder.getPassword());

        blackDuckServerConfigBuilder.setProperties(properties.entrySet());

        assertEquals("test url", blackDuckServerConfigBuilder.getUrl());
        assertEquals("user", blackDuckServerConfigBuilder.getUsername());
        assertEquals("password", blackDuckServerConfigBuilder.getPassword());
    }

    @Test
    public void testValidateBlackDuckURLEmpty() {
        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = new BlackDuckServerConfigBuilder();
        assertFalse(blackDuckServerConfigBuilder.isValid());
    }

    @Test
    public void testNullUrlInvalid() {
        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = new BlackDuckServerConfigBuilder();
        blackDuckServerConfigBuilder.setUrl((String) null);
        blackDuckServerConfigBuilder.setUsername("fakeUser");
        blackDuckServerConfigBuilder.setPassword("fakePassword");
        assertFalse(blackDuckServerConfigBuilder.isValid());
        try {
            blackDuckServerConfigBuilder.build();
            fail("Should have thrown an exception");
        } catch (Exception e) {
            assertTrue(e instanceof IllegalArgumentException);
        }
    }

    @Test
    public void testValidateBlackDuckURLMalformed() {
        String blackDuckUrl = "TestString";

        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = new BlackDuckServerConfigBuilder();
        blackDuckServerConfigBuilder.setUrl(blackDuckUrl);
        assertFalse(blackDuckServerConfigBuilder.isValid());
    }

    @Test
    public void testValidateBlackDuckURLMalformed2() {
        String blackDuckUrl = "http:TestString";

        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = new BlackDuckServerConfigBuilder();
        blackDuckServerConfigBuilder.setUrl(blackDuckUrl);
        assertFalse(blackDuckServerConfigBuilder.isValid());
    }

    @Test
    public void testValidConfig() {
        String blackDuckUrl = "http://this.might.exist/somewhere";

        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = new BlackDuckServerConfigBuilder();
        blackDuckServerConfigBuilder.setUrl(blackDuckUrl);
        blackDuckServerConfigBuilder.setApiToken("a valid, non-empty api token");
        assertTrue(blackDuckServerConfigBuilder.isValid());
    }

    @Test
    public void testValidConfigHttpUrl() throws IntegrationException {
        HttpUrl blackDuckUrl = new HttpUrl("http://this.might.exist/somewhere");

        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = new BlackDuckServerConfigBuilder();
        blackDuckServerConfigBuilder.setUrl(blackDuckUrl);
        blackDuckServerConfigBuilder.setApiToken("a valid, non-empty api token");
        assertTrue(blackDuckServerConfigBuilder.isValid());
    }

    @Test
    public void testValidConfigWithUsernameAndPassword() {
        String blackDuckUrl = "http://this.might.exist/somewhere";

        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = new BlackDuckServerConfigBuilder();
        blackDuckServerConfigBuilder.setUrl(blackDuckUrl);
        CredentialsBuilder credentialsBuilder = Credentials.newBuilder();
        credentialsBuilder.setUsernameAndPassword("a valid, non-blank username", "a valid, non-blank password");
        blackDuckServerConfigBuilder.setCredentials(credentialsBuilder.build());
        assertTrue(blackDuckServerConfigBuilder.isValid());
    }

    @Test
    public void testInvalidConfigWithBlankUsernameAndPassword() {
        String blackDuckUrl = "http://this.might.exist/somewhere";

        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = new BlackDuckServerConfigBuilder();
        blackDuckServerConfigBuilder.setUrl(blackDuckUrl);
        CredentialsBuilder credentialsBuilder = Credentials.newBuilder();
        credentialsBuilder.setUsernameAndPassword("", null);
        blackDuckServerConfigBuilder.setCredentials(credentialsBuilder.build());
        assertFalse(blackDuckServerConfigBuilder.isValid());
    }

    @Test
    public void testTimeout() {
        String blackDuckUrl = "http://this.might.exist/somewhere";

        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = new BlackDuckServerConfigBuilder();
        blackDuckServerConfigBuilder.setUrl(blackDuckUrl);
        blackDuckServerConfigBuilder.setTimeoutInSeconds(0);
        assertFalse(blackDuckServerConfigBuilder.isValid());
    }

    @Test
    public void testPopulatingExecutorService() throws Exception {
        ExecutorService executorService = null;
        try {
            executorService = Executors.newSingleThreadExecutor();

            BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = new BlackDuckServerConfigBuilder();
            blackDuckServerConfigBuilder.setUrl("http://this.might.exist/somewhere");
            blackDuckServerConfigBuilder.setApiToken("a valid, non-empty api token");
            BlackDuckServerConfig blackDuckServerConfig = blackDuckServerConfigBuilder.build();

            Field executorServiceField = BlackDuckServerConfig.class.getDeclaredField("executorService");
            executorServiceField.setAccessible(true);
            assertTrue(executorServiceField.get(blackDuckServerConfig) instanceof NoThreadExecutorService);

            assertNotNull(blackDuckServerConfigBuilder.getExecutorService());
            blackDuckServerConfigBuilder.setExecutorService(executorService);
            assertEquals(executorService, blackDuckServerConfigBuilder.getExecutorService().orElse(null));
            blackDuckServerConfig = blackDuckServerConfigBuilder.build();
            assertNotNull(executorServiceField.get(blackDuckServerConfig));
        } finally {
            assert executorService != null;
            executorService.shutdownNow();
        }
    }

    @Test
    public void testNewBuilder() {
        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = BlackDuckServerConfig.newBuilder();
        Set<BuilderPropertyKey> configBuilderKeys = blackDuckServerConfigBuilder.getKeys();
        assertTrue(configBuilderKeys.contains(BlackDuckServerConfigBuilder.API_TOKEN_KEY));
        assertTrue(configBuilderKeys.contains(BlackDuckServerConfigBuilder.USERNAME_KEY));
        assertTrue(configBuilderKeys.contains(BlackDuckServerConfigBuilder.PASSWORD_KEY));
    }

    @Test
    public void testNewApiBuilder() {
        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = BlackDuckServerConfig.newApiBuilder();
        Set<BuilderPropertyKey> configBuilderKeys = blackDuckServerConfigBuilder.getKeys();
        assertTrue(configBuilderKeys.contains(BlackDuckServerConfigBuilder.API_TOKEN_KEY));
        assertFalse(configBuilderKeys.contains(BlackDuckServerConfigBuilder.USERNAME_KEY));
        assertFalse(configBuilderKeys.contains(BlackDuckServerConfigBuilder.PASSWORD_KEY));

        assertNull(blackDuckServerConfigBuilder.getApiToken());
        blackDuckServerConfigBuilder.setApiToken(API_TOKEN);
        assertEquals(API_TOKEN, blackDuckServerConfigBuilder.getApiToken());
    }

    @Test
    public void testNewUserPassBuilder() {
        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = BlackDuckServerConfig.newUserPassBuilder();
        Set<BuilderPropertyKey> configBuilderKeys = blackDuckServerConfigBuilder.getKeys();
        assertFalse(configBuilderKeys.contains(BlackDuckServerConfigBuilder.API_TOKEN_KEY));
        assertTrue(configBuilderKeys.contains(BlackDuckServerConfigBuilder.USERNAME_KEY));
        assertTrue(configBuilderKeys.contains(BlackDuckServerConfigBuilder.PASSWORD_KEY));

        assertNull(blackDuckServerConfigBuilder.getUsername());
        assertNull(blackDuckServerConfigBuilder.getPassword());
        blackDuckServerConfigBuilder.setUsername(USERNAME);
        blackDuckServerConfigBuilder.setPassword(PASSWORD);
        assertEquals(USERNAME, blackDuckServerConfigBuilder.getUsername());
        assertEquals(PASSWORD, blackDuckServerConfigBuilder.getPassword());
    }

    @Test
    public void testGetPropertyKeys() {
        Set<BuilderPropertyKey> builderPropertyKeys = new HashSet<>();
        builderPropertyKeys.add(BlackDuckServerConfigBuilder.API_TOKEN_KEY);
        BuilderProperties apiTokenBuilderProperties = new BuilderProperties(builderPropertyKeys);
        String apiTokenPropertyKey = apiTokenBuilderProperties.getPropertyKeys().iterator().next();

        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = BlackDuckServerConfig.newApiBuilder();
        Set<String> configBuilderPropertyKeys = blackDuckServerConfigBuilder.getPropertyKeys();

        assertTrue(configBuilderPropertyKeys.contains(apiTokenPropertyKey));
    }

    @Test
    public void testGetEnvironmentVariableKeys() {
        Set<BuilderPropertyKey> builderPropertyKeys = new HashSet<>();
        builderPropertyKeys.add(BlackDuckServerConfigBuilder.API_TOKEN_KEY);
        BuilderProperties apiTokenBuilderProperties = new BuilderProperties(builderPropertyKeys);
        String apiTokenEnvVarKey = apiTokenBuilderProperties.getEnvironmentVariableKeys().iterator().next();

        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = BlackDuckServerConfig.newApiBuilder();
        Set<String> configBuilderPropertyKeys = blackDuckServerConfigBuilder.getEnvironmentVariableKeys();

        assertTrue(configBuilderPropertyKeys.contains(apiTokenEnvVarKey));
    }

    @Test
    public void testGetProperties() {
        Set<BuilderPropertyKey> builderPropertyKeys = new HashSet<>();
        builderPropertyKeys.add(BlackDuckServerConfigBuilder.USERNAME_KEY);
        builderPropertyKeys.add(BlackDuckServerConfigBuilder.PASSWORD_KEY);
        BuilderProperties userBuilderProperties = new BuilderProperties(builderPropertyKeys);

        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = BlackDuckServerConfig.newUserPassBuilder();
        Map<BuilderPropertyKey, String> configBuilderPropertyKeys = blackDuckServerConfigBuilder.getProperties();

        for (Map.Entry<BuilderPropertyKey, String> entry : userBuilderProperties.getProperties().entrySet()) {
            assertTrue(configBuilderPropertyKeys.containsKey(entry.getKey()));
        }
    }

    @Test
    public void testSetProperties() {
        BuilderPropertyKey builderPropertyKey = new BuilderPropertyKey(KEY);
        Map<String, String> properties = new HashMap<>();
        properties.put(KEY, VALUE);

        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = BlackDuckServerConfig.newUserPassBuilder();

        blackDuckServerConfigBuilder.setProperties(properties.entrySet());
        assertTrue(blackDuckServerConfigBuilder.getProperties().containsKey(builderPropertyKey));
        assertEquals(VALUE, blackDuckServerConfigBuilder.getProperties().get(builderPropertyKey));
    }

    @Test
    public void testSetProperty() {
        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = BlackDuckServerConfig.newUserPassBuilder();

        BuilderPropertyKey builderPropertyKey = new BuilderPropertyKey(KEY);
        blackDuckServerConfigBuilder.setProperty(builderPropertyKey.getKey(), VALUE);
        assertTrue(blackDuckServerConfigBuilder.getProperties().containsKey(builderPropertyKey));
        assertEquals(VALUE, blackDuckServerConfigBuilder.getProperties().get(builderPropertyKey));
    }

    @Test
    public void testBuildWithoutValidationNoValues() {
        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = BlackDuckServerConfig.newApiBuilder();
        blackDuckServerConfigBuilder.setUrl("");

        assertTrue(StringUtils.isBlank(blackDuckServerConfigBuilder.getUrl()));
        assertNull(blackDuckServerConfigBuilder.getSolutionDetails().getName());
        assertNull(blackDuckServerConfigBuilder.getSolutionDetails().getVersion());
        assertNull(blackDuckServerConfigBuilder.getUsername());
        assertNull(blackDuckServerConfigBuilder.getPassword());
        assertNull(blackDuckServerConfigBuilder.getApiToken());
        assertTrue(blackDuckServerConfigBuilder.getProxyInfo().isBlank());
        assertEquals(BlackDuckServerConfigBuilder.DEFAULT_TIMEOUT_SECONDS, blackDuckServerConfigBuilder.getTimemoutInSeconds());
        assertFalse(blackDuckServerConfigBuilder.isTrustCert());

        BlackDuckServerConfig blackDuckServerConfig = blackDuckServerConfigBuilder.buildWithoutValidation();

        assertNull(blackDuckServerConfig.getBlackDuckUrl());
        assertNull(blackDuckServerConfig.getSolutionDetails().getName());
        assertNull(blackDuckServerConfig.getSolutionDetails().getVersion());
        assertEquals(Optional.empty(), blackDuckServerConfig.getCredentials().flatMap(Credentials::getUsername));
        assertEquals(Optional.empty(), blackDuckServerConfig.getCredentials().flatMap(Credentials::getPassword));
        assertEquals(Optional.empty(), blackDuckServerConfig.getApiToken());
        assertTrue(blackDuckServerConfig.getProxyInfo().isBlank());
        assertEquals(BlackDuckServerConfigBuilder.DEFAULT_TIMEOUT_SECONDS, blackDuckServerConfig.getTimeout());
        assertFalse(blackDuckServerConfig.isAlwaysTrustServerCertificate());
    }

    @Test
    public void testBuildWithoutValidationApiToken() {
        ProxyInfo proxyInfo = getProxyInfo();

        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = BlackDuckServerConfig.newApiBuilder();

        blackDuckServerConfigBuilder.setApiToken(API_TOKEN)
            .setUrl(GOOD_URL)
            .setSolutionDetails(NAME_VERSION_NAME, NAME_VERSION_VERSION)
            .setProxyInfo(proxyInfo)
            .setTimeoutInSeconds(TIMEOUT)
            .setTrustCert(true);

        assertEquals(API_TOKEN, blackDuckServerConfigBuilder.getApiToken());
        assertEquals(GOOD_URL, blackDuckServerConfigBuilder.getUrl());
        assertEquals(NAME_VERSION_NAME, blackDuckServerConfigBuilder.getSolutionDetails().getName());
        assertEquals(NAME_VERSION_VERSION, blackDuckServerConfigBuilder.getSolutionDetails().getVersion());
        assertNull(blackDuckServerConfigBuilder.getUsername());
        assertNull(blackDuckServerConfigBuilder.getPassword());
        assertEquals(proxyInfo, blackDuckServerConfigBuilder.getProxyInfo());
        assertEquals(TIMEOUT, blackDuckServerConfigBuilder.getTimemoutInSeconds());
        assertTrue(blackDuckServerConfigBuilder.isTrustCert());

        BlackDuckServerConfig blackDuckServerConfig = blackDuckServerConfigBuilder.buildWithoutValidation();

        assertTrue(blackDuckServerConfig.usingApiToken());
        assertEquals(GOOD_URL, blackDuckServerConfig.getBlackDuckUrl().string());
        assertEquals(NAME_VERSION_NAME, blackDuckServerConfig.getSolutionDetails().getName());
        assertEquals(NAME_VERSION_VERSION, blackDuckServerConfig.getSolutionDetails().getVersion());
        assertEquals(Optional.empty(), blackDuckServerConfig.getCredentials());
        assertEquals(API_TOKEN, blackDuckServerConfig.getApiToken().orElse(null));
        assertEquals(proxyInfo, blackDuckServerConfig.getProxyInfo());
        assertEquals(TIMEOUT, blackDuckServerConfig.getTimeout());
        assertTrue(blackDuckServerConfig.isAlwaysTrustServerCertificate());
    }

    @Test
    public void testBuildWithoutValidationUserPass() throws IntegrationException {
        CredentialsBuilder credentialsBuilder = Credentials.newBuilder();
        credentialsBuilder.setUsernameAndPassword(USERNAME, PASSWORD);
        Credentials credentials = credentialsBuilder.build();
        NameVersion nameVersion = new NameVersion(NAME_VERSION_NAME, NAME_VERSION_VERSION);
        HttpUrl httpUrl = new HttpUrl(GOOD_URL);
        ProxyInfo proxyInfo = getProxyInfo();

        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = BlackDuckServerConfig.newUserPassBuilder();

        blackDuckServerConfigBuilder.setCredentials(credentials)
            .setSolutionDetails(nameVersion)
            .setUrl(httpUrl)
            .setProxyInfo(proxyInfo)
            .setTimeoutInSeconds(TIMEOUT)
            .setTrustCert("false");

        assertNull(blackDuckServerConfigBuilder.getApiToken());
        assertEquals(GOOD_URL, blackDuckServerConfigBuilder.getUrl());
        assertEquals(NAME_VERSION_NAME, blackDuckServerConfigBuilder.getSolutionDetails().getName());
        assertEquals(NAME_VERSION_VERSION, blackDuckServerConfigBuilder.getSolutionDetails().getVersion());
        assertEquals(USERNAME, blackDuckServerConfigBuilder.getUsername());
        assertEquals(PASSWORD, blackDuckServerConfigBuilder.getPassword());
        assertEquals(proxyInfo, blackDuckServerConfigBuilder.getProxyInfo());
        assertEquals(TIMEOUT, blackDuckServerConfigBuilder.getTimemoutInSeconds());
        assertFalse(blackDuckServerConfigBuilder.isTrustCert());

        BlackDuckServerConfig blackDuckServerConfig = blackDuckServerConfigBuilder.buildWithoutValidation();

        assertFalse(blackDuckServerConfig.usingApiToken());
        assertEquals(GOOD_URL, blackDuckServerConfig.getBlackDuckUrl().string());
        assertEquals(NAME_VERSION_NAME, blackDuckServerConfig.getSolutionDetails().getName());
        assertEquals(NAME_VERSION_VERSION, blackDuckServerConfig.getSolutionDetails().getVersion());
        assertEquals(USERNAME, blackDuckServerConfig.getCredentials().flatMap(Credentials::getUsername).orElse(null));
        assertEquals(PASSWORD, blackDuckServerConfig.getCredentials().flatMap(Credentials::getPassword).orElse(null));
        assertNull(blackDuckServerConfig.getApiToken().orElse(null));
        assertEquals(proxyInfo, blackDuckServerConfig.getProxyInfo());
        assertEquals(TIMEOUT, blackDuckServerConfig.getTimeout());
        assertFalse(blackDuckServerConfig.isAlwaysTrustServerCertificate());
    }

    @Test
    public void testNullCheckSetters() {
        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = BlackDuckServerConfig.newApiBuilder();

        assertNotNull(blackDuckServerConfigBuilder.getLogger());
        assertNotNull(blackDuckServerConfigBuilder.getAuthenticationSupport());
        assertNotNull(blackDuckServerConfigBuilder.getCookieHeaderParser());
        assertNotNull(blackDuckServerConfigBuilder.getGson());
        assertNotNull(blackDuckServerConfigBuilder.getIntEnvironmentVariables());
        assertNotNull(blackDuckServerConfigBuilder.getObjectMapper());

        blackDuckServerConfigBuilder.setLogger(null)
            .setAuthenticationSupport(null)
            .setCookieHeaderParser(null)
            .setGson(null)
            .setIntEnvironmentVariables(null)
            .setObjectMapper(null);

        assertNotNull(blackDuckServerConfigBuilder.getLogger());
        assertNotNull(blackDuckServerConfigBuilder.getAuthenticationSupport());
        assertNotNull(blackDuckServerConfigBuilder.getCookieHeaderParser());
        assertNotNull(blackDuckServerConfigBuilder.getGson());
        assertNotNull(blackDuckServerConfigBuilder.getIntEnvironmentVariables());
        assertNotNull(blackDuckServerConfigBuilder.getObjectMapper());

        IntLogger logger = new PrintStreamIntLogger(System.out, LogLevel.DEBUG);
        AuthenticationSupport authenticationSupport = new AuthenticationSupport();
        CookieHeaderParser cookieHeaderParser = new CookieHeaderParser();
        Gson gson = BlackDuckServicesFactory.createDefaultGson();
        IntEnvironmentVariables intEnvironmentVariables = IntEnvironmentVariables.empty();
        ObjectMapper objectMapper = BlackDuckServicesFactory.createDefaultObjectMapper();

        blackDuckServerConfigBuilder.setLogger(logger)
            .setAuthenticationSupport(authenticationSupport)
            .setCookieHeaderParser(cookieHeaderParser)
            .setGson(gson)
            .setIntEnvironmentVariables(intEnvironmentVariables)
            .setObjectMapper(objectMapper);

        assertEquals(logger, blackDuckServerConfigBuilder.getLogger());
        assertEquals(authenticationSupport, blackDuckServerConfigBuilder.getAuthenticationSupport());
        assertEquals(cookieHeaderParser, blackDuckServerConfigBuilder.getCookieHeaderParser());
        assertEquals(gson, blackDuckServerConfigBuilder.getGson());
        assertEquals(intEnvironmentVariables, blackDuckServerConfigBuilder.getIntEnvironmentVariables());
        assertEquals(objectMapper, blackDuckServerConfigBuilder.getObjectMapper());
    }

    @Test
    public void testProxyInfoMethods() {
        ProxyInfo proxyInfo = getProxyInfo();

        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = BlackDuckServerConfig.newApiBuilder();
        blackDuckServerConfigBuilder.setProxyInfo(proxyInfo);

        assertEquals(proxyInfo, blackDuckServerConfigBuilder.getProxyInfo());
        assertEquals(proxyInfo.getHost().orElse(null), blackDuckServerConfigBuilder.getProxyHost());
        assertEquals(proxyInfo.getPort(), blackDuckServerConfigBuilder.getProxyPort());
        assertEquals(proxyInfo.getUsername().orElse(null), blackDuckServerConfigBuilder.getProxyUsername());
        assertEquals(proxyInfo.getPassword().orElse(null), blackDuckServerConfigBuilder.getProxyPassword());
        assertEquals(proxyInfo.getNtlmDomain().orElse(null), blackDuckServerConfigBuilder.getProxyNtlmDomain());
        assertEquals(proxyInfo.getNtlmWorkstation().orElse(null), blackDuckServerConfigBuilder.getProxyNtlmWorkstation());

    }

    @Test
    public void testValidateBlankBDUrl() {
        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = BlackDuckServerConfig.newApiBuilder();
        blackDuckServerConfigBuilder.setApiToken(API_TOKEN);

        BuilderStatus builderStatus = new BuilderStatus();
        blackDuckServerConfigBuilder.validate(builderStatus);

        assertEquals("The Black Duck url must be specified.", builderStatus.getFullErrorMessage());
        assertFalse(builderStatus.isValid());
    }

    @Test
    public void testValidateBadBDCredentials() {
        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = BlackDuckServerConfig.newApiBuilder();
        blackDuckServerConfigBuilder.setUsername(USERNAME)
            .setUrl(GOOD_URL);

        BuilderStatus builderStatus = new BuilderStatus();
        blackDuckServerConfigBuilder.validate(builderStatus);

        assertEquals("The username and password must both be populated or both be empty.", builderStatus.getFullErrorMessage());
        assertFalse(builderStatus.isValid());
    }

    @Test
    public void testValidateBlankApiToken() {
        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = BlackDuckServerConfig.newApiBuilder();
        blackDuckServerConfigBuilder.setUrl(GOOD_URL);

        BuilderStatus builderStatus = new BuilderStatus();
        blackDuckServerConfigBuilder.validate(builderStatus);

        assertTrue(builderStatus.getFullErrorMessage().contains("An API token must be specified."));
        assertFalse(builderStatus.isValid());
    }

    @Test
    public void testValidateBadProxyCredentials() {
        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = BlackDuckServerConfig.newApiBuilder();
        blackDuckServerConfigBuilder.setProxyUsername(USERNAME)
            .setApiToken(API_TOKEN)
            .setUrl(GOOD_URL);

        BuilderStatus builderStatus = new BuilderStatus();
        blackDuckServerConfigBuilder.validate(builderStatus);

        assertTrue(builderStatus.getFullErrorMessage().contains("The proxy credentials were not valid."));
        assertFalse(builderStatus.isValid());
    }

    @Test
    public void testValidateBlankProxyHost() {
        BlackDuckServerConfigBuilder blackDuckServerConfigBuilder = BlackDuckServerConfig.newApiBuilder();
        blackDuckServerConfigBuilder.setProxyUsername(USERNAME)
            .setProxyPassword(PASSWORD)
            .setApiToken(API_TOKEN)
            .setUrl(GOOD_URL);

        BuilderStatus builderStatus = new BuilderStatus();
        blackDuckServerConfigBuilder.validate(builderStatus);

        assertTrue(builderStatus.getFullErrorMessage().contains("The proxy host must be specified and the port must be greater than zero."));
        assertFalse(builderStatus.isValid());
    }

    private ProxyInfo getProxyInfo() {
        ProxyInfoBuilder proxyInfoBuilder = ProxyInfo.newBuilder();
        proxyInfoBuilder.setHost("proxy-host");
        proxyInfoBuilder.setPort(1234);
        proxyInfoBuilder.setNtlmDomain("proxy-ntlmDomain");
        proxyInfoBuilder.setNtlmWorkstation("proxy-ntlmWorkstation");

        CredentialsBuilder credentialsBuilder = Credentials.newBuilder();
        credentialsBuilder.setUsernameAndPassword("proxy-username", "proxy-password");
        proxyInfoBuilder.setCredentials(credentialsBuilder.build());

        return proxyInfoBuilder.build();
    }
}
