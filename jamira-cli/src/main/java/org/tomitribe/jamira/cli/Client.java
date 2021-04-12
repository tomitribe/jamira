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

import java.net.URI;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.tomitribe.jamira.cli.cache.CachedMetadataRestClient;

import com.atlassian.jira.rest.client.api.AuditRestClient;
import com.atlassian.jira.rest.client.api.ComponentRestClient;
import com.atlassian.jira.rest.client.api.GroupRestClient;
import com.atlassian.jira.rest.client.api.IssueRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClientFactory;
import com.atlassian.jira.rest.client.api.MetadataRestClient;
import com.atlassian.jira.rest.client.api.MyPermissionsRestClient;
import com.atlassian.jira.rest.client.api.ProjectRestClient;
import com.atlassian.jira.rest.client.api.ProjectRolesRestClient;
import com.atlassian.jira.rest.client.api.SearchRestClient;
import com.atlassian.jira.rest.client.api.SessionRestClient;
import com.atlassian.jira.rest.client.api.UserRestClient;
import com.atlassian.jira.rest.client.api.VersionRestClient;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.Priority;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;

import io.atlassian.util.concurrent.Promise;
import lombok.Data;

@Data
public class Client {

    private final JiraRestClient restClient;
    private final Account account;

    public Client(final Account account) {
        this.account = account;
        final JiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
        final URI jiraServerUri = account.getServerUri();
        restClient = factory.createWithBasicHttpAuthentication(jiraServerUri, account.getUsername(), account.getPassword());
    }

    public IssueType getIssueType(final String name) {
        return stream(getMetadataClient().getIssueTypes())
                .filter(issueType -> name.equalsIgnoreCase(issueType.getName()))
                .findFirst()
                .orElseThrow(() -> new NoSuchIssueTypeException(name));
    }

    public Priority getPriority(final String name) {
        return stream(getMetadataClient().getPriorities())
                .filter(priority -> name.equalsIgnoreCase(priority.getName()))
                .findFirst()
                .orElseThrow(() -> new NoSuchPriorityException(name));
    }

    public static <T> Stream<T> stream(final Promise<Iterable<T>> promise) {
        try {
            return stream(promise.get());
        } catch (final RuntimeException e) {
            throw e;
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public IssueRestClient getIssueClient() {
        return restClient.getIssueClient();
    }

    public SessionRestClient getSessionClient() {
        return restClient.getSessionClient();
    }

    public UserRestClient getUserClient() {
        return restClient.getUserClient();
    }

    public GroupRestClient getGroupClient() {
        return restClient.getGroupClient();
    }

    public ProjectRestClient getProjectClient() {
        return restClient.getProjectClient();
    }

    public ComponentRestClient getComponentClient() {
        return restClient.getComponentClient();
    }

    public MetadataRestClient getMetadataClient() {
        return new CachedMetadataRestClient(restClient.getMetadataClient(), Jamira.cache(Home.get().jamira(), account));
    }

    public SearchRestClient getSearchClient() {
        return restClient.getSearchClient();
    }

    public VersionRestClient getVersionRestClient() {
        return restClient.getVersionRestClient();
    }

    public ProjectRolesRestClient getProjectRolesRestClient() {
        return restClient.getProjectRolesRestClient();
    }

    public AuditRestClient getAuditRestClient() {
        return restClient.getAuditRestClient();
    }

    public MyPermissionsRestClient getMyPermissionsRestClient() {
        return restClient.getMyPermissionsRestClient();
    }

    public static <T> Stream<T> stream(final Iterable<T> iterable) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(
                iterable.iterator(),
                Spliterator.ORDERED)
                , false);
    }
}
