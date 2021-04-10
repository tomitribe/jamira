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
import com.atlassian.jira.rest.client.api.domain.Issue;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import io.atlassian.util.concurrent.Promise;

import java.net.URI;

public class GetIssueExample {
    public static void main(String[] args) throws Exception {
        final JiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
        final URI jiraServerUri = new URI("https://issues.apache.org/jira");
        final JiraRestClient restClient = factory.createWithBasicHttpAuthentication(jiraServerUri, "foo", "bar");
        final IssueRestClient issueClient = restClient.getIssueClient();

        final Promise<Issue> issuePromise = issueClient.getIssue("TOMEE-2865");
        final Issue issue = issuePromise.get();

        System.out.printf("[%s] %s%n", issue.getKey(), issue.getSummary());
    }
}