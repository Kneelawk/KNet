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

package com.kneelawk.knet.api.util;

import org.jetbrains.annotations.NotNull;

import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import com.kneelawk.knet.api.channel.Channel;
import com.kneelawk.knet.impl.platform.KNetPlatform;

/**
 * Represents a connection capable of sending a payload to a receiver.
 */
public interface PayloadConnection extends PayloadSender {
    /**
     * Creates a payload sender from a player.
     *
     * @param player the player to send payloads to.
     * @return a payload sender for the given player.
     */
    static PayloadConnection ofPlayer(ServerPlayerEntity player) {
        return new PayloadConnection() {
            @Override
            public void disconnect(@NotNull Text message) {
                player.networkHandler.disconnect(message);
            }

            @Override
            public void sendPayload(CustomPayload payload) {
                KNetPlatform.INSTANCE.sendPlay(player, payload);
            }

            @Override
            public boolean receiverHasChannel(CustomPayload.Id<?> channel) {
                return KNetPlatform.INSTANCE.clientHasPlayChannel(player, channel);
            }

            @Override
            public String toString() {
                return "PayloadSender(" + player.getGameProfile().getName() + ')';
            }
        };
    }

    /**
     * Creates a Payload sender that sends messages to the server during the 'play' phase.
     *
     * @return a payload sender that sends messages to the server during the 'play' phase.
     */
    static PayloadConnection ofPlayToServer() {
        return new PayloadConnection() {
            @Override
            public void disconnect(@NotNull Text message) {
                KNetPlatform.INSTANCE.disconnectFromServer(message);
            }

            @Override
            public void sendPayload(CustomPayload payload) {
                KNetPlatform.INSTANCE.sendPlayToServer(payload);
            }

            @Override
            public boolean receiverHasChannel(CustomPayload.Id<?> channel) {
                return KNetPlatform.INSTANCE.serverHasPlayChannel(channel);
            }

            @Override
            public String toString() {
                return "PayloadSender(to server)";
            }
        };
    }

    /**
     * Used to disconnect the client with the given message.
     *
     * @param message the message for the client to display when disconnected.
     */
    void disconnect(@NotNull Text message);

    /**
     * Gets whether the receiving end of this connection has declared the ability to receive on the given channel.
     *
     * @param channel the channel to check if the receiver has.
     * @return {@code true} if the receiver has declared the ability to receive on the given channel.
     */
    boolean receiverHasChannel(CustomPayload.Id<?> channel);

    /**
     * Gets whether the receiving end of this connection has declared the ability to receive on the given channel.
     *
     * @param channel the channel to check if the receiver has.
     * @return {@code true} if the receiver has declared the ability to receive on the given channel.
     */
    default boolean receiverHasChannel(Channel channel) {
        return receiverHasChannel(channel.getId());
    }
}
