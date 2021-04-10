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
package org.tomitribe.jamira.cli.cache;

import com.atlassian.jira.rest.client.api.MetadataRestClient;
import com.atlassian.jira.rest.client.api.domain.Field;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.IssuelinksType;
import com.atlassian.jira.rest.client.api.domain.Priority;
import com.atlassian.jira.rest.client.api.domain.Resolution;
import com.atlassian.jira.rest.client.api.domain.ServerInfo;
import com.atlassian.jira.rest.client.api.domain.Status;
import io.atlassian.util.concurrent.Promise;
import org.tomitribe.jamira.cli.Cache;
import org.tomitribe.jamira.cli.Home;
import org.tomitribe.util.IO;

import javax.json.bind.Jsonb;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.tomitribe.jamira.cli.Client.stream;

public class CachedMetadataRestClient implements MetadataRestClient {

    private final MetadataRestClient client;
    private final Cache cache;

    public CachedMetadataRestClient(final MetadataRestClient client) {
        this.client = client;
        this.cache = Home.get().jamira().cache();
    }

    @Override
    public Promise<IssueType> getIssueType(final URI uri) {
        final Optional<IssueType> match = stream(getIssueTypes())
                .filter(issueType -> issueType.getSelf().equals(uri))
                .findFirst();

        if (match.isPresent()) {
            return new CompletedPromise<>(match.get());
        }

        return client.getIssueType(uri);
    }

    @Override
    public Promise<Iterable<IssueType>> getIssueTypes() {
        final File file = cache.issueTypesJson();

        if (file.exists() && age(file) < TimeUnit.DAYS.toMillis(1)) {
            final Jsonb jsonb = JsonbInstances.get();
            final CachedIssueType[] cachedIssueTypes = jsonb.fromJson(slurp(file), CachedIssueType[].class);
            final List<IssueType> issueTypes = Stream.of(cachedIssueTypes)
                    .map(CachedIssueType::toIssueType)
                    .collect(Collectors.toList());
            return new CompletedPromise<>(issueTypes);
        }

        final Iterable<IssueType> issueTypes = get(client.getIssueTypes());
        { // Cache the values
            final List<CachedIssueType> cachedIssueTypes = stream(issueTypes)
                    .map(CachedIssueType::fromIssueType)
                    .collect(Collectors.toList());

            final Jsonb jsonb = JsonbInstances.get();
            final String json = jsonb.toJson(cachedIssueTypes);
            try (final PrintStream out = print(file)) {
                out.print(json);
            }
        }

        return new CompletedPromise<>(issueTypes);
    }

    private <T> Iterable<T> get(final Promise<Iterable<T>> issueTypes) {
        try {
            return issueTypes.get();
        } catch (final Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private long age(final File file) {
        return System.currentTimeMillis() - file.lastModified();
    }

    private Cache.Entry<Iterable<IssueType>> getEntry() {
        final Cache.Entry<IssueType[]> arrayEntry = new Cache.ObjectEntry<>(cache.issueTypesJson(), IssueType[].class);
        return new Cache.Entry<Iterable<IssueType>>() {
            @Override
            public Iterable<IssueType> write(final Iterable<IssueType> object) {
                final List<IssueType> list = new ArrayList<>();
                object.forEach(list::add);
                arrayEntry.write(list.toArray(new IssueType[0]));
                return list;
            }

            @Override
            public Iterable<IssueType> read() {
                return Arrays.asList(arrayEntry.read());
            }

            public File getFile() {
                return arrayEntry.getFile();
            }
        };
    }

    @Override
    public Promise<Iterable<IssuelinksType>> getIssueLinkTypes() {
        return client.getIssueLinkTypes();
    }

    @Override
    public Promise<Status> getStatus(final URI uri) {
        return client.getStatus(uri);
    }

    @Override
    public Promise<Iterable<Status>> getStatuses() {
        return client.getStatuses();
    }

    @Override
    public Promise<Priority> getPriority(final URI uri) {
        return client.getPriority(uri);
    }

    @Override
    public Promise<Iterable<Priority>> getPriorities() {
        return client.getPriorities();
    }

    @Override
    public Promise<Resolution> getResolution(final URI uri) {
        return client.getResolution(uri);
    }

    @Override
    public Promise<Iterable<Resolution>> getResolutions() {
        return client.getResolutions();
    }

    @Override
    public Promise<ServerInfo> getServerInfo() {
        return client.getServerInfo();
    }

    @Override
    public Promise<Iterable<Field>> getFields() {
        return client.getFields();
    }

    static class CompletedPromise<T> implements Promise<T> {
        private final T object;

        public CompletedPromise(final T object) {
            this.object = object;
        }

        @Override
        public T claim() {
            return object;
        }

        @Override
        public Promise<T> done(final Consumer<? super T> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Promise<T> fail(final Consumer<Throwable> c) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Promise<T> then(final TryConsumer<? super T> callback) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <B> Promise<B> map(final Function<? super T, ? extends B> function) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <B> Promise<B> flatMap(final Function<? super T, ? extends Promise<? extends B>> function) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Promise<T> recover(final Function<Throwable, ? extends T> handleThrowable) {
            throw new UnsupportedOperationException();
        }

        @Override
        public <B> Promise<B> fold(final Function<Throwable, ? extends B> handleThrowable, final Function<? super T, ? extends B> function) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean cancel(final boolean mayInterruptIfRunning) {
            return false;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean isDone() {
            return true;
        }

        @Override
        public T get() throws InterruptedException, ExecutionException {
            return object;
        }

        @Override
        public T get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            return object;
        }
    }

    public static String slurp(final File file) {
        try {
            return IO.slurp(file);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static PrintStream print(final File file) {
        try {
            return IO.print(file);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

}
