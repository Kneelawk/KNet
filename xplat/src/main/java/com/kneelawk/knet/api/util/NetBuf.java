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

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import io.netty.buffer.ByteBuf;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketDecoder;
import net.minecraft.network.codec.PacketEncoder;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

/**
 * Super-interface for all net buffers to make sure they have the same interface and implement the same methods.
 *
 * @param <B> the implementing buffer.
 */
public interface NetBuf<B extends PacketByteBuf & NetBuf<B>> {
    /**
     * The minimum value that can fit within a single byte when using signed var-int encoding.
     */
    int MIN_VAR_S_INT_1_BYTE = -(1 << 6);
    /**
     * The maximum value that can fit within a single byte when using signed var-int encoding.
     */
    int MAX_VAR_S_INT_1_BYTE = (1 << 6) - 1;
    /**
     * The minimum value that can fit within two bytes when using signed var-int encoding.
     */
    int MIN_VAR_S_INT_2_BYTES = -(1 << 6 + 7);
    /**
     * The maximum value that can fit within two bytes when using signed var-int encoding.
     */
    int MAX_VAR_S_INT_2_BYTES = (1 << 6 + 7) - 1;
    /**
     * The minimum value that can fit within three bytes when using signed var-int encoding.
     */
    int MIN_VAR_S_INT_3_BYTES = -(1 << 6 + 7 * 2);
    /**
     * The maximum value that can fit within three bytes when using signed var-int encoding.
     */
    int MAX_VAR_S_INT_3_BYTES = (1 << 6 + 7 * 2) - 1;
    /**
     * The minimum value that can fit within four bytes when using signed var-int encoding.
     */
    int MIN_VAR_S_INT_4_BYTES = -(1 << 6 + 7 * 3);
    /**
     * The maximum value that can fit within four bytes when using signed var-int encoding.
     */
    int MAX_VAR_S_INT_4_BYTES = (1 << 6 + 7 * 3) - 1;
    /**
     * The minimum value that can be encoded using unsigned var-int encoding.
     */
    int MIN_VAR_U_INT_SMALL = 0;
    /**
     * The maximum value that can fit within a single byte when using unsigned var-int encoding.
     */
    int MAX_VAR_U_INT_1_BYTE = 1 << 7;
    /**
     * The maximum value that can fit within two bytes when using unsigned var-int encoding.
     */
    int MAX_VAR_U_INT_2_BYTES = 1 << 7 * 2;
    /**
     * The maximum value that can fit within three bytes when using unsigned var-int encoding.
     */
    int MAX_VAR_U_INT_3_BYTES = 1 << 7 * 3;
    /**
     * The maximum value that can fit within four bytes when using unsigned var-int encoding.
     */
    int MAX_VAR_U_INT_4_BYTES = 1 << 7 * 4;

    /**
     * Gets this buffer as a {@link PacketByteBuf}.
     *
     * @return this buffer as a {@link PacketByteBuf}.
     */
    PacketByteBuf self();

    /**
     * Gets the buffer that this is wrapping.
     *
     * @return the wrapped buffer.
     */
    ByteBuf getWrapped();

    /**
     * {@return the read index within the partial byte.}
     *
     * @see #getBitReaderIndex()
     */
    int getReadPartialOffset();

    /**
     * Sets the read index within the partial byte.
     *
     * @param readPartialOffset the new read index within the partial byte.
     */
    void setReadPartialOffset(int readPartialOffset);

    /**
     * {@return the partial byte being read.}
     */
    int getReadPartialCache();

    /**
     * Sets the value of the current partial byte being read.
     *
     * @param readPartialCache the new value of the current partial byte being read.
     */
    void setReadPartialCache(int readPartialCache);

    /**
     * {@return the index of the partial byte being written to.}
     */
    int getWritePartialIndex();

    /**
     * Sets the index of the partial byte being written to.
     *
     * @param writePartialIndex the new index of the partial byte being written to.
     */
    void setWritePartialIndex(int writePartialIndex);

    /**
     * {@return the write index within the partial byte.}
     */
    int getWritePartialOffset();

    /**
     * Sets the write index within the partial byte.
     *
     * @param writePartialOffset the new write index within the partial byte.
     */
    void setWritePartialOffset(int writePartialOffset);

    /**
     * {@return the value of the current partial byte being written.}
     */
    int getWritePartialCache();

    /**
     * Sets the value of the current partial byte being written.
     *
     * @param writePartialCache the new value of the current partial byte being written.
     */
    void setWritePartialCache(int writePartialCache);

    /**
     * Or's the provided byte with the current partial byte being written.
     *
     * @param toWrite the byte to or with the current partial byte.
     */
    void orWritePartialCache(int toWrite);

    /**
     * Increments the write index within the partial byte, returning its previous value.
     *
     * @return the previous value of the write index within the partial byte.
     */
    int incrementWritePartialOffset();

    /**
     * Increments the write index within the partial byte by the given amount.
     *
     * @param amount the amount to add to the write index within the partial byte.
     */
    void incrementWritePartialOffset(int amount);

    /**
     * Increments the read index within the partial byte, returning its previous value.
     *
     * @return the previous value of th read index within the partial byte.
     */
    int incrementReadPartialOffset();

    /**
     * Increments the read index within the partial byte by the given amount.
     *
     * @param amount the amount to add to the read index within the partial byte.
     */
    void incrementReadPartialOffset(int amount);

    /**
     * Writes the value of the current partial byte being written to its determined location.
     */
    void writePartialCache();

    /**
     * {@return the index of the byte being read from.}
     */
    int readerIndex();

    /**
     * {@return the index of the byte being written to.}
     */
    int writerIndex();

    /**
     * Bit version of {@link #writerIndex()}.
     *
     * @return the current writer partial offset.
     */
    int getBitWriterIndex();

    /**
     * Bit version of {@link #readerIndex()}.
     *
     * @return the current reader partial offset.
     */
    int getBitReaderIndex();


    /**
     * Returns a copy of this buffer's readable bytes.  Modifying the content
     * of the returned buffer or this buffer does not affect each other at all.
     * This method is identical to {@code buf.copy(buf.readerIndex(), buf.readableBytes())}.
     * This method does not modify {@code readerIndex} or {@code writerIndex} of
     * this buffer.
     *
     * @return a copy of this buffer.
     */
    ByteBuf copy();

    /**
     * Transfers this buffer's data to a newly created buffer starting at
     * the current {@code readerIndex} and increases the {@code readerIndex}
     * by the number of the transferred bytes (= {@code length}).
     * The returned buffer's {@code readerIndex} and {@code writerIndex} are
     * {@code 0} and {@code length} respectively.
     * <p>
     * <b>Note:</b> this will not carry partial reads over to the returned buffer.
     *
     * @param length the number of bytes to transfer
     * @return the newly created buffer which contains the transferred bytes
     * @throws IndexOutOfBoundsException if {@code length} is greater than {@code this.readableBytes}
     */
    ByteBuf readBytes(int length);

    /**
     * Sets the {@code readerIndex} and {@code writerIndex} of this buffer to
     * {@code 0} while resetting the partial byte data.
     * <p>
     * Please note that the behavior of this method is different
     * from that of NIO buffer, which sets the {@code limit} to
     * the {@code capacity} of the buffer.
     *
     * @return this buffer.
     */
    PacketByteBuf clear();

    /**
     * Marks the current {@code readerIndex} in this buffer.  You can
     * reposition the current {@code readerIndex} to the marked
     * {@code readerIndex} by calling {@link #resetReaderIndex()}.
     * The initial value of the marked {@code readerIndex} is {@code 0}.
     *
     * @return this buffer.
     */
    PacketByteBuf markReaderIndex();

    /**
     * Repositions the current {@code readerIndex} to the marked
     * {@code readerIndex} in this buffer.
     *
     * @return this buffer.
     * @throws IndexOutOfBoundsException if the current {@code writerIndex} is less than the marked
     *                                   {@code readerIndex}
     */
    PacketByteBuf resetReaderIndex();

    /**
     * Creates a saved reader index that can be restored to continue reading from the position of this buffer when this
     * method was called.
     *
     * @return the saved reader index used for restoring the buffer's position.
     */
    SavedReaderIndex saveReaderIndex();

    /**
     * Restores the buffer's reader position to the saved reader index.
     *
     * @param index the saved reader index of the position to restore the buffer to.
     * @return this buffer.
     */
    B resetReaderIndex(SavedReaderIndex index);

    /**
     * Writes a single boolean out to some position in this buffer. The boolean flag might be written to a new byte
     * (increasing the writerIndex) or it might be added to an existing byte that was written with a previous call to
     * this method.
     *
     * @param flag the boolean to write.
     * @return this buffer.
     */
    PacketByteBuf writeBoolean(boolean flag);

    /**
     * Reads a single boolean from some position in this buffer. The boolean flag might be read from a new byte
     * (increasing the readerIndex) or it might be read from a previous byte that was read with a previous call to this
     * method.
     *
     * @return the read boolean.
     */
    boolean readBoolean();

    /**
     * Writes a fixed number of bits out to the stream.
     *
     * @param value  the value to write out.
     * @param length The number of bits to write.
     * @return This buffer.
     * @throws IllegalArgumentException if the length argument was less than 1 or greater than 32.
     */
    B writeFixedBits(int value, int length) throws IllegalArgumentException;

    /**
     * Reads a fixed number of bits from the given stream.
     *
     * @param length The number of bits to read.
     * @return The read bits, compacted into an int.
     * @throws IllegalArgumentException if the length argument was less than 1 or greater than 32.
     */
    int readFixedBits(int length) throws IllegalArgumentException;

    /**
     * Writes an enum constant to this buf. An enum constant is represented
     * by a var int indicating its ordinal.
     *
     * @param value the enum constant to write.
     * @return this buf, for chaining.
     * @see #readEnumConstant(Class)
     */
    PacketByteBuf writeEnumConstant(Enum<?> value);

    /**
     * Reads an enum constant from this buf. An enum constant is represented
     * by a var int indicating its ordinal.
     *
     * @param enumClass the enum class, for constant lookup.
     * @param <E>       the type of enum to read.
     * @return the read enum constant.
     * @see #writeEnumConstant(Enum)
     */
    <E extends Enum<E>> E readEnumConstant(Class<E> enumClass);

    /**
     * Writes out a {@link BlockPos} using 3 {@link #writeVarInt(int)}s rather than {@link BlockPos#asLong()}.
     *
     * @param pos the block position to write.
     * @return this buffer.
     */
    PacketByteBuf writeBlockPos(BlockPos pos);

    /**
     * Reads a {@link BlockPos} using 3 {@link #readVarInt()}s rather than {@link BlockPos#fromLong(long)}.
     *
     * @return the read block position.
     */
    BlockPos readBlockPos();

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
    PacketByteBuf writeVarInt(int ival);

    /**
     * Reads out an integer using a variable number of bytes, assuming it was written by {@link #writeVarInt(int)}
     *
     * @return the read variable-length integer.
     */
    int readVarInt();

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
    B writeVarUnsignedInt(int ival);

    /**
     * Exposes the vanilla method for reading an unsigned integer using a variable number of bytes.
     * <p>
     * Unlike {@link #readVarInt()} this only uses less than 5 bytes for non-negative integers less than
     * <code>pow(2, 8 * 3 - 1)</code> ()
     *
     * @return the read unsigned integer value.
     */
    int readVarUnsignedInt();

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
    PacketByteBuf writeVarLong(long lval);

    /**
     * Reads a single var long from this buf.
     *
     * @return the value read
     * @see #writeVarLong(long)
     */
    long readVarLong();

    /**
     * Exposes the vanilla method for writing out an unsigned long integer using a variable number of bytes.
     * <p>
     * Unlike {@link #writeVarInt(int)} this only uses less than 9 bytes for non-negative integers less than
     * <code>pow(2, 8 * 7 - 1)</code> ()
     *
     * @param lval the unsigned long integer value to write.
     * @return this buffer.
     */
    B writeVarUnsignedLong(long lval);

    /**
     * Exposes the vanilla method for reading an unsigned long integer using a variable number of bytes.
     * <p>
     * Unlike {@link #readVarInt()} this only uses less than 9 bytes for non-negative integers less than
     * <code>pow(2, 8 * 7 - 1)</code> ()
     *
     * @return the read unsigned long integer value.
     */
    long readVarUnsignedLong();

    /**
     * Like {@link PacketByteBuf#readIdentifier()}, but returns null instead of throwing an error if the read string was
     * invalid.
     *
     * @return the valid identifier read, or {@code null} if the read string did not represent a valid identifier.
     */
    @Nullable
    Identifier readIdentifierOrNull();

    /**
     * Reads a string of up to {@link Short#MAX_VALUE} length.
     * <p>
     * NOTE: This is just {@link PacketByteBuf#readString()} but available on the server as well.
     *
     * @return the read string.
     */
    String readString();

    /**
     * Writes an optional value to this buf. An optional value is represented by
     * a boolean indicating if the value is present, followed by the value only if
     * the value is present.
     *
     * @param value  the optional value to write.
     * @param writer the packet writer capable of writing the value.
     * @param <T>    the type this method optionally writes.
     * @return this buffer.
     * @see #readNetOptional(PacketDecoder)
     */
    <T> B writeNetOptional(Optional<T> value, PacketEncoder<? super B, T> writer);

    /**
     * Reads an optional value from this buf. An optional value is represented by
     * a boolean indicating if the value is present, followed by the value only if
     * the value is present.
     *
     * @param reader the packet reader capable of reading the value.
     * @param <T>    the type this method optionally reads.
     * @return the read optional value
     * @see #writeNetOptional(Optional, PacketEncoder)
     */
    <T> Optional<T> readNetOptional(PacketDecoder<? super B, T> reader);

    /**
     * Holds an index into a {@link NetByteBuf} that can be restored.
     */
    final class SavedReaderIndex {
        /**
         * The saved reader index.
         */
        public final int readerIndex;

        /**
         * the saved reader index within the partial byte.
         */
        public final int readPartialOffset;

        /**
         * The saved value of the partial byte.
         */
        public final int readPartialCache;

        /**
         * Creates a SavedReaderIndex using the current state of the buffer.
         *
         * @param buffer the buffer to safe the current reader state of.
         */
        public SavedReaderIndex(NetBuf<?> buffer) {
            readerIndex = buffer.readerIndex();
            readPartialOffset = buffer.getReadPartialOffset();
            readPartialCache = buffer.getReadPartialCache();
        }
    }
}
