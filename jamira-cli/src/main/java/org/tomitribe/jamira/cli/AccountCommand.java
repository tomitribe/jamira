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

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import org.tomitribe.crest.api.Command;

import io.quarkus.runtime.annotations.RegisterForReflection;
import lombok.Data;

@Command("account")
@RegisterForReflection
public class AccountCommand {

    /**
     * Save login information for a JIRA install
     *
     * @param name Short name of the account in the format of `[a-z]+`.
     * @param username Username required to login to the server
     * @param password Password required to login to the server
     * @param uri URI of the server
     */
    @Command
    public void add(final Name name, final Username username, final Password password, final URI uri) throws IOException {
        final Jamira jamira = Home.get().jamira();
        final Account account = Jamira.account(jamira, name.getName());

        if (account.exists()) {
            throw new AccountExistsException(name.getName());
        }

        account.setUsername(username.getUsername());
        account.setPassword(password.getPassword());
        account.setServerUri(uri);
        account.save();
    }

    /**
     * Delete login information for a JIRA install
     *
     * @param name Short name of the account in the format of `[a-z]+`.
     */
    @Command
    public void remove(final Name name) {
        final Jamira jamira = Home.get().jamira();
        final Account account = Jamira.account(jamira, name.getName());

        if (!account.exists()) {
            throw new NoSuchAccountExistsException(name.getName());
        }

        account.delete();
    }

    /**
     * List JIRA installs configured
     */
    @Command
    public String[][] list() {
        final Jamira jamira = Home.get().jamira();

        final List<Account> accounts = Jamira.accounts(jamira).collect(Collectors.toList());
        return Formatting.asTable(accounts, "name username serverUri");
    }

    @Data
    public static class Name {
        private final String name;
    }

    @Data
    public static class Username {
        private final String username;
    }

    @Data
    public static class Password {
        private final String password;
    }

}
