/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.tomitribe.jamira.cli;

import com.atlassian.jira.rest.client.api.ComponentRestClient;
import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.VersionRestClient;
import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.atlassian.jira.rest.client.api.domain.Component;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.Project;
import com.atlassian.jira.rest.client.api.domain.Version;
import com.atlassian.jira.rest.client.api.domain.input.ComponentInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import com.atlassian.jira.rest.client.api.domain.input.VersionInputBuilder;
import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.api.Default;
import org.tomitribe.crest.api.In;
import org.tomitribe.crest.api.Option;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Command("create")
public class CreateCommand {

    /**
     * Creates a new version (which logically belongs to a project)
     *
     * @param projectKey Text key for the JIRA project. Example `TOMEE`
     * @param versionName Version string.  Example `9.0.0-M7`
     * @param description  Long description of the version
     * @param released True if this version has been released
     * @param archived True if this version is archived
     * @param account Shortname of the JIRA server
     */
    @Command
    public String version(final ProjectKey projectKey, final String versionName,
                          @Option("description") final String description,
                          @Option("released") final Boolean released,
                          @Option("archived") final Boolean archived,
                          @Option("account") @Default("default") final Account account) throws ExecutionException, InterruptedException {
        final Client client = account.getClient();
        final VersionRestClient versionRestClient = client.getVersionRestClient();
        final VersionInputBuilder versionInputBuilder = new VersionInputBuilder(projectKey.getKey());
        versionInputBuilder.setName(versionName);
        if (description != null) versionInputBuilder.setDescription(description);
        if (released != null) versionInputBuilder.setReleased(released);
        if (archived != null) versionInputBuilder.setArchived(archived);
        final Version version = versionRestClient.createVersion(versionInputBuilder.build()).get();
        return String.format("%s %s", version.getId(), version.getName());
    }

    /**
     * Create a component in the respective JIRA project
     *
     * @param projectKey Text key for the JIRA project. Example `TOMEE`
     * @param componentName Display name of the component
     * @param description   Long description of the component
     * @param leadUsername  Username of the component lead
     * @param account Shortname of the JIRA server
     */
    @Command
    public String component(final ProjectKey projectKey, final String componentName,
                            @Option("description") final String description,
                            @Option("lead-username") final String leadUsername,
                            @Option("account") @Default("default") final Account account) throws ExecutionException, InterruptedException {

        final Client client = account.getClient();
        final ComponentRestClient componentClient = client.getComponentClient();
        final ComponentInput componentInput = new ComponentInput(componentName, description, leadUsername, null);
        final Component component = componentClient.createComponent(projectKey.getKey(), componentInput).get();
        return String.format("%s %s", component.getId(), component.getName());
    }

    //CHECKSTYLE:OFF

    /**
     * Create a subtask for the specified JIRA issue
     *
     * @param parentKey The issue key for the parent issue. Example TOMEE-123
     * @param summary  The title of the subtask
     * @param descriptionArg Long description of the issue.  Can also be piped to the command via stdin.
     * @param assignee Username of the person to which the issue should be assigned
     * @param reporter Username of the person who is the reporter of the issue
     * @param priority The name of the priority for the issue.  See the `list priorities` command
     * @param affectedVersions  The names of the versions affected by the issue.  See the `list versions` command.  Flag may be repeated.
     * @param fixVersions  The names of the versions affected by the issue.  See the `list versions` command.  Flag may be repeated.
     * @param components  The component names relating to the issue.  See the `list components` command.  Flag may be repeated.
     * @param account Shortname of the JIRA server
     */
    @Command("subtask")
    public String subtask(final IssueKey parentKey,
                          final String summary,
                          @Option("description") String descriptionArg,
                          @Option("assignee") final String assignee,
                          @Option("reporter") final String reporter,
                          @Option("priority") final String priority,
                          @Option("affected-version") final List<String> affectedVersions,
                          @Option("fix-version") final List<String> fixVersions,
                          @Option("component") final List<String> components,
                          @In final InputStream pipedInput,
                          @Option("account") @Default("default") final Account account) throws Exception {

        final Client client = account.getClient();

        final Issue parent = client.getIssueClient().getIssue(parentKey.getKey()).get();
        final IssueType type = client.getIssueType("sub-task");

        final String description = Input.read(pipedInput, descriptionArg);

        final IssueInputBuilder issue = new IssueInputBuilder(parent.getProject(), type, summary);
        if (affectedVersions != null) issue.setAffectedVersionsNames(affectedVersions);
        if (fixVersions != null) issue.setFixVersionsNames(fixVersions);
        if (components != null) issue.setComponentsNames(components);
        if (assignee != null) issue.setAssigneeName(assignee);
        if (reporter != null) issue.setReporterName(reporter);
        if (description != null) issue.setDescription(description);
        if (priority != null) issue.setPriority(client.getPriority(priority));

        issue.setFieldValue("parent", parent);

        final BasicIssue createdIssue = client.getIssueClient().createIssue(issue.build()).get();

        return createdIssue.getKey().trim();
    }


    /**
     * Create a JIRA issue of type task
     * @param projectKey Text key for the JIRA project. Example `TOMEE`
     * @param summary Text to use as the title of the issue
     * @param description Long description of the issue.  Can also be piped to the command via stdin.
     * @param assignee Username of the person to which the issue should be assigned
     * @param reporter Username of the person who is the reporter of the issue
     * @param priority The name of the priority for the issue.  See the `list priorities` command
     * @param affectedVersions  The names of the versions affected by the issue.  See the `list versions` command.  Flag may be repeated.
     * @param fixVersions  The names of the versions affected by the issue.  See the `list versions` command.  Flag may be repeated.
     * @param components  The component names relating to the issue.  See the `list components` command.  Flag may be repeated.
     * @param account Shortname of the JIRA server
     */
    @Command("task")
    public String task(final ProjectKey projectKey,
                       final String summary,
                       @Option("description") final String description,
                       @Option("assignee") final String assignee,
                       @Option("reporter") final String reporter,
                       @Option("priority") final String priority,
                       @Option("affected-version") final List<String> affectedVersions,
                       @Option("fix-version") final List<String> fixVersions,
                       @Option("component") final List<String> components,
                       @In final InputStream pipedInput,
                       @Option("account") @Default("default") final Account account) throws Exception {

        return createIssue("task", projectKey, summary, description, assignee,
                reporter, priority, affectedVersions, fixVersions,
                components, pipedInput, account);
    }

    /**
     * Create a JIRA issue of type improvement
     * @param projectKey Text key for the JIRA project. Example `TOMEE`
     * @param summary Text to use as the title of the issue
     * @param description Long description of the issue.  Can also be piped to the command via stdin.
     * @param assignee Username of the person to which the issue should be assigned
     * @param reporter Username of the person who is the reporter of the issue
     * @param priority The name of the priority for the issue.  See the `list priorities` command
     * @param affectedVersions  The names of the versions affected by the issue.  See the `list versions` command.  Flag may be repeated.
     * @param fixVersions  The names of the versions affected by the issue.  See the `list versions` command.  Flag may be repeated.
     * @param components  The component names relating to the issue.  See the `list components` command.  Flag may be repeated.
     * @param account Shortname of the JIRA server
     */
    @Command("improvement")
    public String improvement(final ProjectKey projectKey,
                              final String summary,
                              @Option("description") final String description,
                              @Option("assignee") final String assignee,
                              @Option("reporter") final String reporter,
                              @Option("priority") final String priority,
                              @Option("affected-version") final List<String> affectedVersions,
                              @Option("fix-version") final List<String> fixVersions,
                              @Option("component") final List<String> components,
                              @In final InputStream pipedInput,
                              @Option("account") @Default("default") final Account account) throws Exception {

        return createIssue("improvement", projectKey, summary, description, assignee,
                reporter, priority, affectedVersions, fixVersions,
                components, pipedInput, account);
    }

    /**
     * Create a JIRA issue of type wish
     * @param projectKey Text key for the JIRA project. Example `TOMEE`
     * @param summary Text to use as the title of the issue
     * @param description Long description of the issue.  Can also be piped to the command via stdin.
     * @param assignee Username of the person to which the issue should be assigned
     * @param reporter Username of the person who is the reporter of the issue
     * @param priority The name of the priority for the issue.  See the `list priorities` command
     * @param affectedVersions  The names of the versions affected by the issue.  See the `list versions` command.  Flag may be repeated.
     * @param fixVersions  The names of the versions affected by the issue.  See the `list versions` command.  Flag may be repeated.
     * @param components  The component names relating to the issue.  See the `list components` command.  Flag may be repeated.
     * @param account Shortname of the JIRA server
     */
    @Command("wish")
    public String wish(final ProjectKey projectKey,
                       final String summary,
                       @Option("description") final String description,
                       @Option("assignee") final String assignee,
                       @Option("reporter") final String reporter,
                       @Option("priority") final String priority,
                       @Option("affected-version") final List<String> affectedVersions,
                       @Option("fix-version") final List<String> fixVersions,
                       @Option("component") final List<String> components,
                       @In final InputStream pipedInput,
                       @Option("account") @Default("default") final Account account) throws Exception {

        return createIssue("wish", projectKey, summary, description, assignee,
                reporter, priority, affectedVersions, fixVersions,
                components, pipedInput, account);
    }

    /**
     * Create a JIRA issue of type bug
     * @param projectKey Text key for the JIRA project. Example `TOMEE`
     * @param summary Text to use as the title of the issue
     * @param description Long description of the issue.  Can also be piped to the command via stdin.
     * @param assignee Username of the person to which the issue should be assigned
     * @param reporter Username of the person who is the reporter of the issue
     * @param priority The name of the priority for the issue.  See the `list priorities` command
     * @param affectedVersions  The names of the versions affected by the issue.  See the `list versions` command.  Flag may be repeated.
     * @param fixVersions  The names of the versions affected by the issue.  See the `list versions` command.  Flag may be repeated.
     * @param components  The component names relating to the issue.  See the `list components` command.  Flag may be repeated.
     * @param account Shortname of the JIRA server
     */
    @Command("bug")
    public String bug(final ProjectKey projectKey,
                      final String summary,
                      @Option("description") final String description,
                      @Option("assignee") final String assignee,
                      @Option("reporter") final String reporter,
                      @Option("priority") final String priority,
                      @Option("affected-version") final List<String> affectedVersions,
                      @Option("fix-version") final List<String> fixVersions,
                      @Option("component") final List<String> components,
                      @In final InputStream pipedInput,
                      @Option("account") @Default("default") final Account account) throws Exception {

        return createIssue("bug", projectKey, summary, description, assignee,
                reporter, priority, affectedVersions, fixVersions,
                components, pipedInput, account);
    }

    /**
     * Create a JIRA issue of the specified type
     * @param projectKey Text key for the JIRA project. Example `TOMEE`
     * @param summary Text to use as the title of the issue
     * @param type The name of the issue type.  See the `list issue-types` command
     * @param description Long description of the issue.  Can also be piped to the command via stdin.
     * @param assignee Username of the person to which the issue should be assigned
     * @param reporter Username of the person who is the reporter of the issue
     * @param priority The name of the priority for the issue.  See the `list priorities` command
     * @param affectedVersions  The names of the versions affected by the issue.  See the `list versions` command.  Flag may be repeated.
     * @param fixVersions  The names of the versions affected by the issue.  See the `list versions` command.  Flag may be repeated.
     * @param components  The component names relating to the issue.  See the `list components` command.  Flag may be repeated.
     * @param account Shortname of the JIRA server
     */
    @Command("issue")
    public String issue(final ProjectKey projectKey,
                        final String summary,
                        @Option("type") @Default("bug") final String type,
                        @Option("description") final String description,
                        @Option("assignee") final String assignee,
                        @Option("reporter") final String reporter,
                        @Option("priority") final String priority,
                        @Option("affected-version") final List<String> affectedVersions,
                        @Option("fix-version") final List<String> fixVersions,
                        @Option("component") final List<String> components,
                        @In final InputStream pipedInput,
                        @Option("account") @Default("default") final Account account) throws Exception {

        return createIssue(type, projectKey, summary, description, assignee,
                reporter, priority, affectedVersions, fixVersions,
                components, pipedInput, account);
    }

    /**
     * Create a JIRA issue of type bug
     * @param projectKey Text key for the JIRA project. Example `TOMEE`
     * @param summary Text to use as the title of the issue
     * @param description Long description of the issue.  Can also be piped to the command via stdin.
     * @param assignee Username of the person to which the issue should be assigned
     * @param reporter Username of the person who is the reporter of the issue
     * @param priority The name of the priority for the issue.  See the `list priorities` command
     * @param affectedVersions  The names of the versions affected by the issue.  See the `list versions` command.  Flag may be repeated.
     * @param fixVersions  The names of the versions affected by the issue.  See the `list versions` command.  Flag may be repeated.
     * @param components  The component names relating to the issue.  See the `list components` command.  Flag may be repeated.
     * @param account Shortname of the JIRA server
     */
    @Command("new-feature")
    public String newFeature(final ProjectKey projectKey,
                             final String summary,
                             @Option("description") final String description,
                             @Option("assignee") final String assignee,
                             @Option("reporter") final String reporter,
                             @Option("priority") final String priority,
                             @Option("affected-version") final List<String> affectedVersions,
                             @Option("fix-version") final List<String> fixVersions,
                             @Option("component") final List<String> components,
                             @In final InputStream pipedInput,
                             @Option("account") @Default("default") final Account account) throws Exception {

        return createIssue("new feature", projectKey, summary, description, assignee,
                reporter, priority, affectedVersions, fixVersions,
                components, pipedInput, account);
    }

    private static String createIssue(final String typeName, final ProjectKey projectKey,
                                      final String summary, final String descriptionArg,
                                      final String assignee, final String reporter,
                                      final String priority, final List<String> affectedVersions,
                                      final List<String> fixVersions, final List<String> components,
                                      final InputStream pipedInput,
                                      final Account account) throws InterruptedException, java.util.concurrent.ExecutionException, IOException {
        final Client client = account.getClient();
        final Project project = client.getProjectClient().getProject(projectKey.getKey()).get();
        final IssueType type = client.getIssueType(typeName);

        final String description = Input.read(pipedInput, descriptionArg);

        final IssueInputBuilder issue = new IssueInputBuilder(project, type, summary);

        if (affectedVersions != null) issue.setAffectedVersionsNames(affectedVersions);
        if (fixVersions != null) issue.setFixVersionsNames(fixVersions);
        if (components != null) issue.setComponentsNames(components);
        if (assignee != null) issue.setAssigneeName(assignee);
        if (reporter != null) issue.setReporterName(reporter);
        if (description != null) issue.setDescription(description);
        if (priority != null) issue.setPriority(client.getPriority(priority));

        final IssueRestClient issueClient = client.getIssueClient();
        final BasicIssue createdIssue = issueClient.createIssue(issue.build()).get();

        return createdIssue.getKey();
    }
    //CHECKSTYLE:ON

}
