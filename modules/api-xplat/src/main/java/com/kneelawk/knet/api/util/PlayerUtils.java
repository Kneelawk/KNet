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

package com.kneelawk.knet.api.util;

import java.util.Collections;
import java.util.List;
import java.util.Set;

import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkSource;

import com.kneelawk.knet.impl.KNetLog;
import com.kneelawk.knet.impl.mixin.impl.Accessor_ChunkMap;
import com.kneelawk.knet.impl.mixin.impl.Accessor_TrackedEntity;

/**
 * Utility methods for getting players.
 */
public final class PlayerUtils {
    private PlayerUtils() {}

    /**
     * {@return all players currently connected to this server}
     */
    public static List<ServerPlayer> getAllPlayers() {
        return Collections.unmodifiableList(ServerAccess.getServer().getPlayerList().getPlayers());
    }

    /**
     * Gets all server player connections that are currently tracking an entity.
     * <p>
     * It is not defined whether the returned set will include the connection to the entity itself if that entity
     * is a player.
     *
     * @param entity the entity to get all players tracking.
     * @return all players tracking the given entity.
     */
    public static Set<ServerPlayerConnection> getConnectionsTracking(Entity entity) {
        ChunkSource chunkSource = entity.level().getChunkSource();
        if (!(chunkSource instanceof ServerChunkCache chunkCache)) {
            KNetLog.LOG.warn("Tried to get the players watching an entity: {} on client", entity,
                new RuntimeException("Stack Trace"));
            return Set.of();
        }

        ChunkMap chunkMap = chunkCache.chunkMap;
        Accessor_TrackedEntity tracked =
            ((Accessor_ChunkMap) chunkMap).knet_backend_badpackets$entityMap().get(entity.getId());

        return Collections.unmodifiableSet(tracked.knet_backend_badpackets$seenBy());
    }

    /**
     * Gets all server players that are currently tracking an entity.
     * <p>
     * It is not defined whether the returned set will include the entity itself it that entity is a player.
     *
     * @param entity the entity to get all players tracking.
     * @return all players tracking the given entity.
     */
    public static Set<ServerPlayer> getPlayersTracking(Entity entity) {
        // using an ObjectLinkedOpenHashSet because they generally have the fastest iteration speed
        return Collections.unmodifiableSet(
            getConnectionsTracking(entity).stream().map(ServerPlayerConnection::getPlayer)
                .collect(ObjectLinkedOpenHashSet.toSet()));
    }

    /**
     * Gets all players that are currently tracking a chunk.
     *
     * @param level the level the chunk is within.
     * @param pos   the position of the chunk.
     * @return all players tracking the given chunk.
     */
    public static List<ServerPlayer> getPlayersTracking(ServerLevel level, ChunkPos pos) {
        return level.getChunkSource().chunkMap.getPlayers(pos, false);
    }

    /**
     * Gets all players that are currently tracking the chunk the given block is within.
     *
     * @param level the level the block is within.
     * @param pos   the position of the block.
     * @return all players tracking the given block.
     */
    public static List<ServerPlayer> getPlayersTracking(ServerLevel level, BlockPos pos) {
        return getPlayersTracking(level, new ChunkPos(pos));
    }

    /**
     * Gets all players that are currently tracking the chunk the given block entity is within.
     *
     * @param be the block entity to get the players tracking.
     * @return all players tracking the given block entity.
     */
    public static List<ServerPlayer> getPlayersTracking(BlockEntity be) {
        if (be.getLevel() instanceof ServerLevel level) {
            return getPlayersTracking(level, be.getBlockPos());
        } else {
            throw new UnsupportedOperationException("Cannot get players tracking a block-entity on the client");
        }
    }
}
