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

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.util.math.BlockPos;

/**
 * NetBuf Codec utilities.
 */
public class NetCodecs {
    private NetCodecs() {}

    /**
     * KNet optimized version of {@link BlockPos#PACKET_CODEC}.
     */
    public static final PacketCodec<NetByteBuf, BlockPos> BLOCK_POS =
        PacketCodec.ofStatic(NetBuf::writeBlockPos, NetBuf::readBlockPos);

    /**
     * KNet optimized version of {@link PacketCodecs#BOOL}.
     */
    public static final PacketCodec<NetByteBuf, Boolean> BOOL =
        PacketCodec.ofStatic(NetBuf::writeBoolean, NetBuf::readBoolean);

    /**
     * Version of {@link PacketCodecs#VAR_INT} that handles negative integers properly.
     * <p>
     * Use {@link PacketCodecs#VAR_INT} for unsigned integers.
     */
    public static final PacketCodec<NetByteBuf, Integer> SIGNED_VAR_INT =
        PacketCodec.ofStatic(NetBuf::writeVarInt, NetBuf::readVarInt);

    /**
     * Version of {@link PacketCodecs#VAR_LONG} that handles negative longs properly.
     * <p>
     * Use {@link PacketCodecs#VAR_LONG} for unsigned longs.
     */
    public static final PacketCodec<NetByteBuf, Long> SIGNED_VAR_LONG =
        PacketCodec.ofStatic(NetBuf::writeVarLong, NetBuf::readVarLong);

    /**
     * Packet codec for reading/writing a specific type of enum.
     *
     * @param enumClass the class of the enum.
     * @param <E>       the type of enum.
     * @return a {@link PacketCodec} for reading/writing the specified
     */
    public static <E extends Enum<E>> PacketCodec<NetByteBuf, E> enumConstant(Class<E> enumClass) {
        return PacketCodec.ofStatic(NetByteBuf::writeEnumConstant, buf -> buf.readEnumConstant(enumClass));
    }

    /**
     * Packet codec for reading/writing a fixed number of bits.
     *
     * @param length the number of bits to write.
     * @return a {@link PacketCodec} for reading/writing the specified number of bits.
     */
    public static PacketCodec<NetByteBuf, Integer> fixedBits(int length) {
        return PacketCodec.ofStatic((buf, bits) -> buf.writeFixedBits(bits, length), buf -> buf.readFixedBits(length));
    }

    /**
     * Converts a {@link NetByteBuf} codec into a {@link PacketByteBuf} codec.
     *
     * @param codec the codec to convert.
     * @param <T>   the type of object the codec encodes/decodes.
     * @return the new codec.
     */
    public static <T> PacketCodec<PacketByteBuf, T> netToVanilla(PacketCodec<? super NetByteBuf, T> codec) {
        return new PacketCodec<>() {
            @Override
            public T decode(PacketByteBuf buf) {
                return codec.decode(NetBufs.netOf(buf));
            }

            @Override
            public void encode(PacketByteBuf buf, T value) {
                codec.encode(NetBufs.netOf(buf), value);
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
    public static <T> PacketCodec<RegistryByteBuf, T> regNetToVanilla(
        PacketCodec<? super RegistryNetByteBuf, T> codec) {
        return new PacketCodec<>() {
            @Override
            public T decode(RegistryByteBuf buf) {
                return codec.decode(NetBufs.regNetOf(buf));
            }

            @Override
            public void encode(RegistryByteBuf buf, T value) {
                codec.encode(NetBufs.regNetOf(buf), value);
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
    public static <T> PacketCodec<RegistryByteBuf, T> netRegToVanilla(
        PacketCodec<? super NetRegistryByteBuf, T> codec) {
        return new PacketCodec<>() {
            @Override
            public T decode(RegistryByteBuf buf) {
                return codec.decode(NetBufs.netRegOf(buf));
            }

            @Override
            public void encode(RegistryByteBuf buf, T value) {
                codec.encode(NetBufs.netRegOf(buf), value);
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
    public static <T> PacketCodec<RegistryNetByteBuf, T> netToReg(
        PacketCodec<? super NetRegistryByteBuf, T> codec) {
        return new PacketCodec<>() {
            @Override
            public T decode(RegistryNetByteBuf buf) {
                return codec.decode(NetBufs.netRegOf(buf));
            }

            @Override
            public void encode(RegistryNetByteBuf buf, T value) {
                codec.encode(NetBufs.netRegOf(buf), value);
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
    public static <T> PacketCodec<NetRegistryByteBuf, T> regToNet(
        PacketCodec<? super RegistryNetByteBuf, T> codec) {
        return new PacketCodec<>() {
            @Override
            public T decode(NetRegistryByteBuf buf) {
                return codec.decode(NetBufs.regNetOf(buf));
            }

            @Override
            public void encode(NetRegistryByteBuf buf, T value) {
                codec.encode(NetBufs.regNetOf(buf), value);
            }
        };
    }
}
