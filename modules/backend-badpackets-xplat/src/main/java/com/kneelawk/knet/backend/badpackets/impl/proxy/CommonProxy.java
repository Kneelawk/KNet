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

package com.kneelawk.knet.backend.badpackets.impl.proxy;

import java.lang.reflect.InvocationTargetException;

import lol.bai.badpackets.api.config.ConfigPackets;
import lol.bai.badpackets.api.play.PlayPackets;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import com.kneelawk.knet.api.channel.ConfigChannel;
import com.kneelawk.knet.api.channel.PlayChannel;
import com.kneelawk.knet.api.handling.PayloadHandlingDisconnectException;
import com.kneelawk.knet.api.handling.PayloadHandlingSilentException;
import com.kneelawk.knet.api.util.NetCodecs;
import com.kneelawk.knet.backend.badpackets.impl.BadPacketsConfigPayloadHandlingContext;
import com.kneelawk.knet.backend.badpackets.impl.BadPacketsPlayPayloadHandlingContext;
import com.kneelawk.knet.backend.badpackets.impl.KNBPLog;
import com.kneelawk.knet.backend.badpackets.impl.Platform;

public class CommonProxy {
    private static final CommonProxy INSTANCE;

    static {
        CommonProxy instance = new CommonProxy();
        if (Platform.INSTANCE.isPhysicalClient()) {
            try {
                instance = (CommonProxy) CommonProxy.class.getClassLoader()
                    .loadClass("com.kneelawk.knet.backend.badpackets.impl.proxy.ClientProxy").getConstructor()
                    .newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        INSTANCE = instance;
    }

    public static CommonProxy getInstance() {
        return INSTANCE;
    }

    public void disconnectFromServer(Component message) {
        KNBPLog.LOG.warn("Attempted to disconnect from the server on the server side, with the message: {}", message);
    }

    @SuppressWarnings("unchecked")
    public void registerPlayChannel(PlayChannel channel) {
        if (channel.isToClient()) {
            PlayPackets.registerClientChannel((CustomPacketPayload.Type<CustomPacketPayload>) channel.getId(),
                (StreamCodec<RegistryFriendlyByteBuf, CustomPacketPayload>) NetCodecs.netRegToVanilla(
                    channel.getCodec()));
        }
        if (channel.isToServer()) {
            PlayPackets.registerServerChannel((CustomPacketPayload.Type<CustomPacketPayload>) channel.getId(),
                (StreamCodec<RegistryFriendlyByteBuf, CustomPacketPayload>) NetCodecs.netRegToVanilla(
                    channel.getCodec()));
            PlayPackets.registerServerReceiver(channel.getId(), (context, payload) -> {
                try {
                    channel.handleServerPayload(payload, new BadPacketsPlayPayloadHandlingContext(context));
                } catch (PayloadHandlingSilentException e) {
                    // do nothing
                } catch (PayloadHandlingDisconnectException e) {
                    context.handler()
                        .disconnect(Component.literal("Channel " + channel.getId().id() + " error: " + e.getMessage()));
                } catch (Exception e) {
                    KNBPLog.LOG.error("Channel {} error:", channel.getId().id(), e);
                }
            });
        }
    }

    @SuppressWarnings("unchecked")
    public void registerConfigChannel(ConfigChannel channel) {
        if (channel.isToClient()) {
            ConfigPackets.registerClientChannel((CustomPacketPayload.Type<CustomPacketPayload>) channel.getId(),
                (StreamCodec<FriendlyByteBuf, CustomPacketPayload>) NetCodecs.netToVanilla(
                    channel.getCodec()));
        }
        if (channel.isToServer()) {
            ConfigPackets.registerServerChannel((CustomPacketPayload.Type<CustomPacketPayload>) channel.getId(),
                (StreamCodec<FriendlyByteBuf, CustomPacketPayload>) NetCodecs.netToVanilla(
                    channel.getCodec()));
            ConfigPackets.registerServerReceiver(channel.getId(), (context, payload) -> {
                try {
                    channel.handleServerPayload(payload, new BadPacketsConfigPayloadHandlingContext(context));
                } catch (PayloadHandlingSilentException e) {
                    // do nothing
                } catch (PayloadHandlingDisconnectException e) {
                    context.handler()
                        .disconnect(Component.literal("Channel " + channel.getId().id() + " error: " + e.getMessage()));
                } catch (Exception e) {
                    KNBPLog.LOG.error("Channel {} error:", channel.getId().id(), e);
                }
            });
        }
    }
}
