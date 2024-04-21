/*
 * Copyright (c) 2019 AlexIIL
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kneelawk.knet.api.util;

import java.util.Optional;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketDecoder;
import net.minecraft.network.codec.PacketEncoder;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

/**
 * Utility class to help with implementing {@link NetBuf}.
 */
public final class NetBufImplHelper {
    private NetBufImplHelper() {}

    private static void writePartialBitsBegin(NetBuf<?> buf) {
        if (buf.getWritePartialIndex() == -1 || buf.getWritePartialOffset() == 8) {
            buf.setWritePartialIndex(buf.writerIndex());
            buf.setWritePartialOffset(0);
            buf.setWritePartialCache(0);
            buf.self().writeByte(0);
        }
    }

    private static void readPartialBitsBegin(NetBuf<?> buf) {
        if (buf.getReadPartialOffset() == 8) {
            buf.setReadPartialOffset(0);
            buf.setReadPartialCache(buf.self().readUnsignedByte());
        }
    }

    /**
     * Writes a boolean to the given buffer.
     *
     * @param buf  the buffer to write to.
     * @param flag the boolean to write.
     * @see NetBuf#writeBoolean(boolean)
     */
    public static void writeBoolean(NetBuf<?> buf, boolean flag) {
        writePartialBitsBegin(buf);
        int toWrite = (flag ? 1 : 0) << buf.getWritePartialOffset();
        buf.orWritePartialCache(toWrite);
        buf.incrementWritePartialOffset();
        buf.writePartialCache();
    }

    /**
     * Reads a boolean from the given buffer.
     *
     * @param buf the buffer to read from
     * @return the read boolean
     * @see NetBuf#readBoolean()
     */
    public static boolean readBoolean(NetBuf<?> buf) {
        readPartialBitsBegin(buf);
        int offset = 1 << buf.incrementReadPartialOffset();
        return (buf.getReadPartialCache() & offset) == offset;
    }

    /**
     * Writes a fixed set of bits to the given buffer.
     *
     * @param buf    the buffer to write to.
     * @param value  the value to write.
     * @param length the number of bits to write.
     * @throws IllegalArgumentException if the length is too small or too large.
     * @see NetBuf#writeFixedBits(int, int)
     */
    public static void writeFixedBits(NetBuf<?> buf, int value, int length) throws IllegalArgumentException {
        if (length <= 0) {
            throw new IllegalArgumentException("Tried to write too few bits! (" + length + ")");
        }
        if (length > 32) {
            throw new IllegalArgumentException("Tried to write more bits than are in an integer! (" + length + ")");
        }

        writePartialBitsBegin(buf);

        // - length = 10
        // - bits = 0123456789

        // current
        // (# = already written, _ is not yet written)
        // - in buffer [######## _#######]
        // - writePartialCache = "_#######"
        // - writePartialOffset = 7

        // want we want:
        // - in buffer [######## 0###### 12345678 _______9 ]
        // - writePartialCache = "_______9"
        // - writePartialOffset = 1

        // first stage: take the toppermost bits and append them to the cache (if the cache contains bits)
        if (buf.getWritePartialOffset() > 0) {

            // top length = 8 - (num bits in cache) or length, whichever is SMALLER
            int availableBits = 8 - buf.getWritePartialOffset();

            if (availableBits >= length) {
                int mask = (1 << length) - 1;
                int bitsToWrite = value & mask;

                buf.orWritePartialCache(bitsToWrite << buf.getWritePartialOffset());
                buf.writePartialCache();
                buf.incrementWritePartialOffset(length);
                // we just wrote out the entire length, no need to do anything else.
                return;
            } else { // topLength < length -- we will still need to be writing out more bits after this
                // length = 10
                // topLength = 1
                // value = __01 2345 6789
                // want == ____ ____ ___0
                // mask == ____ ____ ___1
                // shift back = 9

                int mask = (1 << availableBits) - 1;

                int shift = length - availableBits;

                int bitsToWrite = (value >>> shift) & mask;

                buf.orWritePartialCache(bitsToWrite << buf.getWritePartialOffset());
                buf.writePartialCache();

                // we finished a byte, reset values so that the next write will reset and create a new byte
                buf.setWritePartialCache(0);
                buf.setWritePartialOffset(8);

                // now shift the value down ready for the next iteration
                length -= availableBits;
            }
        }

        while (length >= 8) {
            // write out full 8 bit chunks of the length until we reach 0
            writePartialBitsBegin(buf);

            int byteToWrite = (value >>> (length - 8)) & 0xFF;

            buf.self().setByte(buf.getWritePartialIndex(), byteToWrite);

            // we finished a byte, reset values so that the next write will reset and create a new byte
            buf.setWritePartialCache(0);
            buf.setWritePartialOffset(8);

            length -= 8;
        }

        if (length > 0) {
            // we have a few bits left over to append
            writePartialBitsBegin(buf);

            int mask = (1 << length) - 1;
            buf.setWritePartialCache(value & mask);
            buf.writePartialCache();
            buf.setWritePartialOffset(length);
        }
    }

    /**
     * Reads a fixed set of bits from the given buffer.
     *
     * @param buf    the buffer to read from.
     * @param length the number of bits to read.
     * @return the read bits.
     * @see NetBuf#readFixedBits(int)
     */
    public static int readFixedBits(NetBuf<?> buf, int length) {
        if (length <= 0) {
            throw new IllegalArgumentException("Tried to read too few bits! (" + length + ")");
        }
        if (length > 32) {
            throw new IllegalArgumentException("Tried to read more bits than are in an integer! (" + length + ")");
        }
        readPartialBitsBegin(buf);

        int value = 0;

        if (buf.getReadPartialOffset() > 0) {
            // If we have bits left at the top of the buffer...
            int availableBits = 8 - buf.getReadPartialOffset();
            if (availableBits >= length) {
                // If the wanted bits are completely contained within the cache
                int mask = (1 << length) - 1;
                value = (buf.getReadPartialCache() >>> buf.getReadPartialOffset()) & mask;
                buf.incrementReadPartialOffset(length);
                return value;
            } else {
                // If we need to read more bits than are available in the cache
                int bitsRead = buf.getReadPartialCache() >>> buf.getReadPartialOffset();

                value = bitsRead;

                // We finished reading a byte, reset values so the next step will read them properly

                buf.setReadPartialCache(0);
                buf.setReadPartialOffset(8);

                length -= availableBits;
            }
        }

        while (length >= 8) {
            readPartialBitsBegin(buf);
            length -= 8;
            value <<= 8;
            value |= buf.getReadPartialCache();
            buf.setReadPartialOffset(8);
        }

        if (length > 0) {
            readPartialBitsBegin(buf);

            int mask = (1 << length) - 1;

            value <<= length;
            value |= buf.getReadPartialCache() & mask;
            buf.setReadPartialOffset(length);
        }

        return value;
    }

    /**
     * Writes an enum.
     *
     * @param buf   the buffer to write to.
     * @param value the enum to write.
     * @see NetBuf#writeEnumConstant(Enum)
     */
    public static void writeEnumConstant(NetBuf<?> buf, Enum<?> value) {
        Enum<?>[] possible = value.getDeclaringClass().getEnumConstants();
        if (possible == null) throw new IllegalArgumentException("Not an enum " + value.getClass());
        if (possible.length == 0) {
            throw new IllegalArgumentException("Tried to write an enum value without any values! How did you do this?");
        }
        if (possible.length == 1) return;
        buf.writeFixedBits(value.ordinal(), MathHelper.ceilLog2(possible.length));
    }

    /**
     * Reads an enum.
     *
     * @param buf       the buffer to read from.
     * @param enumClass the class of the enum to read.
     * @param <E>       the type of the enum to read.
     * @return the read enum.
     * @see NetBuf#readEnumConstant(Class)
     */
    public static <E extends Enum<E>> E readEnumConstant(NetBuf<?> buf, Class<E> enumClass) {
        // No need to lookup the declaring class as you cannot refer to sub-classes of Enum.
        E[] enums = enumClass.getEnumConstants();
        if (enums == null) {
            throw new IllegalArgumentException("Not an enum " + enumClass);
        }
        if (enums.length == 0) {
            throw new IllegalArgumentException("Tried to read an enum value without any values! How did you do this?");
        }
        if (enums.length == 1) {
            return enums[0];
        }
        int length = MathHelper.ceilLog2(enums.length);
        int index = buf.readFixedBits(length);
        return enums[index];
    }

    /**
     * Writes a block pos.
     *
     * @param buf the buffer to write to.
     * @param pos the pos to write.
     * @see NetBuf#writeBlockPos(BlockPos)
     */
    public static void writeBlockPos(NetBuf<?> buf, BlockPos pos) {
        buf.writeVarInt(pos.getX());
        buf.writeVarInt(pos.getY());
        buf.writeVarInt(pos.getZ());
    }

    /**
     * Reads a block pos.
     *
     * @param buf the buffer to read from.
     * @return the read block pos.
     * @see NetBuf#readBlockPos()
     */
    public static BlockPos readBlockPos(NetBuf<?> buf) {
        return new BlockPos(buf.readVarInt(), buf.readVarInt(), buf.readVarInt());
    }

    /**
     * Writes a variable-length integer.
     *
     * @param buf  the buffer to write to.
     * @param ival the integer to write.
     * @see NetBuf#writeVarInt(int)
     */
    public static void writeVarInt(NetBuf<?> buf, int ival) {
        // 32 bits
        // svvvVVV VVVV
        // where:
        // s = sign
        // v = bit
        // V = nibble
        // 0 = unused bit
        // # = unused nibble

        final int sign;
        if (ival < 0) {
            ival = ~ival;
            sign = 1;
        } else {
            sign = 0;
        }

        // Now write the remaining bits out:
        // Either:
        // 1 byte: s000# ## ## 00vvV -> 0svvV
        // 2 bytes: s000# ## 000vV VV -> 1vvvV 0svvV
        // 3 bytes: s00 VV VV -> 1vvvV 1vvvV 0svvV
        // 4 bytes: s000 0vvv VV VV VV -> 1vvvV 1vvvV 1vvvV 0svvV
        // 5 bytes: svvvV VV VV VV -> 1vvvV 1vvvV 1vvvV 1vvvV 0s0vV

        while ((ival & ~0x3f) != 0) {
            buf.self().writeByte(0x80 | (ival & 0x7f));
            ival >>>= 7;
        }
        buf.self().writeByte((sign << 6) | ival);
    }

    /**
     * Reads a variable-length integer.
     *
     * @param buf the buffer to read from.
     * @return the read integer.
     * @see NetBuf#readVarInt()
     */
    public static int readVarInt(NetBuf<?> buf) {
        int count = 0;
        int ival = 0;
        int read;
        do {
            read = buf.self().readUnsignedByte();
            if ((read & 0x80) == 0) {
                ival |= (read & 0x3f) << count++ * 7;
                if ((read & 0x40) == 0) {
                    return ival;
                } else {
                    return ~ival;
                }
            }
            ival |= (read & 0x7f) << count++ * 7;
        } while (count < 5);
        return ival;
    }

    /**
     * Writes a variable-length long.
     *
     * @param buf  the buffer to write to.
     * @param lval the long to write.
     * @see NetBuf#writeVarLong(long)
     */
    // TODO: Finish that!
    public static void writeVarLong(NetBuf<?> buf, long lval) {
        // Copy-pasted from writeVarInt
        final int sign;
        if (lval < 0) {
            lval = ~lval;
            sign = 1;
        } else {
            sign = 0;
        }
        while ((lval & ~0x3f) != 0) {
            buf.self().writeByte((int) (0x80 | (lval & 0x7f)));
            lval >>>= 7;
        }
        buf.self().writeByte((int) ((sign << 6) | lval));
    }

    /**
     * Reads a variable-length long.
     *
     * @param buf the buffer to read from.
     * @return the read long.
     * @see NetBuf#readVarLong()
     */
    public static long readVarLong(NetBuf<?> buf) {
        int count = 0;
        long lval = 0;
        long read;
        do {
            read = buf.self().readUnsignedByte();
            if ((read & 0x80) == 0) {
                lval |= (read & 0x3f) << count++ * 7;
                if ((read & 0x40) == 0) {
                    return lval;
                } else {
                    return ~lval;
                }
            }
            lval |= (read & 0x7f) << count++ * 7;
        } while (count < 10);
        return lval;
    }

    /**
     * Writes an optional value.
     *
     * @param buf    the buffer to write to.
     * @param value  the optional to write.
     * @param writer the writer for the optional's value.
     * @param <T>    the type to write.
     * @param <B>    the type of buffer.
     * @return the buffer being written to.
     * @see PacketByteBuf#writeOptional(Optional, PacketEncoder)
     */
    public static <T, B extends PacketByteBuf & NetBuf<B>> B writeOptional(B buf, Optional<T> value,
                                                                           PacketEncoder<? super B, T> writer) {
        if (value.isPresent()) {
            buf.writeBoolean(true);
            writer.encode(buf, value.get());
        } else {
            buf.writeBoolean(false);
        }
        return buf;
    }

    /**
     * Reads an optional value.
     *
     * @param buf    the buffer to read from.
     * @param reader the reader for the optional's value.
     * @param <T>    the type to read.
     * @param <B>    the type of buffer.
     * @return the read optional.
     * @see PacketByteBuf#readOptional(PacketDecoder)
     */
    public static <T, B extends PacketByteBuf & NetBuf<B>> Optional<T> readOptional(B buf,
                                                                                    PacketDecoder<? super B, T> reader) {
        return buf.readBoolean() ? Optional.of(reader.decode(buf)) : Optional.empty();
    }
}
