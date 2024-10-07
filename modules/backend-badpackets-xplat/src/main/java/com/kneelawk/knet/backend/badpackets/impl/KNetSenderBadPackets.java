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

package com.kneelawk.knet.backend.badpackets.impl;

import lol.bai.badpackets.api.PacketSender;

import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;

import com.kneelawk.knet.api.KNetSender;
import com.kneelawk.knet.backend.badpackets.impl.proxy.CommonProxy;

public class KNetSenderBadPackets implements KNetSender {

    @Override
    public void sendPlayToClient(ServerPlayer player, CustomPacketPayload payload) {
        PacketSender.s2c(player).send(payload);
    }

    @Override
    public void sendPlayToServer(CustomPacketPayload payload) {
        PacketSender.c2s().send(payload);
    }

    @Override
    public void disconnectFromServer(Component message) {
        CommonProxy.getInstance().disconnectFromServer(message);
    }

    @Override
    public boolean clientHasPlayChannel(ServerPlayer player, CustomPacketPayload.Type<?> channel) {
        return PacketSender.s2c(player).canSend(channel);
    }

    @Override
    public boolean serverHasPlayChannel(CustomPacketPayload.Type<?> channel) {
        return PacketSender.c2s().canSend(channel);
    }
}
