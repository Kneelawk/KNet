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

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import com.kneelawk.knet.api.handling.PayloadHandlingException;
import com.kneelawk.knet.api.handling.PlayPayloadHandlingContext;
import com.kneelawk.knet.api.util.NetByteBuf;
import com.kneelawk.knet.api.util.NetRegistryByteBuf;
import com.kneelawk.knet.api.util.RegistryNetByteBuf;

/**
 * Describes something capable of supplying context to a channel.
 *
 * @param <C> the type of context supplied.
 */
public interface PlayChannelContext<C> {
    /**
     * {@return the name of this context to be prefixed onto the channel id to differentiate it from other channels}
     */
    @Nullable
    String getChannelIdPrefix();

    /**
     * Decodes a payload from a buffer.
     *
     * @param buf the buffer to decode from.
     * @return the newly decoded payload.
     */
    Object decodePayload(NetRegistryByteBuf buf);

    /**
     * Encodes a payload to a buffer.
     *
     * @param payload the payload to encodel
     * @param buf     the buffer to write to.
     */
    void encodePayload(Object payload, NetRegistryByteBuf buf);

    /**
     * Finds a context using a previously decoded payload.
     *
     * @param payload the payload to use to find the context.
     * @param ctx     the payload handling context used to find the decoded context.
     * @return the decoded context.
     * @throws PayloadHandlingException if an error occurs while finding the context.
     */
    C decodeContext(Object payload, PlayPayloadHandlingContext ctx)
        throws PayloadHandlingException;

    /**
     * Encodes the information necessary to find the context into a payload.
     *
     * @param context the context that needs to be found on the other side.
     * @return the payload encapsulating all the information needed to find the context again.
     */
    Object encodeContext(C context);

    /**
     * Casts this channel context.
     *
     * @param prefix    the name this context prefixes to the channel id.
     * @param castClass the context class to cast to.
     * @param <T>       the type to cast to.
     * @return a new channel context that casts to the desired class.
     */
    default <T> PlayChannelContext<T> cast(@Nullable String prefix, Class<T> castClass) {
        return CastPlayChannelContext.of(prefix, this, castClass);
    }

    /**
     * Casts this channel context.
     *
     * @param castClass the context class to cast to.
     * @param <T>       the type to cast to.
     * @return a new channel context that casts to the desired class.
     */
    default <T> PlayChannelContext<T> cast(Class<T> castClass) {
        return CastPlayChannelContext.of(this, castClass);
    }

    /**
     * Creates a child channel context that accepts a {@link RegistryNetByteBuf} codec or {@link NetByteBuf} codec.
     *
     * @param prefix       the name this context prefixes to the channel id.
     * @param codec        the codec of the child-specific payload.
     * @param decoder      the decoder for the child context from the parent context.
     * @param encoder      the encoder to encode the child-specific information into the payload.
     * @param parentFinder the way to find the parent when given the child.
     * @param <T>          the child type.
     * @param <P>          the child-specific payload type.
     * @return a new channel context that gets the child from the parent channel context.
     */
    default <T, P> PlayChannelContext<T> netChild(@Nullable String prefix,
                                                  StreamCodec<? super RegistryNetByteBuf, P> codec,
                                                  ChildPlayContextDecoder<C, T, P> decoder,
                                                  ContextEncoder<T, P> encoder,
                                                  ParentContextFinder<C, T> parentFinder) {
        return ChildPlayChannelContext.ofNetCodec(prefix, this, codec, decoder, encoder, parentFinder);
    }

    /**
     * Creates a child channel context that accepts a {@link NetRegistryByteBuf} codec or {@link RegistryFriendlyByteBuf} codec.
     *
     * @param prefix       the name this context prefixes to the channel id.
     * @param codec        the codec of the child-specific payload.
     * @param decoder      the decoder for the child context from the parent context.
     * @param encoder      the encoder to encode the child-specific information into the payload.
     * @param parentFinder the way to find the parent when given the child.
     * @param <T>          the child type.
     * @param <P>          the child-specific payload type.
     * @return a new channel context that gets the child from the parent channel context.
     */
    default <T, P> PlayChannelContext<T> registryChild(@Nullable String prefix,
                                                       StreamCodec<? super NetRegistryByteBuf, P> codec,
                                                       ChildPlayContextDecoder<C, T, P> decoder,
                                                       ContextEncoder<T, P> encoder,
                                                       ParentContextFinder<C, T> parentFinder) {
        return ChildPlayChannelContext.ofRegistryCodec(prefix, this, codec, decoder, encoder, parentFinder);
    }

    /**
     * Creates a child channel context that accepts a {@link RegistryNetByteBuf} codec or {@link NetByteBuf} codec.
     *
     * @param codec        the codec of the child-specific payload.
     * @param decoder      the decoder for the child context from the parent context.
     * @param encoder      the encoder to encode the child-specific information into the payload.
     * @param parentFinder the way to find the parent when given the child.
     * @param <T>          the child type.
     * @param <P>          the child-specific payload type.
     * @return a new channel context that gets the child from the parent channel context.
     */
    default <T, P> PlayChannelContext<T> netChild(
        StreamCodec<? super RegistryNetByteBuf, P> codec,
        ChildPlayContextDecoder<C, T, P> decoder,
        ContextEncoder<T, P> encoder,
        ParentContextFinder<C, T> parentFinder) {
        return ChildPlayChannelContext.ofNetCodec(this, codec, decoder, encoder, parentFinder);
    }

    /**
     * Creates a child channel context that accepts a {@link NetRegistryByteBuf} codec or {@link RegistryFriendlyByteBuf} codec.
     *
     * @param codec        the codec of the child-specific payload.
     * @param decoder      the decoder for the child context from the parent context.
     * @param encoder      the encoder to encode the child-specific information into the payload.
     * @param parentFinder the way to find the parent when given the child.
     * @param <T>          the child type.
     * @param <P>          the child-specific payload type.
     * @return a new channel context that gets the child from the parent channel context.
     */
    default <T, P> PlayChannelContext<T> registryChild(
        StreamCodec<? super NetRegistryByteBuf, P> codec,
        ChildPlayContextDecoder<C, T, P> decoder,
        ContextEncoder<T, P> encoder,
        ParentContextFinder<C, T> parentFinder) {
        return ChildPlayChannelContext.ofRegistryCodec(this, codec, decoder, encoder, parentFinder);
    }
}
