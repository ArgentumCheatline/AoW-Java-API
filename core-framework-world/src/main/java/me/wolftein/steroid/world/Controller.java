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
package me.wolftein.steroid.world;

import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;
import com.gs.collections.impl.tuple.Tuples;
import me.wolftein.steroid.framework.Application;
import me.wolftein.steroid.framework.event.EventManager;
import me.wolftein.steroid.framework.event.EventPriority;
import me.wolftein.steroid.framework.event.annotation.EventHandler;
import me.wolftein.steroid.framework.protocol.Session;
import me.wolftein.steroid.framework.protocol.event.SessionConnectEvent;
import me.wolftein.steroid.framework.protocol.event.SessionDisconnectEvent;
import me.wolftein.steroid.world.event.*;

/**
 * Encapsulate the main protocol of the game framework.
 */
public final class Controller {
    /**
     * The address of the server.
     */
    private final static String SERVER_ADDRESS = "ws://138.36.237.209:7667";

    /**
     * Encapsulate the {@link me.wolftein.steroid.world.World} of the game.
     */
    private final World mWorld = new World();

    /**
     * Encapsulate the {@link Application} of the world.
     */
    private final Application mFramework;

    /**
     * The number of players online in the world.
     */
    private int mOnline;

    /**
     * A flag that determinate if the user is logged into the world.
     */
    private boolean mConnected = false;
    private boolean mLogged = false;

    /**
     * Constructor for {@link me.wolftein.steroid.world.Controller}.
     *
     * @param application The framework of the world.
     * @param parent      The parent for the game.
     */
    public Controller(Application application, Object parent) {
        this.mFramework = application;

        // Register all events required for decoupling the framework.
        final EventManager nEventManager = mFramework.getEventManager();
        nEventManager.registerEvents(this);
        nEventManager.registerEvents(parent);

        final Session nSession = mFramework.getSession();

        // General messages.
        nSession.addListener("ACTONLINE", T -> mOnline = T.getInt("usersOnline", 0));

        // Entities messages.
        nSession.addListener("ACTPOS", this::onMessageEntityMove);
        nSession.addListener("DELETEPJ", this::onMessageEntityRemove);
        nSession.addListener("DISCTPJ", this::onMessageEntityRemove);
        nSession.addListener("SENDPJ", this::onMessageEntityCreate);
        nSession.addListener("RESPAWN", this::onMessageEntityRespawn);

        // Player messages.
        nSession.addListener("SENDPJME", this::onMessagePlayerCreate);
        nSession.addListener("RESPAWNME", this::onMessagePlayerRespawn);
        nSession.addListener("UPDTHP", this::onMessagePlayerUpdateHealth);
        nSession.addListener("UPDTMANA", this::onMessagePlayerUpdateManapoint);

        nSession.addListener("ERRLOGIN", this::onMessagePlayerLoginResult);
        nSession.addListener("ERRREGISTER", this::onMessagePlayerRegisterResult);
    }

    /**
     * Retrieves the {@link me.wolftein.steroid.world.World} of the game.
     *
     * @return A reference to the world of the game.
     */
    public World getWorld() {
        return mWorld;
    }

    /**
     * Retrieves the number of players in the world.
     *
     * @return The number of players in the world.
     */
    public int getOnline() {
        return mOnline;
    }

    /**
     * Check whenever the player is connected.
     *
     * @return True if the player is connected, false otherwise.
     */
    public boolean isConnected() {
        return mConnected;
    }

    /**
     * Check whenever the player is logged.
     *
     * @return True if the player is logged, false otherwise.
     */
    public boolean isLogged() {
        return mLogged;
    }

    /**
     * Connects the session.
     */
    public void connect() {
        if (!mConnected) {
            mFramework.getSession().connect(SERVER_ADDRESS);
        }
    }

    /**
     * Authenticate the user.
     *
     * @param username The username of the controller.
     * @param password The password of the controller.
     */
    public void authenticate(String username, String password) {
        if (mConnected && !mLogged) {
            mFramework.getSession().send("CONNECT",
                    Tuples.pair("name", username),
                    Tuples.pair("password", password));
        }
    }

    /**
     * Register a new user.
     *
     * @param username The username of the controller.
     * @param password The password of the controller.
     * @param email    The email of the controller.
     * @param isMale   Whenever the new character is male otherwise female.
     * @param race     The race of the new character.
     */
    public void register(String username, String password, String email, boolean isMale, int race) {
        if (mConnected && !mLogged) {
            mFramework.getSession().send("REGISTER",
                    Tuples.pair("name", username),
                    Tuples.pair("password", password),
                    Tuples.pair("email", email),
                    Tuples.pair("genero", isMale ? 1 : 2),
                    Tuples.pair("raza", race));
        }
    }

    /**
     * Moves the player into a direction.
     *
     * @param heading The heading of the player.
     */
    public void move(Heading heading) {
        if (mLogged && mWorld.isValidMovementForCharacter(heading)) {
            getWorld().getCharacter().setHeading(heading);
            mFramework.getSession().send("POS",
                    Tuples.pair("direccion", heading.ordinal() + 1));
        }
    }

    /**
     * Use an item from the inventory.
     *
     * @param slot The slot where the item is at.
     */
    public void use(int slot) {
        if (mLogged) {
            mFramework.getSession().send("UITEM",
                    Tuples.pair("item", slot == 0 ? slot + 1 : slot));
        }
    }

    /**
     * Throw a spell.
     *
     * @param id The identifier of the spell.
     * @param x  The x coordinates of the spell.
     * @param y  The y coordinates of the spell.
     */
    public void magic(int id, int x, int y) {
        if (mLogged) {
            mFramework.getSession().send("ATKHECHI",
                    Tuples.pair("hechizo", id),
                    Tuples.pair("pos", new JsonObject().add("x", x).add("y", y)));
        }
    }

    /**
     * Handle {@link me.wolftein.steroid.framework.protocol.event.SessionConnectEvent}.
     */
    @EventHandler(priority = EventPriority.HIGH)
    private void pfeOnSessionConnectEvent(SessionConnectEvent event) {
        mConnected = true;
    }

    /**
     * Handle {@link me.wolftein.steroid.framework.protocol.event.SessionDisconnectEvent}.
     */
    @EventHandler(priority = EventPriority.HIGH_IGNORE_CANCELLED)
    private void pfeOnSessionDisconnectEvent(SessionDisconnectEvent event) {
        mConnected = mLogged = false;
    }

    /**
     * Handle the message when an entity has been removed.
     *
     * @param message The object to retrieve the information from.
     */
    private void onMessageEntityRemove(JsonObject message) {
        final long nValue = getEntityIdFromValue(message.get("id"));

        mWorld.pfEntityUnregister(nValue);
    }

    /**
     * Handle the message when an entity has been created.
     *
     * @param message The object to retrieve the information from.
     */
    private void onMessageEntityCreate(JsonObject message) {
        final JsonObject nPlayer = message.get("personaje").asObject();
        final JsonObject nPosition = nPlayer.get("pos").asObject();

        final long nValue = getEntityIdFromValue(nPlayer.get("id"));
        final String nName = nPlayer.get("name").asString();

        final WorldEntity nEntity = new WorldEntity(nValue, nName);
        nEntity.setPosition(nPosition.get("x").asInt(), nPosition.get("y").asInt());
        nEntity.setHealth(nPlayer.get("hp").asInt());
        nEntity.setMaxHealth(nPlayer.get("maxHp").asInt());
        nEntity.setManapoint(nPlayer.get("mana").asInt());
        nEntity.setMaxManapoint(nPlayer.get("maxMana").asInt());
        nEntity.setAdmin(nPlayer.get("esAdmin").asInt() == 1);

        mWorld.pfEntityRegister(nEntity, false);

        mFramework.getEventManager().invokeAsyncEvent(new EntityCreateEvent(nEntity));
    }

    /**
     * Handle the message when an entity has moved.
     *
     * @param message The object to retrieve the information from.
     */
    private void onMessageEntityMove(JsonObject message) {
        final long nValue = getEntityIdFromValue(message.get("id"));
        final int nX = message.get("x").asInt();
        final int nY = message.get("y").asInt();

        final WorldEntity nEntity = mWorld.pfEntityMove(nValue, nX, nY);

        mFramework.getEventManager().invokeAsyncEvent(new EntityMoveEvent(nEntity));
    }

    /**
     * Handle the message when an entity has respawn.
     *
     * @param message The object to retrieve the information from.
     */
    private void onMessageEntityRespawn(JsonObject message) {
        final JsonObject nPosition = message.get("pos").asObject();

        final long nValue = getEntityIdFromValue(message.get("id"));
        final int nX = nPosition.get("x").asInt();
        final int nY = nPosition.get("y").asInt();
        // TODO: HP/Mana

        final WorldEntity nEntity = mWorld.pfEntityMove(nValue, nX, nY);

        mFramework.getEventManager().invokeAsyncEvent(new EntityMoveEvent(nEntity));
    }

    /**
     * Handle the message when the player has been created.
     *
     * @param message The object to retrieve the information from.
     */
    private void onMessagePlayerCreate(JsonObject message) {
        final JsonObject nPlayer = message.get("personaje").asObject();
        final JsonObject nPosition = nPlayer.get("pos").asObject();

        final long nValue = getEntityIdFromValue(nPlayer.get("id"));
        final String nName = nPlayer.get("name").asString();

        final WorldEntity nEntity = new WorldEntity(nValue, nName);
        nEntity.setPosition(nPosition.get("x").asInt(), nPosition.get("y").asInt());
        nEntity.setHealth(nPlayer.get("hp").asInt());
        nEntity.setMaxHealth(nPlayer.get("maxHp").asInt());
        nEntity.setManapoint(nPlayer.get("mana").asInt());
        nEntity.setMaxManapoint(nPlayer.get("maxMana").asInt());
        nEntity.setAdmin(nPlayer.get("esAdmin").asInt() == 1);
        nEntity.setHeading(Heading.values()[((int) getEntityIdFromValue(nPlayer.get("heading")) - 1)]);

        mWorld.pfEntityRegister(nEntity, true);

        mLogged = true;
        mFramework.getEventManager().invokeAsyncEvent(new PlayerJoinEvent(nEntity));
    }

    /**
     * Handle the message when the player has respawn.
     *
     * @param message The object to retrieve the information from.
     */
    private void onMessagePlayerRespawn(JsonObject message) {
        final JsonObject nPosition = message.get("pos").asObject();

        final long nValue = getEntityIdFromValue(message.get("id"));
        final int nX = nPosition.get("x").asInt();
        final int nY = nPosition.get("y").asInt();
        final int nHealth = message.get("hp").asInt();
        final int nManapoint = message.get("mana").asInt();

        final WorldEntity nEntity = mWorld.pfEntityMove(nValue, nX, nY);
        nEntity.setHealth(nHealth);
        nEntity.setManapoint(nManapoint);

        mFramework.getEventManager().invokeAsyncEvent(new EntityMoveEvent(nEntity));
    }

    /**
     * Handle the message when the player has updated health.
     *
     * @param message The object to retrieve the information from.
     */
    private void onMessagePlayerUpdateHealth(JsonObject message) {
        final int nHealth = message.get("hp").asInt();

        final WorldEntity nEntity = mWorld.getCharacter();
        nEntity.setHealth(nHealth);

        // TODO: Event
    }

    /**
     * Handle the message when the player has updated mana.
     *
     * @param message The object to retrieve the information from.
     */
    private void onMessagePlayerUpdateManapoint(JsonObject message) {
        final int nManapoint = message.get("mana").asInt();

        final WorldEntity nEntity = mWorld.getCharacter();
        nEntity.setManapoint(nManapoint);

        // TODO: Event
    }

    /**
     * Handle the message when receive the registration status.
     *
     * @param message The object to retrieve the information from.
     */
    private void onMessagePlayerRegisterResult(JsonObject message) {
        final boolean isValid = message.get("err").asInt() == 2;
        final String nMessage = message.get("msg").asString();

        mFramework.getEventManager().invokeAsyncEvent(new PlayerRegisterEvent(isValid, nMessage));
    }

    /**
     * Handle the message when receive the login status.
     *
     * @param message The object to retrieve the information from.
     */
    private void onMessagePlayerLoginResult(JsonObject message) {
        final boolean isValid = message.get("err").asInt() == 2;
        final String nMessage = message.get("msg").asString();

        mFramework.getEventManager().invokeAsyncEvent(new PlayerErrorEvent(isValid, nMessage));
    }

    /**
     * Retrieves the unique identifier of the entity from a {@link com.eclipsesource.json.JsonValue}.
     * <br/>
     * NOTE: This is required since Midraks sometimes returns string and sometimes returns long. (:rolleyes:)
     *
     * @param value The JSON value to retrieve from.
     *
     * @return The unique identifier of the entity.
     */
    private long getEntityIdFromValue(JsonValue value) {
        return (value.isString() ? Long.parseLong(value.asString()) : value.asLong());
    }
}
