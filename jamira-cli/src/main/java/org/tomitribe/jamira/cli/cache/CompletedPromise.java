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

import io.atlassian.util.concurrent.Promise;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;

class CompletedPromise<T> implements Promise<T> {
    private final T object;

    public CompletedPromise(final T object) {
        this.object = object;
    }

    @Override
    public T claim() {
        return object;
    }

    @Override
    public Promise<T> done(final Consumer<? super T> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Promise<T> fail(final Consumer<Throwable> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Promise<T> then(final TryConsumer<? super T> callback) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <B> Promise<B> map(final Function<? super T, ? extends B> function) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <B> Promise<B> flatMap(final Function<? super T, ? extends Promise<? extends B>> function) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Promise<T> recover(final Function<Throwable, ? extends T> handleThrowable) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <B> Promise<B> fold(final Function<Throwable, ? extends B> handleThrowable, final Function<? super T, ? extends B> function) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean cancel(final boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return true;
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        return object;
    }

    @Override
    public T get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return object;
    }
}
