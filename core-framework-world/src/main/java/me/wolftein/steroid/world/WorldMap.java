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
 * Encapsulate the world map of the game framework.
 */
public final class WorldMap {
    /**
     * Encapsulate the width of the tile map.
     */
    private final static int MAP_WIDTH = 30;

    /**
     * Encapsulate the height of the tile map.
     */
    private final static int MAP_HEIGHT = 30;

    /**
     * Contains each tile with the id of the entity.
     */
    private final long[] mTiles = new long[MAP_WIDTH * MAP_HEIGHT];

    /**
     * Check whenever the given tile is blocked.
     *
     * @param x The x coordinate (in map coordinates).
     * @param y The y coordinate (in map coordinates).
     *
     * @return True if the given tile is blocked, false otherwise.
     */
    public boolean isBlocked(int x, int y) {
        return (x == 1 || y == 1 || x == 29 || y == 29) || getTile(x, y) != 0;
    }

    /**
     * Change the entity that is positioned on top of the given tile.
     *
     * @param x  The x coordinate (in map coordinates).
     * @param y  The y coordinate (in map coordinates).
     * @param id The new unique identifier of the entity.
     */
    public void setTile(int x, int y, long id) {
        mTiles[getIndex(x, y)] = id;
    }

    /**
     * Retrieves the entity that is position on top of the given tile.
     *
     * @param x The x coordinate (in map coordinates).
     * @param y The y coordinate (in map coordinates).
     *
     * @return The unique identifier of the entity or -1 if there isn't any.
     */
    public long getTile(int x, int y) {
        return mTiles[getIndex(x, y)];
    }

    /**
     * Transform a pair of (x, y) into internal map coordinates.
     *
     * @param x The x coordinate (in map coordinates).
     * @param y The y coordinate (in map coordinates).
     *
     * @return The index position of both coordinates.
     */
    private int getIndex(int x, int y) {
        return (y * MAP_HEIGHT + x);
    }
}
