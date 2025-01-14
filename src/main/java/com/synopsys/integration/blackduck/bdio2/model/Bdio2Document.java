/*
 * blackduck-common
 *
 * Copyright (c) 2023 Synopsys, Inc.
 *
 * Use subject to the terms and conditions of the Synopsys End User Software License and Maintenance Agreement. All rights reserved worldwide.
 */
package com.synopsys.integration.blackduck.bdio2.model;

import java.util.List;

import com.blackducksoftware.bdio2.BdioMetadata;
import com.blackducksoftware.bdio2.model.Component;
import com.blackducksoftware.bdio2.model.Project;

public class Bdio2Document {
    private final BdioMetadata bdioMetadata;
    private final Project project;
    private final List<Project> subProjects;
    private final List<Component> components;

    public Bdio2Document(BdioMetadata bdioMetadata, Project project, List<Project> subProjects, List<Component> components) {
        this.bdioMetadata = bdioMetadata;
        this.project = project;
        this.subProjects = subProjects;
        this.components = components;
    }

    public BdioMetadata getBdioMetadata() {
        return bdioMetadata;
    }

    public Project getProject() {
        return project;
    }

    public List<Project> getSubProjects() {
        return subProjects;
    }

    public List<Component> getComponents() {
        return components;
    }
}
