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

package com.kneelawk.knet.api.phase.config;

import org.jetbrains.annotations.NotNull;

import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.network.ConfigurationTask;

import com.kneelawk.knet.api.channel.Channel;
import com.kneelawk.knet.api.handling.ConfigPayloadHandlingContext;
import com.kneelawk.knet.impl.phase.config.ConnectionConfigTaskWrapper;

/**
 * Used for enqueueing connection-configure tasks.
 */
public interface ConnectionConfigTaskQueue {
    /**
     * Gets whether the client has declared the ability to receive on the given channel.
     *
     * @param channel the channel to check if the receiver has.
     * @return {@code true} if the client has declared the ability to receive on the given channel.
     */
    boolean clientHasChannel(@NotNull CustomPacketPayload.Type<?> channel);

    /**
     * Gets whether the client has declared the ability to receive on the given channel.
     *
     * @param channel the channel to check if the receiver has.
     * @return {@code true} if the client has declared the ability to receive on the given channel.
     */
    default boolean clientHasChannel(@NotNull Channel channel) {
        return clientHasChannel(channel.getId());
    }

    /**
     * Disconnects the client with the given message.
     *
     * @param message the message to display when the client is disconnected.
     */
    void disconnect(@NotNull Component message);

    /**
     * Enqueues a raw {@link ConfigurationTask} task.
     * <p>
     * Use {@link #completeTask(ConfigurationTask.Type)} or
     * {@link ConfigPayloadHandlingContext#completeTask(ConfigurationTask.Type)} to mark the given task as
     * completed.
     *
     * @param task the task to enqueue.
     */
    void enqueueRaw(@NotNull ConfigurationTask task);

    /**
     * Enqueues the given task with the given id.
     * <p>
     * Use {@link #completeTask(ResourceLocation)} or {@link ConfigPayloadHandlingContext#completeTask(ResourceLocation)} to mark
     * the given task as completed.
     *
     * @param taskId the id of the task to enqueue.
     * @param task   the task to enqueue.
     */
    default void enqueue(@NotNull ResourceLocation taskId, @NotNull ConnectionConfigTask task) {
        enqueueRaw(new ConnectionConfigTaskWrapper(new ConfigurationTask.Type(taskId.toString()), task));
    }

    /**
     * Marks the given task as completed, so that the configuration can continue with the next task.
     *
     * @param taskId the id of the task that has been completed.
     */
    void completeTask(@NotNull ConfigurationTask.Type taskId);

    /**
     * Marks the given task as completed, so that the configuration can continue with the next task.
     *
     * @param taskId the id of the task that has been completed.
     */
    default void completeTask(@NotNull ResourceLocation taskId) {
        completeTask(new ConfigurationTask.Type(taskId.toString()));
    }
}
