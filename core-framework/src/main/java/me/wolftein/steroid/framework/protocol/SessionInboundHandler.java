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
package me.wolftein.steroid.framework.protocol;

import io.netty.channel.*;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.util.CharsetUtil;

/**
 * Encapsulate the {@link SimpleChannelInboundHandler} for handling WebSockets messages.*.
 */
public final class SessionInboundHandler extends SimpleChannelInboundHandler {
    private final WebSocketClientHandshaker mHandshake;
    private final Session mSession;
    private ChannelPromise mHandshakePromise;

    /**
     * Default constructor for {@link SessionInboundHandler}.
     */
    public SessionInboundHandler(Session session, WebSocketClientHandshaker handshake) {
        this.mSession = session;
        this.mHandshake = handshake;
    }

    /**
     * Retrieves a promise for the handshake of the session.
     *
     * @return a promise for the handshake of the session.
     */
    public ChannelFuture getPromise() {
        return mHandshakePromise;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        mHandshakePromise = ctx.newPromise();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        mHandshake.handshake(ctx.channel());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        mSession.onDisconnectFromHandler();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void messageReceived(ChannelHandlerContext ctx, Object msg) throws Exception {
        final Channel ch = ctx.channel();
        if (!mHandshake.isHandshakeComplete()) {
            mHandshake.finishHandshake(ch, (FullHttpResponse) msg);
            mHandshakePromise.setSuccess();
            mSession.onConnectFromHandler(ch);
            return;
        }

        //! Check whenever the message is a HTTP message.
        if (msg instanceof FullHttpResponse) {
            final FullHttpResponse response = (FullHttpResponse) msg;
            throw new IllegalStateException(
                    "Unexpected FullHttpResponse (getStatus=" + response.status() +
                            ", content=" + response.content().toString(CharsetUtil.UTF_8) + ')');
        }
        final WebSocketFrame frame = (WebSocketFrame) msg;
        if (frame instanceof TextWebSocketFrame) {
            mSession.onMessageFromHandler((TextWebSocketFrame) frame);
        } else if (frame instanceof CloseWebSocketFrame) {
            ch.close();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        if (!mHandshakePromise.isDone()) {
            mHandshakePromise.setFailure(cause);
        }
        ctx.close();
    }
}
