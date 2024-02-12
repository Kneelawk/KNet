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

package com.kneelawk.knet.api.handling;

import java.util.concurrent.Executor;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;

/**
 * Context used for applying a payload.
 * <p>
 * This is an abstraction over the various loader-specific contexts given when receiving a packet.
 */
public interface PayloadHandlingContext {
    /**
     * Gets the executor for running things on the main thread instead of the netty packet-handler threads.
     *
     * @return the main-thread executor.
     */
    @NotNull Executor getExecutor();

    /**
     * Gets the player that received this payload.
     *
     * @return the receiver player.
     */
    @Nullable PlayerEntity getPlayer();

    /**
     * Gets the world in which this payload was received.
     *
     * @return the world where this payload was received.
     */
    default @Nullable World getWorld() {
        PlayerEntity player = getPlayer();
        if (player == null) return null;
        return player.getWorld();
    }

    /**
     * Gets the player that received this payload or throws an exception if no player is present.
     *
     * @return the receiver player.
     * @throws PayloadHandlingException if this payload was received without a player to receive it.
     */
    default @NotNull PlayerEntity mustGetPlayer() throws PayloadHandlingException {
        PlayerEntity player = getPlayer();
        if (player == null) throw new PayloadHandlingErrorException("No player associated with this payload.");
        return player;
    }

    /**
     * Gets the world in which this payload was received or throws an exception if no world is available.
     *
     * @return the world where this payload was received.
     * @throws PayloadHandlingException if this payload was received without a world where it was received.
     */
    default @NotNull World mustGetWorld() throws PayloadHandlingException {
        World world = getWorld();
        if (world == null) throw new PayloadHandlingErrorException("No world associated with this payload.");
        return world;
    }

    /**
     * Used to disconnect the client with the given message.
     *
     * @param message the message for the client to display when disconnected.
     */
    void disconnect(@NotNull Text message);
}
