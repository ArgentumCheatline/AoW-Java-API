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

/**
 * Enumerates the possible priorities of a {@link Task}.
 */
public enum TaskPriority {
    /**
     * Lowest priority task deferred up to 10 seconds.
     */
    LOWEST(10000),
    /**
     * Low priority task deferred up to 1.5 seconds.
     */
    LOW(1500),
    /**
     * Normal priority task deferred up to 0.5 seconds.
     */
    NORMAL(500),
    /**
     * High priority task deferred up to 0.15 seconds.
     */
    HIGH(150),
    /**
     * Highest priority task deferred up to 0.05 seconds.
     */
    HIGHEST(50),
    /**
     * Critical priority task are never deferred.
     */
    CRITICAL(0);

    private final int mDeferredTime;

    /**
     * Default constructor for {@link TaskPriority}.
     */
    private TaskPriority(int deferredTime) {
        mDeferredTime = deferredTime;
    }

    /**
     * Retrieves the timing of the priority in-case the executor is overloaded.
     *
     * @return The time that a task can be deferred in case if the scheduler overloads.
     */
    public int getDeferredTime() {
        return mDeferredTime;
    }
}
