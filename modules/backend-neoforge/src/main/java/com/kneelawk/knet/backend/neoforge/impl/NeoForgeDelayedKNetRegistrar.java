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

package com.kneelawk.knet.backend.neoforge.impl;

import java.util.Set;

import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet;

import com.kneelawk.knet.api.KNetRegistrar;
import com.kneelawk.knet.api.channel.ConfigChannel;
import com.kneelawk.knet.api.channel.PlayChannel;

public class NeoForgeDelayedKNetRegistrar implements KNetRegistrar {
    private final String networkVersion;
    private final Set<PlayChannel> playChannels = new ReferenceLinkedOpenHashSet<>();
    private final Set<ConfigChannel> configChannels = new ReferenceLinkedOpenHashSet<>();

    public NeoForgeDelayedKNetRegistrar(String networkVersion) {this.networkVersion = networkVersion;}

    @Override
    public void register(PlayChannel channel) {
        playChannels.add(channel);
    }

    @Override
    public void register(ConfigChannel channel) {
        configChannels.add(channel);
    }

    public String getNetworkVersion() {
        return networkVersion;
    }

    public Set<PlayChannel> getPlayChannels() {
        return playChannels;
    }

    public Set<ConfigChannel> getConfigChannels() {
        return configChannels;
    }
}
