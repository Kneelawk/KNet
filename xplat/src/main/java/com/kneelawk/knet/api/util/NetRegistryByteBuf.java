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

import net.minecraft.ResourceLocationException;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamDecoder;
import net.minecraft.network.codec.StreamEncoder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;

/**
 * Special {@link FriendlyByteBuf} that is like a {@link RegistryNetByteBuf} but that extends {@link RegistryFriendlyByteBuf}
 * instead of {@link NetByteBuf}.
 * <p>
 * Class hierarchy:
 * <pre>
 *               {@link FriendlyByteBuf}
 *                 /         \
 *   {@link RegistryFriendlyByteBuf}      {@link NetByteBuf}
 *              |               |
 * {@link NetRegistryByteBuf}&lt;-&gt;{@link RegistryNetByteBuf}
 * </pre>
 *
 * @see NetByteBuf
 * @see RegistryFriendlyByteBuf
 * @see RegistryNetByteBuf
 */
public class NetRegistryByteBuf extends RegistryFriendlyByteBuf implements NetBuf<NetRegistryByteBuf> {

    // Hold on to the wrapped buffer, so we can access it when changing passthrough-ness while wrapping.
    private final ByteBuf wrapped;

    // Hold the wrapped buffer as a NetBuf if it is indeed a NetBuf
    // for fast checks when commuting partials.z
    private final @Nullable NetBuf<?> buf;

    /**
     * If true then all {@link FriendlyByteBuf} override methods that this {@link NetByteBuf} optimises will instead just
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
     * Creates a new {@link NetRegistryByteBuf}.
     *
     * @param wrapped         the buffer to wrap.
     * @param registryManager the registry manager for this buffer.
     * @param passthrough     whether to disable optimizations.
     */
    public NetRegistryByteBuf(ByteBuf wrapped, RegistryAccess registryManager, boolean passthrough) {
        super(wrapped, registryManager);
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
    public NetRegistryByteBuf self() {
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
    public NetRegistryByteBuf copy() {
        return NetBufs.netRegistryOf(super.copy(), registryAccess(), passthrough);
    }

    @Override
    public NetRegistryByteBuf readBytes(int length) {
        return NetBufs.netRegistryOf(super.readBytes(length), registryAccess(), passthrough);
    }

    @Override
    public NetRegistryByteBuf clear() {
        super.clear();
        readPartialOffset = 8;
        readPartialCache = 0;
        writePartialIndex = -1;
        writePartialOffset = 0;
        writePartialCache = 0;
        return this;
    }

    @Override
    public NetRegistryByteBuf markReaderIndex() {
        super.markReaderIndex();
        readPartialOffsetMark = readPartialOffset;
        readPartialCacheMark = readPartialCache;
        return this;
    }

    @Override
    public NetRegistryByteBuf resetReaderIndex() {
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
    public NetRegistryByteBuf resetReaderIndex(SavedReaderIndex index) {
        readerIndex(index.readerIndex);
        readPartialOffset = index.readPartialOffset;
        readPartialCache = index.readPartialCache;
        return this;
    }

    @Override
    public NetRegistryByteBuf writeBoolean(boolean flag) {
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
    public NetRegistryByteBuf writeEnum(Enum<?> value) {
        if (passthrough) {
            super.writeEnum(value);
            return this;
        }
        NetBufImplHelper.writeEnumConstant(this, value);
        return this;
    }

    @Override
    public <E extends Enum<E>> E readEnum(Class<E> enumClass) {
        if (passthrough) {
            return super.readEnum(enumClass);
        }
        return NetBufImplHelper.readEnumConstant(this, enumClass);
    }

    @Override
    public NetRegistryByteBuf writeBlockPos(BlockPos pos) {
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
    public FriendlyByteBuf writeChunkPos(ChunkPos pos) {
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
    public FriendlyByteBuf writeSectionPos(SectionPos pos) {
        if (passthrough) {
            return super.writeSectionPos(pos);
        }
        NetBufImplHelper.writeChunkSectionPos(this, pos);
        return this;
    }

    @Override
    public SectionPos readSectionPos() {
        if (passthrough) {
            return super.readSectionPos();
        }
        return NetBufImplHelper.readChunkSectionPos(this);
    }

    @Override
    public NetRegistryByteBuf writeVarInt(int ival) {
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
    public NetRegistryByteBuf writeVarUnsignedInt(int ival) {
        super.writeVarInt(ival);
        return this;
    }

    @Override
    public int readVarUnsignedInt() {
        return super.readVarInt();
    }

    @Override
    public NetRegistryByteBuf writeVarLong(long lval) {
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
    public NetRegistryByteBuf writeVarUnsignedLong(long lval) {
        super.writeVarLong(lval);
        return this;
    }

    @Override
    public long readVarUnsignedLong() {
        return super.readVarLong();
    }

    @Override
    public NetRegistryByteBuf writeResourceLocation(ResourceLocation id) {
        super.writeResourceLocation(id);
        return this;
    }

    @Override
    @Nullable
    public ResourceLocation readIdentifierOrNull() {
        try {
            return super.readResourceLocation();
        } catch (ResourceLocationException iee) {
            return null;
        }
    }

    @Override
    public String readUtf() {
        return readUtf(Short.MAX_VALUE);
    }

    /**
     * Convenience method for writing something that expects a {@link RegistryNetByteBuf} or parent.
     *
     * @param value  the value to write.
     * @param writer the writer for the given type.
     * @param <T>    the type to write.
     * @return this buffer.
     */
    public <T> NetRegistryByteBuf writeReg(T value, StreamEncoder<? super RegistryNetByteBuf, T> writer) {
        writer.encode(NetBufs.registryNetOf(this), value);
        return this;
    }

    /**
     * Convenience method for reading something that expects a {@link RegistryNetByteBuf} or parent.
     *
     * @param reader the reader for the given type.
     * @param <T>    the type to read.
     * @return the read value.
     */
    public <T> T readReg(StreamDecoder<? super RegistryNetByteBuf, T> reader) {
        return reader.decode(NetBufs.registryNetOf(this));
    }
}
