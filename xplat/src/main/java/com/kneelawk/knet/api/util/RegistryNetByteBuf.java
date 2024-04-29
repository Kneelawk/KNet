/*
 * Copyright (c) 2019 AlexIIL
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kneelawk.knet.api.util;

import io.netty.buffer.ByteBuf;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketDecoder;
import net.minecraft.network.codec.PacketEncoder;
import net.minecraft.registry.DynamicRegistryManager;

/**
 * A {@link NetByteBuf} bound to a particular {@link DynamicRegistryManager} instance. This buffer is like a
 * {@link NetRegistryByteBuf} except it extends {@link NetByteBuf} instead of {@link RegistryByteBuf}.
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
 * @see NetRegistryByteBuf
 */
public class RegistryNetByteBuf extends NetByteBuf {

    private final DynamicRegistryManager registryManager;

    /**
     * Creates a new {@link RegistryNetByteBuf}.
     *
     * @param wrapped         the buffer that this buffer wraps.
     * @param registryManager the registry manager to attach.
     */
    public RegistryNetByteBuf(ByteBuf wrapped, DynamicRegistryManager registryManager) {
        super(wrapped);
        this.registryManager = registryManager;
    }

    /**
     * Creates a new {@link RegistryNetByteBuf}, with passthrough optionally enabled.
     *
     * @param wrapped         the buffer that this buffer wraps.
     * @param passthrough     whether to disable optimizations.
     * @param registryManager the registry manager to attach.
     */
    public RegistryNetByteBuf(ByteBuf wrapped, boolean passthrough, DynamicRegistryManager registryManager) {
        super(wrapped, passthrough);
        this.registryManager = registryManager;
    }

    /**
     * Gets this buffer's attached registry manager.
     *
     * @return this buffer's attached registry manager.
     */
    public DynamicRegistryManager getRegistryManager() {
        return registryManager;
    }

    /**
     * Convenience method for writing something that expects a {@link NetRegistryByteBuf} or parent.
     *
     * @param value  the value to write.
     * @param writer the writer for the given type.
     * @param <T>    the type to write.
     * @return this buffer.
     */
    public <T> RegistryNetByteBuf writeReg(T value, PacketEncoder<? super NetRegistryByteBuf, T> writer) {
        writer.encode(NetBufs.netRegOf(this), value);
        return this;
    }

    /**
     * Convenience method for reading something that expects a {@link NetRegistryByteBuf} or parent.
     *
     * @param reader the reader for the given type.
     * @param <T>    the type to read.
     * @return the read value.
     */
    public <T> T readReg(PacketDecoder<? super NetRegistryByteBuf, T> reader) {
        return reader.decode(NetBufs.netRegOf(this));
    }
}
