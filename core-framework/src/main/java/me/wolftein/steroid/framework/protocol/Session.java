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
package me.wolftein.steroid.framework.protocol;

import com.eclipsesource.json.JsonObject;
import com.gs.collections.api.map.primitive.MutableIntObjectMap;
import com.gs.collections.api.tuple.Pair;
import com.gs.collections.impl.factory.primitive.IntObjectMaps;
import com.gs.collections.impl.list.mutable.FastList;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import me.wolftein.steroid.framework.event.EventManager;
import me.wolftein.steroid.framework.protocol.event.SessionConnectEvent;
import me.wolftein.steroid.framework.protocol.event.SessionDisconnectEvent;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;
import java.util.function.Consumer;

/**
 * Define the session for communicating with another session.
 */
public final class Session {
    private final EventManager mEventManager;

    /**
     * The {@link io.netty.bootstrap.Bootstrap} on how the protocol works.
     */
    private final Bootstrap mBootstrap;

    /**
     * The worker of the {@link Channel}.
     */
    private final EventLoopGroup mWorkerGroup;

    /**
     * A map that contains a list of listeners for the given message.
     */
    private final MutableIntObjectMap<Queue<Consumer<JsonObject>>> mListeners = IntObjectMaps.mutable.empty();

    /**
     * The handler of the {@link Channel}.
     */
    private SessionInboundHandler mChannelHandler;

    /**
     * The instance of the channel.
     */
    private Channel mChannel;

    /**
     * Dependency constructor for {@link Session}.
     */
    public Session(EventManager eventManager) {
        this.mEventManager = eventManager;

        this.mWorkerGroup = new NioEventLoopGroup();
        this.mBootstrap = new Bootstrap();
        this.mBootstrap
                .group(mWorkerGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(
                                new HttpClientCodec(),
                                new HttpObjectAggregator(8192),
                                mChannelHandler);
                    }
                })
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true);
    }

    /**
     * Destroy the session.
     */
    public void destroy() {
        mWorkerGroup.shutdownGracefully();
    }

    /**
     * Connects the session.
     *
     * @param address The address where to connect this session.
     *
     * @return True if the session has been connected, false otherwise.
     */
    public boolean connect(String address) {
        final URI nAddress = URI.create(address);
        try {
            mChannelHandler = new SessionInboundHandler(this,
                    WebSocketClientHandshakerFactory.newHandshaker(nAddress,
                            WebSocketVersion.V13,
                            null,
                            false,
                            new DefaultHttpHeaders()));
            mChannel = mBootstrap.connect(nAddress.getHost(), nAddress.getPort()).sync().channel();
            mChannelHandler.getPromise().sync();
        } catch (InterruptedException ignored) {
            mChannel = null;
        }
        return (mChannel != null);
    }

    /**
     * Disconnect the session.
     *
     * @return True if the session has been disconnected, false otherwise.
     */
    public boolean disconnect() {
        if (mChannel == null) {
            return false;
        }
        try {
            mChannel.disconnect().sync();
        } catch (InterruptedException ignored) {
        }
        mChannel = null;
        return true;
    }

    /**
     * Check if the session is still active.
     *
     * @return True if the session is active, false otherwise.
     */
    public boolean isActive() {
        return mChannel != null && mChannel.isActive();
    }

    /**
     * Retrieve the address of the session.
     *
     * @return The remote address where the session is connected.
     */
    public InetSocketAddress getAddress() {
        if (mChannel == null) {
            throw new IllegalStateException(
                    "Cannot retrieve the address if the channel isn't bound.");
        }
        return (InetSocketAddress) mChannel.remoteAddress();
    }

    /**
     * Sends a message to the remote session.
     *
     * @param method The name of the message to be send.
     */
    public void send(String method) {
        send(method, (List<Pair<String, Object>>) null);
    }

    /**
     * Sends a message to the remote session.
     *
     * @param method The name of the message to be send.
     * @param data   The data of the message to be send.
     */
    public void send(String method, Pair<String, Object>... data) {
        send(method, FastList.wrapCopy(data));
    }

    /**
     * Sends a message to the remote session.
     *
     * @param method The name of the message to be send.
     * @param data   The data of the message to be send.
     */
    public void send(String method, List<Pair<String, Object>> data) {
        if (!isActive()) {
            return;
        }
        final JsonObject nRoot = new JsonObject();
        nRoot.add("function", method);
        if (data != null) {
            final JsonObject nData = new JsonObject();
            data.forEach(T -> addTypeIntoObject(nData, T.getOne(), T.getTwo()));
            nRoot.add("data", nData);
        }
        mChannel.writeAndFlush(new TextWebSocketFrame(nRoot.toString()));
    }

    /**
     * Register a listener for the given method.
     *
     * @param method   The name of the method
     * @param consumer The consumer for the given method.
     */
    public void addListener(String method, Consumer<JsonObject> consumer) {
        final Queue<Consumer<JsonObject>> executor
                = mListeners.getIfAbsentPut(method.hashCode(), new ArrayDeque<>());
        executor.add(consumer);
    }

    /**
     * Handle when the session has been disconnected.
     */
    protected void onDisconnectFromHandler() {
        mChannel = null;
        mEventManager.invokeAsyncEvent(new SessionDisconnectEvent());
    }

    /**
     * Handle when the session has been disconnected.
     */
    protected void onConnectFromHandler(Channel channel) {
        final SessionConnectEvent event = mEventManager.invokeEvent(new SessionConnectEvent());
        if (event.isCancelled()) {
            channel.close();
        }
    }

    /**
     * Handle when the session has recieve a message.
     */
    protected void onMessageFromHandler(TextWebSocketFrame frame) {
        final JsonObject nFrame = JsonObject.readFrom(frame.text());
        final JsonObject nMessage = nFrame.get("data").asObject();

        final Queue<Consumer<JsonObject>> consumers
                = mListeners.get(nFrame.getString("function", "none").hashCode());
        if (consumers != null) {
            consumers.forEach(T -> T.accept(nMessage));
        }
    }

    /**
     * Helper method to add a type into the object.
     */
    protected void addTypeIntoObject(JsonObject object, String key, Object value) {
        if (value instanceof JsonObject) {
            object.add(key, (JsonObject) value);
        } else {
            object.add(key, value.toString());
        }
    }
}
