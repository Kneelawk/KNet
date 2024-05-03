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

import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.function.IntFunction;

import io.netty.buffer.ByteBuf;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.encoding.VarInts;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;

/**
 * NetBuf Codec utilities.
 */
public final class NetCodecs {
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
     * KNet optimized {@link ChunkPos} codec.
     */
    public static final PacketCodec<NetByteBuf, ChunkPos> CHUNK_POS =
        PacketCodec.ofStatic(NetBuf::writeChunkPos, NetBuf::readChunkPos);

    /**
     * KNet optimized {@link ChunkSectionPos} codec.
     */
    public static final PacketCodec<NetByteBuf, ChunkSectionPos> CHUNK_SECTION_POS =
        PacketCodec.ofStatic(NetBuf::writeChunkSectionPos, NetByteBuf::readChunkSectionPos);

    /**
     * Codec to write an array of integers.
     */
    public static final PacketCodec<PacketByteBuf, int[]> INT_ARRAY = new PacketCodec<>() {
        @Override
        public int[] decode(PacketByteBuf buf) {
            return buf.readIntArray();
        }

        @Override
        public void encode(PacketByteBuf buf, int[] value) {
            buf.writeIntArray(value);
        }
    };

    /**
     * Codec for reading/writing {@link OptionalDouble}s.
     */
    public static final PacketCodec<NetByteBuf, OptionalDouble> OPTIONAL_DOUBLE =
        PacketCodec.ofStatic(NetBuf::writeNetOptionalDouble, NetBuf::readNetOptionalDouble);

    /**
     * Codec for reading/writing {@link OptionalInt}s.
     */
    public static final PacketCodec<NetByteBuf, OptionalInt> OPTIONAL_INT =
        PacketCodec.ofStatic(NetBuf::writeNetOptionalInt, NetBuf::readNetOptionalInt);

    /**
     * Codec for reading/writing {@link OptionalLong}s.
     */
    public static final PacketCodec<NetByteBuf, OptionalLong> OPTIONAL_LONG =
        PacketCodec.ofStatic(NetBuf::writeNetOptionalLong, NetBuf::readNetOptionalLong);

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
     * Creates a codec for a byte buffer.
     *
     * @param bufferCtor constructs the result buffer when given a buffer length.
     * @param <B>        the type of byte buffer this codec will be for.
     * @return a codec for the specified type of byte buffer.
     */
    public static <B extends ByteBuf> PacketCodec<ByteBuf, B> buffer(IntFunction<B> bufferCtor) {
        return new PacketCodec<>() {
            @Override
            public B decode(ByteBuf buf) {
                // read unsigned length
                int length = VarInts.read(buf);
                B newBuf = bufferCtor.apply(length);
                buf.readBytes(newBuf, length);
                return newBuf;
            }

            @Override
            public void encode(ByteBuf buf, B value) {
                VarInts.write(buf, value.readableBytes());
                buf.writeBytes(value, value.readerIndex(), value.readableBytes());
            }
        };
    }

    /**
     * Creates a codec for a byte buffer that takes context from the buffer being read.
     * <p>
     * This is intended for reading a registry buffer from another registry buffer.
     * <p>
     * <b>Note:</b> Do not read from the original buffer when constructing the new buffer, as that would cause reads and
     * writes to become unbalanced.
     *
     * @param bufferCtor constructs the result buffer when given the original buffer for context and a buffer length.
     * @param <V>        the type of byte buffer this codec will be for.
     * @param <B>        the type of byte buffer this codec read/writes to.
     * @return a codec for the specified type of byte buffer.
     */
    public static <V extends ByteBuf, B extends ByteBuf> PacketCodec<B, V> buffer(
        DerivativeBufferSupplier<B, V> bufferCtor) {
        return new PacketCodec<>() {
            @Override
            public V decode(B buf) {
                // read unsigned length
                int length = VarInts.read(buf);
                V newBuf = bufferCtor.derive(buf, length);
                buf.readBytes(newBuf, length);
                return null;
            }

            @Override
            public void encode(B buf, V value) {
                VarInts.write(buf, value.readableBytes());
                buf.writeBytes(value, value.readerIndex(), value.readableBytes());
            }
        };
    }

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
                return codec.decode(NetBufs.registryNetOf(buf));
            }

            @Override
            public void encode(RegistryByteBuf buf, T value) {
                codec.encode(NetBufs.registryNetOf(buf), value);
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
                return codec.decode(NetBufs.netRegistryOf(buf));
            }

            @Override
            public void encode(RegistryByteBuf buf, T value) {
                codec.encode(NetBufs.netRegistryOf(buf), value);
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
                return codec.decode(NetBufs.netRegistryOf(buf));
            }

            @Override
            public void encode(RegistryNetByteBuf buf, T value) {
                codec.encode(NetBufs.netRegistryOf(buf), value);
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
                return codec.decode(NetBufs.registryNetOf(buf));
            }

            @Override
            public void encode(NetRegistryByteBuf buf, T value) {
                codec.encode(NetBufs.registryNetOf(buf), value);
            }
        };
    }
}
