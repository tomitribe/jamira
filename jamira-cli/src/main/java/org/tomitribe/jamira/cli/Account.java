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

import lombok.Data;
import org.tomitribe.util.Files;
import org.tomitribe.util.IO;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.URI;
import java.util.Properties;

@Data
public class Account {

    private final String name;
    private final File file;
    private final Properties properties;

    public boolean exists() {
        return file.exists();
    }

    public Client getClient() {
        return new Client(this);
    }

    public String getUsername() {
        return properties.getProperty("username");
    }

    public void setUsername(final String username) {
        properties.setProperty("username", username);
    }

    public String getPassword() {
        return properties.getProperty("password");
    }

    public void setPassword(final String password) {
        properties.setProperty("password", password);
    }

    public URI getServerUri() {
        final String uri = properties.getProperty("serverUri");
        return URI.create(uri);
    }

    public void setServerUri(final String uri) {
        setServerUri(URI.create(uri));
    }

    public void setServerUri(final URI uri) {
        properties.setProperty("serverUri", uri.toASCIIString());
    }

    public void save() throws IOException {
        try (final OutputStream out = IO.write(file)) {
            properties.store(out, name);
        }
    }

    public void delete() {
        Files.remove(file);
    }


    public static Account name(final String name) {
        final Jamira jamira = Home.get().jamira();

        if ("default".equalsIgnoreCase(name)) {
            return jamira.accounts().findFirst().orElseThrow(NoAccountSetupException::new);
        }

        final Account account = jamira.account(name);
        if (!account.exists()) {
            throw new NoSuchAccountExistsException(name);
        }
        return account;
    }

    static Account load(final File file) {
        final String name = file.getName().replace(".properties", "");

        if (!file.exists()) {
            return new Account(name, file, new Properties());
        }

        try {
            final Properties properties = new Properties();
            try (final InputStream in = IO.read(file)) {
                properties.load(in);
                return new Account(name, file, properties);
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
