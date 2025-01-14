/*
 * blackduck-common
 *
 * Copyright (c) 2023 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.service.dataservice;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import com.synopsys.integration.blackduck.api.core.response.UrlMultipleResponses;
import com.synopsys.integration.blackduck.api.generated.discovery.ApiDiscovery;
import com.synopsys.integration.blackduck.api.generated.view.ProjectVersionView;
import com.synopsys.integration.blackduck.api.manual.view.ProjectView;
import com.synopsys.integration.blackduck.http.BlackDuckQuery;
import com.synopsys.integration.blackduck.http.BlackDuckRequestBuilder;
import com.synopsys.integration.blackduck.service.BlackDuckApiClient;
import com.synopsys.integration.blackduck.service.DataService;
import com.synopsys.integration.blackduck.service.request.BlackDuckMultipleRequest;
import com.synopsys.integration.exception.IntegrationException;
import com.synopsys.integration.log.IntLogger;

public class ProjectGetService extends DataService {
    private final UrlMultipleResponses<ProjectView> projectsResponses = apiDiscovery.metaMultipleResponses(ApiDiscovery.PROJECTS_PATH);

    public ProjectGetService(BlackDuckApiClient blackDuckApiClient, ApiDiscovery apiDiscovery, IntLogger logger) {
        super(blackDuckApiClient, apiDiscovery, logger);
    }

    public List<ProjectView> getAllProjectMatches(String projectName) throws IntegrationException {
        BlackDuckQuery blackDuckQuery = new BlackDuckQuery("name", projectName);
        BlackDuckRequestBuilder blackDuckRequestBuilder = new BlackDuckRequestBuilder()
                                                              .commonGet()
                                                              .addBlackDuckQuery(blackDuckQuery);
        BlackDuckMultipleRequest<ProjectView> requestMultiple = blackDuckRequestBuilder.buildBlackDuckRequest(projectsResponses);

        return blackDuckApiClient.getAllResponses(requestMultiple);
    }

    public List<ProjectView> getProjectMatches(String projectName, int limit) throws IntegrationException {
        BlackDuckQuery blackDuckQuery = new BlackDuckQuery("name", projectName);
        BlackDuckRequestBuilder blackDuckRequestBuilder = new BlackDuckRequestBuilder()
                                                              .commonGet()
                                                              .addBlackDuckQuery(blackDuckQuery);
        BlackDuckMultipleRequest<ProjectView> requestMultiple = blackDuckRequestBuilder.buildBlackDuckRequest(projectsResponses);

        return blackDuckApiClient.getSomeResponses(requestMultiple, limit);
    }

    public Optional<ProjectView> getProjectViewByProjectName(String projectName) throws IntegrationException {
        List<ProjectView> allProjectItems = getAllProjectMatches(projectName);
        for (ProjectView project : allProjectItems) {
            if (projectName.equalsIgnoreCase(project.getName())) {
                return Optional.of(project);
            }
        }

        return Optional.empty();
    }

    public Optional<ProjectVersionView> getProjectVersionViewByProjectVersionName(ProjectView projectView, String projectVersionName) throws IntegrationException {
        BlackDuckQuery blackDuckQuery = new BlackDuckQuery("versionName", projectVersionName);
        BlackDuckRequestBuilder blackDuckRequestBuilder = new BlackDuckRequestBuilder()
                                                              .commonGet()
                                                              .addBlackDuckQuery(blackDuckQuery);

        BlackDuckMultipleRequest<ProjectVersionView> requestMultiple = blackDuckRequestBuilder.buildBlackDuckRequest(projectView.metaVersionsLink());
        Predicate<ProjectVersionView> predicate = projectVersionView -> projectVersionName.equals(projectVersionView.getVersionName());

        return blackDuckApiClient.getSomeMatchingResponses(requestMultiple, predicate, 1)
                   .stream()
                   .findFirst();
    }

}
