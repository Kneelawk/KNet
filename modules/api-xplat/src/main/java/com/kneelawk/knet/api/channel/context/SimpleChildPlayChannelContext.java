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

import org.jetbrains.annotations.Nullable;

import com.kneelawk.knet.api.handling.PayloadHandlingException;
import com.kneelawk.knet.api.handling.PlayPayloadHandlingContext;
import com.kneelawk.knet.api.util.NetRegistryByteBuf;

/**
 * Like a {@link ChildPlayChannelContext}, wrapping another channel context, extracting more detailed context from it.
 * However, this does not add any extra data to the payload sent. Instead, this essentially just converts one type of
 * context into another.
 *
 * @param <PARENT> the parent context.
 * @param <CHILD>  the child context to extract.
 */
public class SimpleChildPlayChannelContext<PARENT, CHILD> implements PlayChannelContext<CHILD> {
    private final @Nullable String prefix;
    private final PlayChannelContext<PARENT> parentChannelContext;
    private final ChildContextFinder<PARENT, CHILD> childFinder;
    private final ParentContextFinder<PARENT, CHILD> parentFinder;

    /**
     * Creates a new simple child channel context that is capable of getting the child context from a parent context.
     *
     * @param prefix               the name this context prefixes to the channel id.
     * @param parentChannelContext the channel context this wraps and who supplies the parent context.
     * @param childFinder          the way to get the child when given the parent.
     * @param parentFinder         the way to get the parent when given the child.
     * @param <PARENT>             the parent context.
     * @param <CHILD>              the child context to extract.
     * @return a new simple child channel context.
     */
    public static <PARENT, CHILD> SimpleChildPlayChannelContext<PARENT, CHILD> of(@Nullable String prefix,
                                                                                  PlayChannelContext<PARENT> parentChannelContext,
                                                                                  ChildContextFinder<PARENT, CHILD> childFinder,
                                                                                  ParentContextFinder<PARENT, CHILD> parentFinder) {
        return new SimpleChildPlayChannelContext<>(prefix, parentChannelContext, childFinder, parentFinder);
    }

    /**
     * Creates a new simple child channel context that is capable of getting the child context from a parent context.
     *
     * @param parentChannelContext the channel context this wraps and who supplies the parent context.
     * @param childFinder          the way to get the child when given the parent.
     * @param parentFinder         the way to get the parent when given the child.
     * @param <PARENT>             the parent context.
     * @param <CHILD>              the child context to extract.
     * @return a new simple child channel context.
     */
    public static <PARENT, CHILD> SimpleChildPlayChannelContext<PARENT, CHILD> of(
        PlayChannelContext<PARENT> parentChannelContext,
        ChildContextFinder<PARENT, CHILD> childFinder,
        ParentContextFinder<PARENT, CHILD> parentFinder) {
        return new SimpleChildPlayChannelContext<>(null, parentChannelContext, childFinder, parentFinder);
    }

    private SimpleChildPlayChannelContext(@Nullable String prefix, PlayChannelContext<PARENT> parentChannelContext,
                                          ChildContextFinder<PARENT, CHILD> childFinder,
                                          ParentContextFinder<PARENT, CHILD> parentFinder) {
        this.prefix = prefix;
        this.parentChannelContext = parentChannelContext;
        this.childFinder = childFinder;
        this.parentFinder = parentFinder;
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
    public Object decodePayload(NetRegistryByteBuf buf) {
        return parentChannelContext.decodePayload(buf);
    }

    @Override
    public void encodePayload(Object payload, NetRegistryByteBuf buf) {
        parentChannelContext.encodePayload(payload, buf);
    }

    @Override
    public CHILD decodeContext(Object payload, PlayPayloadHandlingContext ctx)
        throws PayloadHandlingException {
        return childFinder.getChild(parentChannelContext.decodeContext(payload, ctx));
    }

    @Override
    public Object encodeContext(CHILD context) {
        return parentChannelContext.encodeContext(parentFinder.getParent(context));
    }
}
