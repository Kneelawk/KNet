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

import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;

import com.kneelawk.knet.api.KNetSender;
import com.kneelawk.knet.api.handling.PayloadHandlingDisconnectException;
import com.kneelawk.knet.api.handling.PayloadHandlingException;
import com.kneelawk.knet.api.handling.PayloadHandlingSilentException;
import com.kneelawk.knet.api.handling.PlayPayloadHandlingContext;
import com.kneelawk.knet.api.util.NetBufs;
import com.kneelawk.knet.api.util.NetByteBuf;
import com.kneelawk.knet.api.util.NetRegistryByteBuf;
import com.kneelawk.knet.api.util.PayloadSender;
import com.kneelawk.knet.api.util.RegistryNetByteBuf;
import com.kneelawk.knet.impl.KNetLog;

/**
 * Describes a {@link CustomPacketPayload} channel that can have payloads sent and received during the 'play' phase.
 *
 * @param <P> the type of payload this channel sends and receives.
 */
public class NoContextPlayChannel<P extends CustomPacketPayload> implements PlayChannel, NoContextChannel<P> {
    private final CustomPacketPayload.Type<P> id;
    private final StreamCodec<? super NetRegistryByteBuf, P> codec;

    private KNetSender backend;
    private NoContextPlayPayloadHandler<P> clientHandler = null;
    private NoContextPlayPayloadHandler<P> serverHandler = null;

    /**
     * Creates a new context-less channel from a {@link RegistryNetByteBuf} codec or {@link NetByteBuf} codec.
     *
     * @param id    the id of this channel. Must be the same as the id of the payloads being sent.
     * @param codec used for converting packet into payloads.
     * @param <P>   the type of payload.
     * @return a new context-less channel.
     */
    public static <P extends CustomPacketPayload> NoContextPlayChannel<P> ofNetCodec(
        CustomPacketPayload.Type<P> id, StreamCodec<? super RegistryNetByteBuf, P> codec) {
        return new NoContextPlayChannel<>(id, codec.mapStream(NetBufs::registryNetOf));
    }

    /**
     * Creates a new context-less channel from a {@link RegistryNetByteBuf} codec or {@link RegistryFriendlyByteBuf} codec.
     *
     * @param id    the id of this channel. Must be the same as the id of the payloads being sent.
     * @param codec used for converting packet into payloads.
     * @param <P>   the type of payload.
     * @return a new context-less channel.
     */
    public static <P extends CustomPacketPayload> NoContextPlayChannel<P> ofRegistryCodec(
        CustomPacketPayload.Type<P> id,
        StreamCodec<? super NetRegistryByteBuf, P> codec) {
        return new NoContextPlayChannel<>(id, codec);
    }

    private NoContextPlayChannel(CustomPacketPayload.Type<P> id,
                                 StreamCodec<? super NetRegistryByteBuf, P> codec) {
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
    public NoContextPlayChannel<P> recvOffThreadClient(NoContextPlayPayloadHandler<P> handler) {
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
    public NoContextPlayChannel<P> recvOffThreadServer(NoContextPlayPayloadHandler<P> handler) {
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
    public NoContextPlayChannel<P> recvOffThreadBoth(NoContextPlayPayloadHandler<P> handler) {
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
    public NoContextPlayChannel<P> recvClient(NoContextPlayPayloadHandler<P> handler) {
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
    public NoContextPlayChannel<P> recvServer(NoContextPlayPayloadHandler<P> handler) {
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
    public NoContextPlayChannel<P> recvBoth(NoContextPlayPayloadHandler<P> handler) {
        serverHandler = clientHandler = sync(handler);
        return this;
    }

    private NoContextPlayPayloadHandler<P> sync(NoContextPlayPayloadHandler<P> handler) {
        return (payload, ctx) -> ctx.getExecutor().execute(() -> {
            try {
                if (KNetLog.debug) {
                    String name = "server";
                    if (handler == serverHandler) {
                        name = "client";
                        Player player = ctx.getPlayer();
                        if (player != null) {
                            name = "client " + player.getGameProfile().getName();
                        }
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

    private NoContextPlayPayloadHandler<P> debugWrap(NoContextPlayPayloadHandler<P> handler) {
        if (KNetLog.debug) {
            return (payload, ctx) -> {
                String name = "server";
                if (handler == serverHandler) {
                    name = "client";
                    Player player = ctx.getPlayer();
                    if (player != null) {
                        name = "client " + player.getGameProfile().getName();
                    }
                }
                KNetLog.logReceive(id, name, payload);

                handler.handle(payload, ctx);
            };
        } else {
            return handler;
        }
    }

    /**
     * Sends a payload to all players connected to this server.
     *
     * @param payload the payload to send.
     */
    public void sendToAll(P payload) {
        checkPayload(payload);
        if (KNetLog.debug) {
            KNetLog.logSend(id, "all", payload);
        }
        getBackend().sendPlayToAll(payload);
    }

    /**
     * Sends a payload to a player.
     *
     * @param player  the player to send to.
     * @param payload the payload to send.
     */
    public void send(Player player, P payload) {
        checkPayload(payload);
        if (KNetLog.debug) {
            KNetLog.logSend(id, player.getGameProfile().toString(), payload);
        }
        getBackend().sendPlay(player, payload);
    }

    /**
     * Sends a payload to a collection of players.
     *
     * @param players the players to send to.
     * @param payload the payload to send.
     */
    public void sendToPlayers(Collection<ServerPlayer> players, P payload) {
        checkPayload(payload);
        if (KNetLog.debug) {
            KNetLog.logSend(id, players.stream().map(player -> player.getGameProfile().getName())
                .collect(Collectors.joining(", ", "[", "]")), payload);
        }
        getBackend().sendPlay(players, payload);
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

    /**
     * Sends a payload from the client to the server.
     *
     * @param payload the payload to send.
     */
    public void sendToServer(P payload) {
        checkPayload(payload);
        if (KNetLog.debug) {
            KNetLog.logSend(id, "server", payload);
        }
        getBackend().sendPlayToServer(payload);
    }

    /**
     * Sends a payload to all players in a dimension.
     *
     * @param dim     the dimension to send to.
     * @param payload the payload to send.
     */
    public void sendToDimension(ServerLevel dim, P payload) {
        checkPayload(payload);
        if (KNetLog.debug) {
            KNetLog.logSend(id, "dimension " + dim.dimension().location(), payload);
        }
        getBackend().sendPlayToDimension(dim, payload);
    }

    /**
     * Sends a payload to all players tracking an entity, except the entity itself, if it is a player.
     *
     * @param entity  the entity that all receiver players should be tracking.
     * @param payload the payload to send.
     */
    public void sendToTracking(Entity entity, P payload) {
        checkPayload(payload);
        if (KNetLog.debug) {
            KNetLog.logSend(id, "tracking entity " + entity, payload);
        }
        getBackend().sendPlayToTrackingEntity(entity, payload);
    }

    /**
     * Sends a payload to all players tracking an entity, including the entity itself, if it is a player.
     *
     * @param entity  the entity that all receiver players should be tracking.
     * @param payload the payload to send.
     */
    public void sendToTrackingAndSelf(Entity entity, P payload) {
        checkPayload(payload);
        if (KNetLog.debug) {
            KNetLog.logSend(id, "tracking entity " + entity + " and self", payload);
        }
        getBackend().sendPlayToTrackingEntityAndSelf(entity, payload);
    }

    /**
     * Sends a payload to all players tracking a chunk.
     *
     * @param level   the level that holds the chunk.
     * @param pos     the position of the chunk.
     * @param payload the payload to send.
     */
    public void sendToTracking(ServerLevel level, ChunkPos pos, P payload) {
        checkPayload(payload);
        if (KNetLog.debug) {
            KNetLog.logSend(id, "tracking chunk " + pos, payload);
        }
        getBackend().sendPlayToTrackingChunk(level, pos, payload);
    }

    /**
     * Sends a payload to all players tracking a block entity.
     *
     * @param be      the block entity that all receiver players should be tracking.
     * @param payload the payload.
     */
    public void sendToTracking(BlockEntity be, P payload) {
        checkPayload(payload);
        if (KNetLog.debug) {
            KNetLog.logSend(id, "tracking block-entity " + be + " @ " + be.getBlockPos(), payload);
        }
        getBackend().sendPlayToTrackingBlockEntity(be, payload);
    }

    /**
     * Sends a payload to all players tracking a block position.
     *
     * @param level   the level that holds the block.
     * @param pos     the position of the block.
     * @param payload the payload to send.
     */
    public void sendToTracking(ServerLevel level, BlockPos pos, P payload) {
        checkPayload(payload);
        if (KNetLog.debug) {
            KNetLog.logSend(id, "tracking pos " + pos, payload);
        }
        getBackend().sendPlayToTrackingBlock(level, pos, payload);
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
    public void setBackend(KNetSender backend) {
        this.backend = backend;
    }

    private KNetSender getBackend() {
        KNetSender backend = this.backend;
        if (backend == null)
            throw new IllegalStateException("This channel has not been registered with a backend yet.");
        return backend;
    }

    @Override
    public StreamCodec<? super NetRegistryByteBuf, ? extends CustomPacketPayload> getCodec() {
        return codec;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void handleClientPayload(CustomPacketPayload payload, PlayPayloadHandlingContext ctx)
        throws PayloadHandlingException {
        if (clientHandler != null) {
            clientHandler.handle((P) payload, ctx);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void handleServerPayload(CustomPacketPayload payload, PlayPayloadHandlingContext ctx)
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
