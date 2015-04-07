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
package me.wolftein.steroid.framework.scheduler;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Define a task being executed by a {@link TaskExecutor}.
 */
public final class Task implements Comparable<Task> {
    private final Consumer<Task> mConsumer;
    private final TaskPriority mPriority;
    private final AtomicBoolean mAlive = new AtomicBoolean(true);
    private final boolean mRepeating;
    private final boolean mAsynchronous;
    private final long mPeriod;
    private final long mTick;

    /**
     * Default constructor for {@link Task}.
     */
    protected Task(Consumer<Task> consumer, TaskPriority priority, boolean async, long tick, long period) {
        this.mConsumer = consumer;
        this.mPriority = priority;
        this.mRepeating = (period != -1);
        this.mAsynchronous = async;
        this.mPeriod = period;
        this.mTick = tick;
    }

    /**
     * Execute the task.
     */
    protected void execute() {
        mConsumer.accept(this);
    }

    /**
     * Cancel the task.
     */
    public void cancel() {
        mAlive.set(false);
    }

    /**
     * Retrieves the consumer of the task.
     *
     * @return The consumer of the task.
     */
    public Consumer<Task> getConsumer() {
        return mConsumer;
    }

    /**
     * Retrieves the time of the task.
     *
     * @return The time of the task.
     */
    public long getTime() {
        return mTick;
    }

    /**
     * Retrieves the period of the task.
     *
     * @return The period of the task.
     */
    public long getPeriod() {
        return mPeriod;
    }

    /**
     * Retrieves the priority of the task.
     *
     * @return The priority of the task.
     */
    public TaskPriority getPriority() {
        return mPriority;
    }

    /**
     * Check if the task is being repeating.
     *
     * @return True if the task is repeating, false otherwise.
     */
    public boolean isRepeating() {
        return mRepeating;
    }

    /**
     * Check if the task is alive.
     *
     * @return True if the task is alive, false otherwise.
     */
    public boolean isAlive() {
        return mAlive.get();
    }

    /**
     * Check if the task is being executed asynchronous.
     *
     * @return True if the task is executed asynchronous, false otherwise.
     */
    public boolean isAsynchronous() {
        return mAsynchronous;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(Task o) {
        final int result = (mTick < o.mTick ? -1 : mTick > o.mTick ? 1 : 0);
        return (result == 0 ? mPriority.getDeferredTime() - o.mPriority.getDeferredTime() : result);
    }
}
