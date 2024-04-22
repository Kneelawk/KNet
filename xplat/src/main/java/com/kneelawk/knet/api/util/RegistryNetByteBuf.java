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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
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
    /**
     * Creates a new {@link RegistryNetByteBuf} without any initial capacity.
     *
     * @param registryManager the registry manager to attach to the buffer.
     * @return a new {@link RegistryNetByteBuf} from {@link Unpooled#buffer()}.
     */
    public static RegistryNetByteBuf buffer(DynamicRegistryManager registryManager) {
        return regNetOf(Unpooled.buffer(), registryManager);
    }

    /**
     * Creates a new {@link RegistryNetByteBuf} without any initial capacity.
     *
     * @param initialCapacity the buffer's initial capacity.
     * @param registryManager the registry manager to attach to the buffer.
     * @return a new {@link RegistryNetByteBuf} from {@link Unpooled#buffer()}.
     */
    public static RegistryNetByteBuf buffer(int initialCapacity, DynamicRegistryManager registryManager) {
        return regNetOf(Unpooled.buffer(initialCapacity), registryManager);
    }

    /**
     * Creates a new {@link RegistryNetByteBuf} without any initial capacity.
     *
     * @param passthrough     whether to disable optimizations.
     * @param registryManager the registry manager to attach to the buffer.
     * @return a new {@link RegistryNetByteBuf} from {@link Unpooled#buffer()}.
     */
    public static RegistryNetByteBuf buffer(boolean passthrough, DynamicRegistryManager registryManager) {
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
    public static RegistryNetByteBuf buffer(int initialCapacity, boolean passthrough,
                                            DynamicRegistryManager registryManager) {
        return regNetOf(Unpooled.buffer(initialCapacity), passthrough, registryManager);
    }

    /**
     * Returns the given {@link RegistryByteBuf} as a {@link NetByteBuf}.
     *
     * @param buf the buffer to be converted into a {@link NetByteBuf}.
     * @return the given buffer as a {@link NetByteBuf}.
     */
    public static RegistryNetByteBuf regNetOf(RegistryByteBuf buf) {
        return regNetOf(buf, false);
    }

    /**
     * Returns the given {@link RegistryByteBuf} as a {@link NetByteBuf}, with passthrough optionally enabled.
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
     *
     * @param buf the buffer to be converted into a {@link NetByteBuf}.
     * @return the given buffer as a {@link NetByteBuf}.
     */
    public static RegistryNetByteBuf regNetOf(NetRegistryByteBuf buf) {
        return regNetOf(buf, false);
    }

    /**
     * Returns the given {@link NetRegistryByteBuf} as a {@link NetByteBuf}.
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
     *
     * @param buf             the buffer to be converted into a {@link RegistryNetByteBuf}.
     * @param passthrough     whether to disable optimizations on the resulting buffer.
     * @param registryManager the registry manager to attach.
     * @return the given buffer as a {@link RegistryNetByteBuf}.
     */
    public static RegistryNetByteBuf regNetOf(ByteBuf buf, boolean passthrough, DynamicRegistryManager registryManager) {
        if (buf instanceof RegistryNetByteBuf registryNetBuf && registryNetBuf.passthrough == passthrough &&
            registryNetBuf.registryManager == registryManager) {
            return registryNetBuf;
        } else {
            return new RegistryNetByteBuf(buf, passthrough, registryManager);
        }
    }

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
}
