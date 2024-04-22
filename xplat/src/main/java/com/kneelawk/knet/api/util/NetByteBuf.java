/*
 * Copyright (c) 2019 AlexIIL
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kneelawk.knet.api.util;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketDecoder;
import net.minecraft.network.codec.PacketEncoder;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.math.BlockPos;

/**
 * Special {@link PacketByteBuf} class that provides methods specific to "offset" reading and writing - like writing a
 * single bit to the stream, and auto-compacting it with similar bits into a single byte.
 * <p>
 * In addition this overrides a number of existing methods (like {@link #writeBoolean(boolean)},
 * {@link #writeEnumConstant(Enum)}, {@link #writeVarInt(int)}, {@link #writeVarLong(long)}, and a few more.
 * <p>
 * Class hierarchy:
 * <pre>
 *               {@link PacketByteBuf}
 *                 /         \
 *   {@link RegistryByteBuf}      {@link NetByteBuf}
 *              |               |
 * {@link NetRegistryByteBuf}&lt;-&gt;{@link RegistryNetByteBuf}
 * </pre>
 *
 * @see RegistryNetByteBuf
 * @see NetRegistryByteBuf
 */
public class NetByteBuf extends PacketByteBuf implements NetBuf<NetByteBuf> {

    /**
     * An empty {@link NetByteBuf}.
     */
    public static final NetByteBuf EMPTY_BUFFER = new NetByteBuf(Unpooled.EMPTY_BUFFER);

    /**
     * Creates a new {@link NetByteBuf} without any initial capacity.
     *
     * @return A new {@link NetByteBuf} from {@link Unpooled#buffer()}
     */
    public static NetByteBuf buffer() {
        return netOf(Unpooled.buffer());
    }

    /**
     * Creates a new {@link NetByteBuf} with the given initial capacity.
     *
     * @param initialCapacity the buffer's initial capacity.
     * @return A new {@link NetByteBuf} from {@link Unpooled#buffer(int)}
     */
    public static NetByteBuf buffer(int initialCapacity) {
        return netOf(Unpooled.buffer(initialCapacity));
    }

    /**
     * Creates a new {@link NetByteBuf} without any initial capacity while optionally disabling optimizations.
     *
     * @param passthrough whether to disable optimizations.
     * @return A new {@link NetByteBuf} from {@link Unpooled#buffer()}
     */
    public static NetByteBuf buffer(boolean passthrough) {
        return netOf(Unpooled.buffer(), passthrough);
    }

    /**
     * Creates a new {@link NetByteBuf} with the given initial capacity while optionally disabling optimizations.
     *
     * @param initialCapacity the buffer's initial capacity.
     * @param passthrough     whether to disable optimizations.
     * @return A new {@link NetByteBuf} from {@link Unpooled#buffer(int)}
     */
    public static NetByteBuf buffer(int initialCapacity, boolean passthrough) {
        return netOf(Unpooled.buffer(initialCapacity), passthrough);
    }

    // Hold on to the wrapped buffer, so we can access it when changing passthrough-ness while wrapping.
    private final ByteBuf wrapped;

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
        this.wrapped = wrapped;
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
    public static NetByteBuf netOf(ByteBuf buf) {
        return netOf(buf, false);
    }

    /**
     * Returns the given {@link ByteBuf} as {@link NetByteBuf}, but with passthrough mode enabled. If the given
     * instance is already a {@link NetByteBuf} then the given buffer is returned (note that this may result in
     * unexpected consequences if multiple read/write Boolean methods are called on the given buffer before you called
     * this).
     *
     * @param buf         the buffer to be converted into a {@link NetByteBuf}.
     * @param passthrough whether to disable optimizations on the resulting buffer.
     * @return the given buffer as a {@link NetByteBuf}.
     */
    public static NetByteBuf netOf(ByteBuf buf, boolean passthrough) {
        if (buf instanceof NetByteBuf netBuf && netBuf.passthrough == passthrough) {
            return netBuf;
        } else {
            return new NetByteBuf(buf, passthrough);
        }
    }

    @Override
    public PacketByteBuf self() {
        return this;
    }

    @Override
    public ByteBuf getWrapped() {
        return wrapped;
    }

    @Override
    public int getReadPartialOffset() {
        return readPartialOffset;
    }

    @Override
    public void setReadPartialOffset(int readPartialOffset) {
        this.readPartialOffset = readPartialOffset;
    }

    @Override
    public int getReadPartialCache() {
        return readPartialCache;
    }

    @Override
    public void setReadPartialCache(int readPartialCache) {
        this.readPartialCache = readPartialCache;
    }

    @Override
    public int getWritePartialIndex() {
        return writePartialIndex;
    }

    @Override
    public void setWritePartialIndex(int writePartialIndex) {
        this.writePartialIndex = writePartialIndex;
    }

    @Override
    public int getWritePartialOffset() {
        return writePartialOffset;
    }

    @Override
    public void setWritePartialOffset(int writePartialOffset) {
        this.writePartialOffset = writePartialOffset;
    }

    @Override
    public int getWritePartialCache() {
        return writePartialCache;
    }

    @Override
    public void setWritePartialCache(int writePartialCache) {
        this.writePartialCache = writePartialCache;
    }

    @Override
    public void orWritePartialCache(int toWrite) {
        writePartialCache |= toWrite;
    }

    @Override
    public int incrementWritePartialOffset() {
        return writePartialOffset++;
    }

    @Override
    public void incrementWritePartialOffset(int amount) {
        writePartialOffset += amount;
    }

    @Override
    public int incrementReadPartialOffset() {
        return readPartialOffset++;
    }

    @Override
    public void incrementReadPartialOffset(int amount) {
        readPartialOffset += amount;
    }

    @Override
    public void writePartialCache() {
        setByte(writePartialIndex, writePartialCache);
    }

    @Override
    public int getBitWriterIndex() {
        return writePartialOffset;
    }

    @Override
    public int getBitReaderIndex() {
        return readPartialOffset;
    }

    @Override
    public NetByteBuf copy() {
        return netOf(super.copy(), passthrough);
    }

    @Override
    public NetByteBuf readBytes(int length) {
        return netOf(super.readBytes(length), passthrough);
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

    @Override
    public SavedReaderIndex saveReaderIndex() {
        return new SavedReaderIndex(this);
    }

    @Override
    public NetByteBuf resetReaderIndex(SavedReaderIndex index) {
        readerIndex(index.readerIndex);
        readPartialOffset = index.readPartialOffset;
        readPartialCache = index.readPartialCache;
        return this;
    }

    @Override
    public NetByteBuf writeBoolean(boolean flag) {
        if (passthrough) {
            super.writeBoolean(flag);
            return this;
        }
        NetBufImplHelper.writeBoolean(this, flag);
        return this;
    }

    @Override
    public boolean readBoolean() {
        if (passthrough) {
            return super.readBoolean();
        }
        return NetBufImplHelper.readBoolean(this);
    }

    @Override
    public NetByteBuf writeFixedBits(int value, int length) throws IllegalArgumentException {
        NetBufImplHelper.writeFixedBits(this, value, length);
        return this;
    }

    @Override
    public int readFixedBits(int length) throws IllegalArgumentException {
        return NetBufImplHelper.readFixedBits(this, length);
    }

    @Override
    public NetByteBuf writeEnumConstant(Enum<?> value) {
        if (passthrough) {
            super.writeEnumConstant(value);
            return this;
        }
        NetBufImplHelper.writeEnumConstant(this, value);
        return this;
    }

    @Override
    public <E extends Enum<E>> E readEnumConstant(Class<E> enumClass) {
        if (passthrough) {
            return super.readEnumConstant(enumClass);
        }
        return NetBufImplHelper.readEnumConstant(this, enumClass);
    }

    @Override
    public NetByteBuf writeBlockPos(BlockPos pos) {
        if (passthrough) {
            super.writeBlockPos(pos);
            return this;
        }
        NetBufImplHelper.writeBlockPos(this, pos);
        return this;
    }

    @Override
    public BlockPos readBlockPos() {
        if (passthrough) {
            return super.readBlockPos();
        }
        return NetBufImplHelper.readBlockPos(this);
    }

    @Override
    public NetByteBuf writeVarInt(int ival) {
        if (passthrough) {
            super.writeVarInt(ival);
            return this;
        }
        NetBufImplHelper.writeVarInt(this, ival);
        return this;
    }

    @Override
    public int readVarInt() {
        if (passthrough) {
            return super.readVarInt();
        }
        return NetBufImplHelper.readVarInt(this);
    }

    @Override
    public NetByteBuf writeVarUnsignedInt(int ival) {
        super.writeVarInt(ival);
        return this;
    }

    @Override
    public int readVarUnsignedInt() {
        return super.readVarInt();
    }

    @Override
    public NetByteBuf writeVarLong(long lval) {
        if (passthrough) {
            super.writeVarLong(lval);
            return this;
        }
        NetBufImplHelper.writeVarLong(this, lval);
        return this;
    }

    @Override
    public long readVarLong() {
        if (passthrough) {
            return super.readVarLong();
        }
        return NetBufImplHelper.readVarLong(this);
    }

    @Override
    public NetByteBuf writeVarUnsignedLong(long lval) {
        super.writeVarLong(lval);
        return this;
    }

    @Override
    public long readVarUnsignedLong() {
        return super.readVarLong();
    }

    @Override
    public NetByteBuf writeIdentifier(Identifier id) {
        super.writeIdentifier(id);
        return this;
    }

    @Override
    @Nullable
    public Identifier readIdentifierOrNull() {
        try {
            return super.readIdentifier();
        } catch (InvalidIdentifierException iee) {
            return null;
        }
    }

    @Override
    public String readString() {
        return readString(Short.MAX_VALUE);
    }

    @Override
    public <T> NetByteBuf writeNetOptional(Optional<T> value, PacketEncoder<? super NetByteBuf, T> writer) {
        return NetBufImplHelper.writeOptional(this, value, writer);
    }

    @Override
    public <T> Optional<T> readNetOptional(PacketDecoder<? super NetByteBuf, T> reader) {
        return NetBufImplHelper.readOptional(this, reader);
    }
}
