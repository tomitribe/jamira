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
package org.tomitribe.jamira.core.cache;

import com.atlassian.jira.rest.client.api.domain.Resolution;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.net.URI;

@Data
@Builder(builderClassName = "Builder")
@NoArgsConstructor
@AllArgsConstructor
public class CachedResolution {

    private URI self;
    private String name;
    private Long id;
    private String description;

    public Resolution toResolution() {
        return new Resolution(self, id, name, description);
    }

    public static CachedResolution fromResolution(final Resolution resolution) {
        return CachedResolution.builder()
                .self(resolution.getSelf())
                .id(resolution.getId())
                .name(resolution.getName())
                .description(resolution.getDescription())
                .build();
    }
}
