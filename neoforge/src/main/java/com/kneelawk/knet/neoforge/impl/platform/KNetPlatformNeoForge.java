package com.kneelawk.knet.neoforge.impl.platform;

import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.PacketDistributor;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;

import com.kneelawk.knet.impl.KNetLog;
import com.kneelawk.knet.impl.platform.KNetPlatform;

public class KNetPlatformNeoForge implements KNetPlatform {
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
}
