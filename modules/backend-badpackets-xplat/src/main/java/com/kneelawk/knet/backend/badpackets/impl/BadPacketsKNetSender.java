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

package com.kneelawk.knet.backend.badpackets.impl;

import java.util.Set;

import lol.bai.badpackets.api.PacketSender;

import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerPlayerConnection;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkSource;

import com.kneelawk.knet.api.KNetSender;
import com.kneelawk.knet.backend.badpackets.impl.mixin.impl.Accessor_ChunkMap;
import com.kneelawk.knet.backend.badpackets.impl.mixin.impl.Accessor_TrackedEntity;
import com.kneelawk.knet.backend.badpackets.impl.proxy.CommonProxy;

public class BadPacketsKNetSender implements KNetSender {
    @Override
    public void sendPlayToAll(CustomPacketPayload payload) {
        for (ServerPlayer player : ServerHolder.getCurrentServer().getPlayerList().getPlayers()) {
            PacketSender.s2c(player).send(payload);
        }
    }

    @Override
    public void sendPlay(Player player, CustomPacketPayload payload) {
        if (player instanceof ServerPlayer serverPlayer) {
            PacketSender.s2c(serverPlayer).send(payload);
        } else if (player.level().isClientSide()) {
            PacketSender.c2s().send(payload);
        }
    }

    @Override
    public void sendPlayToServer(CustomPacketPayload payload) {
        PacketSender.c2s().send(payload);
    }

    @Override
    public void sendPlayToDimension(ServerLevel dim, CustomPacketPayload payload) {
        for (ServerPlayer player : dim.players()) {
            PacketSender.s2c(player).send(payload);
        }
    }

    private static Set<ServerPlayerConnection> getPlayersWatching(Entity entity) {
        ChunkSource chunkSource = entity.level().getChunkSource();
        if (!(chunkSource instanceof ServerChunkCache chunkCache)) {
            KNBPLog.LOG.warn("Tried to get the players watching an entity: {} on client", entity,
                new RuntimeException("Stack Trace"));
            return Set.of();
        }

        ChunkMap chunkMap = chunkCache.chunkMap;
        Accessor_TrackedEntity tracked =
            ((Accessor_ChunkMap) chunkMap).knet_backend_badpackets$entityMap().get(entity.getId());

        if (tracked != null) {
            return tracked.knet_backend_badpackets$seenBy();
        } else {
            return Set.of();
        }
    }

    @Override
    public void sendPlayToTrackingEntity(Entity entity, CustomPacketPayload payload) {
        for (ServerPlayerConnection connection : getPlayersWatching(entity)) {
            ServerPlayer player = connection.getPlayer();
            if (player != entity) {
                PacketSender.s2c(player).send(payload);
            }
        }
    }

    @Override
    public void sendPlayToTrackingEntityAndSelf(Entity entity, CustomPacketPayload payload) {
        boolean sentToPlayer = false;
        for (ServerPlayerConnection connection : getPlayersWatching(entity)) {
            ServerPlayer player = connection.getPlayer();
            PacketSender.s2c(player).send(payload);
            if (player == entity) {
                sentToPlayer = true;
            }
        }
        if (!sentToPlayer && entity instanceof ServerPlayer player) {
            PacketSender.s2c(player).send(payload);
        }
    }

    @Override
    public void sendPlayToTrackingChunk(ServerLevel level, ChunkPos pos, CustomPacketPayload payload) {
        for (ServerPlayer player : level.getChunkSource().chunkMap.getPlayers(pos, false)) {
            PacketSender.s2c(player).send(payload);
        }
    }

    @Override
    public void disconnectFromServer(Component message) {
        CommonProxy.getInstance().disconnectFromServer(message);
    }

    @Override
    public boolean clientHasPlayChannel(ServerPlayer player, CustomPacketPayload.Type<?> channel) {
        return PacketSender.s2c(player).canSend(channel);
    }

    @Override
    public boolean serverHasPlayChannel(CustomPacketPayload.Type<?> channel) {
        return PacketSender.c2s().canSend(channel);
    }
}
