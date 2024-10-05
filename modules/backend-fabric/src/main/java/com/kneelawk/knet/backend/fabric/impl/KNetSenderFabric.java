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

package com.kneelawk.knet.backend.fabric.impl;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;

import com.kneelawk.knet.api.KNetSender;
import com.kneelawk.knet.backend.fabric.impl.proxy.CommonProxy;

public class KNetSenderFabric implements KNetSender {
    @Override
    public void sendPlayToAll(CustomPacketPayload payload) {
        if (KNetFabricMod.currentServer != null) {
            PlayerLookup.all(KNetFabricMod.currentServer)
                .forEach(player -> ServerPlayNetworking.send(player, payload));
        } else {
            KNBFLog.LOG.warn("Attempted to send payload {} to all clients when no server is running on this side.",
                payload.type().id());
        }
    }

    @Override
    public void sendPlay(Player player, CustomPacketPayload payload) {
        if (player.level().isClientSide()) {
            ClientPlayNetworking.send(payload);
        } else if (player instanceof ServerPlayer serverPlayer) {
            ServerPlayNetworking.send(serverPlayer, payload);
        }
    }

    @Override
    public void sendPlayToServer(CustomPacketPayload payload) {
        if (CommonProxy.getInstance().isPhysicalClient()) {
            ClientPlayNetworking.send(payload);
        } else {
            KNBFLog.LOG.warn("Attempted to send payload {} to the server from the server-side.", payload.type().id());
        }
    }

    @Override
    public void sendPlayToDimension(ServerLevel dim, CustomPacketPayload payload) {
        PlayerLookup.world(dim).forEach(player -> ServerPlayNetworking.send(player, payload));
    }

    @Override
    public void sendPlayToTrackingEntity(Entity entity, CustomPacketPayload payload) {
        for (ServerPlayer player : PlayerLookup.tracking(entity)) {
            // no guarantees whether the player is in the tracking list or not
            if (player == entity) continue;
            ServerPlayNetworking.send(player, payload);
        }
    }

    @Override
    public void sendPlayToTrackingEntityAndSelf(Entity entity, CustomPacketPayload payload) {
        boolean sentToEntity = false;
        for (ServerPlayer player : PlayerLookup.tracking(entity)) {
            // no guarantees whether the player is in the tracking list or not
            if (player == entity) sentToEntity = true;
            ServerPlayNetworking.send(player, payload);
        }
        if (!sentToEntity && entity instanceof ServerPlayer player) {
            ServerPlayNetworking.send(player, payload);
        }
    }

    @Override
    public void sendPlayToTrackingChunk(ServerLevel level, ChunkPos pos, CustomPacketPayload payload) {
        PlayerLookup.tracking(level, pos).forEach(player -> ServerPlayNetworking.send(player, payload));
    }

    @Override
    public void disconnectFromServer(Component message) {
        CommonProxy.getInstance().disconnectFromServer(message);
    }

    @Override
    public boolean clientHasPlayChannel(ServerPlayer player, CustomPacketPayload.Type<?> channel) {
        return ServerPlayNetworking.canSend(player, channel);
    }

    @Override
    public boolean serverHasPlayChannel(CustomPacketPayload.Type<?> channel) {
        return CommonProxy.getInstance().serverHasPlayChannel(channel);
    }
}
