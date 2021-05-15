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
package org.tomitribe.jamira.cli.http;

import com.atlassian.httpclient.api.factory.HttpClientOptions;
import com.atlassian.jira.rest.client.api.AuthenticationHandler;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.auth.BasicHttpAuthenticationHandler;
import com.atlassian.jira.rest.client.internal.async.AsynchronousHttpClientFactory;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClient;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.atlassian.jira.rest.client.internal.async.DisposableHttpClient;

import java.net.URI;

/*
 * Allows the specification of custom http client options.
 */
public class CustomAsynchronousJiraRestClientFactory extends AsynchronousJiraRestClientFactory {

    public JiraRestClient create(final URI serverURI, final AuthenticationHandler authenticationHandler, final HttpClientOptions clientOptions) {
        DisposableHttpClient httpClient = (new AsynchronousHttpClientFactory()).createClient(serverURI, authenticationHandler);
        return new AsynchronousJiraRestClient(serverURI, httpClient);
    }

    public JiraRestClient createWithBasicHttpAuthentication(URI serverUri, String username, String password, HttpClientOptions clientOptions) {
        return this.create(serverUri, new BasicHttpAuthenticationHandler(username, password), clientOptions);
    }

    public JiraRestClient createWithAuthenticationHandler(URI serverUri, AuthenticationHandler authenticationHandler, HttpClientOptions clientOptions) {
        return this.create(serverUri, authenticationHandler, clientOptions);
    }
}
