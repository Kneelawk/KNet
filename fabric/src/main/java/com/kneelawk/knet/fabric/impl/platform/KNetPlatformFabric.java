package com.kneelawk.knet.fabric.impl.platform;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.PlayerLookup;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

import com.kneelawk.knet.fabric.impl.KNetFabricMod;
import com.kneelawk.knet.fabric.impl.proxy.CommonProxy;
import com.kneelawk.knet.impl.KNetLog;
import com.kneelawk.knet.impl.platform.KNetPlatform;

public class KNetPlatformFabric implements KNetPlatform {
    @Override
    public void sendPlayToAll(CustomPayload payload) {
        if (KNetFabricMod.currentServer != null) {
            PacketByteBuf buf = PacketByteBufs.create();
            payload.write(buf);
            PlayerLookup.all(KNetFabricMod.currentServer)
                .forEach(player -> ServerPlayNetworking.send(player, payload.id(), buf));
        } else {
            KNetLog.LOG.warn("Attempted to send payload {} to all clients when no server is running on this side.",
                payload.id());
        }
    }

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

    @Override
    public void sendPlayToDimension(RegistryKey<World> dim, CustomPayload payload) {
        if (KNetFabricMod.currentServer != null) {
            ServerWorld world = KNetFabricMod.currentServer.getWorld(dim);
            if (world != null) {
                PacketByteBuf buf = PacketByteBufs.create();
                payload.write(buf);
                PlayerLookup.world(world).forEach(player -> ServerPlayNetworking.send(player, payload.id(), buf));
            } else {
                KNetLog.LOG.warn("Attempted to send payload {} to world {} but that world does not exist.",
                    payload.id(), dim.getValue());
            }
        } else {
            KNetLog.LOG.warn(
                "Attempted to send payload {} to all clients in world {} but no server is running on this side.",
                payload.id(), dim.getValue());
        }
    }

    @Override
    public void sendPlayToTrackingEntity(Entity entity, CustomPayload payload) {
        PacketByteBuf buf = PacketByteBufs.create();
        payload.write(buf);
        for (ServerPlayerEntity player : PlayerLookup.tracking(entity)) {
            // no guarantees whether the player is in the tracking list or not
            if (player == entity) continue;
            ServerPlayNetworking.send(player, payload.id(), buf);
        }
    }

    @Override
    public void sendPlayToTrackingEntityAndSelf(Entity entity, CustomPayload payload) {
        PacketByteBuf buf = PacketByteBufs.create();
        payload.write(buf);
        boolean sentToEntity = false;
        for (ServerPlayerEntity player : PlayerLookup.tracking(entity)) {
            // no guarantees whether the player is in the tracking list or not
            if (player == entity) sentToEntity = true;
            ServerPlayNetworking.send(player, payload.id(), buf);
        }
        if (!sentToEntity && entity instanceof ServerPlayerEntity player) {
            ServerPlayNetworking.send(player, payload.id(), buf);
        }
    }

    @Override
    public void sendPlayToTrackingChunk(ServerWorld world, ChunkPos pos, CustomPayload payload) {
        PacketByteBuf buf = PacketByteBufs.create();
        payload.write(buf);
        PlayerLookup.tracking(world, pos).forEach(player -> ServerPlayNetworking.send(player, payload.id(), buf));
    }
}
