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

import java.net.URI;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.tomitribe.jamira.cli.Client.stream;

public class CachedMetadataRestClient implements MetadataRestClient {

    private final MetadataRestClient client;
    private final Cache cache;

    public CachedMetadataRestClient(final MetadataRestClient client, final Cache cache) {
        this.client = client;
        this.cache = cache;
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
        final Cache.Entry<CachedIssueType[]> entry = new Cache.Entry<>(cache.issueTypesJson(), CachedIssueType[].class);

        if (entry.isFresh()) {
            final CachedIssueType[] cachedIssueTypes = entry.read();
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

            entry.write(cachedIssueTypes);
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
        final Cache.Entry<CachedStatus[]> entry = new Cache.Entry<>(cache.statusesJson(), CachedStatus[].class);

        if (entry.isFresh()) {
            final CachedStatus[] cachedStatuses = entry.read();
            final List<Status> statuses = Stream.of(cachedStatuses)
                    .map(CachedStatus::toStatus)
                    .collect(Collectors.toList());
            return new CompletedPromise<>(statuses);
        }

        final Iterable<Status> statuses = get(client.getStatuses());
        { // Cache the values
            final List<CachedStatus> cachedStatuses = stream(statuses)
                    .map(CachedStatus::fromStatus)
                    .collect(Collectors.toList());

            entry.write(cachedStatuses);
        }

        return new CompletedPromise<>(statuses);
    }

    @Override
    public Promise<Priority> getPriority(final URI uri) {
        return client.getPriority(uri);
    }

    @Override
    public Promise<Iterable<Priority>> getPriorities() {
        final Cache.Entry<CachedPriority[]> entry = new Cache.Entry<>(cache.prioritiesJson(), CachedPriority[].class);

        if (entry.isFresh()) {
            final CachedPriority[] cachedPriorities = entry.read();
            final List<Priority> priorities = Stream.of(cachedPriorities)
                    .map(CachedPriority::toPriority)
                    .collect(Collectors.toList());
            return new CompletedPromise<>(priorities);
        }

        final Iterable<Priority> priorities = get(client.getPriorities());
        { // Cache the values
            final List<CachedPriority> cachedPriorities = stream(priorities)
                    .map(CachedPriority::fromPriority)
                    .collect(Collectors.toList());

            entry.write(cachedPriorities);
        }

        return new CompletedPromise<>(priorities);
    }

    @Override
    public Promise<Resolution> getResolution(final URI uri) {
        return client.getResolution(uri);
    }

    @Override
    public Promise<Iterable<Resolution>> getResolutions() {
        final Cache.Entry<CachedResolution[]> entry = new Cache.Entry<>(cache.resolutionsJson(), CachedResolution[].class);

        if (entry.isFresh()) {
            final CachedResolution[] cachedResolutions = entry.read();
            final List<Resolution> resolutions = Stream.of(cachedResolutions)
                    .map(CachedResolution::toResolution)
                    .collect(Collectors.toList());
            return new CompletedPromise<>(resolutions);
        }

        final Iterable<Resolution> resolutions = get(client.getResolutions());
        { // Cache the values
            final List<CachedResolution> cachedResolutions = stream(resolutions)
                    .map(CachedResolution::fromResolution)
                    .collect(Collectors.toList());

            entry.write(cachedResolutions);
        }

        return new CompletedPromise<>(resolutions);
    }

    @Override
    public Promise<ServerInfo> getServerInfo() {
        return client.getServerInfo();
    }

    @Override
    public Promise<Iterable<Field>> getFields() {
        return client.getFields();
    }

}
