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

import lol.bai.badpackets.api.config.ConfigPackets;

import net.minecraft.resources.ResourceLocation;

import com.kneelawk.knet.api.KNet;
import com.kneelawk.knet.api.KNetRegistrar;
import com.kneelawk.knet.api.KNetSender;
import com.kneelawk.knet.api.phase.config.ConfigTask;

public class BadPacketsKNet implements KNet {
    public static final BadPacketsKNet INSTANCE = new BadPacketsKNet();

    private final KNetSenderBadPackets sender = new KNetSenderBadPackets();

    @Override
    public KNetRegistrar getRegistrar(String modId, String networkVersion) {
        return new KNetRegistrarBadPackets();
    }

    @Override
    public void registerConfigTask(ResourceLocation taskId, ConfigTask task) {
        ConfigPackets.registerTask(taskId, context -> task.start(new BadPacketsConfigTaskContext(context)));
    }

    @Override
    public KNetSender getSender() {
        return sender;
    }
}
