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

import com.atlassian.jira.rest.client.api.domain.Priority;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.URI;

@Data
@Builder(builderClassName = "Builder")
@NoArgsConstructor
@AllArgsConstructor
public class CachedPriority {

    private URI self;
    private String name;
    private Long id;
    private String statusColor;
    private String description;
    private URI iconUrl;

    public Priority toPriority() {
        return new Priority(self, id, name, statusColor, description, iconUrl);
    }

    public static CachedPriority fromPriority(final Priority priority) {
        return CachedPriority.builder()
                .self(priority.getSelf())
                .id(priority.getId())
                .name(priority.getName())
                .description(priority.getDescription())
                .iconUrl(priority.getIconUri())
                .statusColor(priority.getStatusColor())
                .build();
    }
}
