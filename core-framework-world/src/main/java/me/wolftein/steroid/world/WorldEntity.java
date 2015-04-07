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
package me.wolftein.steroid.world;

/**
 * Encapsulate a DAO for a player.
 */
public final class WorldEntity {
    protected final long mId;
    protected final String mName;
    protected int mX;
    protected int mY;
    protected int mHealth, mManapoint;
    protected int mMaxHealth, mMaxManapoint;
    protected Heading mHeading;
    protected boolean mAdmin;

    /**
     * Constructor for {@link me.wolftein.steroid.world.WorldEntity}.
     *
     * @param id   The unique identifier of the player.
     * @param name The name of the player.
     */
    public WorldEntity(long id, String name) {
        this.mId = id;
        this.mName = name;
    }

    /**
     * Retrieves the identifier of the entity.
     *
     * @return the identifier of the entity.
     */
    public long getId() {
        return mId;
    }

    /**
     * Retrieves the name of the entity.
     *
     * @return the name of the entity.
     */
    public String getName() {
        return mName;
    }

    /**
     * Retrieves the x coordinates of the entity.
     *
     * @return the x coordinates of the entity.
     */
    public int getX() {
        return mX;
    }

    /**
     * Retrieves the y coordinates of the entity.
     *
     * @return the y coordinates of the entity.
     */
    public int getY() {
        return mY;
    }

    /**
     * Retrieves the heading of the entity.
     *
     * @return the heading of the entity.
     */
    public Heading getHeading() {
        return mHeading;
    }

    /**
     * Retrieves the health of the entity.
     *
     * @return the health of the entity.
     */
    public int getHealth() {
        return mHealth;
    }

    /**
     * Retrieves the manapoint of the entity.
     *
     * @return the manapoint of the entity.
     */
    public int getManapoint() {
        return mManapoint;
    }

    /**
     * Retrieves the max health of the entity.
     *
     * @return the max health of the entity.
     */
    public int getMaxHealth() {
        return mMaxHealth;
    }

    /**
     * Retrieves the max manapoint of the entity.
     *
     * @return the max manapoint of the entity.
     */
    public int getMaxManapoint() {
        return mMaxManapoint;
    }

    /**
     * Retrieves whenever the entity is administrator.
     *
     * @return True if the entity is administrator, false otherwise.
     */
    public boolean isAdmin() {
        return mAdmin;
    }

    /**
     * Change the position of the entity.
     *
     * @param x The new x coordinate of the entity.
     * @param y The new y coordinate of the entity.
     */
    public void setPosition(int x, int y) {
        mX = x;
        mY = y;
    }

    /**
     * Change the heading of the entity.
     *
     * @param heading The new heading of the entity.
     */
    public void setHeading(Heading heading) {
        mHeading = heading;
    }

    /**
     * Change the health of the entity.
     *
     * @param health The new health of the entity.
     */
    public void setHealth(int health) {
        mHealth = health;
    }

    /**
     * Change the manapoint of the entity.
     *
     * @param manapoint The new manapoint of the entity.
     */
    public void setManapoint(int manapoint) {
        mManapoint = manapoint;
    }

    /**
     * Change the max health of the entity.
     *
     * @param maxHealth The new max health of the entity.
     */
    public void setMaxHealth(int maxHealth) {
        mMaxHealth = maxHealth;
    }

    /**
     * Change the max manapoint of the entity.
     *
     * @param maxManapoint The new max manapoint of the entity.
     */
    public void setMaxManapoint(int maxManapoint) {
        mMaxManapoint = maxManapoint;
    }

    /**
     * Change the admin flag of the entity.
     *
     * @param isAdmin True if the entity is now administrator, false otherwise.
     */
    public void setAdmin(boolean isAdmin) {
        mAdmin = isAdmin;
    }
}
