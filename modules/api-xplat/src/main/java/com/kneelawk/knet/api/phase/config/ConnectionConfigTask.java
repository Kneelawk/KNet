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

import com.kneelawk.knet.api.handling.ConfigPayloadHandlingContext;
import com.kneelawk.knet.api.util.PayloadSender;

/**
 * Simplified version of {@link net.minecraft.server.network.ConfigurationTask}.
 * <p>
 * This is used to initiate a chain of back-and-forth messages to configure a specific aspect of the client. When
 * configuration is complete, the implementor must call either {@link ConnectionConfigTaskQueue#completeTask(ResourceLocation)}
 * or {@link ConfigPayloadHandlingContext#completeTask(ResourceLocation)}, to allow configuration to proceed to the next task.
 *
 * @deprecated Use {@link ConfigTask} instead as that is compatible with more backends.
 */
@Deprecated
@FunctionalInterface
public interface ConnectionConfigTask {
    /**
     * Sends the initial payload to the client.
     *
     * @param sender the payload sender that can send the payload to the client.
     */
    void sendInitialPayload(PayloadSender sender);
}
