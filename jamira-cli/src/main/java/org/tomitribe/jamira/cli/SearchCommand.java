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

import com.atlassian.jira.rest.client.api.SearchRestClient;
import com.atlassian.jira.rest.client.api.domain.SearchResult;
import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.api.Default;
import org.tomitribe.crest.api.Option;
import org.tomitribe.jamira.core.Account;
import org.tomitribe.jamira.core.Client;
import org.tomitribe.jamira.core.Formatting;

import java.util.concurrent.ExecutionException;

@Command("search")
public class SearchCommand {

    @Command
    public String[][] jql(final String query,
                          @Option("fields") @Default("key issueType.name priority.name status.name summary") final String fields,
                          @Option("sort") @Default("issueType.name priority.name status.name") final String sort,
                          @Option("account") @Default("default") final Account account) throws ExecutionException, InterruptedException {
        final Client client = account.getClient();
        final SearchRestClient searchClient = client.getSearchClient();
        final SearchResult result = searchClient.searchJql(query).get();
        return Formatting.asTable(result.getIssues(), fields, sort);
    }

}
