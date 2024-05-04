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

package com.kneelawk.knet.neoforge.api;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import com.kneelawk.knet.api.channel.ConfigChannel;
import com.kneelawk.knet.api.channel.PlayChannel;
import com.kneelawk.knet.api.handling.PayloadHandlingDisconnectException;
import com.kneelawk.knet.api.handling.PayloadHandlingSilentException;
import com.kneelawk.knet.api.util.NetCodecs;
import com.kneelawk.knet.impl.KNetLog;
import com.kneelawk.knet.neoforge.impl.NeoForgeConfigPayloadHandlingContext;
import com.kneelawk.knet.neoforge.impl.NeoForgePlayPayloadHandlingContext;

/**
 * NeoForge-specific KNet public interface.
 * <p>
 * This is primarily used for registering channels.
 */
public class KNetNeoForge {
    private KNetNeoForge() {}

    /**
     * Registers a channel for receiving packets during play state.
     *
     * @param registrar the payload registrar received during the
     *                  {@link net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent} event.
     * @param channel   the channel to register.
     */
    @SuppressWarnings("unchecked")
    public static void registerPlay(PayloadRegistrar registrar, PlayChannel channel) {
        if (channel.isToServer() && channel.isToClient()) {
            registrar.playBidirectional((CustomPacketPayload.Type<CustomPacketPayload>) channel.getId(),
                (StreamCodec<RegistryFriendlyByteBuf, CustomPacketPayload>) NetCodecs.netRegToVanilla(channel.getCodec()),
                (payload, ctx) -> {
                    if (ctx.flow().isServerbound()) {
                        handleServerPlay(channel, payload, ctx);
                    } else {
                        handleClientPlay(channel, payload, ctx);
                    }
                });
        } else if (channel.isToServer()) {
            registrar.playToServer((CustomPacketPayload.Type<CustomPacketPayload>) channel.getId(),
                (StreamCodec<RegistryFriendlyByteBuf, CustomPacketPayload>) NetCodecs.netRegToVanilla(channel.getCodec()),
                (payload, ctx) -> handleServerPlay(channel, payload, ctx));
        } else if (channel.isToClient()) {
            registrar.playToClient((CustomPacketPayload.Type<CustomPacketPayload>) channel.getId(),
                (StreamCodec<RegistryFriendlyByteBuf, CustomPacketPayload>) NetCodecs.netRegToVanilla(channel.getCodec()),
                (payload, ctx) -> handleClientPlay(channel, payload, ctx));
        }
    }

    private static void handleServerPlay(PlayChannel channel, CustomPacketPayload payload, IPayloadContext ctx) {
        try {
            channel.handleServerPayload(payload, new NeoForgePlayPayloadHandlingContext(ctx));
        } catch (PayloadHandlingSilentException e) {
            // do nothing
        } catch (PayloadHandlingDisconnectException e) {
            ctx.disconnect(Component.literal("Channel " + channel.getId() + " error: " + e.getMessage()));
        } catch (Exception e) {
            // just log as an error by default
            KNetLog.LOG.error("Channel {} error:", channel.getId(), e);
        }
    }

    private static void handleClientPlay(PlayChannel channel, CustomPacketPayload payload, IPayloadContext ctx) {
        try {
            channel.handleClientPayload(payload, new NeoForgePlayPayloadHandlingContext(ctx));
        } catch (PayloadHandlingSilentException e) {
            // do nothing
        } catch (PayloadHandlingDisconnectException e) {
            ctx.disconnect(Component.literal("Channel " + channel.getId() + " error: " + e.getMessage()));
        } catch (Exception e) {
            // just log as an error by default
            KNetLog.LOG.error("Channel {} error:", channel.getId(), e);
        }
    }

    /**
     * Registers a channel for receiving packets during configuration state.
     *
     * @param registrar the payload registrar received during the
     *                  {@link net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent} event.
     * @param channel   the channel to register.
     */
    @SuppressWarnings("unchecked")
    public static void registerConfig(PayloadRegistrar registrar, ConfigChannel channel) {
        if (channel.isToServer() && channel.isToClient()) {
            registrar.configurationBidirectional((CustomPacketPayload.Type<CustomPacketPayload>) channel.getId(),
                (StreamCodec<FriendlyByteBuf, CustomPacketPayload>) NetCodecs.netToVanilla(channel.getCodec()),
                (payload, ctx) -> {
                    if (ctx.flow().isServerbound()) {
                        handleServerConfig(channel, payload, ctx);
                    } else {
                        handleClientConfig(channel, payload, ctx);
                    }
                });
        } else if (channel.isToServer()) {
            registrar.configurationToServer((CustomPacketPayload.Type<CustomPacketPayload>) channel.getId(),
                (StreamCodec<FriendlyByteBuf, CustomPacketPayload>) NetCodecs.netToVanilla(channel.getCodec()),
                (payload, ctx) -> handleServerConfig(channel, payload, ctx));
        } else if (channel.isToClient()) {
            registrar.configurationToClient((CustomPacketPayload.Type<CustomPacketPayload>) channel.getId(),
                (StreamCodec<FriendlyByteBuf, CustomPacketPayload>) NetCodecs.netToVanilla(channel.getCodec()),
                (payload, ctx) -> handleClientConfig(channel, payload, ctx));
        }
    }

    private static void handleServerConfig(ConfigChannel channel, CustomPacketPayload payload, IPayloadContext ctx) {
        try {
            channel.handleServerPayload(payload, new NeoForgeConfigPayloadHandlingContext(ctx));
        } catch (PayloadHandlingSilentException e) {
            // do nothing
        } catch (PayloadHandlingDisconnectException e) {
            ctx.disconnect(Component.literal("Channel " + channel.getId() + " error: " + e.getMessage()));
        } catch (Exception e) {
            // just log as an error by default
            KNetLog.LOG.error("Channel {} error:", channel.getId(), e);
        }
    }

    private static void handleClientConfig(ConfigChannel channel, CustomPacketPayload payload, IPayloadContext ctx) {
        try {
            channel.handleClientPayload(payload, new NeoForgeConfigPayloadHandlingContext(ctx));
        } catch (PayloadHandlingSilentException e) {
            // do nothing
        } catch (PayloadHandlingDisconnectException e) {
            ctx.disconnect(Component.literal("Channel " + channel.getId() + " error: " + e.getMessage()));
        } catch (Exception e) {
            // just log as an error by default
            KNetLog.LOG.error("Channel {} error:", channel.getId(), e);
        }
    }
}
