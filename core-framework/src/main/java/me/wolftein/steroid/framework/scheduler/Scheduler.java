/*
 * This file is part of AoW (On Steroids), licensed under the Apache 2.0 License.
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

import java.util.ArrayDeque;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * Encapsulate the main thread of the framework.
 */
public final class Scheduler implements TaskExecutor {
    /**
     * Define how many milliseconds are in a second.
     */
    protected final static long SECOND_AS_MILLISECOND = 1000L;

    protected final Executor mExecutor = Executors.newWorkStealingPool();
    protected final Queue<Task> mQueue = new PriorityQueue<>();
    protected final Queue<Task> mDirtyQueue = new ArrayDeque<>();
    protected final AtomicBoolean mActive = new AtomicBoolean(false);
    protected final AtomicBoolean mOverloaded = new AtomicBoolean(false);
    protected final long mStartTime = System.currentTimeMillis();
    protected final long mDesiredTicks;
    protected long mLoopTickTime, mLoopFrameTime;

    /**
     * Default constructor for {@link Scheduler}.
     */
    public Scheduler(long desiredTicks) {
        this.mDesiredTicks = desiredTicks;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Task invoke(Consumer<Task> consumer, TaskPriority priority, long delay, long period, boolean isAsync) {
        final Task task
                = new Task(consumer, priority, isAsync, System.currentTimeMillis() - mStartTime + delay, period);
        mDirtyQueue.add(task);
        return task;
    }

    /**
     * Starts the scheduler
     */
    public void start() {
        if (mActive.get()) {
            throw new IllegalStateException("Scheduler has been already started.");
        }
        mActive.set(true);

        final Queue<Task> defQueue = new ArrayDeque<>();
        mLoopFrameTime = System.currentTimeMillis();
        do {
            // Add all task that has been added into the executor with
            while (!mDirtyQueue.isEmpty()) {
                final Task task = mDirtyQueue.poll();
                if (task != null) {
                    mQueue.add(task);
                }
            }

            // Deferred all tasks that needs to be executed in asynchronous channel
            // or the synchronous channel.
            while (!mQueue.isEmpty()) {
                final Task task = mQueue.peek();
                if (task.getTime() > System.currentTimeMillis() - mStartTime) {
                    break;
                }
                if (task.isAlive()) {
                    if (task.isAsynchronous()) {
                        mExecutor.execute(() -> executeTaskIfNotDestroyOrRepeat(task));
                    } else {
                        defQueue.add(task);
                    }
                }
                mQueue.poll();
            }

            // Run all tasks deferred to the synchronous channel.
            while (!defQueue.isEmpty()) {
                executeTaskIfNotDestroyOrRepeat(defQueue.poll());
            }

            final long current = System.currentTimeMillis();
            if (current - mLoopFrameTime >= SECOND_AS_MILLISECOND) {
                mLoopFrameTime = current;
                mLoopTickTime = 0;
                mOverloaded.set(mLoopTickTime < mDesiredTicks);
            } else {
                mLoopTickTime++;
            }
        } while (mActive.get());

        // Remove all references to the old task to ensure GC collect them when
        // the executor has been stopped.
        mQueue.clear();
        mDirtyQueue.clear();
    }

    /**
     * Stops the scheduler.
     */
    public void stop() {
        if (!mActive.getAndSet(false)) {
            throw new IllegalStateException("Executor has not been started.");
        }
    }

    /**
     * Cancel all tasks.
     */
    public void cancelAllTasks() {
        mQueue.forEach(Task::cancel);
        mDirtyQueue.clear();
    }

    /**
     * Check if the executor is active.
     */
    public boolean isActive() {
        return mActive.get();
    }

    /**
     * Check if the executor is overloaded.
     */
    public boolean isOverloaded() {
        return mActive.get();
    }

    /**
     * Execute a task.
     */
    private void executeTaskIfNotDestroyOrRepeat(Task task) {
        try {
            task.execute();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        if (task.isRepeating() && task.isAlive()) {
            invoke(task.getConsumer(),
                    task.getPriority(),
                    task.getPeriod(),
                    task.getPeriod(),
                    task.isAsynchronous());
        }
    }
}
