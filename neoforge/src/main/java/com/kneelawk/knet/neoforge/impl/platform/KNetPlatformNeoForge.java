package com.kneelawk.knet.neoforge.impl.platform;

import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.PacketDistributor;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.ChunkPos;

import com.kneelawk.knet.impl.KNetLog;
import com.kneelawk.knet.impl.platform.KNetPlatform;
import com.kneelawk.knet.neoforge.impl.proxy.CommonProxy;

public class KNetPlatformNeoForge implements KNetPlatform {
    @Override
    public void sendPlayToAll(CustomPayload payload) {
        PacketDistributor.sendToAllPlayers(payload);
    }

    @Override
    public void sendPlay(PlayerEntity player, CustomPayload payload) {
        if (player.getWorld().isClient()) {
            PacketDistributor.sendToServer(payload);
        } else if (player instanceof ServerPlayerEntity serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, payload);
        }
    }

    @Override
    public void sendPlayToServer(CustomPayload payload) {
        if (FMLEnvironment.dist.isClient()) {
            PacketDistributor.sendToServer(payload);
        } else {
            KNetLog.LOG.warn("Attempted to send payload {} to the server from the server-side.", payload.getId());
        }
    }

    @Override
    public void sendPlayToDimension(ServerWorld dim, CustomPayload payload) {
        PacketDistributor.sendToPlayersInDimension(dim, payload);
    }

    @Override
    public void sendPlayToTrackingEntity(Entity entity, CustomPayload payload) {
        PacketDistributor.sendToPlayersTrackingEntity(entity, payload);
    }

    @Override
    public void sendPlayToTrackingEntityAndSelf(Entity entity, CustomPayload payload) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, payload);
    }

    @Override
    public void sendPlayToTrackingChunk(ServerWorld world, ChunkPos pos, CustomPayload payload) {
        PacketDistributor.sendToPlayersTrackingChunk(world, pos, payload);
    }

    @Override
    public void disconnectFromServer(Text message) {
        CommonProxy.getInstance().disconnectFromServer(message);
    }

    @Override
    public boolean clientHasPlayChannel(ServerPlayerEntity player, CustomPayload.Id<?> channel) {
        return player.networkHandler.hasChannel(channel);
    }

    @Override
    public boolean serverHasPlayChannel(CustomPayload.Id<?> channel) {
        return CommonProxy.getInstance().serverHasPlayChannel(channel);
    }
}
