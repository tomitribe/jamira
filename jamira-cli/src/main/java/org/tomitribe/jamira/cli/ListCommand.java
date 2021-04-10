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

import com.atlassian.jira.rest.client.api.GroupRestClient;
import com.atlassian.jira.rest.client.api.MetadataRestClient;
import com.atlassian.jira.rest.client.api.SearchRestClient;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.Project;
import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.api.Default;
import org.tomitribe.crest.api.Option;

@Command("list")
public class ListCommand {

    @Command("subtasks")
    public String[][] subtasks(final IssueKey parent,
                               @Option("account") @Default("default") final Account account,
                               @Option("fields") @Default("issueKey summary status.name") final String fields) throws Exception {
        final Client client = account.getClient();
        final Issue issue = client.getIssueClient().getIssue(parent.getKey()).get();
        return Formatting.asTable(issue.getSubtasks(), fields);
    }

    @Command("projects")
    public String[][] projects(
            @Option("account") @Default("default") final Account account,
            @Option("fields") @Default("id key name") final String fields) throws Exception {
        final Client client = account.getClient();
        return Formatting.asTable(client.getProjectClient().getAllProjects().get(), fields);
    }

    @Command("versions")
    public String[][] versions(final ProjectKey projectKey,
                               @Option("account") @Default("default") final Account account,
                               @Option("fields") @Default("name released releaseDate") final String fields) throws Exception {
        final Client client = account.getClient();
        final Project project = client.getProjectClient().getProject(projectKey.getKey()).get();
        return Formatting.asTable(project.getVersions(), fields);
    }

    @Command("components")
    public String[][] components(final ProjectKey projectKey,
                                 @Option("account") @Default("default") final Account account,
                                 @Option("fields") @Default("id name description") final String fields) throws Exception {

        final Client client = account.getClient();
        final Project project = client.getProjectClient().getProject(projectKey.getKey()).get();
        return Formatting.asTable(project.getComponents(), fields);
    }

    /**
     * Returns a list of users that match the search string.
     * This resource cannot be accessed anonymously.
     *
     * @param username        A query string used to search username, name or e-mail address
     * @param startAt         The index of the first user to return (0-based)
     * @param maxResults      The maximum number of users to return (defaults to 50). The maximum allowed value is 1000.
     *                        If you specify a value that is higher than this number, your search results will be truncated.
     * @param includeActive   If true, then active users are included in the results (default true)
     * @param includeInactive If true, then inactive users are included in the results (default false)
     * @return list of users that match the search string
     */
    @Command("users")
    public String[][] users(
            final String username,
            @Option("account") @Default("default") final Account account,
            @Option("start-at") final Integer startAt,
            @Option("maxResults") final Integer maxResults,
            @Option("include-active") final Boolean includeActive,
            @Option("include-inactive") final Boolean includeInactive,
            @Option("fields") @Default("name displayName timezone active") final String fields) throws Exception {
        final Client client = account.getClient();
        return Formatting.asTable(client.getUserClient().findUsers(
                username,
                startAt,
                maxResults,
                includeActive,
                includeInactive
        ).get(), fields);
    }

    /**
     * Returns groups with substrings matching a given query.
     * This is mainly for use with the group picker, so the returned groups contain html to be used as picker suggestions.
     * The groups are also wrapped in a single response object that also contains a header for use in the picker,
     * specifically showing X of Y matching groups.
     *
     * The number of groups returned is limited by the system property "jira.ajax.autocomplete.limit"
     *
     * The groups will be unique and sorted.
     *
     * @param query      A string to match groups against
     * @param exclude    Exclude groups
     * @param maxResults The maximum number of groups to return
     * @param userName   A user name
     * @return list of groups that match the search string
     */
    @Command("groups")
    public String[][] groups(@Option("query") final String query,
                             @Option("account") @Default("default") final Account account,
                             @Option("exclude") final String exclude,
                             @Option("maxResults") final Integer maxResults,
                             @Option("username") final String userName,
                             @Option("fields") @Default("id name description") final String fields) throws Exception {
        final Client client = account.getClient();
        final GroupRestClient groupClient = client.getGroupClient();
        return Formatting.asTable(groupClient.findGroups(query, exclude, maxResults, userName).get(), fields);
    }

    @Command("project-roles")
    public String[][] projectRoles(final ProjectKey projectKey,
                                   @Option("account") @Default("default") final Account account,
                                   @Option("fields") @Default("name") final String fields) throws Exception {
        final Client client = account.getClient();
        final Project project = client.getProjectClient().getProject(projectKey.getKey()).get();
        return Formatting.asTable(project.getProjectRoles(), fields);
    }

    @Command("favourite-filters")
    public String[][] favouriteFilters(@Option("account") @Default("default") final Account account,
                                       @Option("fields") @Default("id name jql") final String fields) throws Exception {
        final Client client = account.getClient();
        final SearchRestClient searchClient = client.getSearchClient();
        return Formatting.asTable(searchClient.getFavouriteFilters().get(), fields);
    }

    @Command("issue-types")
    public String[][] issueTypes(@Option("account") @Default("default") final Account account,
                                 @Option("fields") @Default("id name description") final String fields) throws Exception {
        final Client client = account.getClient();
        final MetadataRestClient metadataClient = client.getMetadataClient();
        return Formatting.asTable(metadataClient.getIssueTypes().get(), fields);
    }

    @Command("issue-types")
    public String[][] issueTypes(final ProjectKey projectKey,
                                 @Option("account") @Default("default") final Account account,
                                 @Option("fields") @Default("id name description") final String fields) throws Exception {
        final Client client = account.getClient();
        final Project project = client.getProjectClient().getProject(projectKey.getKey()).get();
        return Formatting.asTable(project.getIssueTypes(), fields);
    }

    @Command("issue-link-types")
    public String[][] issueLinkTypes(@Option("account") @Default("default") final Account account,
                                     @Option("fields") @Default("id name inward outward") final String fields) throws Exception {
        final Client client = account.getClient();
        final MetadataRestClient metadataClient = client.getMetadataClient();
        return Formatting.asTable(metadataClient.getIssueLinkTypes().get(), fields);
    }

    @Command("statuses")
    public String[][] statuses(@Option("account") @Default("default") final Account account,
                               @Option("fields") @Default("id name statusCategory.key description") final String fields) throws Exception {
        final Client client = account.getClient();
        final MetadataRestClient metadataClient = client.getMetadataClient();
        return Formatting.asTable(metadataClient.getStatuses().get(), fields);
    }

    @Command("priorities")
    public String[][] priorities(@Option("account") @Default("default") final Account account,
                                 @Option("fields") @Default("id name description") final String fields) throws Exception {
        final Client client = account.getClient();
        final MetadataRestClient metadataClient = client.getMetadataClient();
        return Formatting.asTable(metadataClient.getPriorities().get(), fields);
    }

    @Command("resolutions")
    public String[][] resolutions(@Option("account") @Default("default") final Account account,
                                  @Option("fields") @Default("id name description") final String fields) throws Exception {
        final Client client = account.getClient();
        final MetadataRestClient metadataClient = client.getMetadataClient();
        return Formatting.asTable(metadataClient.getResolutions().get(), fields);
    }

    @Command("fields")
    public String[][] fields(@Option("account") @Default("default") final Account account,
                             @Option("fields") @Default("fieldType id name") final String fields) throws Exception {
        final Client client = account.getClient();
        final MetadataRestClient metadataClient = client.getMetadataClient();
        return Formatting.asTable(metadataClient.getFields().get(), fields);
    }

    //    @Command("subtasks")
//    public Stream<String> subtasks2(final IssueKey parent) throws Exception {
//        final Issue issue = client.getIssueClient().getIssue(parent.getKey()).get();
//
//        return Client.stream(issue.getSubtasks())
//                .map(subtask -> String.format("%s  %s", subtask.getIssueKey(), subtask.getSummary()));
//    }


}
