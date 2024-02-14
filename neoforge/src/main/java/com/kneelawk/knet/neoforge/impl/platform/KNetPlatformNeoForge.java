package com.kneelawk.knet.neoforge.impl.platform;

import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.PacketDistributor;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

import com.kneelawk.knet.impl.KNetLog;
import com.kneelawk.knet.impl.platform.KNetPlatform;

public class KNetPlatformNeoForge implements KNetPlatform {
    @Override
    public void sendPlayToAll(CustomPayload payload) {
        PacketDistributor.ALL.noArg().send(payload);
    }

    @Override
    public void sendPlay(PlayerEntity player, CustomPayload payload) {
        if (player.getWorld().isClient()) {
            PacketDistributor.SERVER.noArg().send(payload);
        } else if (player instanceof ServerPlayerEntity serverPlayer) {
            PacketDistributor.PLAYER.with(serverPlayer).send(payload);
        }
    }

    @Override
    public void sendPlayToServer(CustomPayload payload) {
        if (FMLEnvironment.dist.isClient()) {
            PacketDistributor.SERVER.noArg().send(payload);
        } else {
            KNetLog.LOG.warn("Attempted to send payload {} to the server from the server-side.", payload.id());
        }
    }

    @Override
    public void sendPlayToDimension(RegistryKey<World> dim, CustomPayload payload) {
        PacketDistributor.DIMENSION.with(dim).send(payload);
    }

    @Override
    public void sendPlayToTrackingEntity(Entity entity, CustomPayload payload) {
        PacketDistributor.TRACKING_ENTITY.with(entity).send(payload);
    }

    @Override
    public void sendPlayToTrackingEntityAndSelf(Entity entity, CustomPayload payload) {
        PacketDistributor.TRACKING_ENTITY_AND_SELF.with(entity).send(payload);
    }

    @Override
    public void sendPlayToTrackingChunk(ServerWorld world, ChunkPos pos, CustomPayload payload) {
        PacketDistributor.TRACKING_CHUNK.with(world.getChunk(pos.x, pos.z)).send(payload);
    }
}
