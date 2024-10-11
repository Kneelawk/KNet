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

import net.minecraft.resources.ResourceLocation;

import com.kneelawk.knet.api.channel.Channel;
import com.kneelawk.knet.api.handling.ConfigPayloadHandlingContext;
import com.kneelawk.knet.api.util.PayloadConnection;

/**
 * Implemented to create a custom configuration-phase task.
 * <p>
 * This is used to initiate a chain of back-and-forth messages to configure a specific aspect of the client. When
 * configuration is complete, the implementor must call either {@link ConnectionConfigTaskQueue#completeTask(ResourceLocation)}
 * or {@link ConfigPayloadHandlingContext#completeTask(ResourceLocation)}, to allow configuration to proceed to the next task.
 */
@FunctionalInterface
public interface ConfigTask {
    /**
     * Called every time a client connects to run this config task.
     * <p>
     * This task can use {@link Context#receiverHasChannel(Channel)} to check if the client has the given
     * channel. Then can return {@code false} to cancel this task if the client does not have the given channel.
     *
     * @param ctx the context that this config task can use to send packets, check for channels, disconnect the client,
     *            and finish the task.
     * @return whether to continue this task.
     */
    boolean start(Context ctx);

    /**
     * Context for a {@link ConfigTask}, used to manage a configuration task's lifecycle.
     */
    interface Context extends PayloadConnection {
        /**
         * Finishes the task with the given id, allowing the connection to progress.
         *
         * @param taskId the task id to finish.
         */
        void completeTask(ResourceLocation taskId);
    }
}
