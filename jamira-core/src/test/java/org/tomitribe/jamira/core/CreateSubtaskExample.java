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
package org.tomitribe.jamira.core;

import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClientFactory;
import com.atlassian.jira.rest.client.api.ProjectRestClient;
import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.Project;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class CreateSubtaskExample {
    public static void main(String[] args) throws Exception {
        final JiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
        final URI jiraServerUri = new URI("https://issues.apache.org/jira");
        final JiraRestClient restClient = factory.createWithBasicHttpAuthentication(jiraServerUri, "foo", "bar");
        final IssueRestClient issueClient = restClient.getIssueClient();
        final Issue parent = issueClient.getIssue("OPENEJB-142").get();
        final ProjectRestClient projectClient = restClient.getProjectClient();

        final Project project = projectClient.getProject("OPENEJB").get();

        final String name = "sub-task";
        final IssueType type = stream(project.getIssueTypes())
                .filter(issueType -> name.equalsIgnoreCase(issueType.getName()))
                .findFirst()
                .get();

        final IssueInputBuilder issue = new IssueInputBuilder(project, type, "Testing JIRA Client");
        final Map<String, String> parentMap = new HashMap<String, String>();
        parentMap.put("key", parent.getKey());
        issue.setFieldValue("parent", parent);

        final BasicIssue createdIssue = issueClient.createIssue(issue.build()).get();
        System.out.println(createdIssue.getKey());
//        IssueType.
//        final IssueInputBuilder builder = new IssueInputBuilder("TOMEE", 0l, "Testing Jira Client");
//
//        issueClient.createIssue(issue);
//
    }

    public static <T> Stream<T> stream(final Iterable<T> iterable) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(
                iterable.iterator(),
                Spliterator.ORDERED)
                , false);
    }
}