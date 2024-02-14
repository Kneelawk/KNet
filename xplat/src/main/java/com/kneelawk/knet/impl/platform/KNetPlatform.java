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

import java.util.ServiceLoader;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

public interface KNetPlatform {
    KNetPlatform INSTANCE = ServiceLoader.load(KNetPlatform.class).findFirst()
        .orElseThrow(() -> new RuntimeException("Unable to find KNet platform"));

    void sendPlayToAll(CustomPayload payload);

    void sendPlay(PlayerEntity player, CustomPayload payload);

    void sendPlayToServer(CustomPayload payload);

    void sendPlayToDimension(RegistryKey<World> dim, CustomPayload payload);

    void sendPlayToTrackingEntity(Entity entity, CustomPayload payload);

    void sendPlayToTrackingEntityAndSelf(Entity entity, CustomPayload payload);

    void sendPlayToTrackingChunk(ServerWorld world, ChunkPos pos, CustomPayload payload);

    default void sendPlayToTrackingBlockEntity(BlockEntity be, CustomPayload payload) {
        if (be.getWorld() instanceof ServerWorld serverWorld) {
            sendPlayToTrackingChunk(serverWorld, new ChunkPos(be.getPos()), payload);
        }
    }

    default void sendPlayToTrackingBlock(ServerWorld world, BlockPos pos, CustomPayload payload) {
        sendPlayToTrackingChunk(world, new ChunkPos(pos), payload);
    }
}
