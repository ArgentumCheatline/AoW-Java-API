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

import com.gs.collections.api.block.predicate.Predicate;
import com.gs.collections.api.collection.ImmutableCollection;
import com.gs.collections.api.map.primitive.MutableLongObjectMap;
import com.gs.collections.impl.factory.primitive.LongObjectMaps;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Define the world of the game framework.
 */
public final class World {
    /**
     * Contains the map of the world.
     */
    private final WorldMap mMap = new WorldMap();

    /**
     * Contains all the entities in the world.
     */
    private final MutableLongObjectMap<WorldEntity> mEntities = LongObjectMaps.mutable.empty();

    /**
     * The identifier of the character.
     */
    private final AtomicLong mCharacter = new AtomicLong(-1L);

    /**
     * Check whenever the heading is valid.
     *
     * @param heading The heading to move to.
     *
     * @return True if the heading is valid, false otherwise.
     */
    public boolean isValidMovementForCharacter(Heading heading) {
        final WorldEntity nEntity = getCharacter();

        if (!nEntity.getHeading().equals(heading)) {
            switch (heading) {
                case NORTH:
                    return !mMap.isBlocked(nEntity.getX(), nEntity.getY() - 1);
                case SOUTH:
                    return !mMap.isBlocked(nEntity.getX(), nEntity.getY() + 1);
                case EAST:
                    return !mMap.isBlocked(nEntity.getX() - 1, nEntity.getY());
                case WEST:
                    return !mMap.isBlocked(nEntity.getX() + 1, nEntity.getY());
            }
        }
        return false;
    }

    /**
     * Retrieves the entity that belongs to the player.
     *
     * @return The entity that belongs to the player.
     */
    public WorldEntity getCharacter() {
        return getEntity(mCharacter.get());
    }

    /**
     * Retrieves an entity from the world.
     *
     * @param id The unique identifier of the entity.
     *
     * @return The entity instance or null if the identifier is not valid.
     */
    public WorldEntity getEntity(long id) {
        return mEntities.get(id);
    }

    /**
     * Retrieves all entities in the world.
     *
     * @return All entities that exists in the world.
     */
    public ImmutableCollection<WorldEntity> getEntities() {
        return getEntities(T -> true);
    }

    /**
     * Retrieves all entities in the world except the player.
     *
     * @return All entities that exists in the world except the player.
     */
    public ImmutableCollection<WorldEntity> getEntitiesNotPlayer() {
        return getEntities(T -> T.getId() != mCharacter.get());
    }

    /**
     * Retrieves all entities in the world except the player.
     *
     * @param predicate The predicate for filtering those entities.
     *
     * @return All entities that exists in the world except the player.
     */
    public ImmutableCollection<WorldEntity> getEntitiesNotPlayer(Predicate<WorldEntity> predicate) {
        return getEntities(T -> T.getId() != mCharacter.get() && predicate.accept(T));
    }

    /**
     * Retrieves N entities in the world.
     *
     * @param predicate The predicate for filtering those entities.
     *
     * @return N entities that exists in the world.
     */
    public ImmutableCollection<WorldEntity> getEntities(Predicate<WorldEntity> predicate) {
        return mEntities.select(predicate).toImmutable();
    }

    /**
     * Called when an entity has been registered into the world.
     *
     * @param entity The new entity to be registered into the world.
     *
     * @return The entity registered.
     */
    public WorldEntity pfEntityRegister(WorldEntity entity, boolean isCharacter) {
        Objects.requireNonNull(entity);

        if (isCharacter) {
            mCharacter.set(entity.getId());
        }

        mEntities.put(entity.getId(), entity);
        mMap.setTile(entity.getX(), entity.getY(), entity.getId());
        return entity;
    }

    /**
     * Called when an entity has been unregistered from the world.
     *
     * @param id The unique identifier of the entity.
     *
     * @return The entity unregistered.
     */
    public WorldEntity pfEntityUnregister(long id) {
        final WorldEntity nEntity = mEntities.remove(id);
        if (nEntity == null) {
            return null;
        }
        mMap.setTile(nEntity.getX(), nEntity.getY(), 0L);
        return nEntity;
    }

    /**
     * Called when an entity has moved in the world.
     *
     * @param id The unique identifier of the entity.
     * @param x  The new x coordinates of the entity.
     * @param y  The new y coordinates of the entity.
     *
     * @return The entity unregistered.
     */
    public WorldEntity pfEntityMove(long id, int x, int y) {
        final WorldEntity nEntity = mEntities.get(id);
        mMap.setTile(nEntity.getX(), nEntity.getY(), 0L);

        nEntity.setPosition(x, y);
        mMap.setTile(nEntity.getX(), nEntity.getY(), id);
        return nEntity;
    }
}
