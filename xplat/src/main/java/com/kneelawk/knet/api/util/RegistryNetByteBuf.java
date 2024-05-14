/*
 * Copyright (c) 2019 AlexIIL
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kneelawk.knet.api.util;

import io.netty.buffer.ByteBuf;

import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamDecoder;
import net.minecraft.network.codec.StreamEncoder;

/**
 * A {@link NetByteBuf} bound to a particular {@link RegistryAccess} instance. This buffer is like a
 * {@link NetRegistryByteBuf} except it extends {@link NetByteBuf} instead of {@link RegistryFriendlyByteBuf}.
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
 * @see NetRegistryByteBuf
 */
public class RegistryNetByteBuf extends NetByteBuf {

    private final RegistryAccess registryAccess;

    /**
     * Creates a new {@link RegistryNetByteBuf}.
     *
     * @param wrapped        the buffer that this buffer wraps.
     * @param registryAccess the registry manager to attach.
     */
    public RegistryNetByteBuf(ByteBuf wrapped, RegistryAccess registryAccess) {
        super(wrapped);
        this.registryAccess = registryAccess;
    }

    /**
     * Creates a new {@link RegistryNetByteBuf}, with passthrough optionally enabled.
     *
     * @param wrapped        the buffer that this buffer wraps.
     * @param passthrough    whether to disable optimizations.
     * @param registryAccess the registry manager to attach.
     */
    public RegistryNetByteBuf(ByteBuf wrapped, boolean passthrough, RegistryAccess registryAccess) {
        super(wrapped, passthrough);
        this.registryAccess = registryAccess;
    }

    /**
     * Gets this buffer's attached registry manager.
     *
     * @return this buffer's attached registry manager.
     */
    public RegistryAccess registryAccess() {
        return registryAccess;
    }

    /**
     * Convenience method for writing something that expects a {@link NetRegistryByteBuf} or parent.
     *
     * @param value  the value to write.
     * @param writer the writer for the given type.
     * @param <T>    the type to write.
     * @return this buffer.
     */
    public <T> RegistryNetByteBuf writeReg(T value, StreamEncoder<? super NetRegistryByteBuf, T> writer) {
        writer.encode(NetBufs.netRegistryOf(this), value);
        return this;
    }

    /**
     * Convenience method for reading something that expects a {@link NetRegistryByteBuf} or parent.
     *
     * @param reader the reader for the given type.
     * @param <T>    the type to read.
     * @return the read value.
     */
    public <T> T readReg(StreamDecoder<? super NetRegistryByteBuf, T> reader) {
        return reader.decode(NetBufs.netRegistryOf(this));
    }
}
