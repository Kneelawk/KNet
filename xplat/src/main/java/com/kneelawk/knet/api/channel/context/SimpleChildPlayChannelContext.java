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

import com.kneelawk.knet.api.handling.PlayPayloadHandlingContext;
import com.kneelawk.knet.api.handling.PayloadHandlingException;
import com.kneelawk.knet.api.util.RegistryNetByteBuf;

/**
 * Like a {@link ChildPlayChannelContext}, wrapping another channel context, extracting more detailed context from it.
 * However, this does not add any extra data to the payload sent. Instead, this essentially just converts one type of
 * context into another.
 *
 * @param <PARENT> the parent context.
 * @param <CHILD>  the child context to extract.
 */
public class SimpleChildPlayChannelContext<PARENT, CHILD> implements PlayChannelContext<CHILD> {
    private final PlayChannelContext<PARENT> parentChannelContext;
    private final ChildContextFinder<PARENT, CHILD> childFinder;
    private final ParentContextFinder<PARENT, CHILD> parentFinder;

    /**
     * Creates a new simple child channel context that is capable of getting the child context from a parent context.
     *
     * @param parentChannelContext the channel context this wraps and who supplies the parent context.
     * @param childFinder          the way to get the child when given the parent.
     * @param parentFinder         the way to get the parent when given the child.
     */
    public SimpleChildPlayChannelContext(PlayChannelContext<PARENT> parentChannelContext,
                                         ChildContextFinder<PARENT, CHILD> childFinder,
                                         ParentContextFinder<PARENT, CHILD> parentFinder) {
        this.parentChannelContext = parentChannelContext;
        this.childFinder = childFinder;
        this.parentFinder = parentFinder;
    }

    @Override
    public @NotNull Object decodePayload(@NotNull RegistryNetByteBuf buf) {
        return parentChannelContext.decodePayload(buf);
    }

    @Override
    public void encodePayload(@NotNull Object payload, @NotNull RegistryNetByteBuf buf) {
        parentChannelContext.encodePayload(payload, buf);
    }

    @Override
    public @NotNull CHILD decodeContext(@NotNull Object payload, @NotNull PlayPayloadHandlingContext ctx)
        throws PayloadHandlingException {
        return childFinder.getChild(parentChannelContext.decodeContext(payload, ctx));
    }

    @Override
    public @NotNull Object encodeContext(@NotNull CHILD context) {
        return parentChannelContext.encodeContext(parentFinder.getParent(context));
    }
}
