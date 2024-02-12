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

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import net.minecraft.text.Text;

import com.kneelawk.knet.api.channel.Channel;
import com.kneelawk.knet.api.handling.PayloadHandlingDisconnectException;
import com.kneelawk.knet.api.handling.PayloadHandlingException;
import com.kneelawk.knet.api.handling.PayloadHandlingSilentException;
import com.kneelawk.knet.fabric.impl.FabricPayloadHandlingContext;
import com.kneelawk.knet.impl.KNetLog;

public class ClientProxy extends CommonProxy {
    public static void init() {
        CommonProxy.instance = new ClientProxy();
    }

    @Override
    public boolean isPhysicalClient() {
        return true;
    }

    @Override
    public void registerPlayChannel(Channel channel) {
        super.registerPlayChannel(channel);
        if (channel.isToClient()) {
            ClientPlayNetworking.registerGlobalReceiver(channel.getId(), (client, handler, buf, responseSender) -> {
                try {
                    channel.handleClientPayload(channel.getReader().apply(buf),
                        new FabricPayloadHandlingContext(client, client.player, handler.getConnection()::disconnect));
                } catch (PayloadHandlingSilentException e) {
                    // do nothing
                } catch (PayloadHandlingDisconnectException e) {
                    handler.getConnection()
                        .disconnect(Text.literal("Channel " + channel.getId() + " error: " + e.getMessage()));
                } catch (Exception e) {
                    // just log as an error by default
                    KNetLog.LOG.error("Channel {} error:", channel.getId(), e);
                }
            });
        }
    }
}
