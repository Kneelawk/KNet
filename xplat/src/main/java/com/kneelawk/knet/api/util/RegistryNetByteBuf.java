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

import java.util.function.Function;

import io.netty.buffer.ByteBuf;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.registry.DynamicRegistryManager;

/**
 * A {@link NetByteBuf} bound to a particular {@link DynamicRegistryManager} instance.
 */
public class RegistryNetByteBuf extends NetByteBuf {
    /**
     * Returns the given {@link RegistryByteBuf} as a {@link NetByteBuf}.
     *
     * @param buf the buffer to be converted into a {@link NetByteBuf}.
     * @return the given buffer as a {@link NetByteBuf}.
     */
    public static RegistryNetByteBuf asNetByteBuf(RegistryByteBuf buf) {
        return asNetByteBuf(buf, false);
    }

    /**
     * Returns the given {@link RegistryByteBuf} as a {@link NetByteBuf} but with passthrough enabled.
     *
     * @param buf the buffer to be converted into a {@link NetByteBuf}.
     * @return the given buffer as a {@link NetByteBuf}.
     */
    public static RegistryNetByteBuf asPassthroughNetByteBuf(RegistryByteBuf buf) {
        return asNetByteBuf(buf, true);
    }

    /**
     * Returns the given {@link RegistryByteBuf} as a {@link NetByteBuf}, with passthrough optionally enabled.
     *
     * @param buf         the buffer to be converted into a {@link NetByteBuf}.
     * @param passthrough whether to disable optimizations on the resulting buffer.
     * @return the given buffer as a {@link NetByteBuf}.
     */
    public static RegistryNetByteBuf asNetByteBuf(RegistryByteBuf buf, boolean passthrough) {
        return new RegistryNetByteBuf(buf, passthrough, buf.getRegistryManager());
    }

    /**
     * Returns the given {@link ByteBuf} as a {@link RegistryNetByteBuf}, attaching a registry manager.
     *
     * @param buf             the buffer to be converted into a {@link RegistryNetByteBuf}.
     * @param registryManager the registry manager to attach.
     * @return the given buffer as a {@link RegistryNetByteBuf}.
     */
    public static RegistryNetByteBuf asNetByteBuf(ByteBuf buf, DynamicRegistryManager registryManager) {
        return asNetByteBuf(buf, false, registryManager);
    }

    /**
     * Returns the given {@link ByteBuf} as a {@link RegistryNetByteBuf}, attaching a registry manager, but with
     * passthrough enabled.
     *
     * @param buf             the buffer to be converted into a {@link RegistryNetByteBuf}.
     * @param registryManager the registry manager to attach.
     * @return the given buffer as a {@link RegistryNetByteBuf}.
     */
    public static RegistryNetByteBuf asPassthroughNetByteBuf(ByteBuf buf, DynamicRegistryManager registryManager) {
        return asNetByteBuf(buf, true, registryManager);
    }

    /**
     * Returns the given {@link ByteBuf} as a {@link RegistryNetByteBuf}, attaching a registry manager, but with
     * passthrough optionally enabled.
     *
     * @param buf             the buffer to be converted into a {@link RegistryNetByteBuf}.
     * @param passthrough     whether to disable optimizations on the resulting buffer.
     * @param registryManager the registry manager to attach.
     * @return the given buffer as a {@link RegistryNetByteBuf}.
     */
    public static RegistryNetByteBuf asNetByteBuf(ByteBuf buf, boolean passthrough,
                                                  DynamicRegistryManager registryManager) {
        if (buf instanceof RegistryNetByteBuf registryNetBuf && registryNetBuf.passthrough == passthrough &&
            registryNetBuf.registryManager == registryManager) {
            return registryNetBuf;
        } else {
            return new RegistryNetByteBuf(buf, passthrough, registryManager);
        }
    }

    /**
     * Creates a function that wraps a {@link ByteBuf}, attaching the given registry manager.
     *
     * @param registryManager the registry manager to attach.
     * @return function that wraps any {@link ByteBuf} that is passed to it.
     */
    public static Function<ByteBuf, RegistryNetByteBuf> makeFactory(DynamicRegistryManager registryManager) {
        return buf -> asNetByteBuf(buf, registryManager);
    }

    /**
     * Creates a {@link PacketCodec} that converts any {@link RegistryByteBuf} passed to it into a {@link RegistryNetByteBuf}.
     *
     * @param codec the codec that expects a {@link RegistryNetByteBuf}.
     * @param <T>   the type the codec encodes/decodes.
     * @return the codec that converts byte buffers.
     */
    public static <T> PacketCodec<RegistryByteBuf, T> registryCodec(PacketCodec<? super RegistryNetByteBuf, T> codec) {
        return new PacketCodec<>() {
            @Override
            public T decode(RegistryByteBuf buf) {
                return codec.decode(asNetByteBuf(buf));
            }

            @Override
            public void encode(RegistryByteBuf buf, T value) {
                codec.encode(asNetByteBuf(buf), value);
            }
        };
    }

    /**
     * Creates a {@link PacketCodec} that wraps any {@link PacketByteBuf} passed to it in a {@link RegistryNetByteBuf},
     * attaching a registry manager.
     *
     * @param codec           the codec that expects a {@link RegistryNetByteBuf}.
     * @param registryManager the registry manager to attach.
     * @param <T>             the type the codec encodes/decodes.
     * @return the codec that wraps byte buffers.
     */
    public static <T> PacketCodec<PacketByteBuf, T> registryCodec(PacketCodec<? super RegistryNetByteBuf, T> codec,
                                                                  DynamicRegistryManager registryManager) {
        return new PacketCodec<>() {
            @Override
            public T decode(PacketByteBuf buf) {
                return codec.decode(asNetByteBuf(buf, registryManager));
            }

            @Override
            public void encode(PacketByteBuf buf, T value) {
                codec.encode(asNetByteBuf(buf, registryManager), value);
            }
        };
    }

    private final DynamicRegistryManager registryManager;

    /**
     * Creates a new {@link RegistryNetByteBuf}.
     *
     * @param wrapped         the buffer that this buffer wraps.
     * @param registryManager the registry manager to attach.
     */
    public RegistryNetByteBuf(ByteBuf wrapped, DynamicRegistryManager registryManager) {
        super(wrapped);
        this.registryManager = registryManager;
    }

    /**
     * Creates a new {@link RegistryNetByteBuf}, with passthrough optionally enabled.
     *
     * @param wrapped         the buffer that this buffer wraps.
     * @param passthrough     whether to disable optimizations.
     * @param registryManager the registry manager to attach.
     */
    public RegistryNetByteBuf(ByteBuf wrapped, boolean passthrough, DynamicRegistryManager registryManager) {
        super(wrapped, passthrough);
        this.registryManager = registryManager;
    }

    /**
     * Gets this buffer's attached registry manager.
     *
     * @return this buffer's attached registry manager.
     */
    public DynamicRegistryManager getRegistryManager() {
        return registryManager;
    }
}
