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

package com.kneelawk.knet.impl.phase.config;

import java.util.function.Consumer;

import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.server.network.ServerPlayerConfigurationTask;

import com.kneelawk.knet.api.phase.config.ConnectionConfigTask;
import com.kneelawk.knet.api.util.PayloadSender;

public record ConnectionConfigTaskWrapper(Key key, ConnectionConfigTask task) implements ServerPlayerConfigurationTask {
    @Override
    public void sendPacket(Consumer<Packet<?>> sender) {
        task.sendInitialPayload(new PayloadSender() {
            @Override
            public void sendPayload(CustomPayload payload) {
                sender.accept(new CustomPayloadS2CPacket(payload));
            }

            @Override
            public String toString() {
                return "config task sender " + key.id();
            }
        });
    }

    @Override
    public Key getKey() {
        return key;
    }
}
