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

import java.util.Collection;
import java.util.stream.Collectors;

import org.jetbrains.annotations.NotNull;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

import com.kneelawk.knet.api.channel.Channel;
import com.kneelawk.knet.api.channel.NetPayload;
import com.kneelawk.knet.api.handling.PayloadHandlingContext;
import com.kneelawk.knet.api.handling.PayloadHandlingDisconnectException;
import com.kneelawk.knet.api.handling.PayloadHandlingSilentException;
import com.kneelawk.knet.api.util.NetByteBuf;
import com.kneelawk.knet.impl.KNetLog;
import com.kneelawk.knet.impl.platform.KNetPlatform;

/**
 * Describes a channel that sends and receives contextual payloads.
 * <p>
 * Note: Payloads to not have to extend {@link Payload}.
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
     * Sends a payload to all players connected to this server.
     *
     * @param context the context to send.
     * @param payload the payload to send.
     */
    public void sendPlayToAll(@NotNull C context, @NotNull P payload) {
        Payload toSend = payload(context, payload);
        if (KNetLog.debug) {
            KNetLog.logSend(id, "all", toSend);
        }
        KNetPlatform.INSTANCE.sendPlayToAll(toSend);
    }

    /**
     * Sends a payload to a player.
     *
     * @param player  the player to send to.
     * @param context the context to send.
     * @param payload the payload to send.
     */
    public void sendPlay(@NotNull PlayerEntity player, @NotNull C context, @NotNull P payload) {
        Payload toSend = payload(context, payload);
        if (KNetLog.debug) {
            KNetLog.logSend(id, player.getGameProfile().getName(), toSend);
        }
        KNetPlatform.INSTANCE.sendPlay(player, toSend);
    }

    /**
     * Sends a payload to a collection of players.
     *
     * @param players the players to send to.
     * @param context the context to send.
     * @param payload the payload to send.
     */
    public void sendPlay(@NotNull Collection<ServerPlayerEntity> players, @NotNull C context, @NotNull P payload) {
        Payload toSend = payload(context, payload);
        if (KNetLog.debug) {
            KNetLog.logSend(id, players.stream().map(player -> player.getGameProfile().getName())
                .collect(Collectors.joining(", ", "[", "]")), toSend);
        }
        KNetPlatform.INSTANCE.sendPlay(players, toSend);
    }

    /**
     * Sends a payload from the client to the server.
     *
     * @param context the context to send.
     * @param payload the payload to send.
     */
    public void sendPlayToServer(@NotNull C context, @NotNull P payload) {
        Payload toSend = payload(context, payload);
        if (KNetLog.debug) {
            KNetLog.logSend(id, "server", toSend);
        }
        KNetPlatform.INSTANCE.sendPlayToServer(toSend);
    }

    /**
     * Sends a payload to all players in a dimension.
     *
     * @param dim     the dimension to send to.
     * @param context the context to send.
     * @param payload the payload to send.
     */
    public void sendPlayToDimension(@NotNull RegistryKey<World> dim, @NotNull C context, @NotNull P payload) {
        Payload toSend = payload(context, payload);
        if (KNetLog.debug) {
            KNetLog.logSend(id, "dimension " + dim.getValue(), toSend);
        }
        KNetPlatform.INSTANCE.sendPlayToDimension(dim, toSend);
    }

    /**
     * Sends a payload to all players tracking an entity, except the entity itself, if it is a player.
     *
     * @param entity  the entity that all receiver players should be tracking.
     * @param context the context to send.
     * @param payload the payload to send.
     */
    public void sendPlayToTracking(@NotNull Entity entity, @NotNull C context, @NotNull P payload) {
        Payload toSend = payload(context, payload);
        if (KNetLog.debug) {
            KNetLog.logSend(id, "tracking entity " + entity, toSend);
        }
        KNetPlatform.INSTANCE.sendPlayToTrackingEntity(entity, toSend);
    }

    /**
     * Sends a payload to all players tracking an entity, including the entity itself, if it is a player.
     *
     * @param entity  the entity that all receiver players should be tracking.
     * @param context the context to send.
     * @param payload the payload to send.
     */
    public void sendPlayToTrackingAndSelf(@NotNull Entity entity, @NotNull C context, @NotNull P payload) {
        Payload toSend = payload(context, payload);
        if (KNetLog.debug) {
            KNetLog.logSend(id, "tracking entity " + entity + " and self", toSend);
        }
        KNetPlatform.INSTANCE.sendPlayToTrackingEntityAndSelf(entity, toSend);
    }

    /**
     * Sends a payload to all players tracking a chunk.
     *
     * @param world   the world that holds the chunk.
     * @param pos     the position of the chunk.
     * @param context the context to send.
     * @param payload the payload to send.
     */
    public void sendPlayToTracking(@NotNull ServerWorld world, @NotNull ChunkPos pos, @NotNull C context,
                                   @NotNull P payload) {
        Payload toSend = payload(context, payload);
        if (KNetLog.debug) {
            KNetLog.logSend(id, "tracking chunk " + pos, toSend);
        }
        KNetPlatform.INSTANCE.sendPlayToTrackingChunk(world, pos, toSend);
    }

    /**
     * Sends a payload to all players tracking a block entity.
     *
     * @param be      the block entity that all receiver players should be tracking.
     * @param context the context to send.
     * @param payload the payload.
     */
    public void sendPlayToTracking(@NotNull BlockEntity be, @NotNull C context, @NotNull P payload) {
        Payload toSend = payload(context, payload);
        if (KNetLog.debug) {
            KNetLog.logSend(id, "tracking block-entity " + be + " @ " + be.getPos(), toSend);
        }
        KNetPlatform.INSTANCE.sendPlayToTrackingBlockEntity(be, toSend);
    }

    /**
     * Sends a payload to all players tracking a block position.
     *
     * @param world   the world that holds the block.
     * @param pos     the position of the block.
     * @param context the context to send.
     * @param payload the payload to send.
     */
    public void sendPlatyToTracking(@NotNull ServerWorld world, @NotNull BlockPos pos, @NotNull C context,
                                    @NotNull P payload) {
        Payload toSend = payload(context, payload);
        if (KNetLog.debug) {
            KNetLog.logSend(id, "tracking pos " + pos, toSend);
        }
        KNetPlatform.INSTANCE.sendPlayToTrackingBlock(world, pos, toSend);
    }

    private Payload payload(C context, P payload) {
        return new Payload(channelContext.encodeContext(context), payload);
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public NetByteBuf.NetReader<? extends NetPayload> getReader() {
        return this::read;
    }

    private NetPayload read(NetByteBuf buf) {
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
                    if (KNetLog.debug) {
                        String name = "server";
                        if (handler == serverHandler) {
                            name = "client";
                            PlayerEntity player = ctx.getPlayer();
                            if (player != null) {
                                name = "client " + player.getGameProfile().getName();
                            }
                        }
                        KNetLog.logReceive(id, name, payload);
                    }

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

    private class Payload implements NetPayload {
        private final Object contextPayload;
        private final P payload;

        private Payload(Object contextPayload, P payload) {
            this.contextPayload = contextPayload;
            this.payload = payload;
        }

        @Override
        public void write(NetByteBuf buf) {
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
            return "ContextualChannel.Payload{" +
                "contextPayload=" + contextPayload +
                ", payload=" + payload +
                '}';
        }
    }
}
