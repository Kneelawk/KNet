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
import org.jetbrains.annotations.Nullable;

import com.kneelawk.knet.api.handling.PayloadHandlingErrorException;
import com.kneelawk.knet.api.handling.PayloadHandlingException;
import com.kneelawk.knet.api.handling.PlayPayloadHandlingContext;
import com.kneelawk.knet.api.util.NetRegistryByteBuf;

/**
 * Creates a channel context that casts the parent into the child.
 *
 * @param <TO>   the child to cast into.
 * @param <FROM> the parent to cast from.
 */
public class CastPlayChannelContext<FROM, TO> implements PlayChannelContext<TO> {
    private final @Nullable String prefix;
    private final PlayChannelContext<FROM> parentChannelContext;
    private final Class<TO> toClass;

    /**
     * Creates a new channel context that casts the parent into the child.
     *
     * @param prefix               the name this context prefixes to the channel id.
     * @param parentChannelContext the parent channel context.
     * @param toClass              the class of the child to cast into.
     * @param <FROM>               the parent to cast from.
     * @param <TO>                 the child to cast into.
     * @return a new cast channel context.
     */
    public static <FROM, TO> CastPlayChannelContext<FROM, TO> of(@Nullable String prefix,
                                                                 @NotNull PlayChannelContext<FROM> parentChannelContext,
                                                                 @NotNull Class<TO> toClass) {
        return new CastPlayChannelContext<>(prefix, parentChannelContext, toClass);
    }

    /**
     * Creates a new channel context that casts the parent into the child.
     *
     * @param parentChannelContext the parent channel context.
     * @param toClass              the class of the child to cast into.
     * @param <FROM>               the parent to cast from.
     * @param <TO>                 the child to cast into.
     * @return a new cast channel context.
     */
    public static <FROM, TO> CastPlayChannelContext<FROM, TO> of(@NotNull PlayChannelContext<FROM> parentChannelContext,
                                                                 @NotNull Class<TO> toClass) {
        return new CastPlayChannelContext<>(null, parentChannelContext, toClass);
    }

    private CastPlayChannelContext(@Nullable String prefix, @NotNull PlayChannelContext<FROM> parentChannelContext,
                                   @NotNull Class<TO> toClass) {
        this.prefix = prefix;
        this.parentChannelContext = parentChannelContext;
        this.toClass = toClass;
    }

    @Override
    public @Nullable String getChannelIdPrefix() {
        String parent = parentChannelContext.getChannelIdPrefix();
        if (prefix == null) {
            return parent;
        } else {
            if (parent == null) {
                return prefix;
            } else {
                return parent + "/" + prefix;
            }
        }
    }

    @Override
    public @NotNull Object decodePayload(@NotNull NetRegistryByteBuf buf) {
        return parentChannelContext.decodePayload(buf);
    }

    @Override
    public void encodePayload(@NotNull Object payload, @NotNull NetRegistryByteBuf buf) {
        parentChannelContext.encodePayload(payload, buf);
    }

    @Override
    public @NotNull TO decodeContext(@NotNull Object payload, @NotNull PlayPayloadHandlingContext ctx)
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
