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

package com.kneelawk.knet.neoforge.api;

import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.network.registration.IPayloadRegistrar;

import net.minecraft.text.Text;

import com.kneelawk.knet.api.channel.Channel;
import com.kneelawk.knet.api.handling.PayloadHandlingDisconnectException;
import com.kneelawk.knet.api.handling.PayloadHandlingException;
import com.kneelawk.knet.api.handling.PayloadHandlingSilentException;
import com.kneelawk.knet.impl.KNetLog;
import com.kneelawk.knet.neoforge.impl.NeoForgePayloadHandlingContext;

/**
 * NeoForge-specific KNet public interface.
 * <p>
 * This is primarily used for registering channels.
 */
public class KNetNeoForge {
    private KNetNeoForge() {}

    /**
     * Registers a channel for receiving packets during play state.
     *
     * @param registrar the payload registrar received during the
     *                  {@link net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent} event.
     * @param channel   the channel to register.
     */
    public static void registerPlay(IPayloadRegistrar registrar, Channel channel) {
        registrar.play(channel.getId(), channel.getReader().intoPacketReader(), handler -> {
            if (channel.isToServer()) {
                handler.server((payload, ctx) -> {
                    try {
                        channel.handleServerPayload(payload,
                            new NeoForgePayloadHandlingContext(ctx.workHandler()::execute, ctx.player().orElse(null),
                                ctx.packetHandler()::disconnect));
                    } catch (PayloadHandlingSilentException e) {
                        // do nothing
                    } catch (PayloadHandlingDisconnectException e) {
                        ctx.packetHandler()
                            .disconnect(Text.literal("Channel " + channel.getId() + " error: " + e.getMessage()));
                    } catch (Exception e) {
                        // just log as an error by default
                        KNetLog.LOG.error("Channel {} error:", channel.getId(), e);
                    }
                });
            }
            if (channel.isToClient() && FMLEnvironment.dist.isClient()) {
                handler.client((payload, ctx) -> {
                    try {
                        channel.handleClientPayload(payload,
                            new NeoForgePayloadHandlingContext(ctx.workHandler()::execute, ctx.player().orElse(null),
                                ctx.packetHandler()::disconnect));
                    } catch (PayloadHandlingSilentException e) {
                        // do nothing
                    } catch (PayloadHandlingDisconnectException e) {
                        ctx.packetHandler()
                            .disconnect(Text.literal("Channel " + channel.getId() + " error: " + e.getMessage()));
                    } catch (Exception e) {
                        // just log as an error by default
                        KNetLog.LOG.error("Channel {} error:", channel.getId(), e);
                    }
                });
            }
        });
    }
}
