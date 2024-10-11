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

package com.kneelawk.knet.api.channel;

import java.util.Collection;
import java.util.stream.Collectors;

import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import com.kneelawk.knet.api.handling.ConfigPayloadHandlingContext;
import com.kneelawk.knet.api.handling.PayloadHandlingDisconnectException;
import com.kneelawk.knet.api.handling.PayloadHandlingException;
import com.kneelawk.knet.api.handling.PayloadHandlingSilentException;
import com.kneelawk.knet.api.util.NetByteBuf;
import com.kneelawk.knet.api.util.PayloadSender;
import com.kneelawk.knet.impl.KNetLog;

/**
 * Describes a {@link CustomPacketPayload} channel that can have payloads sent and received during the 'configuration' phase.
 *
 * @param <P> the type of payload this channel sends and receives.
 */
public class NoContextConfigChannel<P extends CustomPacketPayload> implements ConfigChannel, NoContextChannel<P> {
    private final CustomPacketPayload.Type<P> id;
    private final StreamCodec<? super NetByteBuf, P> codec;

    private NoContextConfigPayloadHandler<P> clientHandler = null;
    private NoContextConfigPayloadHandler<P> serverHandler = null;

    /**
     * Creates a new context-less channel.
     *
     * @param id    the id of this channel. Must be the same as the id of the payloads being sent.
     * @param codec used for converting packets into payloads.
     * @param <P>   the type of payload.
     * @return a new context-less channel.
     */
    public static <P extends CustomPacketPayload> NoContextConfigChannel<P> of(CustomPacketPayload.Type<P> id,
                                                                               StreamCodec<? super NetByteBuf, P> codec) {
        return new NoContextConfigChannel<>(id, codec);
    }

    private NoContextConfigChannel(CustomPacketPayload.Type<P> id, StreamCodec<? super NetByteBuf, P> codec) {
        this.id = id;
        this.codec = codec;
    }

    /**
     * Handle a payload on the client.
     * <p>
     * Note: this is executed on the netty thread pool.
     *
     * @param handler the payload handler.
     * @return this.
     */
    public NoContextConfigChannel<P> recvOffThreadClient(NoContextConfigPayloadHandler<P> handler) {
        clientHandler = debugWrap(handler);
        return this;
    }

    /**
     * Handle a payload on the server.
     * <p>
     * Note: this is executed on the netty thread pool.
     *
     * @param handler the payload handler.
     * @return this.
     */
    public NoContextConfigChannel<P> recvOffThreadServer(NoContextConfigPayloadHandler<P> handler) {
        serverHandler = debugWrap(handler);
        return this;
    }

    /**
     * Handle a payload on both sides.
     * <p>
     * Note: this is executed on the netty thread pool.
     *
     * @param handler the payload handler.
     * @return this.
     */
    public NoContextConfigChannel<P> recvOffThreadBoth(NoContextConfigPayloadHandler<P> handler) {
        serverHandler = clientHandler = debugWrap(handler);
        return this;
    }

    /**
     * Synchronously handle a payload on the client.
     * <p>
     * This is executed on the main client thread.
     *
     * @param handler the payload handler.
     * @return this.
     */
    public NoContextConfigChannel<P> recvClient(NoContextConfigPayloadHandler<P> handler) {
        clientHandler = sync(handler);
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
    public NoContextConfigChannel<P> recvServer(NoContextConfigPayloadHandler<P> handler) {
        serverHandler = sync(handler);
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
    public NoContextConfigChannel<P> recvBoth(NoContextConfigPayloadHandler<P> handler) {
        serverHandler = clientHandler = sync(handler);
        return this;
    }

    private NoContextConfigPayloadHandler<P> sync(NoContextConfigPayloadHandler<P> handler) {
        return (payload, ctx) -> ctx.getExecutor().execute(() -> {
            try {
                if (KNetLog.debug) {
                    String name;
                    if (handler == serverHandler) {
                        name = "client";
                    } else {
                        name = "server";
                    }
                    KNetLog.logReceive(id, name, payload);
                }

                handler.handle(payload, ctx);
            } catch (PayloadHandlingSilentException e) {
                // do nothing
            } catch (PayloadHandlingDisconnectException e) {
                ctx.disconnect(Component.literal("Channel " + id + " error: " + e.getMessage()));
            } catch (Exception e) {
                // just log as an error by default
                KNetLog.LOG.error("Channel {} error:", id, e);
            }
        });
    }

    private NoContextConfigPayloadHandler<P> debugWrap(NoContextConfigPayloadHandler<P> handler) {
        if (KNetLog.debug) {
            return (payload, ctx) -> {
                String name;
                if (handler == serverHandler) {
                    name = "client";
                } else {
                    name = "server";
                }
                KNetLog.logReceive(id, name, payload);

                handler.handle(payload, ctx);
            };
        } else {
            return handler;
        }
    }

    /**
     * Sends a payload through a {@link PayloadSender}.
     *
     * @param sender  the payload sender that will send the payload.
     * @param payload the payload to send.
     */
    @Override
    public void send(PayloadSender sender, P payload) {
        checkPayload(payload);
        if (KNetLog.debug) {
            KNetLog.logSend(id, sender.toString(), payload);
        }
        sender.sendPayload(payload);
    }

    /**
     * Sends a payload to a collection of {@link PayloadSender}.
     *
     * @param senders the collection of payload senders that will send the payload.
     * @param payload the payload to send.
     */
    @Override
    public void sendToSenders(Collection<PayloadSender> senders, P payload) {
        checkPayload(payload);
        if (KNetLog.debug) {
            KNetLog.logSend(id,
                senders.stream().map(PayloadSender::toString).collect(Collectors.joining(", ", "[", "]")), payload);
        }
        for (PayloadSender sender : senders) {
            sender.sendPayload(payload);
        }
    }

    private void checkPayload(P payload) {
        if (!payload.type().equals(id)) throw new IllegalStateException(
            "Payload id does not match channel id. Payload id: " + payload.type() + ", channel id: " + id);
    }

    @Override
    public CustomPacketPayload.Type<?> getId() {
        return id;
    }

    @Override
    public StreamCodec<? super NetByteBuf, ? extends CustomPacketPayload> getCodec() {
        return codec;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void handleClientPayload(CustomPacketPayload payload, ConfigPayloadHandlingContext ctx)
        throws PayloadHandlingException {
        if (clientHandler != null) {
            clientHandler.handle((P) payload, ctx);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void handleServerPayload(CustomPacketPayload payload, ConfigPayloadHandlingContext ctx)
        throws PayloadHandlingException {
        if (serverHandler != null) {
            serverHandler.handle((P) payload, ctx);
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
}
