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

package com.kneelawk.knet.backend.fabric.impl;

import java.util.concurrent.Executor;

import org.jetbrains.annotations.NotNull;

import net.fabricmc.fabric.api.networking.v1.ServerConfigurationNetworking;

import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.network.ConfigurationTask;

import com.kneelawk.knet.api.handling.ConfigPayloadHandlingContext;

public record FabricConfigPayloadHandlingContext(ServerConfigurationNetworking.Context ctx) implements
    ConfigPayloadHandlingContext {
    @Override
    public @NotNull Executor getExecutor() {
        // Configuration payload handling *is* actually done on a netty thread after all
        Executor executor = KNetFabricMod.currentServer;
        if (executor != null) return executor;
        return Runnable::run;
    }

    @Override
    public void disconnect(@NotNull Component message) {
        ctx.responseSender().disconnect(message);
    }

    @Override
    public boolean receiverHasChannel(CustomPacketPayload.Type<?> channel) {
        return ServerConfigurationNetworking.canSend(ctx.networkHandler(), channel);
    }

    @Override
    public void sendPayload(CustomPacketPayload payload) {
        ctx.responseSender().sendPacket(payload);
    }

    @Override
    public void completeTask(ConfigurationTask.@NotNull Type taskId) {
        ctx.networkHandler().completeTask(taskId);
    }
}
