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
import com.kneelawk.knet.api.handling.PayloadHandlingException;
import com.kneelawk.knet.api.util.NetByteBuf;

/**
 * Describes something capable of supplying context to a channel.
 *
 * @param <C> the type of context supplied.
 */
public interface ChannelContext<C> {
    /**
     * Decodes a payload from a buffer.
     *
     * @param buf the buffer to decode from.
     * @return the newly decoded payload.
     */
    @NotNull Object decodePayload(@NotNull NetByteBuf buf);

    /**
     * Encodes a payload to a buffer.
     *
     * @param payload the payload to encodel
     * @param buf     the buffer to write to.
     */
    void encodePayload(@NotNull Object payload, @NotNull NetByteBuf buf);

    /**
     * Finds a context using a previously decoded payload.
     *
     * @param payload the payload to use to find the context.
     * @param ctx     the payload handling context used to find the decoded context.
     * @return the decoded context.
     * @throws PayloadHandlingException if an error occurs while finding the context.
     */
    @NotNull C decodeContext(@NotNull Object payload, @NotNull PayloadHandlingContext ctx)
        throws PayloadHandlingException;

    /**
     * Encodes the information necessary to find the context into a payload.
     *
     * @param context the context that needs to be found on the other side.
     * @return the payload encapsulating all the information needed to find the context again.
     */
    @NotNull Object encodeContext(@NotNull C context);

    /**
     * Casts this channel context.
     *
     * @param castClass the context class to cast to.
     * @param <T>       the type to cast to.
     * @return a new channel context that casts to the desired class.
     */
    default <T extends C> @NotNull ChannelContext<T> cast(@NotNull Class<T> castClass) {
        return new CastChannelContext<>(this, castClass);
    }

    /**
     * Creates a child channel context.
     *
     * @param codec        the codec of the child-specific payload.
     * @param decoder      the decoder for the child context from the parent context.
     * @param encoder      the encoder to encode the child-specific information into the payload.
     * @param parentFinder the way to find the parent when given the child.
     * @param <T>          the child type.
     * @param <P>          the child-specific payload type.
     * @return a new channel context that gets the child from the parent channel context.
     */
    default <T, P> @NotNull ChannelContext<T> child(@NotNull PayloadCodec<P> codec,
                                                    @NotNull ChildContextDecoder<C, T, P> decoder,
                                                    @NotNull ContextEncoder<T, P> encoder,
                                                    @NotNull ParentContextFinder<C, T> parentFinder) {
        return new ChildChannelContext<>(this, codec, decoder, encoder, parentFinder);
    }
}
