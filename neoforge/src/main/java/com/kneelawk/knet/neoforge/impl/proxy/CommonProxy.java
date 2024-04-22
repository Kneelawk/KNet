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

package com.kneelawk.knet.neoforge.impl.proxy;

import java.lang.reflect.InvocationTargetException;

import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.common.extensions.IServerCommonPacketListenerExtension;

import net.minecraft.network.listener.PacketListener;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.text.Text;

import com.kneelawk.knet.impl.KNetLog;

public class CommonProxy {
    private static final CommonProxy INSTANCE;

    static {
        CommonProxy instance = new CommonProxy();
        if (FMLLoader.getDist().isClient()) {
            try {
                instance = (CommonProxy) CommonProxy.class.getClassLoader()
                    .loadClass("com.kneelawk.knet.neoforge.impl.proxy.ClientProxy").getConstructor().newInstance();
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

    public void disconnectFromServer(Text message) {
        KNetLog.LOG.warn("Attempted to disconnect from the server on the server side, with the message: {}", message);
    }

    public boolean hasChannel(PacketListener packetListener, CustomPayload.Id<?> channel) {
        // TODO: use the better method once NeoForge adds the extension I need
        if (packetListener instanceof IServerCommonPacketListenerExtension extension) {
            return extension.hasChannel(channel);
        } else {
            throw new AssertionError("PacketListener " + packetListener +
                " is not a ServerCommonPacketListener. It came from a non-play, non-configuration phase");
        }
    }

    public boolean serverHasPlayChannel(CustomPayload.Id<?> channel) {
        return false;
    }
}
