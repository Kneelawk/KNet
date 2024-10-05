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

import org.jetbrains.annotations.Nullable;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerConfigurationConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;

import net.minecraft.server.MinecraftServer;

import com.kneelawk.knet.api.KNet;
import com.kneelawk.knet.api.event.ConnectionConfigCallback;
import com.kneelawk.knet.backend.fabric.impl.phase.config.FabricConfigTaskQueue;

public class KNetFabricMod implements ModInitializer {
    public static @Nullable MinecraftServer currentServer;

    @Override
    public void onInitialize() {
        KNBFLog.LOG.info("Initializing KNet {}",
            FabricLoader.getInstance().getModContainer(KNBFConstants.MOD_ID).get().getMetadata().getVersion());

        ServerLifecycleEvents.SERVER_STARTING.register(server -> currentServer = server);
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> currentServer = null);

        ServerConfigurationConnectionEvents.CONFIGURE.register(
            (handler, server) -> {
                FabricConfigTaskQueue queue = new FabricConfigTaskQueue(handler);
                KNet.load();
                FabricKNet.INSTANCE.connectionConfig().invoker().enqueueTasks(queue);
                ConnectionConfigCallback.EVENT.invoker().enqueueTasks(queue);
            });

        KNet.load();
        FabricKNet.INSTANCE.channelRegistration().invoker().registerChannels(ChannelRegistrationContext.INSTANCE);
    }
}
