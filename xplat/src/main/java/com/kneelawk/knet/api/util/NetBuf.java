/*
 * Copyright (c) 2019 AlexIIL
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kneelawk.knet.api.util;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;
import java.util.function.IntFunction;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.jetbrains.annotations.Nullable;

import io.netty.buffer.ByteBuf;

import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamDecoder;
import net.minecraft.network.codec.StreamEncoder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;

/**
 * Super-interface for all net buffers to make sure they have the same interface and implement the same methods.
 *
 * @param <B> the implementing buffer.
 */
public interface NetBuf<B extends FriendlyByteBuf & NetBuf<? super B>> {
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
     * Gets this buffer as a {@link B}.
     *
     * @return this buffer as a {@link B}.
     */
    B self();

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
    FriendlyByteBuf clear();

    /**
     * Marks the current {@code readerIndex} in this buffer.  You can
     * reposition the current {@code readerIndex} to the marked
     * {@code readerIndex} by calling {@link #resetReaderIndex()}.
     * The initial value of the marked {@code readerIndex} is {@code 0}.
     *
     * @return this buffer.
     */
    FriendlyByteBuf markReaderIndex();

    /**
     * Repositions the current {@code readerIndex} to the marked
     * {@code readerIndex} in this buffer.
     *
     * @return this buffer.
     * @throws IndexOutOfBoundsException if the current {@code writerIndex} is less than the marked
     *                                   {@code readerIndex}
     */
    FriendlyByteBuf resetReaderIndex();

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
    FriendlyByteBuf writeBoolean(boolean flag);

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
    default B writeFixedBits(int value, int length) throws IllegalArgumentException {
        NetBufImplHelper.writeFixedBits(this, value, length);
        return self();
    }

    /**
     * Reads a fixed number of bits from the given stream.
     *
     * @param length The number of bits to read.
     * @return The read bits, compacted into an int.
     * @throws IllegalArgumentException if the length argument was less than 1 or greater than 32.
     */
    default int readFixedBits(int length) throws IllegalArgumentException {
        return NetBufImplHelper.readFixedBits(this, length);
    }

    /**
     * Writes an enum constant to this buf. An enum constant is represented
     * by a var int indicating its ordinal.
     *
     * @param value the enum constant to write.
     * @return this buf, for chaining.
     * @see #readEnum(Class)
     */
    FriendlyByteBuf writeEnum(Enum<?> value);

    /**
     * Reads an enum constant from this buf. An enum constant is represented
     * by a var int indicating its ordinal.
     *
     * @param enumClass the enum class, for constant lookup.
     * @param <E>       the type of enum to read.
     * @return the read enum constant.
     * @see #writeEnum(Enum)
     */
    <E extends Enum<E>> E readEnum(Class<E> enumClass);

    /**
     * Writes out a {@link BlockPos} using 3 {@link #writeVarInt(int)}s rather than {@link BlockPos#asLong()}.
     *
     * @param pos the block position to write.
     * @return this buffer.
     */
    FriendlyByteBuf writeBlockPos(BlockPos pos);

    /**
     * Reads a {@link BlockPos} using 3 {@link #readVarInt()}s rather than {@link BlockPos#of(long)}.
     *
     * @return the read block position.
     */
    BlockPos readBlockPos();

    /**
     * Writes out a {@link ChunkPos} using 2 {@link #writeVarInt(int)}s rather than a {@link ChunkPos#toLong()}.
     *
     * @param pos the chunk position to write.
     * @return this buffer.
     */
    FriendlyByteBuf writeChunkPos(ChunkPos pos);

    /**
     * Reads a {@link ChunkPos} using 2 {@link #readVarInt()}s rather than a {@link ChunkPos#ChunkPos(long)}.
     *
     * @return the read chunk pos.
     */
    ChunkPos readChunkPos();

    /**
     * Writes out a {@link SectionPos} using 3 {@link #writeVarInt(int)}s rather than a {@link SectionPos#asLong()}.
     *
     * @param pos the cunk section position to write.
     * @return this buffer.
     */
    FriendlyByteBuf writeSectionPos(SectionPos pos);

    /**
     * Reads a {@link SectionPos} using 3 {@link #readVarInt()}s rather than a {@link SectionPos#of(long)}.
     *
     * @return the read chunk section pos.
     */
    SectionPos readSectionPos();

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
    FriendlyByteBuf writeVarInt(int ival);

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
    FriendlyByteBuf writeVarLong(long lval);

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
     * Like {@link FriendlyByteBuf#readResourceLocation()}, but returns null instead of throwing an error if the read string was
     * invalid.
     * <p>
     * <b>Note:</b> this <em>will</em> attempt to read a string from the buffer regardless of whether that string is a valid {@link ResourceLocation}.
     *
     * @return the valid identifier read, or {@code null} if the read string did not represent a valid identifier.
     */
    @Nullable
    ResourceLocation readIdentifierOrNull();

    /**
     * Reads a string of up to {@link Short#MAX_VALUE} length.
     * <p>
     * NOTE: This is just {@link FriendlyByteBuf#readUtf()} but available on the server as well.
     *
     * @return the read string.
     */
    String readUtf();

    /**
     * Writes an optional value to this buf. An optional value is represented by
     * a boolean indicating if the value is present, followed by the value only if
     * the value is present.
     *
     * @param value  the optional value to write.
     * @param writer the packet writer capable of writing the value.
     * @param <T>    the type this method optionally writes.
     * @return this buffer.
     * @see #readNetOptional(StreamDecoder)
     */
    default <T> B writeNetOptional(Optional<T> value, StreamEncoder<? super B, T> writer) {
        if (value.isPresent()) {
            writeBoolean(true);
            writer.encode(self(), value.get());
        } else {
            writeBoolean(false);
        }
        return self();
    }

    /**
     * Reads an optional value from this buf. An optional value is represented by
     * a boolean indicating if the value is present, followed by the value only if
     * the value is present.
     *
     * @param reader the packet reader capable of reading the value.
     * @param <T>    the type this method optionally reads.
     * @return the read optional value
     * @see #writeNetOptional(Optional, StreamEncoder)
     */
    default <T> Optional<T> readNetOptional(StreamDecoder<? super B, T> reader) {
        return readBoolean() ? Optional.of(reader.decode(self())) : Optional.empty();
    }

    /**
     * Writes an optional integer to this buf.
     *
     * @param value the optional integer to write.
     * @return this buffer.
     */
    default B writeNetOptionalInt(OptionalInt value) {
        if (value.isPresent()) {
            writeBoolean(true);
            writeVarInt(value.getAsInt());
        } else {
            writeBoolean(false);
        }
        return self();
    }

    /**
     * Reads an optional integer from this buf.
     *
     * @return the value read.
     */
    default OptionalInt readNetOptionalInt() {
        return readBoolean() ? OptionalInt.of(readVarInt()) : OptionalInt.empty();
    }

    /**
     * Writes an optional long to this buf.
     *
     * @param value the optional long to write.
     * @return this buffer.
     */
    default B writeNetOptionalLong(OptionalLong value) {
        if (value.isPresent()) {
            writeBoolean(true);
            writeVarLong(value.getAsLong());
        } else {
            writeBoolean(false);
        }
        return self();
    }

    /**
     * Reads an optional long from this buf.
     *
     * @return the value to read.
     */
    default OptionalLong readNetOptionalLong() {
        return readBoolean() ? OptionalLong.of(readVarLong()) : OptionalLong.empty();
    }

    /**
     * Writes an optional double to this buf.
     *
     * @param value the optional double to write.
     * @return this buffer.
     */
    default B writeNetOptionalDouble(OptionalDouble value) {
        if (value.isPresent()) {
            writeBoolean(true);
            self().writeDouble(value.getAsDouble());
        } else {
            writeBoolean(false);
        }
        return self();
    }

    /**
     * Reads an optional double from this buf.
     *
     * @return the value read.
     */
    default OptionalDouble readNetOptionalDouble() {
        return readBoolean() ? OptionalDouble.of(self().readDouble()) : OptionalDouble.empty();
    }

    /**
     * Writes a nullable value to this buf. A nullable value is represented by
     * a boolean indicating if the value is not null, followed by the value only if
     * the value is not null.
     *
     * @param value  the value to write.
     * @param writer the writer for the given type.
     * @param <T>    the type to write.
     * @return this buffer.
     * @see #readNetNullable(StreamDecoder)
     */
    default <T> B writeNetNullable(@Nullable T value, StreamEncoder<? super B, T> writer) {
        if (value != null) {
            writeBoolean(true);
            writer.encode(self(), value);
        } else {
            writeBoolean(false);
        }
        return self();
    }

    /**
     * Reads a nullable value from this buf. A nullable value is represented by
     * a boolean indicating if the value is not null, followed by the value only if
     * the value is not null.
     *
     * @param reader the reader for the given type.
     * @param <T>    the type to write.
     * @return the read nullable value
     * @see #writeNetNullable(Object, StreamEncoder)
     */
    default <T> @Nullable T readNetNullable(StreamDecoder<? super B, T> reader) {
        return readBoolean() ? reader.decode(self()) : null;
    }

    /**
     * Writes an array of values to this buf. The array is represented by a length
     * {@linkplain #writeVarUnsignedInt(int) unsigned var int}, followed by values written via the writer.
     *
     * @param array  the array to write.
     * @param writer the writer for the given type.
     * @param <T>    the type of the array to write.
     * @return this buffer.
     */
    default <T> B writeNetArray(T[] array, StreamEncoder<? super B, T> writer) {
        writeVarUnsignedInt(array.length);
        for (T obj : array) {
            writer.encode(self(), obj);
        }
        return self();
    }

    /**
     * Reads an array of values from this buf. The array is represented by a length
     * {@linkplain #readVarUnsignedInt() unsigned var int}, followed by values read via the reader.
     *
     * @param arrayCtor the array constructor.
     * @param reader    the reader for the given type.
     * @param <T>       the type of the array to read.
     * @return the read array.
     */
    default <T> T[] readNetArray(IntFunction<T[]> arrayCtor, StreamDecoder<? super B, T> reader) {
        int length = readVarUnsignedInt();
        T[] array = arrayCtor.apply(length);
        for (int i = 0; i < length; i++) {
            array[i] = reader.decode(self());
        }
        return array;
    }

    /**
     * Writes a collection to this buf. The collection is represented by a length
     * {@linkplain #readVarUnsignedInt() unsigned var int}, followed by values written via the writer.
     *
     * @param collection the collection to write.
     * @param writer     the writer for the given type.
     * @param <T>        the type within the collection to write.
     * @return this buffer.
     */
    default <T> B writeNetCollection(Collection<T> collection, StreamEncoder<? super B, T> writer) {
        writeVarUnsignedInt(collection.size());

        for (T obj : collection) {
            writer.encode(self(), obj);
        }

        return self();
    }

    /**
     * Reads a collection from this buf. The collection is represented by a length
     * {@linkplain #readVarUnsignedInt() unsigned var int}, followed by the values read via the reader.
     *
     * @param collectionCtor the collection constructor.
     * @param reader         the reader for the given type.
     * @param <T>            the type of value in the collection.
     * @param <C>            the type of collection.
     * @return the read collection.
     */
    default <T, C extends Collection<T>> C readNetCollection(IntFunction<C> collectionCtor,
                                                             StreamDecoder<? super B, T> reader) {
        int length = readVarUnsignedInt();
        C collection = collectionCtor.apply(length);

        for (int i = 0; i < length; i++) {
            collection.add(reader.decode(self()));
        }

        return collection;
    }

    /**
     * Reads a list via {@link #readNetCollection(IntFunction, StreamDecoder)}.
     *
     * @param reader the reader to read list elements.
     * @param <T>    the type of value in the list.
     * @return the read list.
     */
    default <T> List<T> readNetList(StreamDecoder<? super B, T> reader) {
        return readNetCollection(Lists::newArrayListWithCapacity, reader);
    }

    /**
     * Writes a map to this buf. The map is represented by a length
     * {@linkplain #writeVarUnsignedInt(int) unsigned var int}, followed by key-value pairs.
     *
     * @param map         the map to write.
     * @param keyWriter   the writer for the keys.
     * @param valueWriter the write for the values.
     * @param <K>         the key type.
     * @param <V>         the value type.
     * @return this buffer.
     */
    default <K, V> B writeNetMap(Map<K, V> map, StreamEncoder<? super B, K> keyWriter,
                                 StreamEncoder<? super B, V> valueWriter) {
        writeVarUnsignedInt(map.size());
        map.forEach((key, value) -> {
            keyWriter.encode(self(), key);
            valueWriter.encode(self(), value);
        });
        return self();
    }

    /**
     * Reads a map from this buf. The map is represented by a length
     * {@linkplain #readVarUnsignedInt() unsigned var int}, followed by key-value pairs.
     *
     * @param mapCtor     the map's constructor.
     * @param keyReader   the reader for keys.
     * @param valueReader the reader for values.
     * @param <K>         the key type.
     * @param <V>         the value type.
     * @param <M>         the map type.
     * @return the read map.
     */
    default <K, V, M extends Map<K, V>> M readNetMap(IntFunction<M> mapCtor, StreamDecoder<? super B, K> keyReader,
                                                     StreamDecoder<? super B, V> valueReader) {
        int length = readVarUnsignedInt();
        M map = mapCtor.apply(length);

        for (int i = 0; i < length; i++) {
            K key = keyReader.decode(self());
            V value = valueReader.decode(self());
            map.put(key, value);
        }

        return map;
    }

    /**
     * Reads a map from this buf via {@link #readNetMap(IntFunction, StreamDecoder, StreamDecoder)}.
     *
     * @param keyReader   the reader for keys.
     * @param valueReader the reader for values.
     * @param <K>         the key type.
     * @param <V>         the value type.
     * @return the read map.
     */
    default <K, V> Map<K, V> readNetMap(StreamDecoder<? super B, K> keyReader,
                                        StreamDecoder<? super B, V> valueReader) {
        return readNetMap(Maps::newHashMapWithExpectedSize, keyReader, valueReader);
    }

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
