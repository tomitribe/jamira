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

import com.atlassian.jira.rest.client.api.StatusCategory;
import com.atlassian.jira.rest.client.api.domain.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.URI;

@Data
@Builder(builderClassName = "Builder")
@NoArgsConstructor
@AllArgsConstructor
public class CachedStatus {

    private URI self;
    private String name;
    private Long id;
    private String description;
    private URI iconUrl;
    private StatusCategory statusCategory;

    public Status toStatus() {
        return new Status(self, id, name, description, iconUrl, statusCategory);
    }

    public static CachedStatus fromStatus(final Status status) {
        return CachedStatus.builder()
                .self(status.getSelf())
                .id(status.getId())
                .name(status.getName())
                .description(status.getDescription())
                .iconUrl(status.getIconUrl())
                .statusCategory(status.getStatusCategory())
                .build();
    }
}
