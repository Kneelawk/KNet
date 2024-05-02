/*
 * Copyright (c) 2019 AlexIIL
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kneelawk.knet.api.util;

import org.jetbrains.annotations.Nullable;

import io.netty.buffer.ByteBuf;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;

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

    // Hold on to the wrapped buffer, just in case we're wrapping a RegistryByteBuf or something.
    // Though you should really be using a RegistryNetByteBuf in that case.
    private final ByteBuf wrapped;

    // Hold the wrapped buffer as a NetBuf if it is indeed a NetBuf
    // for fast checks when commuting partials.z
    private final @Nullable NetBuf<?> buf;

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

        if (wrapped instanceof NetBuf<?> buf) {
            this.buf = buf;
            readPartialOffset = buf.getReadPartialOffset();
            readPartialCache = buf.getReadPartialCache();
            writePartialIndex = buf.getWritePartialIndex();
            writePartialOffset = buf.getWritePartialOffset();
            writePartialCache = buf.getWritePartialCache();
        } else {
            this.buf = null;
        }
    }

    @Override
    public NetByteBuf self() {
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
        if (buf != null) buf.setReadPartialOffset(readPartialOffset);
    }

    @Override
    public int getReadPartialCache() {
        return readPartialCache;
    }

    @Override
    public void setReadPartialCache(int readPartialCache) {
        this.readPartialCache = readPartialCache;
        if (buf != null) buf.setReadPartialCache(readPartialCache);
    }

    @Override
    public int getWritePartialIndex() {
        return writePartialIndex;
    }

    @Override
    public void setWritePartialIndex(int writePartialIndex) {
        this.writePartialIndex = writePartialIndex;
        if (buf != null) buf.setWritePartialIndex(writePartialIndex);
    }

    @Override
    public int getWritePartialOffset() {
        return writePartialOffset;
    }

    @Override
    public void setWritePartialOffset(int writePartialOffset) {
        this.writePartialOffset = writePartialOffset;
        if (buf != null) buf.setWritePartialOffset(writePartialOffset);
    }

    @Override
    public int getWritePartialCache() {
        return writePartialCache;
    }

    @Override
    public void setWritePartialCache(int writePartialCache) {
        this.writePartialCache = writePartialCache;
        if (buf != null) buf.setWritePartialCache(writePartialCache);
    }

    @Override
    public void orWritePartialCache(int toWrite) {
        writePartialCache |= toWrite;
        if (buf != null) buf.setWritePartialCache(writePartialCache);
    }

    @Override
    public int incrementWritePartialOffset() {
        int res = writePartialOffset++;
        if (buf != null) buf.setWritePartialOffset(writePartialOffset);
        return res;
    }

    @Override
    public void incrementWritePartialOffset(int amount) {
        writePartialOffset += amount;
        if (buf != null) buf.setWritePartialOffset(writePartialOffset);
    }

    @Override
    public int incrementReadPartialOffset() {
        int res = readPartialOffset++;
        if (buf != null) buf.setReadPartialOffset(readPartialOffset);
        return res;
    }

    @Override
    public void incrementReadPartialOffset(int amount) {
        readPartialOffset += amount;
        if (buf != null) buf.setReadPartialOffset(readPartialOffset);
    }

    @Override
    public void writePartialCache() {
        setByte(writePartialIndex, writePartialCache);
        // no need to carry as this operation is automatically performed on the underlying buffer
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
        return NetBufs.netOf(super.copy(), passthrough);
    }

    @Override
    public NetByteBuf readBytes(int length) {
        return NetBufs.netOf(super.readBytes(length), passthrough);
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
    public PacketByteBuf writeChunkPos(ChunkPos pos) {
        if (passthrough) {
            return super.writeChunkPos(pos);
        }
        NetBufImplHelper.writeChunkPos(this, pos);
        return this;
    }

    @Override
    public ChunkPos readChunkPos() {
        if (passthrough) {
            return super.readChunkPos();
        }
        return NetBufImplHelper.readChunkPos(this);
    }

    @Override
    public PacketByteBuf writeChunkSectionPos(ChunkSectionPos pos) {
        if (passthrough) {
            return super.writeChunkSectionPos(pos);
        }
        NetBufImplHelper.writeChunkSectionPos(this, pos);
        return this;
    }

    @Override
    public ChunkSectionPos readChunkSectionPos() {
        if (passthrough) {
            return super.readChunkSectionPos();
        }
        return NetBufImplHelper.readChunkSectionPos(this);
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
}
