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

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketDecoder;
import net.minecraft.network.codec.PacketEncoder;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;

/**
 * Special {@link PacketByteBuf} that is like a {@link RegistryNetByteBuf} but that extends {@link RegistryByteBuf}
 * instead of {@link NetByteBuf}.
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
 * @see NetByteBuf
 * @see RegistryByteBuf
 * @see RegistryNetByteBuf
 */
public class NetRegistryByteBuf extends RegistryByteBuf implements NetBuf<NetRegistryByteBuf> {

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
     * Creates a new {@link NetRegistryByteBuf}.
     *
     * @param buf             the buffer to wrap.
     * @param registryManager the registry manager for this buffer.
     * @param passthrough     whether to disable optimizations.
     */
    public NetRegistryByteBuf(ByteBuf buf, DynamicRegistryManager registryManager, boolean passthrough) {
        super(buf, registryManager);
        this.wrapped = buf;
        this.passthrough = passthrough;
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
    public NetRegistryByteBuf copy() {
        return NetBufs.netRegOf(super.copy(), getRegistryManager(), passthrough);
    }

    @Override
    public NetRegistryByteBuf readBytes(int length) {
        return NetBufs.netRegOf(super.readBytes(length), getRegistryManager(), passthrough);
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
    public NetRegistryByteBuf writeEnumConstant(Enum<?> value) {
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
    public NetRegistryByteBuf writeIdentifier(Identifier id) {
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
