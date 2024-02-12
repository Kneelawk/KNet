/*
 * MIT License
 *
 * Copyright (c) 2024 Kneelawk.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package com.kneelawk.knet.api.channel.context;

import org.jetbrains.annotations.NotNull;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import com.kneelawk.knet.api.channel.Channel;
import com.kneelawk.knet.api.handling.PayloadHandlingContext;
import com.kneelawk.knet.api.handling.PayloadHandlingDisconnectException;
import com.kneelawk.knet.api.handling.PayloadHandlingSilentException;
import com.kneelawk.knet.impl.KNetLog;
import com.kneelawk.knet.impl.platform.KNetPlatform;

/**
 * Describes a channel that sends and receives contextual payloads.
 * <p>
 * Note: Payloads to not have to extend {@link CustomPayload}.
 *
 * @param <C> the type of context this channel has.
 * @param <P> the type of payload this channel sends and receives.
 */
public class ContextualChannel<C, P> implements Channel {
    private final Identifier id;
    private final ChannelContext<C> channelContext;
    private final PayloadCodec<P> codec;

    private ContextualPayloadHandler<C, P> clientHandler = null;
    private ContextualPayloadHandler<C, P> serverHandler = null;

    /**
     * Creates a new contextual channel.
     *
     * @param id             the id of the channel.
     * @param channelContext the context of the channel.
     * @param codec          the payload codec of the channel.
     */
    public ContextualChannel(@NotNull Identifier id, @NotNull ChannelContext<C> channelContext,
                             @NotNull PayloadCodec<P> codec) {
        this.id = id;
        this.channelContext = channelContext;
        this.codec = codec;
    }

    /**
     * Synchronously handle a payload on the client.
     * <p>
     * This is executed on the main client thread.
     *
     * @param handler the payload handler.
     * @return this.
     */
    public @NotNull ContextualChannel<C, P> recvClient(@NotNull ContextualPayloadHandler<C, P> handler) {
        clientHandler = handler;
        return this;
    }

    /**
     * Synchronously handle a payload on the server.
     * <p>
     * This is executed on the main server thread.
     *
     * @param handler the payload handler.
     * @return this.
     */
    public @NotNull ContextualChannel<C, P> recvServer(@NotNull ContextualPayloadHandler<C, P> handler) {
        serverHandler = handler;
        return this;
    }

    /**
     * Synchronously handle a payload on both sides.
     * <p>
     * This is executed on the main thread.
     *
     * @param handler the payload handler.
     * @return this.
     */
    public @NotNull ContextualChannel<C, P> recvBoth(@NotNull ContextualPayloadHandler<C, P> handler) {
        serverHandler = clientHandler = handler;
        return this;
    }

    /**
     * Sends a payload to a player.
     *
     * @param player  the player to send to.
     * @param context the context to send.
     * @param payload the payload to send.
     */
    public void sendPlay(@NotNull PlayerEntity player, @NotNull C context, @NotNull P payload) {
        KNetPlatform.INSTANCE.sendPlay(player, new Payload(channelContext.encodeContext(context), payload));
    }

    /**
     * Sends a payload from the client to the server.
     *
     * @param context the context to send.
     * @param payload the payload to send.
     */
    public void sendPlayToServer(@NotNull C context, @NotNull P payload) {
        KNetPlatform.INSTANCE.sendPlayToServer(new Payload(channelContext.encodeContext(context), payload));
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public PacketByteBuf.PacketReader<? extends CustomPayload> getReader() {
        return this::read;
    }

    private Payload read(PacketByteBuf buf) {
        Object contextPayload = channelContext.decodePayload(buf);
        P payload = codec.decoder().apply(buf);
        return new Payload(contextPayload, payload);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void handleClientPayload(CustomPayload payload, PayloadHandlingContext ctx) {
        handlePayload(clientHandler, (Payload) payload, ctx);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void handleServerPayload(CustomPayload payload, PayloadHandlingContext ctx) {
        handlePayload(serverHandler, (Payload) payload, ctx);
    }

    private void handlePayload(ContextualPayloadHandler<C, P> handler, Payload payload,
                               PayloadHandlingContext ctx) {
        if (handler != null) {
            ctx.getExecutor().execute(() -> {
                try {
                    C handlerContext = channelContext.decodeContext(payload.contextPayload, ctx);
                    handler.handle(handlerContext, payload.payload, ctx);
                } catch (PayloadHandlingSilentException e) {
                    // do nothing
                } catch (PayloadHandlingDisconnectException e) {
                    ctx.disconnect(Text.literal("Channel " + id + " error: " + e.getMessage()));
                } catch (Exception e) {
                    // just log as an error by default
                    KNetLog.LOG.error("Channel {} error:", id, e);
                }
            });
        }
    }

    @Override
    public boolean isToServer() {
        return serverHandler != null;
    }

    @Override
    public boolean isToClient() {
        return clientHandler != null;
    }

    private class Payload implements CustomPayload {
        private final Object contextPayload;
        private final P payload;

        private Payload(Object contextPayload, P payload) {
            this.contextPayload = contextPayload;
            this.payload = payload;
        }

        @Override
        public void write(PacketByteBuf buf) {
            channelContext.encodePayload(contextPayload, buf);
            codec.encoder().accept(buf, payload);
        }

        @Override
        public Identifier id() {
            return id;
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Payload payload1 = (Payload) o;

            if (!contextPayload.equals(payload1.contextPayload)) return false;
            return payload.equals(payload1.payload);
        }

        @Override
        public int hashCode() {
            int result = contextPayload.hashCode();
            result = 31 * result + payload.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "Payload{" +
                "contextPayload=" + contextPayload +
                ", payload=" + payload +
                '}';
        }
    }
}
