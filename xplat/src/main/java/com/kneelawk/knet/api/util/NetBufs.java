/*
 * Copyright (c) 2019 AlexIIL
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.kneelawk.knet.api.util;

import java.util.function.Function;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.registry.DynamicRegistryManager;

/**
 * Utility methods for working with KNet buffers.
 * <p>
 * Net buffer class hierarchy:
 * <pre>
 *               {@link PacketByteBuf}
 *                 /         \
 *   {@link RegistryByteBuf}      {@link NetByteBuf}
 *              |               |
 * {@link NetRegistryByteBuf}&lt;-&gt;{@link RegistryNetByteBuf}
 * </pre>
 *
 * @see NetBuf
 * @see NetByteBuf
 * @see RegistryNetByteBuf
 * @see NetRegistryByteBuf
 */
public final class NetBufs {
    /**
     * An empty {@link NetByteBuf}.
     */
    public static final NetByteBuf EMPTY_BUFFER = new NetByteBuf(Unpooled.EMPTY_BUFFER);

    private NetBufs() {}

    /**
     * Creates a function that wraps a {@link ByteBuf} in a {@link RegistryNetByteBuf}, attaching the given registry
     * manager.
     *
     * @param registryManager the registry manager to attach.
     * @return the function that wraps buffers.
     */
    public static Function<ByteBuf, RegistryNetByteBuf> regNetFactory(DynamicRegistryManager registryManager) {
        return buf -> regNetOf(buf, registryManager);
    }

    /**
     * Creates a function that wraps a {@link ByteBuf} in a {@link NetRegistryByteBuf}, attaching the given registry
     * manager.
     *
     * @param registryManager the registry manager to attach.
     * @return the function that wraps buffers.
     */
    public static Function<ByteBuf, NetRegistryByteBuf> netRegFactory(DynamicRegistryManager registryManager) {
        return buf -> netRegOf(buf, registryManager);
    }

    /**
     * Creates a new {@link NetByteBuf} without any initial capacity.
     *
     * @return A new {@link NetByteBuf} from {@link Unpooled#buffer()}
     */
    public static NetByteBuf netBuf() {
        return netOf(Unpooled.buffer());
    }

    /**
     * Creates a new {@link NetByteBuf} with the given initial capacity.
     *
     * @param initialCapacity the buffer's initial capacity.
     * @return A new {@link NetByteBuf} from {@link Unpooled#buffer(int)}
     */
    public static NetByteBuf netBuf(int initialCapacity) {
        return netOf(Unpooled.buffer(initialCapacity));
    }

    /**
     * Creates a new {@link NetByteBuf} without any initial capacity while optionally disabling optimizations.
     *
     * @param passthrough whether to disable optimizations.
     * @return A new {@link NetByteBuf} from {@link Unpooled#buffer()}
     */
    public static NetByteBuf netBuf(boolean passthrough) {
        return netOf(Unpooled.buffer(), passthrough);
    }

    /**
     * Creates a new {@link NetByteBuf} with the given initial capacity while optionally disabling optimizations.
     *
     * @param initialCapacity the buffer's initial capacity.
     * @param passthrough     whether to disable optimizations.
     * @return A new {@link NetByteBuf} from {@link Unpooled#buffer(int)}
     */
    public static NetByteBuf netBuf(int initialCapacity, boolean passthrough) {
        return netOf(Unpooled.buffer(initialCapacity), passthrough);
    }

    /**
     * Returns the given {@link ByteBuf} as {@link NetByteBuf}. If the given instance is already a {@link NetByteBuf}
     * then the given buffer is returned.
     * <p>
     * <b>Note:</b> read/write boolean/fixed-bits being called on the underlying buffer does not update this buffer's
     * partials. Try to only read/write to a single buffer at a time.
     *
     * @param buf the buffer to be converted into a {@link NetByteBuf}.
     * @return the given buffer as a {@link NetByteBuf}.
     */
    public static NetByteBuf netOf(ByteBuf buf) {
        return netOf(buf, false);
    }

    /**
     * Returns the given {@link ByteBuf} as {@link NetByteBuf}, but with passthrough mode enabled. If the given
     * instance is already a {@link NetByteBuf} then the given buffer is returned.
     * <p>
     * <b>Note:</b> read/write boolean/fixed-bits being called on the underlying buffer does not update this buffer's
     * partials. Try to only read/write to a single buffer at a time.
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

    /**
     * Creates a new {@link RegistryNetByteBuf} without any initial capacity.
     *
     * @param registryManager the registry manager to attach to the buffer.
     * @return a new {@link RegistryNetByteBuf} from {@link Unpooled#buffer()}.
     */
    public static RegistryNetByteBuf regNetBuf(DynamicRegistryManager registryManager) {
        return regNetOf(Unpooled.buffer(), registryManager);
    }

    /**
     * Creates a new {@link RegistryNetByteBuf} without any initial capacity.
     *
     * @param initialCapacity the buffer's initial capacity.
     * @param registryManager the registry manager to attach to the buffer.
     * @return a new {@link RegistryNetByteBuf} from {@link Unpooled#buffer()}.
     */
    public static RegistryNetByteBuf regNetBuf(int initialCapacity, DynamicRegistryManager registryManager) {
        return regNetOf(Unpooled.buffer(initialCapacity), registryManager);
    }

    /**
     * Creates a new {@link RegistryNetByteBuf} without any initial capacity.
     *
     * @param passthrough     whether to disable optimizations.
     * @param registryManager the registry manager to attach to the buffer.
     * @return a new {@link RegistryNetByteBuf} from {@link Unpooled#buffer()}.
     */
    public static RegistryNetByteBuf regNetBuf(boolean passthrough, DynamicRegistryManager registryManager) {
        return regNetOf(Unpooled.buffer(), passthrough, registryManager);
    }

    /**
     * Creates a new {@link RegistryNetByteBuf} without any initial capacity.
     *
     * @param initialCapacity the buffer's initial capacity.
     * @param passthrough     whether to disable optimizations.
     * @param registryManager the registry manager to attach to the buffer.
     * @return a new {@link RegistryNetByteBuf} from {@link Unpooled#buffer()}.
     */
    public static RegistryNetByteBuf regNetBuf(int initialCapacity, boolean passthrough,
                                               DynamicRegistryManager registryManager) {
        return regNetOf(Unpooled.buffer(initialCapacity), passthrough, registryManager);
    }

    /**
     * Returns the given {@link RegistryByteBuf} as a {@link NetByteBuf}.
     * <p>
     * <b>Note:</b> read/write boolean/fixed-bits being called on the underlying buffer does not update this buffer's
     * partials. Try to only read/write to a single buffer at a time.
     *
     * @param buf the buffer to be converted into a {@link NetByteBuf}.
     * @return the given buffer as a {@link NetByteBuf}.
     */
    public static RegistryNetByteBuf regNetOf(RegistryByteBuf buf) {
        return regNetOf(buf, false);
    }

    /**
     * Returns the given {@link RegistryByteBuf} as a {@link NetByteBuf}, with passthrough optionally enabled.
     * <p>
     * <b>Note:</b> read/write boolean/fixed-bits being called on the underlying buffer does not update this buffer's
     * partials. Try to only read/write to a single buffer at a time.
     *
     * @param buf         the buffer to be converted into a {@link NetByteBuf}.
     * @param passthrough whether to disable optimizations on the resulting buffer.
     * @return the given buffer as a {@link NetByteBuf}.
     */
    public static RegistryNetByteBuf regNetOf(RegistryByteBuf buf, boolean passthrough) {
        return new RegistryNetByteBuf(buf, passthrough, buf.getRegistryManager());
    }

    /**
     * Returns the given {@link NetRegistryByteBuf} as a {@link NetByteBuf}.
     * <p>
     * <b>Note:</b> read/write boolean/fixed-bits being called on the underlying buffer does not update this buffer's
     * partials. Try to only write to a single buffer at a time.
     *
     * @param buf the buffer to be converted into a {@link NetByteBuf}.
     * @return the given buffer as a {@link NetByteBuf}.
     */
    public static RegistryNetByteBuf regNetOf(NetRegistryByteBuf buf) {
        return regNetOf(buf, false);
    }

    /**
     * Returns the given {@link NetRegistryByteBuf} as a {@link NetByteBuf}.
     * <p>
     * <b>Note:</b> read/write boolean/fixed-bits being called on the underlying buffer does not update this buffer's
     * partials. Try to only read/write to a single buffer at a time.
     *
     * @param buf         the buffer to be converted into a {@link NetByteBuf}.
     * @param passthrough whether to disable optimizations.
     * @return the given buffer as a {@link NetByteBuf}.
     */
    public static RegistryNetByteBuf regNetOf(NetRegistryByteBuf buf, boolean passthrough) {
        return new RegistryNetByteBuf(buf, passthrough, buf.getRegistryManager());
    }

    /**
     * Returns the given {@link ByteBuf} as a {@link RegistryNetByteBuf}, attaching a registry manager.
     * <p>
     * <b>Note:</b> read/write boolean/fixed-bits being called on the underlying buffer does not update this buffer's
     * partials. Try to only read/write to a single buffer at a time.
     *
     * @param buf             the buffer to be converted into a {@link RegistryNetByteBuf}.
     * @param registryManager the registry manager to attach.
     * @return the given buffer as a {@link RegistryNetByteBuf}.
     */
    public static RegistryNetByteBuf regNetOf(ByteBuf buf, DynamicRegistryManager registryManager) {
        return regNetOf(buf, false, registryManager);
    }

    /**
     * Returns the given {@link ByteBuf} as a {@link RegistryNetByteBuf}, attaching a registry manager, but with
     * passthrough optionally enabled.
     * <p>
     * <b>Note:</b> read/write boolean/fixed-bits being called on the underlying buffer does not update this buffer's
     * partials. Try to only read/write to a single buffer at a time.
     *
     * @param buf             the buffer to be converted into a {@link RegistryNetByteBuf}.
     * @param passthrough     whether to disable optimizations on the resulting buffer.
     * @param registryManager the registry manager to attach.
     * @return the given buffer as a {@link RegistryNetByteBuf}.
     */
    public static RegistryNetByteBuf regNetOf(ByteBuf buf, boolean passthrough,
                                              DynamicRegistryManager registryManager) {
        if (buf instanceof RegistryNetByteBuf registryNetBuf && registryNetBuf.passthrough == passthrough &&
            registryNetBuf.getRegistryManager() == registryManager) {
            return registryNetBuf;
        } else {
            return new RegistryNetByteBuf(buf, passthrough, registryManager);
        }
    }

    /**
     * Creates a new {@link NetRegistryByteBuf} with the given registry manager.
     *
     * @param registryManager the registry manager for the new buffer.
     * @return the new buffer.
     */
    public static NetRegistryByteBuf netRegBuf(DynamicRegistryManager registryManager) {
        return netRegOf(Unpooled.buffer(), registryManager);
    }

    /**
     * Creates a new {@link NetRegistryByteBuf} with the given registry manager.
     *
     * @param registryManager the registry manager for the new buffer.
     * @param passthrough     whether to disable optimizations.
     * @return the new buffer.
     */
    public static NetRegistryByteBuf netRegBuf(DynamicRegistryManager registryManager, boolean passthrough) {
        return netRegOf(Unpooled.buffer(), registryManager, passthrough);
    }

    /**
     * Creates a new {@link NetRegistryByteBuf} with the given initial capacity and registry manager.
     *
     * @param initialCapacity the initial capacity of the new buffer.
     * @param registryManager the registry manager for the new buffer.
     * @return the new buffer.
     */
    public static NetRegistryByteBuf netRegBuf(int initialCapacity, DynamicRegistryManager registryManager) {
        return netRegOf(Unpooled.buffer(initialCapacity), registryManager);
    }

    /**
     * Creates a new {@link NetRegistryByteBuf} with the given initial capacity and registry manager.
     *
     * @param initialCapacity the initial capacity of the new buffer.
     * @param registryManager the registry manager for the new buffer.
     * @param passthrough     whether to disable optimizations.
     * @return the new buffer.
     */
    public static NetRegistryByteBuf netRegBuf(int initialCapacity, DynamicRegistryManager registryManager,
                                               boolean passthrough) {
        return netRegOf(Unpooled.buffer(initialCapacity), registryManager, passthrough);
    }

    /**
     * Wraps a {@link RegistryByteBuf} into a {@link NetRegistryByteBuf}.
     * <p>
     * <b>Note:</b> read/write boolean/fixed-bits being called on the underlying buffer does not update this buffer's
     * partials. Try to only read/write to a single buffer at a time.
     *
     * @param buf the original buffer.
     * @return the wrapping buffer.
     */
    public static NetRegistryByteBuf netRegOf(RegistryByteBuf buf) {
        return netRegOf(buf, false);
    }

    /**
     * Wraps a {@link RegistryByteBuf} into a {@link NetRegistryByteBuf}, with passthrough optionally enabled.
     * <p>
     * <b>Note:</b> read/write boolean/fixed-bits being called on the underlying buffer does not update this buffer's
     * partials. Try to only read/write to a single buffer at a time.
     *
     * @param buf         the original buffer.
     * @param passthrough whether to disable optimizations.
     * @return the wrapping buffer.
     */
    public static NetRegistryByteBuf netRegOf(RegistryByteBuf buf, boolean passthrough) {
        return new NetRegistryByteBuf(buf, buf.getRegistryManager(), passthrough);
    }

    /**
     * Wraps a {@link RegistryNetByteBuf} into a {@link NetRegistryByteBuf}.
     * <p>
     * <b>Note:</b> read/write boolean/fixed-bits being called on the underlying buffer does not update this buffer's
     * partials. Try to only read/write to a single buffer at a time.
     *
     * @param buf the original buffer.
     * @return the wrapping buffer.
     */
    public static NetRegistryByteBuf netRegOf(RegistryNetByteBuf buf) {
        return netRegOf(buf, false);
    }

    /**
     * Wraps a {@link RegistryNetByteBuf} into a {@link NetRegistryByteBuf}.
     * <p>
     * <b>Note:</b> read/write boolean/fixed-bits being called on the underlying buffer does not update this buffer's
     * partials. Try to only read/write to a single buffer at a time.
     *
     * @param buf         the original buffer.
     * @param passthrough the
     * @return the wrapping buffer.
     */
    public static NetRegistryByteBuf netRegOf(RegistryNetByteBuf buf, boolean passthrough) {
        return new NetRegistryByteBuf(buf, buf.getRegistryManager(), passthrough);
    }

    /**
     * Wraps a {@link ByteBuf} into a {@link NetRegistryByteBuf}, attaching the given registry manager.
     * <p>
     * <b>Note:</b> read/write boolean/fixed-bits being called on the underlying buffer does not update this buffer's
     * partials. Try to only read/write to a single buffer at a time.
     *
     * @param buf             the original buffer.
     * @param registryManager the registry manager to attach.
     * @return the wrapping buffer.
     */
    public static NetRegistryByteBuf netRegOf(ByteBuf buf, DynamicRegistryManager registryManager) {
        return netRegOf(buf, registryManager, false);
    }

    /**
     * Wraps a {@link ByteBuf} into a {@link NetRegistryByteBuf}, attaching the given registry manager and with passthrough optionally enabled.
     * <p>
     * <b>Note:</b> read/write boolean/fixed-bits being called on the underlying buffer does not update this buffer's
     * partials. Try to only read/write to a single buffer at a time.
     *
     * @param buf             the original buffer.
     * @param registryManager the registry manager to attach.
     * @param passthrough     whether to disable optimizations.
     * @return the wrapping buffer.
     */
    public static NetRegistryByteBuf netRegOf(ByteBuf buf, DynamicRegistryManager registryManager,
                                              boolean passthrough) {
        if (buf instanceof NetRegistryByteBuf regBuf && regBuf.passthrough == passthrough &&
            regBuf.getRegistryManager() == registryManager) {
            return regBuf;
        } else {
            return new NetRegistryByteBuf(buf, registryManager, passthrough);
        }
    }
}
