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

import java.util.function.Consumer;

/**
 * Encapsulate an executor of {@link Task}s.
 */
public interface TaskExecutor {
    /**
     * Invokes a task to be executed.
     *
     * @param consumer The executor method of the task.
     * @param priority The priority of the task.
     * @param delay    The delay in ticks to execute the task.
     * @param period   The period in ticks to repeat the task.
     * @param isAsync  True if the task runs parallel, false otherwise.
     *
     * @return A reference to the task created.
     */
    public Task invoke(Consumer<Task> consumer, TaskPriority priority, long delay, long period, boolean isAsync);

    /**
     * Invokes a synchronised task with {@link TaskPriority#NORMAL} priority.
     *
     * @param consumer The executor method of the task.
     *
     * @return A reference to the task created.
     */
    default public Task invokeTask(Consumer<Task> consumer) {
        return invoke(consumer, TaskPriority.NORMAL, 0, -1, false);
    }

    /**
     * Invokes a synchronised task with the given priority.
     *
     * @param consumer The executor method of the task.
     * @param priority The priority of the task.
     *
     * @return A reference to the task created.
     */
    default public Task invokeTask(Consumer<Task> consumer, TaskPriority priority) {
        return invoke(consumer, priority, 0, -1, false);
    }

    /**
     * Invokes a synchronised task to be executed with the given priority and executed after the delay period.
     *
     * @param consumer The executor method of the task.
     * @param priority The priority of the task.
     * @param delay    The delay in ticks to execute the task.
     *
     * @return A reference to the task created.
     */
    default public Task invokeDelayedTask(Consumer<Task> consumer, TaskPriority priority, long delay) {
        return invoke(consumer, priority, delay, -1, false);
    }

    /**
     * Invokes a repeating synchronised task with the given priority and executed after the delay period.
     *
     * @param consumer The executor method of the task.
     * @param priority The priority of the task.
     * @param delay    The delay in ticks to execute the task.
     * @param period   The period in ticks to repeat the task.
     *
     * @return A reference to the task created.
     */
    default public Task invokeRepeatingTask(Consumer<Task> consumer, TaskPriority priority, long delay, long period) {
        return invoke(consumer, priority, delay, period, false);
    }

    /**
     * Invokes an asynchronous task with {@link TaskPriority#NORMAL} priority.
     *
     * @param consumer The executor method of the task.
     *
     * @return A reference to the task created.
     */
    default public Task invokeAsyncTask(Consumer<Task> consumer) {
        return invoke(consumer, TaskPriority.NORMAL, 0, -1, true);
    }

    /**
     * Invokes an asynchronous task with the given priority.
     *
     * @param consumer The executor method of the task.
     * @param priority The priority of the task.
     *
     * @return A reference to the task created.
     */
    default public Task invokeAsyncTask(Consumer<Task> consumer, TaskPriority priority) {
        return invoke(consumer, priority, 0, -1, true);
    }

    /**
     * Invokes an asynchronous task with the given priority and executed after the delay period.
     *
     * @param consumer The executor method of the task.
     * @param priority The priority of the task.
     * @param delay    The delay in ticks to execute the task.
     *
     * @return A reference to the task created.
     */
    default public Task invokeDelayedAsyncTask(Consumer<Task> consumer, TaskPriority priority, long delay) {
        return invoke(consumer, priority, delay, -1, true);
    }

    /**
     * Invokes a repeating asynchronous task with the given priority and executed after the delay period.
     *
     * @param consumer The executor method of the task.
     * @param priority The priority of the task.
     * @param delay    The delay in ticks to execute the task.
     * @param period   The period in ticks to repeat the task.
     *
     * @return A reference to the task created.
     */
    default public Task invokeRepeatingAsyncTask(Consumer<Task> consumer, TaskPriority priority, long delay, long period) {
        return invoke(consumer, priority, delay, period, true);
    }
}
