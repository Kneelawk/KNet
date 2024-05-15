package com.kneelawk.knet.neoforge.impl.platform;

import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.PacketDistributor;

import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;

import com.kneelawk.knet.impl.KNetLog;
import com.kneelawk.knet.impl.platform.KNetPlatform;
import com.kneelawk.knet.neoforge.impl.proxy.CommonProxy;

public class KNetPlatformNeoForge implements KNetPlatform {
    @Override
    public void sendPlayToAll(CustomPacketPayload payload) {
        PacketDistributor.sendToAllPlayers(payload);
    }

    @Override
    public void sendPlay(Player player, CustomPacketPayload payload) {
        if (player.level().isClientSide()) {
            PacketDistributor.sendToServer(payload);
        } else if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, payload);
        }
    }

    @Override
    public void sendPlayToServer(CustomPacketPayload payload) {
        if (FMLEnvironment.dist.isClient()) {
            PacketDistributor.sendToServer(payload);
        } else {
            KNetLog.LOG.warn("Attempted to send payload {} to the server from the server-side.", payload.type());
        }
    }

    @Override
    public void sendPlayToDimension(ServerLevel dim, CustomPacketPayload payload) {
        PacketDistributor.sendToPlayersInDimension(dim, payload);
    }

    @Override
    public void sendPlayToTrackingEntity(Entity entity, CustomPacketPayload payload) {
        PacketDistributor.sendToPlayersTrackingEntity(entity, payload);
    }

    @Override
    public void sendPlayToTrackingEntityAndSelf(Entity entity, CustomPacketPayload payload) {
        PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, payload);
    }

    @Override
    public void sendPlayToTrackingChunk(ServerLevel level, ChunkPos pos, CustomPacketPayload payload) {
        PacketDistributor.sendToPlayersTrackingChunk(level, pos, payload);
    }

    @Override
    public void disconnectFromServer(Component message) {
        CommonProxy.getInstance().disconnectFromServer(message);
    }

    @Override
    public boolean clientHasPlayChannel(ServerPlayer player, CustomPacketPayload.Type<?> channel) {
        return player.connection.hasChannel(channel);
    }

    @Override
    public boolean serverHasPlayChannel(CustomPacketPayload.Type<?> channel) {
        return CommonProxy.getInstance().serverHasPlayChannel(channel);
    }
}
