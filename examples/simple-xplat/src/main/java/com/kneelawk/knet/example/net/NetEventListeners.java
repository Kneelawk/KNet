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

package com.kneelawk.knet.example.net;

import net.minecraft.resources.ResourceLocation;

import com.kneelawk.commonevents.api.Listen;
import com.kneelawk.commonevents.api.Scan;
import com.kneelawk.knet.api.channel.NoContextConfigChannel;
import com.kneelawk.knet.api.event.KNetLoadedCallback;
import com.kneelawk.knet.api.handling.ConfigPayloadHandlingContext;
import com.kneelawk.knet.api.phase.config.ConnectionConfigTaskQueue;
import com.kneelawk.knet.example.KNetExample;

@Scan
public class NetEventListeners {
    public static final String NETWORK_VERSION = "1";
    public static final ResourceLocation PING_PONG_TASK = KNetExample.id("ping_pong");
    public static final NoContextConfigChannel<PingPongPayload> CONFIG_CHANNEL =
        NoContextConfigChannel.of(PingPongPayload.ID, PingPongPayload.CODEC).recvClient(
            NetEventListeners::receiveClient).recvServer(NetEventListeners::receiveServer);

    @Listen(KNetLoadedCallback.class)
    public static void onLoad(KNetLoadedCallback.Context ctx) {
        ctx.getDefault().channelRegistration()
            .register(ctx1 -> KNetExample.registerChannels(ctx1.getRegistrar(KNetExample.MOD_ID, NETWORK_VERSION)));
        ctx.getDefault().connectionConfig().register(NetEventListeners::enqueueTasks);
    }

    private static void enqueueTasks(ConnectionConfigTaskQueue queue) {
        KNetExample.LOGGER.info("Enqueueing configuration tasks...");
        queue.enqueue(PING_PONG_TASK, sender -> {
            KNetExample.LOGGER.info("Server sending config payload...");
            CONFIG_CHANNEL.send(sender, new PingPongPayload("ping"));
        });
    }

    private static void receiveClient(PingPongPayload payload, ConfigPayloadHandlingContext ctx) {
        KNetExample.LOGGER.info("Client received config payload: {}", payload);
        CONFIG_CHANNEL.send(ctx, new PingPongPayload("pong"));
    }

    private static void receiveServer(PingPongPayload payload, ConfigPayloadHandlingContext ctx) {
        KNetExample.LOGGER.info("Server received config payload: {}", payload);
        ctx.completeTask(PING_PONG_TASK);
    }
}
