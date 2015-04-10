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
package me.wolftein.steroid.framework.protocol.event;

import com.eclipsesource.json.JsonObject;
import me.wolftein.steroid.framework.event.Event;
import me.wolftein.steroid.framework.protocol.Session;

/**
 * Encapsulate an {@link Event} that define when {@link Session} send a message.
 */
public final class SessionSendMessageEvent extends Event {
    private final String mFunction;
    private final JsonObject mBody;

    /**
     * Default constructor for {@link SessionConnectEvent}.
     */
    public SessionSendMessageEvent(String function, JsonObject body) {
        super(true);
        this.mFunction = function;
        this.mBody = body;
    }

    /**
     * Retrieves the function of the message.
     *
     * @return The function of the message.
     */
    public String getFunction() {
        return mFunction;
    }

    /**
     * Retrieves the body of the message.
     *
     * @return The body of the message.
     */
    public JsonObject getBody() {
        return mBody;
    }
}
