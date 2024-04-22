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

package com.kneelawk.knet.neoforge.impl.proxy;

import net.neoforged.neoforge.common.extensions.IClientCommonPacketListenerExtension;
import net.neoforged.neoforge.common.extensions.IServerCommonPacketListenerExtension;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.text.Text;

public class ClientProxy extends CommonProxy {
    @Override
    public boolean isPhysicalClient() {
        return true;
    }

    @Override
    public void disconnectFromServer(Text message) {
        ClientPlayNetworkHandler networkHandler = MinecraftClient.getInstance().getNetworkHandler();
        if (networkHandler != null) {
            networkHandler.getConnection().disconnect(message);
        }
    }

    @Override
    public boolean hasChannel(PacketListener packetListener, CustomPayload.Id<?> channel) {
        if (packetListener instanceof IServerCommonPacketListenerExtension extension) {
            return extension.hasChannel(channel);
        } else if (packetListener instanceof IClientCommonPacketListenerExtension extension) {
            return extension.hasChannel(channel);
        } else {
            throw new AssertionError("PacketListener " + packetListener +
                " is not a Server/ClientCommonPacketListener. It came from a non-play, non-configuration phase");
        }
    }

    @Override
    public boolean serverHasPlayChannel(CustomPayload.Id<?> channel) {
        ClientPlayNetworkHandler networkHandler = MinecraftClient.getInstance().getNetworkHandler();
        if (networkHandler != null) {
            return networkHandler.hasChannel(channel);
        }
        return false;
    }
}
