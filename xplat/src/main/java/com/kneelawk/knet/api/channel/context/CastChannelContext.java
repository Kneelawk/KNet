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

package com.kneelawk.knet.api.channel.context;

import org.jetbrains.annotations.NotNull;

import com.kneelawk.knet.api.handling.PayloadHandlingContext;
import com.kneelawk.knet.api.handling.PayloadHandlingErrorException;
import com.kneelawk.knet.api.handling.PayloadHandlingException;
import com.kneelawk.knet.api.util.NetByteBuf;

/**
 * Creates a channel context that casts the parent into the child.
 *
 * @param <TO>  the child to cast into.
 * @param <FROM> the parent to cast from.
 */
public class CastChannelContext<FROM, TO> implements ChannelContext<TO> {
    private final ChannelContext<FROM> parentChannelContext;
    private final Class<TO> toClass;

    /**
     * Creates a new channel context that casts the parent into the child.
     *
     * @param parentChannelContext the parent channel context.
     * @param toClass           the class of the child to cast into.
     */
    public CastChannelContext(@NotNull ChannelContext<FROM> parentChannelContext, @NotNull Class<TO> toClass) {
        this.parentChannelContext = parentChannelContext;
        this.toClass = toClass;
    }

    @Override
    public @NotNull Object decodePayload(@NotNull NetByteBuf buf) {
        return parentChannelContext.decodePayload(buf);
    }

    @Override
    public void encodePayload(@NotNull Object payload, @NotNull NetByteBuf buf) {
        parentChannelContext.encodePayload(payload, buf);
    }

    @Override
    public @NotNull TO decodeContext(@NotNull Object payload, @NotNull PayloadHandlingContext ctx)
        throws PayloadHandlingException {
        FROM from = parentChannelContext.decodeContext(payload, ctx);
        try {
            return toClass.cast(from);
        } catch (ClassCastException e) {
            throw new PayloadHandlingErrorException(
                "Channel context cast failed. Tried to context into: " + toClass + ", but was: " + from.getClass(),
                e);
        }
    }

    @Override
    @SuppressWarnings("unchecked") // not technically safe, but whatever
    public @NotNull Object encodeContext(@NotNull TO context) {
        return parentChannelContext.encodeContext((FROM) context);
    }
}
