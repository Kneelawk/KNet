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

import java.util.concurrent.Executor;

import org.jetbrains.annotations.Nullable;

import lol.bai.badpackets.api.play.ServerPlayContext;

import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;

import com.kneelawk.knet.api.handling.PlayPayloadHandlingContext;

public record BadPacketsPlayPayloadHandlingContext(ServerPlayContext context) implements PlayPayloadHandlingContext {
    @Override
    public @Nullable Player getPlayer() {
        return context.player();
    }

    @Override
    public Executor getExecutor() {
        return context.server();
    }

    @Override
    public void disconnect(Component message) {
        context.handler().disconnect(message);
    }

    @Override
    public boolean receiverHasChannel(CustomPacketPayload.Type<?> channel) {
        return context.canSend(channel);
    }

    @Override
    public void sendPayload(CustomPacketPayload payload) {
        context.send(payload);
    }
}
