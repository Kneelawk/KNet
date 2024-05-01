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

package com.kneelawk.knet.fabric.impl;

import java.util.concurrent.Executor;
import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.text.Text;

import com.kneelawk.knet.api.handling.PlayPayloadHandlingContext;

public record FabricPlayPayloadHandlingContext(ServerPlayNetworking.Context ctx) implements PlayPayloadHandlingContext {
    @Override
    public @NotNull Executor getExecutor() {
        // Fabric currently invokes the handlers on the main thread, but I'm not sure if that's part of the API contract.
        // Client and server executors will run immediately if on the main thread.
        Executor executor = KNetFabricMod.currentServer;
        if (executor != null) return executor;
        return Runnable::run;
    }

    @Override
    public PlayerEntity getPlayer() {
        return ctx.player();
    }

    @Override
    public void disconnect(@NotNull Text message) {
        ctx.responseSender().disconnect(message);
    }

    @Override
    public boolean receiverHasChannel(CustomPayload.Id<?> channel) {
        return ServerPlayNetworking.canSend(ctx.player(), channel);
    }

    @Override
    public void sendPayload(CustomPayload payload) {
        ctx.responseSender().sendPacket(payload);
    }
}
