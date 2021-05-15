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
import com.atlassian.jira.rest.client.api.domain.Comment;
import com.atlassian.jira.rest.client.api.domain.Issue;
import io.atlassian.util.concurrent.Promise;
import org.joda.time.DateTime;
import org.tomitribe.crest.api.Command;
import org.tomitribe.crest.api.Default;
import org.tomitribe.crest.api.In;
import org.tomitribe.crest.api.Option;
import org.tomitribe.jamira.core.Account;
import org.tomitribe.jamira.core.Client;
import org.tomitribe.jamira.core.IssueKey;
import org.tomitribe.util.IO;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ExecutionException;

@Command("add")
public class AddCommand {

    @Command("attachment")
    public void attachment(final IssueKey issueKey,
                           @Option("account") @Default("default") final Account account,
                           final File[] files) throws ExecutionException, InterruptedException {
        for (final File file : files) {
            if (!file.exists()) throw new InvalidAttachementException(file, "Does not exist");
            if (!file.canRead()) throw new InvalidAttachementException(file, "Cannot be read");
        }

        final Client client = account.getClient();
        final IssueRestClient issueClient = client.getIssueClient();

        final Issue issue = issueClient.getIssue(issueKey.getKey()).get();

        final Promise<Void> voidPromise = issueClient.addAttachments(issue.getAttachmentsUri(), files);
        voidPromise.get();
    }

    //    @Command("comment")
    public void comment(final IssueKey issueKey,
                        final String message,
                        @Option("account") @Default("default") final Account account) throws ExecutionException, InterruptedException, IOException {

        final Client client = account.getClient();
        final IssueRestClient issueClient = client.getIssueClient();

        final Issue issue = issueClient.getIssue(issueKey.getKey()).get();
        final DateTime creationDate = null;
        final Comment comment = new Comment(null, message, null, null, creationDate, creationDate, null, null);

        issueClient.addComment(issue.getCommentsUri(), comment).get();
    }

    @Command("comment")
    public void comment(final IssueKey issueKey,
                        @In final InputStream pipedInput,
                        @Option("account") @Default("default") final Account account) throws ExecutionException, InterruptedException, IOException {

        final Client client = account.getClient();
        final IssueRestClient issueClient = client.getIssueClient();

        final String content = IO.slurp(pipedInput);

        final Issue issue = issueClient.getIssue(issueKey.getKey()).get();
        final DateTime creationDate = null;
        final Comment comment = new Comment(null, content, null, null, creationDate, creationDate, null, null);

        issueClient.addComment(issue.getCommentsUri(), comment).get();
    }
}
