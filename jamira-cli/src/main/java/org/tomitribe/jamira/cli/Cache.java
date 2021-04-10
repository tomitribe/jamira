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

import org.tomitribe.jamira.cli.cache.JsonbInstances;
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

    public static class ObjectEntry<T> implements Entry<T> {

        private final File file;
        private final Class<T> type;

        public ObjectEntry(final File file, final Class<T> type) {
            this.file = file;
            this.type = type;
        }

        @Override
        public File getFile() {
            return file;
        }

        @Override
        public T write(final T object) {
            System.out.println("Writing cache");
            final Jsonb jsonb = JsonbInstances.get();
            try {
                final String json = jsonb.toJson(object);
                try (final OutputStream out = IO.write(file)) {
                    out.write(json.getBytes());
                }
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            return object;
        }

        @Override
        public T read() {
            System.out.println("Reading cache");
            final Jsonb jsonb = JsonbInstances.get();
            try {
                final String json = IO.slurp(file);
                return jsonb.fromJson(json, type);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }


    }

    interface Entry<T> {
        T write(T object);

        T read();

        File getFile();

        default boolean olderThan(final long duration, final TimeUnit unit) {
            final long now = System.currentTimeMillis();
            final long age = now - getFile().lastModified();
            return age > unit.toMillis(duration);
        }
    }
}
