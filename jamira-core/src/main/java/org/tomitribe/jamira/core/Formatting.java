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

import org.tomitribe.util.Join;
import org.tomitribe.util.collect.ObjectMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class Formatting {

    private Formatting() {
    }

    public static <T> String[][] asTable(final Iterable<T> items, final String fields) {
        if ("all".equalsIgnoreCase(fields)) {
            return asTable(items);
        }
        return asTable(items, fields.split("[ ,]+"));
    }

    public static <T> String[][] asTable(final Object object, final String[] fields) {
        return null;
    }

    public static <T> String[][] asTable(final Iterable<T> items, final String[] fields) {
        final List<List<String>> rows = new ArrayList<>();

        for (final T item : items) {
            final List<String> row = new ArrayList<>();

            final ObjectMap map = new ObjectMap(item);
            for (final String field : fields) {
                row.add(resolve(map, field));
            }
            rows.add(row);
        }

        // sort the rows
//        Collections.sort(rows,(a, b) -> {
//            a.
//        });

        final String[][] data = new String[rows.size() + 1][fields.length];
        int rowCount = 0;

        // Add the headers
        data[rowCount++] = fields;

        for (final List<String> row : rows) {
            data[rowCount++] = row.toArray(new String[fields.length]);
        }


        return data;
    }

    public static <T> String[][] asTable(final Iterable<T> items) {
        final List<List<String>> rows = new ArrayList<>();

        int columns = 0;
        List<String> keys = null;
        for (final T item : items) {
            final List<String> row = new ArrayList<>();

            final ObjectMap map = new ObjectMap(item);

            keys = new ArrayList<>(map.keySet());
            Collections.sort(keys);

            columns = Math.max(columns, keys.size());

            for (final String field : keys) {
                row.add(resolve(map, field));
            }

            rows.add(row);
        }

        // sort the rows
//        Collections.sort(rows,(a, b) -> {
//            a.
//        });

        final String[][] data = new String[rows.size() + 1][columns];
        int rowCount = 0;

        // Add the headers
        data[rowCount++] = keys.toArray(new String[columns]);

        for (final List<String> row : rows) {
            data[rowCount++] = row.toArray(new String[columns]);
        }

        return data;
    }

    private static String resolve(final ObjectMap map, final String field) {
        final List<String> parts = new ArrayList<>(Arrays.asList(field.split("\\.")));

        if (parts.size() > 1) {
            final String part = parts.remove(0);
            final Object object = map.get(part);
            return resolve(new ObjectMap(object), Join.join(".", parts));
        }

        final Object o = map.get(field);
        return o != null ? o.toString() : "null";
    }
}
