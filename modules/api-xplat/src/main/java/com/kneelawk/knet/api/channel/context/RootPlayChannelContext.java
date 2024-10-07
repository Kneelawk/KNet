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
import com.kneelawk.knet.api.util.NetBufs;
import com.kneelawk.knet.api.util.NetByteBuf;
import com.kneelawk.knet.api.util.NetRegistryByteBuf;
import com.kneelawk.knet.api.util.RegistryNetByteBuf;

/**
 * A channel context that supplies a context object but has no parents.
 *
 * @param <C> the context this supplies.
 * @param <P> the payload this uses.
 */
public class RootPlayChannelContext<C, P> implements PlayChannelContext<C> {
    private final @Nullable String prefix;
    private final StreamCodec<? super NetRegistryByteBuf, P> codec;
    private final PlayContextDecoder<C, P> decoder;
    private final ContextEncoder<C, P> encoder;

    /**
     * Creates a new root channel context that accepts a {@link RegistryNetByteBuf} codec or {@link NetByteBuf} codec.
     *
     * @param prefix  the name this context prefixes to the channel id.
     * @param codec   the payload codec.
     * @param decoder a decoder for decoding context from the payload.
     * @param encoder an encoder for encoding context into a payload.
     * @param <C>     the type of context.
     * @param <P>     the type of payload.
     * @return a new root channel context.
     */
    public static <C, P> RootPlayChannelContext<C, P> ofNetCodec(@Nullable String prefix,
                                                                 StreamCodec<? super RegistryNetByteBuf, P> codec,
                                                                 PlayContextDecoder<C, P> decoder,
                                                                 ContextEncoder<C, P> encoder) {
        return new RootPlayChannelContext<>(prefix, codec.mapStream(NetBufs::registryNetOf), decoder, encoder);
    }

    /**
     * Creates a new root channel context that accepts a {@link NetRegistryByteBuf} codec or {@link RegistryFriendlyByteBuf} codec.
     *
     * @param prefix  the name this context prefixes to the channel id.
     * @param codec   the payload codec.
     * @param decoder a decoder for decoding context from the payload.
     * @param encoder an encoder for encoding context into a payload.
     * @param <C>     the type of context.
     * @param <P>     the type of payload.
     * @return a new root channel context.
     */
    public static <C, P> RootPlayChannelContext<C, P> ofRegistryCodec(@Nullable String prefix,
                                                                      StreamCodec<? super NetRegistryByteBuf, P> codec,
                                                                      PlayContextDecoder<C, P> decoder,
                                                                      ContextEncoder<C, P> encoder) {
        return new RootPlayChannelContext<>(prefix, codec, decoder, encoder);
    }

    /**
     * Creates a new root channel context that accepts a {@link RegistryNetByteBuf} codec or {@link NetByteBuf} codec.
     *
     * @param codec   the payload codec.
     * @param decoder a decoder for decoding context from the payload.
     * @param encoder an encoder for encoding context into a payload.
     * @param <C>     the type of context.
     * @param <P>     the type of payload.
     * @return a new root channel context.
     */
    public static <C, P> RootPlayChannelContext<C, P> ofNetCodec(
        StreamCodec<? super RegistryNetByteBuf, P> codec, PlayContextDecoder<C, P> decoder,
        ContextEncoder<C, P> encoder) {
        return new RootPlayChannelContext<>(null, codec.mapStream(NetBufs::registryNetOf), decoder, encoder);
    }

    /**
     * Creates a new root channel context that accepts a {@link NetRegistryByteBuf} codec or {@link RegistryFriendlyByteBuf} codec.
     *
     * @param codec   the payload codec.
     * @param decoder a decoder for decoding context from the payload.
     * @param encoder an encoder for encoding context into a payload.
     * @param <C>     the type of context.
     * @param <P>     the type of payload.
     * @return a new root channel context.
     */
    public static <C, P> RootPlayChannelContext<C, P> ofRegistryCodec(
        StreamCodec<? super NetRegistryByteBuf, P> codec, PlayContextDecoder<C, P> decoder,
        ContextEncoder<C, P> encoder) {
        return new RootPlayChannelContext<>(null, codec, decoder, encoder);
    }

    private RootPlayChannelContext(@Nullable String prefix, StreamCodec<? super NetRegistryByteBuf, P> codec,
                                   PlayContextDecoder<C, P> decoder, ContextEncoder<C, P> encoder) {
        this.prefix = prefix;
        this.codec = codec;
        this.decoder = decoder;
        this.encoder = encoder;
    }

    @Override
    public @Nullable String getChannelIdPrefix() {
        return prefix;
    }

    @Override
    public Object decodePayload(NetRegistryByteBuf buf) {
        return codec.decode(buf);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void encodePayload(Object payload, NetRegistryByteBuf buf) {
        codec.encode(buf, (P) payload);
    }

    @SuppressWarnings("unchecked")
    @Override
    public C decodeContext(Object payload, PlayPayloadHandlingContext ctx)
        throws PayloadHandlingException {
        return decoder.decode((P) payload, ctx);
    }

    @Override
    public Object encodeContext(C context) {
        return encoder.encode(context);
    }
}
