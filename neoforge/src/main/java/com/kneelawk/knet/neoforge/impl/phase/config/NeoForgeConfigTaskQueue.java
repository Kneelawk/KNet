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

package com.kneelawk.knet.neoforge.impl.phase.config;

import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.network.ConfigurationTask;
import net.neoforged.neoforge.network.event.RegisterConfigurationTasksEvent;

import org.jetbrains.annotations.NotNull;
import com.kneelawk.knet.api.phase.config.ConnectionConfigTaskQueue;

public record NeoForgeConfigTaskQueue(RegisterConfigurationTasksEvent event) implements ConnectionConfigTaskQueue {
    @Override
    public boolean clientHasChannel(CustomPacketPayload.@NotNull Type<?> channel) {
        return event.getListener().hasChannel(channel);
    }

    @Override
    public void disconnect(@NotNull Component message) {
        event.getListener().disconnect(message);
    }

    @Override
    public void enqueueRaw(@NotNull ConfigurationTask task) {
        event.register(task);
    }

    @Override
    public void completeTask(ConfigurationTask.@NotNull Type taskId) {
        event.getListener().finishCurrentTask(taskId);
    }
}
