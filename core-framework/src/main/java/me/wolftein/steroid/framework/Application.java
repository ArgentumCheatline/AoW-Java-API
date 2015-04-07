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
package me.wolftein.steroid.framework;

import me.wolftein.steroid.framework.event.EventManager;
import me.wolftein.steroid.framework.protocol.Session;
import me.wolftein.steroid.framework.scheduler.Scheduler;

/**
 * Encapsulate the framework and all its components.
 */
public final class Application {
    /**
     * An instance of the {@link Scheduler} that runs for 60fps.
     */
    private final Scheduler mScheduler = new Scheduler(60L);

    /**
     * An instance of the {@link EventManager}.
     */
    private final EventManager mEventManager = new EventManager(mScheduler);

    /**
     * An instance of the {@link Session}.
     */
    private final Session mSession = new Session(mEventManager);

    /**
     * Start the execution of the framework.
     */
    public void start() {
        if (mScheduler.isActive()) {
            throw new IllegalStateException(
                    "Trying to execute the scheduler which is already being executing.");
        }
        mScheduler.start();
    }

    /**
     * Stops the execution of the framework.
     */
    public void stop() {
        if (!mScheduler.isActive()) {
            throw new IllegalStateException(
                    "Trying to stop the scheduler which is already stopped.");
        }
        mScheduler.stop();
    }

    /**
     * Retrieves the version of the framework.
     *
     * @return A string that represent the version of the framework.
     */
    public String getVersion() {
        return "jAoW (On Steroid) v0.05d";
    }

    /**
     * Retrieves the {@link Session} of the framework.
     *
     * @return the session of the framework.
     */
    public Session getSession() {
        return mSession;
    }

    /**
     * Retrieves the {@link Scheduler} of the framework.
     *
     * @return the scheduler of the framework.
     */
    public Scheduler getScheduler() {
        return mScheduler;
    }

    /**
     * Retrieves the {@link EventManager} of the framework.
     *
     * @return the event manager of the framework.
     */
    public EventManager getEventManager() {
        return mEventManager;
    }
}
