/*
 * Copyright (c) 2019 AlexIIL
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kneelawk.knet.api.util;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.DecoderException;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;

/**
 * Special {@link PacketByteBuf} class that provides methods specific to "offset" reading and writing - like writing a
 * single bit to the stream, and auto-compacting it with similar bits into a single byte.
 * <p>
 * In addition this overrides a number of existing methods (like {@link #writeBoolean(boolean)},
 * {@link #writeEnumConstant(Enum)}, {@link #writeVarInt(int)}, {@link #writeVarLong(long)}, and a few more.
 */
public class NetByteBuf extends PacketByteBuf {

    /**
     * Holds an index into a {@link NetByteBuf} that can be restored.
     */
    public static final class SavedReaderIndex {
        /**
         * The saved reader index.
         */
        public final int readerIndex;
        final int readPartialOffset;
        final int readPartialCache;

        SavedReaderIndex(NetByteBuf buffer) {
            readerIndex = buffer.readerIndex();
            readPartialOffset = buffer.readPartialOffset;
            readPartialCache = buffer.readPartialCache;
        }
    }

    /**
     * A functional interface to read a value from {@link NetByteBuf}.
     */
    @FunctionalInterface
    public interface PacketReader<T> extends Function<NetByteBuf, T> {
        default PacketReader<Optional<T>> asOptional() {
            return buf -> buf.readOptional(this);
        }
    }

    /**
     * A functional interface to write a value to {@link NetByteBuf}.
     */
    @FunctionalInterface
    public interface PacketWriter<T> extends BiConsumer<NetByteBuf, T> {
        default PacketWriter<Optional<T>> asOptional() {
            return (buf, value) -> buf.writeOptional(value, this);
        }
    }

    /**
     * An empty {@link NetByteBuf}.
     */
    public static final NetByteBuf EMPTY_BUFFER = new NetByteBuf(Unpooled.EMPTY_BUFFER);

    /**
     * The minimum value that can fit within a single byte when using signed var-int encoding.
     */
    public static final int MIN_VAR_S_INT_1_BYTE = -(1 << 6);
    /**
     * The maximum value that can fit within a single byte when using signed var-int encoding.
     */
    public static final int MAX_VAR_S_INT_1_BYTE = (1 << 6) - 1;
    /**
     * The minimum value that can fit within two bytes when using signed var-int encoding.
     */
    public static final int MIN_VAR_S_INT_2_BYTES = -(1 << 6 + 7);
    /**
     * The maximum value that can fit within two bytes when using signed var-int encoding.
     */
    public static final int MAX_VAR_S_INT_2_BYTES = (1 << 6 + 7) - 1;
    /**
     * The minimum value that can fit within three bytes when using signed var-int encoding.
     */
    public static final int MIN_VAR_S_INT_3_BYTES = -(1 << 6 + 7 * 2);
    /**
     * The maximum value that can fit within three bytes when using signed var-int encoding.
     */
    public static final int MAX_VAR_S_INT_3_BYTES = (1 << 6 + 7 * 2) - 1;
    /**
     * The minimum value that can fit within four bytes when using signed var-int encoding.
     */
    public static final int MIN_VAR_S_INT_4_BYTES = -(1 << 6 + 7 * 3);
    /**
     * The maximum value that can fit within four bytes when using signed var-int encoding.
     */
    public static final int MAX_VAR_S_INT_4_BYTES = (1 << 6 + 7 * 3) - 1;

    /**
     * The minimum value that can be encoded using unsigned var-int encoding.
     */
    public static final int MIN_VAR_U_INT_SMALL = 0;
    /**
     * The maximum value that can fit within a single byte when using unsigned var-int encoding.
     */
    public static final int MAX_VAR_U_INT_1_BYTE = 1 << 7;
    /**
     * The maximum value that can fit within two bytes when using unsigned var-int encoding.
     */
    public static final int MAX_VAR_U_INT_2_BYTES = 1 << 7 * 2;
    /**
     * The maximum value that can fit within three bytes when using unsigned var-int encoding.
     */
    public static final int MAX_VAR_U_INT_3_BYTES = 1 << 7 * 3;
    /**
     * The maximum value that can fit within four bytes when using unsigned var-int encoding.
     */
    public static final int MAX_VAR_U_INT_4_BYTES = 1 << 7 * 4;

    /**
     * Creates a new {@link NetByteBuf} without any initial capacity.
     *
     * @return A new {@link NetByteBuf} from {@link Unpooled#buffer()}
     */
    public static NetByteBuf buffer() {
        return asNetByteBuf(Unpooled.buffer());
    }

    /**
     * Creates a new {@link NetByteBuf} with the given initial capacity.
     *
     * @param initialCapacity the buffer's initial capacity.
     * @return A new {@link NetByteBuf} from {@link Unpooled#buffer(int)}
     */
    public static NetByteBuf buffer(int initialCapacity) {
        return asNetByteBuf(Unpooled.buffer(initialCapacity));
    }

    /**
     * Creates a new {@link NetByteBuf} without any initial capacity while optionally disabling optimizations.
     *
     * @param passthrough whether to disable optimizations.
     * @return A new {@link NetByteBuf} from {@link Unpooled#buffer()}
     */
    public static NetByteBuf buffer(boolean passthrough) {
        return asNetByteBuf(Unpooled.buffer(), passthrough);
    }

    /**
     * Creates a new {@link NetByteBuf} with the given initial capacity while optionally disabling optimizations.
     *
     * @param initialCapacity the buffer's initial capacity.
     * @param passthrough     whether to disable optimizations.
     * @return A new {@link NetByteBuf} from {@link Unpooled#buffer(int)}
     */
    public static NetByteBuf buffer(int initialCapacity, boolean passthrough) {
        return asNetByteBuf(Unpooled.buffer(initialCapacity), passthrough);
    }

    /**
     * If true then all {@link PacketByteBuf} override methods that this {@link NetByteBuf} optimises will instead just
     * write using the normal minecraft methods, rather than the (potentially) optimised versions.
     */
    public final boolean passthrough;

    // Byte-based flag access
    private int readPartialOffset = 8;// so it resets down to 0 and reads a byte on read
    private int readPartialCache;

    private int readPartialOffsetMark = 8;
    private int readPartialCacheMark;

    /**
     * The byte position that is currently being written to. -1 means that no bytes have been written to yet.
     */
    private int writePartialIndex = -1;
    /**
     * The current bit based offset, used to add successive flags into the cached value held in
     * {@link #writePartialCache}
     */
    private int writePartialOffset;
    /**
     * Holds the current set of flags that will be written out. This only saves having a read
     */
    private int writePartialCache;

    /**
     * Creates a new {@link NetByteBuf}, wrapping the given buffer.
     *
     * @param wrapped the buffer this buffer writes to.
     */
    public NetByteBuf(ByteBuf wrapped) {
        this(wrapped, false);
    }

    /**
     * Creates a new {@link NetByteBuf}, wrapping the given buffer an optionally disabling optimizations.
     *
     * @param wrapped     the buffer this buffer writes to.
     * @param passthrough whether to disable optimizations.
     */
    public NetByteBuf(ByteBuf wrapped, boolean passthrough) {
        super(wrapped);
        this.passthrough = passthrough;
    }

    /**
     * Returns the given {@link ByteBuf} as {@link NetByteBuf}. If the given instance is already a {@link NetByteBuf}
     * then the given buffer is returned (note that this may result in unexpected consequences if multiple read/write
     * Boolean methods are called on the given buffer before you called this).
     *
     * @param buf the buffer to be converted into a {@link NetByteBuf}.
     * @return the given buffer as a {@link NetByteBuf}.
     */
    public static NetByteBuf asNetByteBuf(ByteBuf buf) {
        return asNetByteBuf(buf, false);
    }

    /**
     * Returns the given {@link ByteBuf} as {@link NetByteBuf}, but with passthrough mode enabled. if the given
     * instance is already a {@link NetByteBuf} then the given buffer is returned (note that this may result in
     * unexpected consequences if multiple read/write Boolean methods are called on the given buffer before you called
     * this).
     *
     * @param buf the buffer to be converted into a {@link NetByteBuf}.
     * @return the given buffer as a {@link NetByteBuf}.
     */
    public static NetByteBuf asPassthroughNetByteBuf(ByteBuf buf) {
        return asNetByteBuf(buf, true);
    }

    /**
     * Returns the given {@link ByteBuf} as {@link NetByteBuf}, but with passthrough mode enabled. if the given
     * instance is already a {@link NetByteBuf} then the given buffer is returned (note that this may result in
     * unexpected consequences if multiple read/write Boolean methods are called on the given buffer before you called
     * this).
     *
     * @param buf         the buffer to be converted into a {@link NetByteBuf}.
     * @param passthrough whether to disable optimizations on the resulting buffer.
     * @return the given buffer as a {@link NetByteBuf}.
     */
    public static NetByteBuf asNetByteBuf(ByteBuf buf, boolean passthrough) {
        if (buf instanceof NetByteBuf netBuf && netBuf.passthrough == passthrough) {
            return netBuf;
        } else {
            return new NetByteBuf(buf, passthrough);
        }
    }

    /**
     * Bit version of {@link #writerIndex()}.
     *
     * @return the current writer partial offset.
     */
    public int getBitWriterIndex() {
        return writePartialOffset;
    }

    /**
     * Bit version of {@link #readerIndex()}.
     *
     * @return the current reader partial offset.
     */
    public int getBitReaderIndex() {
        return readPartialOffset;
    }

    @Override
    public NetByteBuf copy() {
        return asNetByteBuf(super.copy(), passthrough);
    }

    @Override
    public NetByteBuf readBytes(int length) {
        return asNetByteBuf(super.readBytes(length), passthrough);
    }

    @Override
    public NetByteBuf clear() {
        super.clear();
        readPartialOffset = 8;
        readPartialCache = 0;
        writePartialIndex = -1;
        writePartialOffset = 0;
        writePartialCache = 0;
        return this;
    }

    @Override
    public NetByteBuf markReaderIndex() {
        super.markReaderIndex();
        readPartialOffsetMark = readPartialOffset;
        readPartialCacheMark = readPartialCache;
        return this;
    }

    @Override
    public NetByteBuf resetReaderIndex() {
        super.resetReaderIndex();
        readPartialOffset = readPartialOffsetMark;
        readPartialCache = readPartialCacheMark;
        return this;
    }

    /**
     * Creates a saved reader index that can be restored to continue reading from the position of this buffer when this
     * method was called.
     *
     * @return the saved reader index used for restoring the buffer's position.
     */
    public SavedReaderIndex saveReaderIndex() {
        return new SavedReaderIndex(this);
    }

    /**
     * Restores the buffer's reader position to the saved reader index.
     *
     * @param index the saved reader index of the position to restore the buffer to.
     * @return this buffer.
     */
    public NetByteBuf resetReaderIndex(SavedReaderIndex index) {
        readerIndex(index.readerIndex);
        readPartialOffset = index.readPartialOffset;
        readPartialCache = index.readPartialCache;
        return this;
    }

    void writePartialBitsBegin() {
        if (writePartialIndex == -1 || writePartialOffset == 8) {
            writePartialIndex = writerIndex();
            writePartialOffset = 0;
            writePartialCache = 0;
            writeByte(0);
        }
    }

    void readPartialBitsBegin() {
        if (readPartialOffset == 8) {
            readPartialOffset = 0;
            readPartialCache = readUnsignedByte();
        }
    }

    /**
     * Writes a single boolean out to some position in this buffer. The boolean flag might be written to a new byte
     * (increasing the writerIndex) or it might be added to an existing byte that was written with a previous call to
     * this method.
     */
    @Override
    public NetByteBuf writeBoolean(boolean flag) {
        if (passthrough) {
            super.writeBoolean(flag);
            return this;
        }
        writePartialBitsBegin();
        int toWrite = (flag ? 1 : 0) << writePartialOffset;
        writePartialCache |= toWrite;
        writePartialOffset++;
        setByte(writePartialIndex, writePartialCache);
        return this;
    }

    /**
     * Reads a single boolean from some position in this buffer. The boolean flag might be read from a new byte
     * (increasing the readerIndex) or it might be read from a previous byte that was read with a previous call to this
     * method.
     */
    @Override
    public boolean readBoolean() {
        if (passthrough) {
            return super.readBoolean();
        }
        readPartialBitsBegin();
        int offset = 1 << readPartialOffset++;
        return (readPartialCache & offset) == offset;
    }

    /**
     * Writes a fixed number of bits out to the stream.
     *
     * @param value  the value to write out.
     * @param length The number of bits to write.
     * @return This buffer.
     * @throws IllegalArgumentException if the length argument was less than 1 or greater than 32.
     */
    public NetByteBuf writeFixedBits(int value, int length) throws IllegalArgumentException {
        if (length <= 0) {
            throw new IllegalArgumentException("Tried to write too few bits! (" + length + ")");
        }
        if (length > 32) {
            throw new IllegalArgumentException("Tried to write more bits than are in an integer! (" + length + ")");
        }

        writePartialBitsBegin();

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
        if (writePartialOffset > 0) {

            // top length = 8 - (num bits in cache) or length, whichever is SMALLER
            int availableBits = 8 - writePartialOffset;

            if (availableBits >= length) {
                int mask = (1 << length) - 1;
                int bitsToWrite = value & mask;

                writePartialCache |= bitsToWrite << writePartialOffset;
                setByte(writePartialIndex, writePartialCache);
                writePartialOffset += length;
                // we just wrote out the entire length, no need to do anything else.
                return this;
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

                writePartialCache |= bitsToWrite << writePartialOffset;
                setByte(writePartialIndex, writePartialCache);

                // we finished a byte, reset values so that the next write will reset and create a new byte
                writePartialCache = 0;
                writePartialOffset = 8;

                // now shift the value down ready for the next iteration
                length -= availableBits;
            }
        }

        while (length >= 8) {
            // write out full 8 bit chunks of the length until we reach 0
            writePartialBitsBegin();

            int byteToWrite = (value >>> (length - 8)) & 0xFF;

            setByte(writePartialIndex, byteToWrite);

            // we finished a byte, reset values so that the next write will reset and create a new byte
            writePartialCache = 0;
            writePartialOffset = 8;

            length -= 8;
        }

        if (length > 0) {
            // we have a few bits left over to append
            writePartialBitsBegin();

            int mask = (1 << length) - 1;
            writePartialCache = value & mask;
            setByte(writePartialIndex, writePartialCache);
            writePartialOffset = length;
        }

        return this;
    }

    /**
     * Reads a fixed number of bits from the given stream.
     *
     * @param length The number of bits to read.
     * @return The read bits, compacted into an int.
     * @throws IllegalArgumentException if the length argument was less than 1 or greater than 32.
     */
    public int readFixedBits(int length) throws IllegalArgumentException {
        if (length <= 0) {
            throw new IllegalArgumentException("Tried to read too few bits! (" + length + ")");
        }
        if (length > 32) {
            throw new IllegalArgumentException("Tried to read more bits than are in an integer! (" + length + ")");
        }
        readPartialBitsBegin();

        int value = 0;

        if (readPartialOffset > 0) {
            // If we have bits left at the top of the buffer...
            int availableBits = 8 - readPartialOffset;
            if (availableBits >= length) {
                // If the wanted bits are completely contained within the cache
                int mask = (1 << length) - 1;
                value = (readPartialCache >>> readPartialOffset) & mask;
                readPartialOffset += length;
                return value;
            } else {
                // If we need to read more bits than are available in the cache
                int bitsRead = readPartialCache >>> readPartialOffset;

                value = bitsRead;

                // We finished reading a byte, reset values so the next step will read them properly

                readPartialCache = 0;
                readPartialOffset = 8;

                length -= availableBits;
            }
        }

        while (length >= 8) {
            readPartialBitsBegin();
            length -= 8;
            value <<= 8;
            value |= readPartialCache;
            readPartialOffset = 8;
        }

        if (length > 0) {
            readPartialBitsBegin();

            int mask = (1 << length) - 1;

            value <<= length;
            value |= readPartialCache & mask;
            readPartialOffset = length;
        }

        return value;
    }

    @Override
    public NetByteBuf writeEnumConstant(Enum<?> value) {
        if (passthrough) {
            super.writeEnumConstant(value);
            return this;
        }
        Enum<?>[] possible = value.getDeclaringClass().getEnumConstants();
        if (possible == null) throw new IllegalArgumentException("Not an enum " + value.getClass());
        if (possible.length == 0) {
            throw new IllegalArgumentException("Tried to write an enum value without any values! How did you do this?");
        }
        if (possible.length == 1) return this;
        writeFixedBits(value.ordinal(), MathHelper.ceilLog2(possible.length));
        return this;
    }

    @Override
    public <E extends Enum<E>> E readEnumConstant(Class<E> enumClass) {
        if (passthrough) {
            return super.readEnumConstant(enumClass);
        }
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
        int index = readFixedBits(length);
        return enums[index];
    }

    /**
     * Writes out a {@link BlockPos} using 3 {@link #writeVarInt(int)}s rather than {@link BlockPos#asLong()}.
     *
     * @param pos the block position to write.
     */
    @Override
    public NetByteBuf writeBlockPos(BlockPos pos) {
        if (passthrough) {
            super.writeBlockPos(pos);
            return this;
        }
        writeVarInt(pos.getX());
        writeVarInt(pos.getY());
        writeVarInt(pos.getZ());
        return this;
    }

    /**
     * Reads a {@link BlockPos} using 3 {@link #readVarInt()}s rather than {@link BlockPos#fromLong(long)}.
     *
     * @return the read block position.
     */
    @Override
    public BlockPos readBlockPos() {
        if (passthrough) {
            return super.readBlockPos();
        }
        return new BlockPos(readVarInt(), readVarInt(), readVarInt());
    }

    /**
     * Writes out an integer using a variable number of bytes.
     * <ul>
     * <li>1 byte for {@link #MIN_VAR_S_INT_1_BYTE} to {@link #MAX_VAR_S_INT_1_BYTE}</li>
     * <li>2 bytes for {@link #MIN_VAR_S_INT_2_BYTES} to {@link #MAX_VAR_S_INT_2_BYTES}</li>
     * <li>3 bytes for {@link #MIN_VAR_S_INT_3_BYTES} to {@link #MAX_VAR_S_INT_3_BYTES}</li>
     * <li>4 bytes for {@link #MIN_VAR_S_INT_4_BYTES} to {@link #MAX_VAR_S_INT_4_BYTES}</li>
     * <li>5 bytes for {@link Integer#MIN_VALUE} to {@link Integer#MAX_VALUE}</li>
     * </ul>
     * <p>
     * Unlike vanilla this doesn't use 5 bytes for all negative numbers.
     *
     * @param ival the integer value to write.
     * @return this buffer.
     */
    @Override
    public NetByteBuf writeVarInt(int ival) {
        if (passthrough) {
            super.writeVarInt(ival);
            return this;
        }
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
            writeByte(0x80 | (ival & 0x7f));
            ival >>>= 7;
        }
        writeByte((sign << 6) | ival);
        return this;
    }

    /**
     * Reads out an integer using a variable number of bytes, assuming it was written by {@link #writeVarInt(int)}
     *
     * @return the read variable-length integer.
     */
    @Override
    public int readVarInt() {
        if (passthrough) {
            return super.readVarInt();
        }
        int count = 0;
        int ival = 0;
        int read;
        do {
            read = readUnsignedByte();
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
     * Exposes the vanilla method for writing out an unsigned integer using a variable number of bytes.
     * <ul>
     * <li>1 byte for {@link #MIN_VAR_U_INT_SMALL} to {@link #MAX_VAR_S_INT_1_BYTE}</li>
     * <li>2 bytes for {@link #MIN_VAR_U_INT_SMALL} to {@link #MAX_VAR_S_INT_2_BYTES}</li>
     * <li>3 bytes for {@link #MIN_VAR_U_INT_SMALL} to {@link #MAX_VAR_S_INT_3_BYTES}</li>
     * <li>4 bytes for {@link #MIN_VAR_U_INT_SMALL} to {@link #MAX_VAR_S_INT_4_BYTES}</li>
     * <li>5 bytes for {@link Integer#MIN_VALUE} to {@link Integer#MAX_VALUE}</li>
     * </ul>
     * <p>
     * Unlike {@link #writeVarInt(int)} this only uses less than 5 bytes for non-negative integers less than
     * <code>pow(2, 8 * 3 - 1)</code> ()
     *
     * @param ival the unsigned integer value to write.
     * @return this buffer.
     */
    public NetByteBuf writeVarUnsignedInt(int ival) {
        super.writeVarInt(ival);
        return this;
    }

    /**
     * Exposes the vanilla method for reading an unsigned integer using a variable number of bytes.
     * <p>
     * Unlike {@link #readVarInt()} this only uses less than 5 bytes for non-negative integers less than
     * <code>pow(2, 8 * 3 - 1)</code> ()
     *
     * @return the read unsigned integer value.
     */
    public int readVarUnsignedInt() {
        return super.readVarInt();
    }

    /**
     * Writes out a long integer using a variable number of bytes.
     * <ul>
     * <li>1 byte for -64 to 63</li>
     * <li>2 bytes for -8,192 to 8,191</li>
     * <li>3 bytes for -1,048,576 to 1,048,575</li>
     * <li>4 bytes for -134,217,728 to 134,217,727</li>
     * </ul>
     * <p>
     * Unlike vanilla this doesn't use 9 bytes for all negative numbers.
     *
     * @param lval the long integer value to write.
     * @return this buffer.
     */
    // TODO: Finish that!
    @Override
    public NetByteBuf writeVarLong(long lval) {
        if (passthrough) {
            super.writeVarLong(lval);
            return this;
        }
        // Copy-pasted from writeVarInt
        final int sign;
        if (lval < 0) {
            lval = ~lval;
            sign = 1;
        } else {
            sign = 0;
        }
        while ((lval & ~0x3f) != 0) {
            writeByte((int) (0x80 | (lval & 0x7f)));
            lval >>>= 7;
        }
        writeByte((int) ((sign << 6) | lval));
        return this;
    }

    @Override
    public long readVarLong() {
        if (passthrough) {
            return super.readVarLong();
        }
        int count = 0;
        long lval = 0;
        long read;
        do {
            read = readUnsignedByte();
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
     * Exposes the vanilla method for writing out an unsigned long integer using a variable number of bytes.
     * <p>
     * Unlike {@link #writeVarInt(int)} this only uses less than 9 bytes for non-negative integers less than
     * <code>pow(2, 8 * 7 - 1)</code> ()
     *
     * @param lval the unsigned long integer value to write.
     * @return this buffer.
     */
    public NetByteBuf writeVarUnsignedLong(long lval) {
        super.writeVarLong(lval);
        return this;
    }

    /**
     * Exposes the vanilla method for reading an unsigned long integer using a variable number of bytes.
     * <p>
     * Unlike {@link #readVarInt()} this only uses less than 9 bytes for non-negative integers less than
     * <code>pow(2, 8 * 7 - 1)</code> ()
     *
     * @return the read unsigned long integer value.
     */
    public long readVarUnsignedLong() {
        return super.readVarLong();
    }

    @Override
    public NetByteBuf writeIdentifier(Identifier id) {
        super.writeIdentifier(id);
        return this;
    }

    /**
     * Reads an identifier value from the buffer.
     *
     * @return the valid identifier read.
     * @deprecated Because {@link PacketByteBuf#readIdentifier()} can throw an {@link InvalidIdentifierException}.
     */
    @Override
    @Deprecated
    public Identifier readIdentifier() {
        return super.readIdentifier();
    }

    /**
     * Reads in a string, and tries to parse it as an {@link Identifier}. If the string is a valid identifier then it
     * is returned, however if it isn't then {@link DecoderException} is thrown.
     *
     * @return the valid identifier read.
     * @throws DecoderException if the read string wasn't a valid {@link Identifier}.
     */
    public Identifier readIdentifierSafe() throws DecoderException {
        try {
            return super.readIdentifier();
        } catch (InvalidIdentifierException iee) {
            throw new DecoderException("Invalid Identifier", iee);
        }
    }

    /**
     * Like {@link #readIdentifierSafe()}, but returns null instead of throwing an error if the read string was
     * invalid.
     *
     * @return the valid identifier read, or {@code null} if the read string did not represent a valid identifier.
     */
    @Nullable
    public Identifier readIdentifierOrNull() {
        try {
            return super.readIdentifier();
        } catch (InvalidIdentifierException iee) {
            return null;
        }
    }

    /**
     * Reads a string of up to {@link Short#MAX_VALUE} length.
     * <p>
     * NOTE: This is just {@link PacketByteBuf#readString()} but available on the server as well.
     *
     * @return the read string.
     */
    @Override
    public String readString() {
        return readString(Short.MAX_VALUE);
    }

    /**
     * Writes an optional value to this buf. An optional value is represented by
     * a boolean indicating if the value is present, followed by the value only if
     * the value is present.
     *
     * @param value  the optional value to write.
     * @param writer the packet writer capable of writing the value.
     * @see #readOptional(PacketReader)
     */
    public <T> void writeOptional(Optional<T> value, PacketWriter<T> writer) {
        if (value.isPresent()) {
            this.writeBoolean(true);
            writer.accept(this, value.get());
        } else {
            this.writeBoolean(false);
        }
    }

    /**
     * Reads an optional value from this buf. An optional value is represented by
     * a boolean indicating if the value is present, followed by the value only if
     * the value is present.
     *
     * @param reader the packet reader capable of reading the value.
     * @return the read optional value
     * @see #writeOptional(Optional, PacketWriter)
     */
    public <T> Optional<T> readOptional(PacketReader<T> reader) {
        return this.readBoolean() ? Optional.of(reader.apply(this)) : Optional.empty();
    }
}
