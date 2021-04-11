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
import com.atlassian.jira.rest.client.api.domain.BasicProject;
import com.atlassian.jira.rest.client.api.domain.BulkOperationResult;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueFieldId;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.input.FieldInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.api.Default;
import org.tomitribe.crest.api.In;
import org.tomitribe.crest.api.Option;
import org.tomitribe.crest.api.PrintOutput;
import org.tomitribe.util.IO;

import java.io.InputStream;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Command("bulk-create")
public class BulkCreateCommand {

    /**
     * Bulk-creates subtasks for the specified parent issue by reading the
     * summaries from the piped input.  It is expected each summary is on
     * a separate line.
     *
     * Any flags supplied such as `fix-version` or `component` will be applied
     * to all sub-tasks created
     *
     * @param parentKey The issue key of the parent issue
     * @param input Issue summaries will be read via piped input
     * @param assignee Username of the person to which the issue should be assigned
     * @param reporter Username of the person who is the reporter of the issue
     * @param priority The name of the priority for the issue.  See the `list priorities` command
     * @param affectedVersions  The names of the versions affected by the issue.  See the `list versions` command.  Flag may be repeated.
     * @param fixVersions  The names of the versions affected by the issue.  See the `list versions` command.  Flag may be repeated.
     * @param components  The component names relating to the issue.  See the `list components` command.  Flag may be repeated.
     * @param account The shortname of the jira install configured via the `setup` command
     */
    @Command("subtasks")
    public PrintOutput createSubtask(final IssueKey parentKey,
                                     @In final InputStream input,
                                     @Option("assignee") final String assignee,
                                     @Option("reporter") final String reporter,
                                     @Option("priority") final String priority,
                                     @Option("affected-version") final List<String> affectedVersions,
                                     @Option("fix-version") final List<String> fixVersions,
                                     @Option("component") final List<String> components,
                                     @Option("account") @Default("default") final Account account) throws Exception {

        final Client client = account.getClient();
        final IssueRestClient issueClient = client.getIssueClient();

        final Issue parent = issueClient.getIssue(parentKey.getKey()).get();
        final IssueType type = client.getIssueType("sub-task");

        final String slurp = IO.slurp(input);
        final List<IssueInput> issues = Stream.of(slurp.split(System.lineSeparator()))
                .map(String::trim)
                .filter(s -> s.length() >= 0)
                .map(s -> new IssueInputBuilder(parent.getProject(), type, s))
                .peek(issue -> {
                    issue.setFieldValue("parent", parent);
                    if (affectedVersions != null) issue.setAffectedVersionsNames(affectedVersions);
                    if (fixVersions != null) issue.setFixVersionsNames(fixVersions);
                    if (components != null) issue.setComponentsNames(components);
                    if (assignee != null) issue.setAssigneeName(assignee);
                    if (reporter != null) issue.setReporterName(reporter);
                    if (priority != null) issue.setPriority(client.getPriority(priority));
                })
                .map(IssueInputBuilder::build)
                .collect(Collectors.toList());

        if (issues.size() == 0) {
            throw new NoIssueSummariesSuppliedException();
        }

        final BulkOperationResult<BasicIssue> result = issueClient.createIssues(issues).get();

        return out -> {

            result.getErrors().forEach(errorResult -> {
                final int number = errorResult.getFailedElementNumber();
                final IssueInput failedIssue = issues.get(number);
                final FieldInput field = failedIssue.getField(IssueFieldId.SUMMARY_FIELD.id);
                out.printf("FAILED: %s%n", field.getValue());
                errorResult.getElementErrors().getErrorMessages().forEach(s -> {
                    out.printf("    ERROR: %s%n", s);
                });
            });

            result.getIssues().forEach(basicIssue -> {
                out.printf("%s%n", basicIssue.getKey());
            });
        };
    }

    /**
     * Bulk-creates issues for the specified project by reading the
     * summaries from the piped input.  It is expected each summary is on
     * a separate line.
     *
     * Any flags supplied such as `fix-version` or `component` will be applied
     * to all issues created
     *
     * @param projectKey Text key for the JIRA project. Example `TOMEE`
     * @param input Issue summaries will be read via piped input
     * @param typeName The name of the issue type.  See the `list issue-types` command
     * @param assignee Username of the person to which the issue should be assigned
     * @param reporter Username of the person who is the reporter of the issue
     * @param priority The name of the priority for the issue.  See the `list priorities` command
     * @param affectedVersions  The names of the versions affected by the issue.  See the `list versions` command.  Flag may be repeated.
     * @param fixVersions  The names of the versions affected by the issue.  See the `list versions` command.  Flag may be repeated.
     * @param components  The component names relating to the issue.  See the `list components` command.  Flag may be repeated.
     * @param account The shortname of the jira install configured via the `setup` command
     */
    @Command("issues")
    public PrintOutput issues(final ProjectKey projectKey,
                              @In final InputStream input,
                              @Option("type") @Default("bug") final String typeName,
                              @Option("assignee") final String assignee,
                              @Option("reporter") final String reporter,
                              @Option("priority") final String priority,
                              @Option("affected-version") final List<String> affectedVersions,
                              @Option("fix-version") final List<String> fixVersions,
                              @Option("component") final List<String> components,
                              @Option("account") @Default("default") final Account account) throws Exception {

        final Client client = account.getClient();
        final IssueRestClient issueClient = client.getIssueClient();
        final IssueType type = client.getIssueType(typeName);

        final String slurp = IO.slurp(input);
        final List<IssueInput> issues = Stream.of(slurp.split(System.lineSeparator()))
                .map(String::trim)
                .filter(s -> s.length() >= 0)
                .map(s -> {
                    final BasicProject project = new BasicProject(null, projectKey.getKey(), null, null);
                    return new IssueInputBuilder(project, type, s);
                })
                .peek(issue -> {
                    if (affectedVersions != null) issue.setAffectedVersionsNames(affectedVersions);
                    if (fixVersions != null) issue.setFixVersionsNames(fixVersions);
                    if (components != null) issue.setComponentsNames(components);
                    if (assignee != null) issue.setAssigneeName(assignee);
                    if (reporter != null) issue.setReporterName(reporter);
                    if (priority != null) issue.setPriority(client.getPriority(priority));
                })
                .map(IssueInputBuilder::build)
                .collect(Collectors.toList());

        if (issues.size() == 0) {
            throw new NoIssueSummariesSuppliedException();
        }

        final BulkOperationResult<BasicIssue> result = issueClient.createIssues(issues).get();

        return out -> {

            result.getErrors().forEach(errorResult -> {
                final int number = errorResult.getFailedElementNumber();
                final IssueInput failedIssue = issues.get(number);
                final FieldInput field = failedIssue.getField(IssueFieldId.SUMMARY_FIELD.id);
                out.printf("FAILED: %s%n", field.getValue());
                errorResult.getElementErrors().getErrorMessages().forEach(s -> {
                    out.printf("    ERROR: %s%n", s);
                });
            });

            result.getIssues().forEach(basicIssue -> {
                out.printf("%s%n", basicIssue.getKey());
            });
        };
    }
}
