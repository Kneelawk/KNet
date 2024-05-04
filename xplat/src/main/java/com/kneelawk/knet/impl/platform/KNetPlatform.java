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

package com.kneelawk.knet.impl.platform;

import java.util.Collection;
import java.util.ServiceLoader;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface KNetPlatform {
    KNetPlatform INSTANCE = ServiceLoader.load(KNetPlatform.class).findFirst()
        .orElseThrow(() -> new RuntimeException("Unable to find KNet platform"));

    void sendPlayToAll(CustomPacketPayload payload);

    void sendPlay(Player player, CustomPacketPayload payload);

    void sendPlayToServer(CustomPacketPayload payload);

    void sendPlayToDimension(ServerLevel dim, CustomPacketPayload payload);

    void sendPlayToTrackingEntity(Entity entity, CustomPacketPayload payload);

    void sendPlayToTrackingEntityAndSelf(Entity entity, CustomPacketPayload payload);

    void sendPlayToTrackingChunk(ServerLevel world, ChunkPos pos, CustomPacketPayload payload);

    default void sendPlayToTrackingBlockEntity(BlockEntity be, CustomPacketPayload payload) {
        if (be.getLevel() instanceof ServerLevel serverWorld) {
            sendPlayToTrackingChunk(serverWorld, new ChunkPos(be.getBlockPos()), payload);
        } else {
            sendPlayToServer(payload);
        }
    }

    default void sendPlayToTrackingBlock(ServerLevel world, BlockPos pos, CustomPacketPayload payload) {
        sendPlayToTrackingChunk(world, new ChunkPos(pos), payload);
    }

    default void sendPlay(Collection<ServerPlayer> players, CustomPacketPayload payload) {
        for (ServerPlayer player : players) {
            sendPlay(player, payload);
        }
    }

    void disconnectFromServer(Component message);

    boolean clientHasPlayChannel(ServerPlayer player, CustomPacketPayload.Type<?> channel);

    boolean serverHasPlayChannel(CustomPacketPayload.Type<?> channel);
}
