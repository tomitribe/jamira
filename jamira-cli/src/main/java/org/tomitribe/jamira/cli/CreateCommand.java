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

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.Project;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.api.Default;
import org.tomitribe.crest.api.Option;

import java.util.List;

@Command("create")
public class CreateCommand {


    @Command("subtask")
    public String createSubtask(final IssueKey parentKey,
                                final String summary,
                                @Option("account") @Default("default") final Account account) throws Exception {

        final Client client = account.getClient();

        final Issue parent = client.getIssueClient().getIssue(parentKey.getKey()).get();
        final IssueType type = client.getIssueType("sub-task");

        final IssueInputBuilder issue = new IssueInputBuilder(parent.getProject(), type, summary);
        issue.setFieldValue("parent", parent);

        final BasicIssue createdIssue = client.getIssueClient().createIssue(issue.build()).get();

        return createdIssue.getKey();
    }

    //CHECKSTYLE:OFF
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
                       @Option("account") @Default("default") final Account account) throws Exception {

        return createIssue("task", projectKey, summary, description, assignee,
                reporter, priority, affectedVersions, fixVersions,
                components, account);
    }

    private static String createIssue(final String typeName, final ProjectKey projectKey,
                                      final String summary, final String description,
                                      final String assignee, final String reporter,
                                      final String priority, final List<String> affectedVersions,
                                      final List<String> fixVersions, final List<String> components,
                                      final Account account) throws InterruptedException, java.util.concurrent.ExecutionException {
        final Client client = account.getClient();
        final Project project = client.getProjectClient().getProject(projectKey.getKey()).get();
        final IssueType type = client.getIssueType(typeName);

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
