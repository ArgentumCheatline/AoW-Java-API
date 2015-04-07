/*
 * This file is part of jAoW (On Steroids), licensed under the Apache 2.0 License.
 *
 * Copyright (c) 2014 Agustin Alvarez <wolftein1@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package me.wolftein.steroid.framework.event;

import java.util.function.Consumer;

/**
 * A wrapper class to store each event consumer, its priority and if it ignores cancelled events.
 */
final class EventExecutor<T extends Event> implements Comparable<EventExecutor> {
    protected final int mId;
    protected final Object mContainer;
    protected final Consumer<T> mFunction;
    protected final EventPriority mPriority;

    /**
     * Default constructor for {@link EventExecutor}.
     */
    protected EventExecutor(int id, Object container, Consumer<T> function, EventPriority priority) {
        this.mId = id;
        this.mContainer = container;
        this.mFunction = function;
        this.mPriority = priority;
    }

    /**
     * Execute this executor.
     *
     * @param event The event to execute.
     */
    public void execute(T event) {
        if (!event.isCancelled() || mPriority.doesIgnoresCancelled()) {
            mFunction.accept(event);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(EventExecutor o) {
        return o.mPriority.ordinal() - mPriority.ordinal();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return mFunction.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        return (obj instanceof EventExecutor && super.equals(obj));
    }
}