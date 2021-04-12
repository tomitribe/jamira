/*
 * Copyright 2021 Tomitribe and community
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tomitribe.jamira.cli;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.api.Default;
import org.tomitribe.crest.api.In;
import org.tomitribe.crest.api.Option;
import org.tomitribe.util.IO;

import java.io.InputStream;
import java.util.List;

@Command("update")
public class UpdateCommand {

    //CHECKSTYLE:OFF

    /**
     * Update the specified JIRA issue.  The issue description can be supplied as a
     * flag or alternatively by reading it from piped input to the command.
     *
     *     echo "This is the new description" | jamira update issue TOMEE-3141 --summary="New title"
     *
     * If both the description flag and piped input is used, the resulting description will
     * be the contents of the flag, a new line, then the contents of the piped input.
     * For example the following would set the description to "Hello\nWorld":
     *
     *     echo "World" | jamira update issue TOMEE-3141 --description="Hello"
     *
     *
     * @param issueKey Text key for the issue.  For example TOMEE-123
     * @param summary Text to use as the title of the issue
     * @param type The name of the issue type.  See the `list issue-types` command
     * @param description Long description of the issue
     * @param assignee Username of the person to which the issue should be assigned
     * @param reporter Username of the person who is the reporter of the issue
     * @param priority The name of the priority for the issue.  See the `list priorities` command
     * @param affectedVersions  The names of the versions affected by the issue.  See the `list versions` command.  Flag may be repeated.
     * @param fixVersions  The names of the versions affected by the issue.  See the `list versions` command.  Flag may be repeated.
     * @param components  The component names relating to the issue.  See the `list components` command.  Flag may be repeated.
     * @param account Shortname of the JIRA server
     */
    @Command("issue")
    public void issue(final IssueKey issueKey,
                      @Option("summary") final String summary,
                      @Option("type") final String type,
                      @Option("description") String description,
                      @Option("assignee") final String assignee,
                      @Option("reporter") final String reporter,
                      @Option("priority") final String priority,
                      @Option("affected-version") final List<String> affectedVersions,
                      @Option("fix-version") final List<String> fixVersions,
                      @Option("component") final List<String> components,
                      @In final InputStream descriptionInput,
                      @Option("account") @Default("default") final Account account) throws Exception {

        final Client client = account.getClient();
        final IssueRestClient issueClient = client.getIssueClient();

        { // Read the description from System input
            final String content = IO.slurp(descriptionInput);
            if (content != null && content.length() > 0) {
                if (description == null) {
                    description = content;
                } else {
                    description += System.lineSeparator() + content;
                }
            }
        }

        final IssueInputBuilder issue = new IssueInputBuilder();

        if (summary != null) issue.setSummary(summary);
        if (type != null) issue.setIssueType(client.getIssueType(type));
        if (description != null) issue.setDescription(description);
        if (assignee != null) issue.setAssigneeName(assignee);
        if (reporter != null) issue.setReporterName(reporter);
        if (priority != null) issue.setPriority(client.getPriority(priority));
        if (affectedVersions != null) issue.setAffectedVersionsNames(affectedVersions);
        if (fixVersions != null) issue.setFixVersionsNames(fixVersions);
        if (components != null) issue.setComponentsNames(components);

        issueClient.updateIssue(issueKey.getKey(), issue.build()).get();
    }
    //CHECKSTYLE:ON

}
