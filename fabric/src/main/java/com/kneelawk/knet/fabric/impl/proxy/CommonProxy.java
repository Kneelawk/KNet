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

package com.kneelawk.knet.fabric.impl.proxy;

import java.lang.reflect.InvocationTargetException;

import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;

import net.minecraft.text.Text;

import com.kneelawk.knet.api.channel.Channel;
import com.kneelawk.knet.api.handling.PayloadHandlingDisconnectException;
import com.kneelawk.knet.api.handling.PayloadHandlingSilentException;
import com.kneelawk.knet.api.util.NetByteBuf;
import com.kneelawk.knet.fabric.impl.FabricPayloadHandlingContext;
import com.kneelawk.knet.impl.KNetLog;

public class CommonProxy {
    private static final CommonProxy INSTANCE;

    static {
        CommonProxy instance = new CommonProxy();
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            try {
                instance = (CommonProxy) CommonProxy.class.getClassLoader()
                    .loadClass("com.kneelawk.knet.fabric.impl.proxy.ClientProxy").getConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
        INSTANCE = instance;
    }

    public static CommonProxy getInstance() {
        return INSTANCE;
    }

    public boolean isPhysicalClient() {
        return false;
    }

    public void registerPlayChannel(Channel channel) {
        if (channel.isToServer()) {
            ServerPlayNetworking.registerGlobalReceiver(channel.getId(),
                (server, player, handler, buf, responseSender) -> {
                    try {
                        channel.handleServerPayload(channel.getReader().apply(NetByteBuf.asNetByteBuf(buf)),
                            new FabricPayloadHandlingContext(server, player, handler::disconnect));
                    } catch (PayloadHandlingSilentException e) {
                        // do nothing
                    } catch (PayloadHandlingDisconnectException e) {
                        handler.disconnect(Text.literal("Channel " + channel.getId() + " error: " + e.getMessage()));
                    } catch (Exception e) {
                        // just log as an error by default
                        KNetLog.LOG.error("Channel {} error:", channel.getId(), e);
                    }
                });
        }
    }
}
