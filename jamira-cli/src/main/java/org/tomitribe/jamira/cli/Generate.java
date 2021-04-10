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

import org.tomitribe.util.StringTemplate;
import org.tomitribe.util.Strings;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class Generate {

    private final List<String> objects = Arrays.asList(
            "Audit",
            "Component",
            "Group",
            "Issue",
            "Metadata",
            "MyPermissions",
            "Project",
            "ProjectRoles",
            "Search",
            "Session",
            "User",
            "Version"
    );

    private final List<String> issueTypes = Arrays.asList();

    public static void main(String[] args) {
        new Generate().generateListCommands();
    }

    private void generateListCommands() {
        for (final String className : objects) {
            String variableName = Strings.lcfirst(className);

            final StringTemplate template = new StringTemplate("" +
                    "    @Command(\"{variableName}s\")\n" +
                    "    public String[][] {variableName}s(final ProjectKey projectKey,\n" +
                    "                               @Option(\"fields\") @Default(\"name released releaseDate\") final String fields) throws Exception {\n" +
                    "        final {className}Client {variableName}Client = client.get{className}Client();\n" +
                    "        return asTable({variableName}Client.getVersions(), fields.split(\"[ ,]+\"));\n" +
                    "    }\n");

            final HashMap<String, Object> map = new HashMap<>();
            map.put("variableName", variableName);
            map.put("className", className);
            System.out.println(template.apply(map));
        }
    }
}
