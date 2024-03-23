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

package com.kneelawk.knet.api.channel;

import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import com.kneelawk.knet.api.handling.PayloadHandlingContext;
import com.kneelawk.knet.api.handling.PayloadHandlingException;
import com.kneelawk.knet.api.util.NetByteBuf;

/**
 * A channel that can be registered with a platform.
 */
public interface Channel {
    /**
     * Gets this channel's id.
     *
     * @return this channel's id.
     */
    Identifier getId();

    /**
     * Gets this channel's payload reader.
     *
     * @return this channel's payload reader.
     */
    NetByteBuf.PacketReader<? extends NetPayload> getReader();

    /**
     * Called by net-util platform code when this channel receives a payload on the client-side.
     *
     * @param payload the payload received.
     * @param ctx     the context used for applying the payload.
     * @throws PayloadHandlingException if an error occurred while handling the payload.
     */
    void handleClientPayload(CustomPayload payload, PayloadHandlingContext ctx) throws PayloadHandlingException;

    /**
     * Called by net-util platform code when this channel receives a payload on the server-side.
     *
     * @param payload the payload received.
     * @param ctx     the context used for applying the payload.
     * @throws PayloadHandlingException if an error occurred while handling the payload.
     */
    void handleServerPayload(CustomPayload payload, PayloadHandlingContext ctx) throws PayloadHandlingException;

    /**
     * Gets whether this channel receives payloads on the server.
     * <p>
     * Note: channels can receive on both server and client.
     *
     * @return {@code true} if this channel receives on the server.
     */
    boolean isToServer();

    /**
     * Gets whether this channel receives payloads on the client.
     * <p>
     * Note: channels can receive on both server and client.
     *
     * @return {@code true} if this channel receives on the client.
     */
    boolean isToClient();
}
