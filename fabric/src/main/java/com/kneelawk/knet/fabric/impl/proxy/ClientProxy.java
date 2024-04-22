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

package com.kneelawk.knet.fabric.impl.proxy;

import net.fabricmc.fabric.api.client.networking.v1.ClientConfigurationNetworking;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.text.Text;

import com.kneelawk.knet.api.channel.ConfigChannel;
import com.kneelawk.knet.api.channel.PlayChannel;
import com.kneelawk.knet.api.handling.PayloadHandlingDisconnectException;
import com.kneelawk.knet.api.handling.PayloadHandlingSilentException;
import com.kneelawk.knet.fabric.impl.client.ClientFabricConfigPayloadHandlingContext;
import com.kneelawk.knet.fabric.impl.client.ClientFabricPlayPayloadHandlingContext;
import com.kneelawk.knet.impl.KNetLog;

public class ClientProxy extends CommonProxy {
    @Override
    public boolean isPhysicalClient() {
        return true;
    }

    @Override
    public void registerPlayChannel(PlayChannel channel) {
        super.registerPlayChannel(channel);
        if (channel.isToClient()) {
            ClientPlayNetworking.registerGlobalReceiver(channel.getId(), (payload, ctx) -> {
                try {
                    channel.handleClientPayload(payload, new ClientFabricPlayPayloadHandlingContext(ctx));
                } catch (PayloadHandlingSilentException e) {
                    // do nothing
                } catch (PayloadHandlingDisconnectException e) {
                    ctx.responseSender()
                        .disconnect(Text.literal("Channel " + channel.getId() + " error: " + e.getMessage()));
                } catch (Exception e) {
                    // just log as an error by default
                    KNetLog.LOG.error("Channel {} error:", channel.getId(), e);
                }
            });
        }
    }

    @Override
    public void registerConfigChannel(ConfigChannel channel) {
        super.registerConfigChannel(channel);
        if (channel.isToClient()) {
            ClientConfigurationNetworking.registerGlobalReceiver(channel.getId(), (payload, ctx) -> {
                try {
                    channel.handleClientPayload(payload, new ClientFabricConfigPayloadHandlingContext(ctx));
                } catch (PayloadHandlingSilentException e) {
                    // do nothing
                } catch (PayloadHandlingDisconnectException e) {
                    ctx.responseSender()
                        .disconnect(Text.literal("Channel " + channel.getId() + " error: " + e.getMessage()));
                } catch (Exception e) {
                    // just log as an error by default
                    KNetLog.LOG.error("Channel {} error:", channel.getId(), e);
                }
            });
        }
    }

    @Override
    public void disconnectFromServer(Text message) {
        ClientPlayNetworkHandler networkHandler = MinecraftClient.getInstance().getNetworkHandler();
        if (networkHandler != null) {
            networkHandler.getConnection().disconnect(message);
        }
    }

    @Override
    public boolean serverHasPlayChannel(CustomPayload.Id<?> channel) {
        return ClientPlayNetworking.canSend(channel);
    }
}
