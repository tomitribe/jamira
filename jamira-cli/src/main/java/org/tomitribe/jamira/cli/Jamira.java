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

import org.tomitribe.util.dir.Mkdir;

import java.io.File;
import java.util.stream.Stream;

public interface Jamira extends org.tomitribe.util.dir.Dir {

    @Mkdir
    Cache cache(final String name);

    default Cache cache(final Account account) {
        return cache(account.getName());
    }

    default Account account() {
        return account("default");
    }

    default Account account(final String name) {
        final File file = file(name + ".properties");
        return Account.load(file);
    }

    default Stream<Account> accounts() {
        return files()
                .filter(file -> file.getName().endsWith(".properties"))
                .map(Account::load);
    }

}
