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
package me.wolftein.steroid.framework.event;

import com.gs.collections.api.map.primitive.MutableIntIntMap;
import com.gs.collections.api.map.primitive.MutableIntObjectMap;
import com.gs.collections.impl.factory.primitive.IntIntMaps;
import com.gs.collections.impl.factory.primitive.IntObjectMaps;
import me.wolftein.steroid.framework.event.annotation.EventHandler;
import me.wolftein.steroid.framework.scheduler.Scheduler;
import net.jodah.typetools.TypeResolver;

import java.lang.reflect.Method;
import java.util.PriorityQueue;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Define a service for subscribing to {@link Event}s.
 */
public final class EventManager {
    private final Scheduler mScheduler;
    private final MutableIntIntMap mIds = IntIntMaps.mutable.empty();
    private final MutableIntObjectMap<PriorityQueue<EventExecutor>> mEvents = IntObjectMaps.mutable.empty();
    private int mIndex;

    /**
     * Dependency constructor for {@link EventManager}.
     */
    public EventManager(Scheduler scheduler) {
        this.mScheduler = scheduler;
    }

    /**
     * Invokes a synchronised event.
     *
     * @param event The event to be invoked by the manager.
     *
     * @return The event that has been invoked.
     */
    public <T extends Event> T invokeEvent(T event) {
        final PriorityQueue<EventExecutor> executors = mEvents.get(event.getClass().hashCode());
        if (executors != null) {
            executors.forEach(executor -> executor.execute(event));
        }
        return event;
    }

    /**
     * Invokes a synchronised event.
     *
     * @param event    The event to be invoked by the manager.
     * @param consumer The consumer to be called after being invoked.
     *
     * @return The event that has been invoked.
     */
    public <T extends Event> T invokeEvent(T event, Consumer<T> consumer) {
        consumer.accept(invokeEvent(event));
        return event;
    }

    /**
     * Invokes an asynchronous event.
     *
     * @param event The event to be invoked by the manager.
     */
    public <T extends Event> void invokeAsyncEvent(T event) {
        mScheduler.invokeAsyncTask(T -> invokeEvent(event));
    }

    /**
     * Invokes an asynchronous event and executes the completion consumer afterwards.
     *
     * @param event    The event to be invoked by the manager.
     * @param consumer The consumer to be called after being invoked.
     */
    public <T extends Event> void invokeAsyncEvent(T event, Consumer<T> consumer) {
        mScheduler.invokeAsyncTask(T -> invokeEvent(event, consumer));
    }

    /**
     * Subscribe for a particular {@link Event} with {@link EventPriority#NORMAL} priority.
     *
     * @param consumer The consumer to register to the given event.
     *
     * @return An unique identifier for the consumer.
     */
    public <T extends Event> int registerEvent(Consumer<T> consumer) {
        return registerEvent(consumer, EventPriority.NORMAL);
    }

    /**
     * Subscribe for a particular {@link Event} with the given {@link EventPriority}.
     *
     * @param consumer The consumer to register to the given event.
     * @param priority The priority of the consumer.
     *
     * @return An unique identifier for the consumer.
     */
    public <T extends Event> int registerEvent(Consumer<T> consumer, EventPriority priority) {
        final Class<?> clazz = TypeResolver.resolveRawArgument(Consumer.class, consumer.getClass());

        final int id = mIndex++;
        mIds.put(id, clazz.hashCode());

        PriorityQueue<EventExecutor> executor = mEvents.get(clazz.hashCode());
        if (executor == null) {
            mEvents.put(clazz.hashCode(), executor = new PriorityQueue<>());
        }
        executor.add(new EventExecutor<>(id, null, consumer, priority));
        return id;
    }

    /**
     * Subscribe any number of consumers for any number of {@link Event}s.
     *
     * @param listener The object that contains the consumers.
     */
    public void registerEvents(Object listener) {
        final Method[] methods = listener.getClass().getDeclaredMethods();
        for (final Method method : methods) {
            // Only those methods with EventHandler annotation.
            if (!method.isAnnotationPresent(EventHandler.class)) {
                continue;
            }

            // Make the method accessible if it is not.
            // NOTE: Private and protected methods are not accessible.
            if (!method.isAccessible()) {
                method.setAccessible(true);
            }

            final EventHandler annotation = method.getAnnotation(EventHandler.class);

            final Class<?> clazz = method.getParameterTypes()[0];
            PriorityQueue<EventExecutor> executor = mEvents.get(clazz.hashCode());
            if (executor == null) {
                mEvents.put(clazz.hashCode(), executor = new PriorityQueue<>());
            }
            executor.add(new EventExecutor<>(mIndex++, listener, (e) -> {
                try {
                    method.invoke(listener, e);
                } catch (Exception ignored) {
                    ignored.printStackTrace();
                }
            }, annotation.priority()));
        }
    }

    /**
     * Unsubscribe a {@link Event} previously registered.
     *
     * @param id The unique identifier for the event.
     */
    public void unregisterEvent(int id) {
        final int type = mIds.get(id);
        final PriorityQueue<EventExecutor> executors = mEvents.get(type);

        if (executors == null) {
            throw new IllegalStateException("No events to unregistered of the given type");
        }
        final boolean isRemoved = executors.removeIf(
                (EventExecutor executor) -> executor.mId == id
        );
        if (!isRemoved) {
            throw new IllegalStateException("Failed to unregister event from given plug-in");
        }
    }

    /**
     * Unsubscribe any number of consumers for any number of {@link Event}s.
     *
     * @param listener The object that contains the consumers.
     */
    public void unregisterEvents(Object listener) {
        final Predicate<EventExecutor> predicate = executor ->
                executor.mContainer != null && executor.mContainer.equals(listener);
        mEvents.forEachValue(collection -> collection.removeIf(predicate));
    }

    /**
     * Unregister all {@link Event}s.
     */
    public void unregisterAllEvents() {
        mEvents.clear();
    }
}
