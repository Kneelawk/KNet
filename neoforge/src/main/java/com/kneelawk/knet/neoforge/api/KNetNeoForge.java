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

import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.text.Text;

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
            registrar.playBidirectional((CustomPayload.Id<CustomPayload>) channel.getId(),
                (PacketCodec<RegistryByteBuf, CustomPayload>) NetCodecs.regNetToVanilla(channel.getCodec()),
                (payload, ctx) -> {
                    if (ctx.flow().isServerbound()) {
                        handleServerPlay(channel, payload, ctx);
                    } else {
                        handleClientPlay(channel, payload, ctx);
                    }
                });
        } else if (channel.isToServer()) {
            registrar.playToServer((CustomPayload.Id<CustomPayload>) channel.getId(),
                (PacketCodec<RegistryByteBuf, CustomPayload>) NetCodecs.regNetToVanilla(channel.getCodec()),
                (payload, ctx) -> handleServerPlay(channel, payload, ctx));
        } else if (channel.isToClient()) {
            registrar.playToClient((CustomPayload.Id<CustomPayload>) channel.getId(),
                (PacketCodec<RegistryByteBuf, CustomPayload>) NetCodecs.regNetToVanilla(channel.getCodec()),
                (payload, ctx) -> handleClientPlay(channel, payload, ctx));
        }
    }

    private static void handleServerPlay(PlayChannel channel, CustomPayload payload, IPayloadContext ctx) {
        try {
            channel.handleServerPayload(payload, new NeoForgePlayPayloadHandlingContext(ctx));
        } catch (PayloadHandlingSilentException e) {
            // do nothing
        } catch (PayloadHandlingDisconnectException e) {
            ctx.disconnect(Text.literal("Channel " + channel.getId() + " error: " + e.getMessage()));
        } catch (Exception e) {
            // just log as an error by default
            KNetLog.LOG.error("Channel {} error:", channel.getId(), e);
        }
    }

    private static void handleClientPlay(PlayChannel channel, CustomPayload payload, IPayloadContext ctx) {
        try {
            channel.handleClientPayload(payload, new NeoForgePlayPayloadHandlingContext(ctx));
        } catch (PayloadHandlingSilentException e) {
            // do nothing
        } catch (PayloadHandlingDisconnectException e) {
            ctx.disconnect(Text.literal("Channel " + channel.getId() + " error: " + e.getMessage()));
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
            registrar.configurationBidirectional((CustomPayload.Id<CustomPayload>) channel.getId(),
                (PacketCodec<PacketByteBuf, CustomPayload>) NetCodecs.netToVanilla(channel.getCodec()),
                (payload, ctx) -> {
                    if (ctx.flow().isServerbound()) {
                        handleServerConfig(channel, payload, ctx);
                    } else {
                        handleClientConfig(channel, payload, ctx);
                    }
                });
        } else if (channel.isToServer()) {
            registrar.configurationToServer((CustomPayload.Id<CustomPayload>) channel.getId(),
                (PacketCodec<PacketByteBuf, CustomPayload>) NetCodecs.netToVanilla(channel.getCodec()),
                (payload, ctx) -> handleServerConfig(channel, payload, ctx));
        } else if (channel.isToClient()) {
            registrar.configurationToClient((CustomPayload.Id<CustomPayload>) channel.getId(),
                (PacketCodec<PacketByteBuf, CustomPayload>) NetCodecs.netToVanilla(channel.getCodec()),
                (payload, ctx) -> handleClientConfig(channel, payload, ctx));
        }
    }

    private static void handleServerConfig(ConfigChannel channel, CustomPayload payload, IPayloadContext ctx) {
        try {
            channel.handleServerPayload(payload, new NeoForgeConfigPayloadHandlingContext(ctx));
        } catch (PayloadHandlingSilentException e) {
            // do nothing
        } catch (PayloadHandlingDisconnectException e) {
            ctx.disconnect(Text.literal("Channel " + channel.getId() + " error: " + e.getMessage()));
        } catch (Exception e) {
            // just log as an error by default
            KNetLog.LOG.error("Channel {} error:", channel.getId(), e);
        }
    }

    private static void handleClientConfig(ConfigChannel channel, CustomPayload payload, IPayloadContext ctx) {
        try {
            channel.handleClientPayload(payload, new NeoForgeConfigPayloadHandlingContext(ctx));
        } catch (PayloadHandlingSilentException e) {
            // do nothing
        } catch (PayloadHandlingDisconnectException e) {
            ctx.disconnect(Text.literal("Channel " + channel.getId() + " error: " + e.getMessage()));
        } catch (Exception e) {
            // just log as an error by default
            KNetLog.LOG.error("Channel {} error:", channel.getId(), e);
        }
    }
}
