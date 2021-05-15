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

import org.tomitribe.jamira.core.cache.JsonbInstances;
import org.tomitribe.util.IO;
import org.tomitribe.util.dir.Dir;
import org.tomitribe.util.dir.Name;

import javax.json.bind.Jsonb;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.concurrent.TimeUnit;

public interface Cache extends Dir {

    @Name("issue-types.json")
    File issueTypesJson();

    @Name("statuses.json")
    File statusesJson();

    @Name("priorities.json")
    File prioritiesJson();

    @Name("resolutions.json")
    File resolutionsJson();

    class Entry<T> {

        private final File file;
        private final Class<T> type;

        public Entry(final File file, final Class<T> type) {
            this.file = file;
            this.type = type;
            final Bar bar = new Bar();
        }

        public static long age(final File file) {
            return System.currentTimeMillis() - file.lastModified();
        }

        public File getFile() {
            return file;
        }

        public boolean isFresh() {
            return file.exists() && Cache.Entry.age(file) < TimeUnit.DAYS.toMillis(30);
        }

        public void write(final Object object) {
            final Jsonb jsonb = JsonbInstances.get();
            try {
                final String json = jsonb.toJson(object);
                try (final OutputStream out = IO.write(file)) {
                    out.write(json.getBytes());
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        public T read() {
            final Jsonb jsonb = JsonbInstances.get();
            try {
                final String json = IO.slurp(file);
                return jsonb.fromJson(json, type);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }

}
