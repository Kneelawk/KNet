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

package com.kneelawk.knet.api;

import java.util.Collection;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * Sender part of a KNet backend. Usually you will want to use {@link com.kneelawk.knet.api.channel.Channel}s instead,
 * by listening for the {@link KNet#channelRegistration()} callback.
 */
public interface KNetSender {
    /**
     * Send a raw payload to all players on the current server.
     * <p>
     * This can only be sent from the logical server.
     *
     * @param payload the payload to send.
     */
    void sendPlayToAll(CustomPacketPayload payload);

    /**
     * Send a raw payload to the given player.
     *
     * @param player  the player to send the payload to.
     * @param payload the paylod to send.
     */
    void sendPlay(Player player, CustomPacketPayload payload);

    /**
     * Send a raw payload from the client to the server.
     * <p>
     * This can only be sent from the logical client.
     *
     * @param payload the payload to send.
     */
    void sendPlayToServer(CustomPacketPayload payload);

    /**
     * Send a raw payload to all players in the given dimension.
     * <p>
     * This can only be sent from the logical server.
     *
     * @param dim     the level of the dimension to send the payload to all players in.
     * @param payload the payload to send.
     */
    void sendPlayToDimension(ServerLevel dim, CustomPacketPayload payload);

    /**
     * Send a raw payload to all players tracking an entity.
     * <p>
     * This can only be sent from the logical server.
     * <p>
     * This excludes sending the payload to the given entity if, even if it is a player.
     *
     * @param entity  the entity to send the payload to all the trackers of.
     * @param payload the payload to send.
     */
    void sendPlayToTrackingEntity(Entity entity, CustomPacketPayload payload);

    /**
     * Send a raw payload to all players tracking an entity, including the given entity if it is a player.
     * <p>
     * This can only be sent from the logical server.
     *
     * @param entity  the entity to send the payload to all the trackers of.
     * @param payload the payload to send.
     */
    void sendPlayToTrackingEntityAndSelf(Entity entity, CustomPacketPayload payload);

    /**
     * Send a raw payload to all players tracking a chunk.
     * <p>
     * This can only be sent from the logical server.
     *
     * @param level   the level that the chunk is within.
     * @param pos     the position of the chunk.
     * @param payload the payload to send.
     */
    void sendPlayToTrackingChunk(ServerLevel level, ChunkPos pos, CustomPacketPayload payload);

    /**
     * Send a raw payload to all players tracking a block entity.
     *
     * @param be      the block entity to send the payload to all the trackers of.
     * @param payload the payload to send.
     */
    default void sendPlayToTrackingBlockEntity(BlockEntity be, CustomPacketPayload payload) {
        if (be.getLevel() instanceof ServerLevel serverLevel) {
            sendPlayToTrackingChunk(serverLevel, new ChunkPos(be.getBlockPos()), payload);
        } else {
            sendPlayToServer(payload);
        }
    }

    /**
     * Send a raw payload to all players tracking a block.
     * <p>
     * This can only be sent from the logical server.
     *
     * @param level   the level that the block is within.
     * @param pos     the position of the block.
     * @param payload the payload to send.
     */
    default void sendPlayToTrackingBlock(ServerLevel level, BlockPos pos, CustomPacketPayload payload) {
        sendPlayToTrackingChunk(level, new ChunkPos(pos), payload);
    }

    /**
     * Send a raw payload to all players in the given collection.
     * <p>
     * This can only be sent from the logical server.
     *
     * @param players the collection of players to send the payload to.
     * @param payload the payload to send.
     */
    default void sendPlay(Collection<ServerPlayer> players, CustomPacketPayload payload) {
        for (ServerPlayer player : players) {
            sendPlay(player, payload);
        }
    }

    /**
     * Disconnects the current client from the server.
     *
     * @param message the message to show when this client disconnects.
     */
    void disconnectFromServer(Component message);

    /**
     * Checks to see if a client connected to the current server has a channel.
     *
     * @param player  the player to check the client of.
     * @param channel the channel to check for the existence of.
     * @return whether the given client has the given channel.
     */
    boolean clientHasPlayChannel(ServerPlayer player, CustomPacketPayload.Type<?> channel);

    /**
     * Checks to see if the server connected to this client has a channel.
     *
     * @param channel the channel to check for the existence of.
     * @return whether the server connected to this client has the given channel.
     */
    boolean serverHasPlayChannel(CustomPacketPayload.Type<?> channel);
}
