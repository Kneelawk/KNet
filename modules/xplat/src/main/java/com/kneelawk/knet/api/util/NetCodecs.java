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

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.VarInt;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.level.ChunkPos;

/**
 * NetBuf Codec utilities.
 */
public final class NetCodecs {
    private NetCodecs() {}

    /**
     * KNet optimized version of {@link BlockPos#STREAM_CODEC}.
     */
    public static final StreamCodec<NetByteBuf, BlockPos> BLOCK_POS =
        StreamCodec.of(NetBuf::writeBlockPos, NetBuf::readBlockPos);

    /**
     * KNet optimized version of {@link ByteBufCodecs#BOOL}.
     */
    public static final StreamCodec<NetByteBuf, Boolean> BOOL =
        StreamCodec.of(NetBuf::writeBoolean, NetBuf::readBoolean);

    /**
     * KNet optimized {@link ChunkPos} codec.
     */
    public static final StreamCodec<NetByteBuf, ChunkPos> CHUNK_POS =
        StreamCodec.of(NetBuf::writeChunkPos, NetBuf::readChunkPos);

    /**
     * KNet optimized {@link SectionPos} codec.
     */
    public static final StreamCodec<NetByteBuf, SectionPos> CHUNK_SECTION_POS =
        StreamCodec.of(NetBuf::writeSectionPos, NetByteBuf::readSectionPos);

    /**
     * Codec to write an array of integers.
     */
    public static final StreamCodec<FriendlyByteBuf, int[]> INT_ARRAY = new StreamCodec<>() {
        @Override
        public int[] decode(FriendlyByteBuf buf) {
            return buf.readVarIntArray();
        }

        @Override
        public void encode(FriendlyByteBuf buf, int[] value) {
            buf.writeVarIntArray(value);
        }
    };

    /**
     * Codec for reading/writing {@link OptionalDouble}s.
     */
    public static final StreamCodec<NetByteBuf, OptionalDouble> OPTIONAL_DOUBLE =
        StreamCodec.of(NetBuf::writeNetOptionalDouble, NetBuf::readNetOptionalDouble);

    /**
     * Codec for reading/writing {@link OptionalInt}s.
     */
    public static final StreamCodec<NetByteBuf, OptionalInt> OPTIONAL_INT =
        StreamCodec.of(NetBuf::writeNetOptionalInt, NetBuf::readNetOptionalInt);

    /**
     * Codec for reading/writing {@link OptionalLong}s.
     */
    public static final StreamCodec<NetByteBuf, OptionalLong> OPTIONAL_LONG =
        StreamCodec.of(NetBuf::writeNetOptionalLong, NetBuf::readNetOptionalLong);

    /**
     * Version of {@link ByteBufCodecs#VAR_INT} that handles negative integers properly.
     * <p>
     * Use {@link ByteBufCodecs#VAR_INT} for unsigned integers.
     */
    public static final StreamCodec<NetByteBuf, Integer> SIGNED_VAR_INT =
        StreamCodec.of(NetBuf::writeVarInt, NetBuf::readVarInt);

    /**
     * Version of {@link ByteBufCodecs#VAR_LONG} that handles negative longs properly.
     * <p>
     * Use {@link ByteBufCodecs#VAR_LONG} for unsigned longs.
     */
    public static final StreamCodec<NetByteBuf, Long> SIGNED_VAR_LONG =
        StreamCodec.of(NetBuf::writeVarLong, NetBuf::readVarLong);

    /**
     * Creates a codec for a byte buffer.
     *
     * @param bufferCtor constructs the result buffer when given a buffer length.
     * @param <B>        the type of byte buffer this codec will be for.
     * @return a codec for the specified type of byte buffer.
     */
    public static <B extends ByteBuf> StreamCodec<ByteBuf, B> buffer(IntFunction<B> bufferCtor) {
        return new StreamCodec<>() {
            @Override
            public B decode(ByteBuf buf) {
                // read unsigned length
                int length = VarInt.read(buf);
                B newBuf = bufferCtor.apply(length);
                buf.readBytes(newBuf, length);
                return newBuf;
            }

            @Override
            public void encode(ByteBuf buf, B value) {
                VarInt.write(buf, value.readableBytes());
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
    public static <V extends ByteBuf, B extends ByteBuf> StreamCodec<B, V> buffer(
        DerivativeBufferSupplier<B, V> bufferCtor) {
        return new StreamCodec<>() {
            @Override
            public V decode(B buf) {
                // read unsigned length
                int length = VarInt.read(buf);
                V newBuf = bufferCtor.derive(buf, length);
                buf.readBytes(newBuf, length);
                return null;
            }

            @Override
            public void encode(B buf, V value) {
                VarInt.write(buf, value.readableBytes());
                buf.writeBytes(value, value.readerIndex(), value.readableBytes());
            }
        };
    }

    /**
     * Packet codec for reading/writing a specific type of enum.
     *
     * @param enumClass the class of the enum.
     * @param <E>       the type of enum.
     * @return a {@link StreamCodec} for reading/writing the specified
     */
    public static <E extends Enum<E>> StreamCodec<NetByteBuf, E> enumConstant(Class<E> enumClass) {
        return StreamCodec.of(NetByteBuf::writeEnum, buf -> buf.readEnum(enumClass));
    }

    /**
     * Packet codec for reading/writing a fixed number of bits.
     *
     * @param length the number of bits to write.
     * @return a {@link StreamCodec} for reading/writing the specified number of bits.
     */
    public static StreamCodec<NetByteBuf, Integer> fixedBits(int length) {
        return StreamCodec.of((buf, bits) -> buf.writeFixedBits(bits, length), buf -> buf.readFixedBits(length));
    }

    /**
     * Converts a {@link NetByteBuf} codec into a {@link FriendlyByteBuf} codec.
     *
     * @param codec the codec to convert.
     * @param <T>   the type of object the codec encodes/decodes.
     * @return the new codec.
     */
    public static <T> StreamCodec<FriendlyByteBuf, T> netToVanilla(StreamCodec<? super NetByteBuf, T> codec) {
        return new StreamCodec<>() {
            @Override
            public T decode(FriendlyByteBuf buf) {
                return codec.decode(NetBufs.netOf(buf));
            }

            @Override
            public void encode(FriendlyByteBuf buf, T value) {
                codec.encode(NetBufs.netOf(buf), value);
            }
        };
    }

    /**
     * Converts a {@link RegistryNetByteBuf} codec into a {@link RegistryFriendlyByteBuf} codec.
     *
     * @param codec the codec to convert.
     * @param <T>   the type of object the codec encodes/decodes.
     * @return the new codec.
     */
    public static <T> StreamCodec<RegistryFriendlyByteBuf, T> regNetToVanilla(
        StreamCodec<? super RegistryNetByteBuf, T> codec) {
        return new StreamCodec<>() {
            @Override
            public T decode(RegistryFriendlyByteBuf buf) {
                return codec.decode(NetBufs.registryNetOf(buf));
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buf, T value) {
                codec.encode(NetBufs.registryNetOf(buf), value);
            }
        };
    }

    /**
     * Converts a {@link NetRegistryByteBuf} codec into a {@link RegistryFriendlyByteBuf} codec.
     *
     * @param codec the codec to convert.
     * @param <T>   the type of object the codec encodes/decodes.
     * @return the new codec.
     */
    public static <T> StreamCodec<RegistryFriendlyByteBuf, T> netRegToVanilla(
        StreamCodec<? super NetRegistryByteBuf, T> codec) {
        return new StreamCodec<>() {
            @Override
            public T decode(RegistryFriendlyByteBuf buf) {
                return codec.decode(NetBufs.netRegistryOf(buf));
            }

            @Override
            public void encode(RegistryFriendlyByteBuf buf, T value) {
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
    public static <T> StreamCodec<RegistryNetByteBuf, T> netToReg(
        StreamCodec<? super NetRegistryByteBuf, T> codec) {
        return new StreamCodec<>() {
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
    public static <T> StreamCodec<NetRegistryByteBuf, T> regToNet(
        StreamCodec<? super RegistryNetByteBuf, T> codec) {
        return new StreamCodec<>() {
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
