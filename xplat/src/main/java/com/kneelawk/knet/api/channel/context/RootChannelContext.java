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

import net.minecraft.network.PacketByteBuf;

import com.kneelawk.knet.api.handling.PayloadHandlingContext;
import com.kneelawk.knet.api.handling.PayloadHandlingException;

/**
 * A channel context that supplies a context object but has no parents.
 *
 * @param <C> the context this supplies.
 * @param <P> the payload this uses.
 */
public class RootChannelContext<C, P> implements ChannelContext<C> {
    private final PayloadCodec<P> codec;
    private final ContextDecoder<C, P> decoder;
    private final ContextEncoder<C, P> encoder;

    /**
     * Creates a new root channel context.
     *
     * @param codec   the payload codec.
     * @param decoder a decoder for decoding context from the payload.
     * @param encoder an encoder for encoding context into a payload.
     */
    public RootChannelContext(@NotNull PayloadCodec<P> codec, @NotNull ContextDecoder<C, P> decoder,
                              @NotNull ContextEncoder<C, P> encoder) {
        this.codec = codec;
        this.decoder = decoder;
        this.encoder = encoder;
    }

    @Override
    public @NotNull Object decodePayload(@NotNull PacketByteBuf buf) {
        return codec.decoder().apply(buf);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void encodePayload(@NotNull Object payload, @NotNull PacketByteBuf buf) {
        codec.encoder().accept(buf, (P) payload);
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull C decodeContext(@NotNull Object payload, @NotNull PayloadHandlingContext ctx)
        throws PayloadHandlingException {
        return decoder.decode((P) payload, ctx);
    }

    @Override
    public @NotNull Object encodeContext(@NotNull C context) {
        return encoder.encode(context);
    }
}
