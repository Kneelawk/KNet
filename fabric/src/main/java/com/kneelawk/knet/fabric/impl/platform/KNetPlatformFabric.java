package com.kneelawk.knet.fabric.impl.platform;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;

import com.kneelawk.knet.fabric.impl.proxy.CommonProxy;
import com.kneelawk.knet.impl.KNetLog;
import com.kneelawk.knet.impl.platform.KNetPlatform;

public class KNetPlatformFabric implements KNetPlatform {
    @Override
    public void sendPlay(PlayerEntity player, CustomPayload payload) {
        PacketByteBuf buf = PacketByteBufs.create();
        payload.write(buf);

        if (player.getWorld().isClient()) {
            ClientPlayNetworking.send(payload.id(), buf);
        } else if (player instanceof ServerPlayerEntity serverPlayer) {
            ServerPlayNetworking.send(serverPlayer, payload.id(), buf);
        }
    }

    @Override
    public void sendPlayToServer(CustomPayload payload) {
        if (CommonProxy.getInstance().isPhysicalClient()) {
            PacketByteBuf buf = PacketByteBufs.create();
            payload.write(buf);
            ClientPlayNetworking.send(payload.id(), buf);
        } else {
            KNetLog.LOG.warn("Attempted to send payload {} to the server from the server-side.", payload.id());
        }
    }
}
