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

import org.jetbrains.annotations.NotNull;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

import com.kneelawk.knet.api.handling.PayloadHandlingContext;
import com.kneelawk.knet.api.handling.PayloadHandlingDisconnectException;
import com.kneelawk.knet.api.handling.PayloadHandlingException;
import com.kneelawk.knet.api.handling.PayloadHandlingSilentException;
import com.kneelawk.knet.api.util.NetByteBuf;
import com.kneelawk.knet.impl.KNetLog;
import com.kneelawk.knet.impl.platform.KNetPlatform;

/**
 * Describes a {@link NetPayload} channel that can have payloads sent and received.
 *
 * @param <P> the type of payload this channel sends and receives.
 */
public class NoContextChannel<P extends NetPayload> implements Channel {
    private final Identifier id;
    private final NetByteBuf.PacketReader<P> reader;

    private NoContextPayloadHandler<P> clientHandler = null;
    private NoContextPayloadHandler<P> serverHandler = null;

    /**
     * Creates a new context-less channel.
     *
     * @param id     the id of this channel. Must be the same as the id of the payloads being sent.
     * @param reader used for converting packets into payloads.
     */
    public NoContextChannel(@NotNull Identifier id, @NotNull NetByteBuf.PacketReader<P> reader) {
        this.id = id;
        this.reader = reader;
    }

    /**
     * Handle a payload on the client.
     * <p>
     * Note: this is executed on the netty thread pool.
     *
     * @param handler the payload handler.
     * @return this.
     */
    public NoContextChannel<P> recvOffThreadClient(@NotNull NoContextPayloadHandler<P> handler) {
        clientHandler = handler;
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
    public NoContextChannel<P> recvOffThreadServer(@NotNull NoContextPayloadHandler<P> handler) {
        serverHandler = handler;
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
    public NoContextChannel<P> recvOffThreadBoth(@NotNull NoContextPayloadHandler<P> handler) {
        clientHandler = handler;
        serverHandler = handler;
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
    public NoContextChannel<P> recvClient(@NotNull NoContextPayloadHandler<P> handler) {
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
    public NoContextChannel<P> recvServer(@NotNull NoContextPayloadHandler<P> handler) {
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
    public NoContextChannel<P> recvBoth(@NotNull NoContextPayloadHandler<P> handler) {
        serverHandler = clientHandler = sync(handler);
        return this;
    }

    private NoContextPayloadHandler<P> sync(NoContextPayloadHandler<P> handler) {
        return (payload, ctx) -> ctx.getExecutor().execute(() -> {
            try {
                handler.handle(payload, ctx);
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

    /**
     * Sends a payload to all players connected to this server.
     *
     * @param payload the payload to send.
     */
    public void sendPlayToAll(@NotNull P payload) {
        checkPayload(payload);
        KNetPlatform.INSTANCE.sendPlayToAll(payload);
    }

    /**
     * Sends a payload to a player.
     *
     * @param player  the player to send to.
     * @param payload the payload to send.
     */
    public void sendPlay(@NotNull PlayerEntity player, @NotNull P payload) {
        checkPayload(payload);
        KNetPlatform.INSTANCE.sendPlay(player, payload);
    }

    /**
     * Sends a payload from the client to the server.
     *
     * @param payload the payload to send.
     */
    public void sendPlayToServer(@NotNull P payload) {
        checkPayload(payload);
        KNetPlatform.INSTANCE.sendPlayToServer(payload);
    }

    /**
     * Sends a payload to all players in a dimension.
     *
     * @param dim     the dimension to send to.
     * @param payload the payload to send.
     */
    public void sendPlayToDimension(@NotNull RegistryKey<World> dim, @NotNull P payload) {
        checkPayload(payload);
        KNetPlatform.INSTANCE.sendPlayToDimension(dim, payload);
    }

    /**
     * Sends a payload to all players tracking an entity, except the entity itself, if it is a player.
     *
     * @param entity  the entity that all receiver players should be tracking.
     * @param payload the payload to send.
     */
    public void sendPlayToTracking(@NotNull Entity entity, @NotNull P payload) {
        checkPayload(payload);
        KNetPlatform.INSTANCE.sendPlayToTrackingEntity(entity, payload);
    }

    /**
     * Sends a payload to all players tracking an entity, including the entity itself, if it is a player.
     *
     * @param entity  the entity that all receiver players should be tracking.
     * @param payload the payload to send.
     */
    public void sendPlayToTrackingAndSelf(@NotNull Entity entity, @NotNull P payload) {
        checkPayload(payload);
        KNetPlatform.INSTANCE.sendPlayToTrackingEntityAndSelf(entity, payload);
    }

    /**
     * Sends a payload to all players tracking a chunk.
     *
     * @param world   the world that holds the chunk.
     * @param pos     the position of the chunk.
     * @param payload the payload to send.
     */
    public void sendPlayToTracking(@NotNull ServerWorld world, @NotNull ChunkPos pos, @NotNull P payload) {
        checkPayload(payload);
        KNetPlatform.INSTANCE.sendPlayToTrackingChunk(world, pos, payload);
    }

    /**
     * Sends a payload to all players tracking a block entity.
     *
     * @param be      the block entity that all receiver players should be tracking.
     * @param payload the payload.
     */
    public void sendPlayToTracking(@NotNull BlockEntity be, @NotNull P payload) {
        checkPayload(payload);
        KNetPlatform.INSTANCE.sendPlayToTrackingBlockEntity(be, payload);
    }

    /**
     * Sends a payload to all players tracking a block position.
     *
     * @param world   the world that holds the block.
     * @param pos     the position of the block.
     * @param payload the payload to send.
     */
    public void sendPlatyToTracking(@NotNull ServerWorld world, @NotNull BlockPos pos, @NotNull P payload) {
        checkPayload(payload);
        KNetPlatform.INSTANCE.sendPlayToTrackingBlock(world, pos, payload);
    }

    private void checkPayload(P payload) {
        if (payload.id() != id) throw new IllegalStateException("Payload id does not match channel id");
    }

    @Override
    public Identifier getId() {
        return id;
    }

    @Override
    public NetByteBuf.PacketReader<? extends NetPayload> getReader() {
        return reader;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void handleClientPayload(CustomPayload payload, PayloadHandlingContext ctx) throws PayloadHandlingException {
        if (clientHandler != null) {
            clientHandler.handle((P) payload, ctx);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void handleServerPayload(CustomPayload payload, PayloadHandlingContext ctx) throws PayloadHandlingException {
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
