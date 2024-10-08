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

import java.util.List;
import java.util.Map;

import net.neoforged.neoforge.network.event.RegisterConfigurationTasksEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.network.ConfigurationTask;

import com.kneelawk.knet.api.KNet;
import com.kneelawk.knet.api.KNetRegistrar;
import com.kneelawk.knet.api.KNetSender;
import com.kneelawk.knet.api.channel.ConfigChannel;
import com.kneelawk.knet.api.channel.PlayChannel;
import com.kneelawk.knet.api.phase.config.ConfigTask;
import com.kneelawk.knet.neoforge.api.KNetNeoForge;

public class NeoForgeKNet implements KNet {
    public static final NeoForgeKNet INSTANCE = new NeoForgeKNet();

    private final KNetSenderNeoForge sender = new KNetSenderNeoForge();
    private final List<NeoForgeDelayedKNetRegistrar> registrars = new ObjectArrayList<>();
    private final Map<ResourceLocation, ConfigTask> configTasks = new Object2ObjectLinkedOpenHashMap<>();

    public void registerPayloads(RegisterPayloadHandlersEvent event) {
        for (NeoForgeDelayedKNetRegistrar registrar : registrars) {
            PayloadRegistrar neoRegistrar = event.registrar(registrar.getNetworkVersion());
            for (PlayChannel playChannel : registrar.getPlayChannels()) {
                KNetNeoForge.registerPlay(neoRegistrar, playChannel);
            }
            for (ConfigChannel configChannel : registrar.getConfigChannels()) {
                KNetNeoForge.registerConfig(neoRegistrar, configChannel);
            }
        }
    }

    public void registerConfigTasks(RegisterConfigurationTasksEvent event) {
        for (var entry : configTasks.entrySet()) {
            event.register(
                new NeoForgeConfigTaskWrapper(new ConfigurationTask.Type(entry.getKey().toString()), entry.getValue(),
                    event.getListener()));
        }
    }

    @Override
    public KNetRegistrar getRegistrar(String networkVersion) {
        synchronized (registrars) {
            NeoForgeDelayedKNetRegistrar registrar = new NeoForgeDelayedKNetRegistrar(networkVersion);
            registrars.add(registrar);
            return registrar;
        }
    }

    @Override
    public void registerConfigTask(ResourceLocation taskId, ConfigTask task) {
        configTasks.putIfAbsent(taskId, task);
    }

    @Override
    public KNetSender getSender() {
        return sender;
    }
}
