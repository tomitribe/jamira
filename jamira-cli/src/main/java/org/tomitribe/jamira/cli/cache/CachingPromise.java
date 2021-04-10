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
import org.tomitribe.jamira.cli.Cache;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Function;

public class CachingPromise<T> implements Promise<T> {
    private final Promise<T> promise;
    private final Cache.Entry<T> cache;

    public CachingPromise(final Promise<T> promise, final Cache.Entry<T> cache) {
        this.promise = promise;
        this.cache = cache;
    }

    @Override
    public T claim() {
        return cache(promise.claim());
    }

    @Override
    public Promise<T> done(final Consumer<? super T> c) {
        return promise.done(c);
    }

    @Override
    public Promise<T> fail(final Consumer<Throwable> c) {
        return promise.fail(c);
    }

    @Override
    public Promise<T> then(final TryConsumer<? super T> callback) {
        return promise.then(callback);
    }

    @Override
    public <B> Promise<B> map(final Function<? super T, ? extends B> function) {
        return promise.map(function);
    }

    @Override
    public <B> Promise<B> flatMap(final Function<? super T, ? extends Promise<? extends B>> function) {
        return promise.flatMap(function);
    }

    @Override
    public Promise<T> recover(final Function<Throwable, ? extends T> handleThrowable) {
        return promise.recover(handleThrowable);
    }

    @Override
    public <B> Promise<B> fold(final Function<Throwable, ? extends B> handleThrowable, final Function<? super T, ? extends B> function) {
        return promise.fold(handleThrowable, function);
    }

    @Override
    public boolean cancel(final boolean mayInterruptIfRunning) {
        return promise.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        return promise.isCancelled();
    }

    @Override
    public boolean isDone() {
        return promise.isDone();
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        return cache(promise.get());
    }

    @Override
    public T get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return cache(promise.get(timeout, unit));
    }

    private T cache(final T object) {
        return cache.write(object);
    }

}
