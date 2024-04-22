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
 * Utility methods for working with KNet buffers.
 * <p>
 * Net buffer class hierarchy:
 * <pre>
 *               {@link PacketByteBuf}
 *                 /         \
 *   {@link RegistryByteBuf}      {@link NetByteBuf}
 *              |               |
 * {@link NetRegistryByteBuf}&lt;-&gt;{@link RegistryNetByteBuf}
 * </pre>
 *
 * @see NetBuf
 * @see NetByteBuf
 * @see RegistryNetByteBuf
 * @see NetRegistryByteBuf
 */
public final class NetBufs {
    private NetBufs() {}

    /**
     * Converts a {@link NetByteBuf} codec into a {@link PacketByteBuf} codec.
     *
     * @param codec the codec to convert.
     * @param <T>   the type of object the codec encodes/decodes.
     * @return the new codec.
     */
    public static <T> PacketCodec<PacketByteBuf, T> netToVanillaCodec(PacketCodec<? super NetByteBuf, T> codec) {
        return new PacketCodec<>() {
            @Override
            public T decode(PacketByteBuf buf) {
                return codec.decode(NetByteBuf.netOf(buf));
            }

            @Override
            public void encode(PacketByteBuf buf, T value) {
                codec.encode(NetByteBuf.netOf(buf), value);
            }
        };
    }

    /**
     * Converts a {@link RegistryNetByteBuf} codec into a {@link RegistryByteBuf} codec.
     *
     * @param codec the codec to convert.
     * @param <T>   the type of object the codec encodes/decodes.
     * @return the new codec.
     */
    public static <T> PacketCodec<RegistryByteBuf, T> regNetToVanillaCodec(
        PacketCodec<? super RegistryNetByteBuf, T> codec) {
        return new PacketCodec<>() {
            @Override
            public T decode(RegistryByteBuf buf) {
                return codec.decode(RegistryNetByteBuf.regNetOf(buf));
            }

            @Override
            public void encode(RegistryByteBuf buf, T value) {
                codec.encode(RegistryNetByteBuf.regNetOf(buf), value);
            }
        };
    }

    /**
     * Converts a {@link NetRegistryByteBuf} codec into a {@link RegistryByteBuf} codec.
     *
     * @param codec the codec to convert.
     * @param <T>   the type of object the codec encodes/decodes.
     * @return the new codec.
     */
    public static <T> PacketCodec<RegistryByteBuf, T> netRegToVanillaCodec(
        PacketCodec<? super NetRegistryByteBuf, T> codec) {
        return new PacketCodec<>() {
            @Override
            public T decode(RegistryByteBuf buf) {
                return codec.decode(NetRegistryByteBuf.netRegOf(buf));
            }

            @Override
            public void encode(RegistryByteBuf buf, T value) {
                codec.encode(NetRegistryByteBuf.netRegOf(buf), value);
            }
        };
    }

    /**
     * Converts a {@link NetRegistryByteBuf} codec into a {@link RegistryNetByteBuf} codec.
     *
     * @param codec the codec to convert.
     * @param <T>   the type of object the codec encodes/decodes.
     * @return the new codec.
     */
    public static <T> PacketCodec<RegistryNetByteBuf, T> netToRegCodec(
        PacketCodec<? super NetRegistryByteBuf, T> codec) {
        return new PacketCodec<>() {
            @Override
            public T decode(RegistryNetByteBuf buf) {
                return codec.decode(NetRegistryByteBuf.netRegOf(buf));
            }

            @Override
            public void encode(RegistryNetByteBuf buf, T value) {
                codec.encode(NetRegistryByteBuf.netRegOf(buf), value);
            }
        };
    }

    /**
     * Converts a {@link RegistryNetByteBuf} codec into a {@link NetRegistryByteBuf} codec.
     *
     * @param codec the codec to convert.
     * @param <T>   the type of object the codec encodes/decodes.
     * @return the new codec.
     */
    public static <T> PacketCodec<NetRegistryByteBuf, T> regToNetCodec(
        PacketCodec<? super RegistryNetByteBuf, T> codec) {
        return new PacketCodec<>() {
            @Override
            public T decode(NetRegistryByteBuf buf) {
                return codec.decode(RegistryNetByteBuf.regNetOf(buf));
            }

            @Override
            public void encode(NetRegistryByteBuf buf, T value) {
                codec.encode(RegistryNetByteBuf.regNetOf(buf), value);
            }
        };
    }

    /**
     * Creates a function that wraps a {@link ByteBuf} in a {@link RegistryNetByteBuf}, attaching the given registry
     * manager.
     *
     * @param registryManager the registry manager to attach.
     * @return the function that wraps buffers.
     */
    public static Function<ByteBuf, RegistryNetByteBuf> regNetFactory(DynamicRegistryManager registryManager) {
        return buf -> RegistryNetByteBuf.regNetOf(buf, registryManager);
    }

    /**
     * Creates a function that wraps a {@link ByteBuf} in a {@link NetRegistryByteBuf}, attaching the given registry
     * manager.
     *
     * @param registryManager the registry manager to attach.
     * @return the function that wraps buffers.
     */
    public static Function<ByteBuf, NetRegistryByteBuf> netRegFactory(DynamicRegistryManager registryManager) {
        return buf -> NetRegistryByteBuf.netRegOf(buf, registryManager);
    }
}
