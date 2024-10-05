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

import com.kneelawk.commonevents.api.Event;
import com.kneelawk.knet.api.KNet;
import com.kneelawk.knet.api.KNetSender;
import com.kneelawk.knet.api.event.ChannelRegistrationCallback;
import com.kneelawk.knet.api.event.ConnectionConfigCallback;

public class NeoForgeKNet implements KNet {
    public static final NeoForgeKNet INSTANCE = new NeoForgeKNet();

    private final KNetSenderNeoForge sender = new KNetSenderNeoForge();
    private final Event<ChannelRegistrationCallback> channelRegistration =
        Event.builderSimple(ChannelRegistrationCallback.class,
            KNBNFLog.warn("Error while firing channel registration event")).scanned(false).build();
    private final Event<ConnectionConfigCallback> connectionConfig =
        Event.builderSimple(ConnectionConfigCallback.class, KNBNFLog.warn("Error while firing connection config event"))
            .scanned(false).build();

    @Override
    public Event<ChannelRegistrationCallback> channelRegistration() {
        return channelRegistration;
    }

    @Override
    public Event<ConnectionConfigCallback> connectionConfig() {
        return connectionConfig;
    }

    @Override
    public KNetSender getSender() {
        return sender;
    }
}
