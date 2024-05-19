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

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

import com.kneelawk.knet.api.handling.PayloadHandlingException;
import com.kneelawk.knet.api.handling.PlayPayloadHandlingContext;
import com.kneelawk.knet.api.util.NetBufs;
import com.kneelawk.knet.api.util.NetByteBuf;
import com.kneelawk.knet.api.util.NetRegistryByteBuf;
import com.kneelawk.knet.api.util.RegistryNetByteBuf;

/**
 * A channel context that wraps another channel context, extracting extra information from it.
 *
 * @param <CHILD>   the child context to extract.
 * @param <PARENT>  the parent context.
 * @param <PAYLOAD> the payload used to find the child in the parent.
 */
public class ChildPlayChannelContext<PARENT, CHILD, PAYLOAD> implements PlayChannelContext<CHILD> {
    private final @Nullable String prefix;
    private final PlayChannelContext<PARENT> parentChannelContext;
    private final StreamCodec<? super NetRegistryByteBuf, PAYLOAD> codec;
    private final ChildPlayContextDecoder<PARENT, CHILD, PAYLOAD> decoder;
    private final ContextEncoder<CHILD, PAYLOAD> encoder;
    private final ParentContextFinder<PARENT, CHILD> parentFinder;

    /**
     * Creates a new child channel context that is capable of finding a child context based on a parent context.
     * This accepts a {@link RegistryNetByteBuf} codec or {@link NetByteBuf} codec.
     *
     * @param prefix               the name this context prefixes to the channel id.
     * @param parentChannelContext the channel context this wraps and who supplies the parent context.
     * @param codec                the codec for the payload holding information about the child.
     * @param decoder              the decoder for finding the child in the parent based on the information supplied by the payload.
     * @param encoder              the encoder for encoding child-specific information into a payload.
     * @param parentFinder         the way to get the parent when given the child.
     * @param <PARENT>             the parent context type.
     * @param <CHILD>              the child context type.
     * @param <PAYLOAD>            the payload type.
     * @return a new child channel context.
     */
    public static <PARENT, CHILD, PAYLOAD> ChildPlayChannelContext<PARENT, CHILD, PAYLOAD> ofNetCodec(
        @Nullable String prefix,
        @NotNull PlayChannelContext<PARENT> parentChannelContext,
        @NotNull StreamCodec<? super RegistryNetByteBuf, PAYLOAD> codec,
        @NotNull ChildPlayContextDecoder<PARENT, CHILD, PAYLOAD> decoder,
        @NotNull ContextEncoder<CHILD, PAYLOAD> encoder, @NotNull ParentContextFinder<PARENT, CHILD> parentFinder) {
        return new ChildPlayChannelContext<>(prefix, parentChannelContext, codec.mapStream(NetBufs::registryNetOf),
            decoder,
            encoder,
            parentFinder);
    }

    /**
     * Creates a new child channel context that is capable of finding a child context based on a parent context.
     * This accepts a {@link NetRegistryByteBuf} codec or {@link RegistryFriendlyByteBuf} codec.
     *
     * @param prefix               the name this context prefixes to the channel id.
     * @param parentChannelContext the channel context this wraps and who supplies the parent context.
     * @param codec                the codec for the payload holding information about the child.
     * @param decoder              the decoder for finding the child in the parent based on the information supplied by the payload.
     * @param encoder              the encoder for encoding child-specific information into a payload.
     * @param parentFinder         the way to get the parent when given the child.
     * @param <PARENT>             the parent context type.
     * @param <CHILD>              the child context type.
     * @param <PAYLOAD>            the payload type.
     * @return a new child channel context.
     */
    public static <PARENT, CHILD, PAYLOAD> ChildPlayChannelContext<PARENT, CHILD, PAYLOAD> ofRegistryCodec(
        @Nullable String prefix,
        @NotNull PlayChannelContext<PARENT> parentChannelContext,
        @NotNull StreamCodec<? super NetRegistryByteBuf, PAYLOAD> codec,
        @NotNull ChildPlayContextDecoder<PARENT, CHILD, PAYLOAD> decoder,
        @NotNull ContextEncoder<CHILD, PAYLOAD> encoder, @NotNull ParentContextFinder<PARENT, CHILD> parentFinder) {
        return new ChildPlayChannelContext<>(prefix, parentChannelContext, codec, decoder, encoder,
            parentFinder);
    }

    /**
     * Creates a new child channel context that is capable of finding a child context based on a parent context.
     * This accepts a {@link RegistryNetByteBuf} codec or {@link NetByteBuf} codec.
     *
     * @param parentChannelContext the channel context this wraps and who supplies the parent context.
     * @param codec                the codec for the payload holding information about the child.
     * @param decoder              the decoder for finding the child in the parent based on the information supplied by the payload.
     * @param encoder              the encoder for encoding child-specific information into a payload.
     * @param parentFinder         the way to get the parent when given the child.
     * @param <PARENT>             the parent context type.
     * @param <CHILD>              the child context type.
     * @param <PAYLOAD>            the payload type.
     * @return a new child channel context.
     */
    public static <PARENT, CHILD, PAYLOAD> ChildPlayChannelContext<PARENT, CHILD, PAYLOAD> ofNetCodec(
        @NotNull PlayChannelContext<PARENT> parentChannelContext,
        @NotNull StreamCodec<? super RegistryNetByteBuf, PAYLOAD> codec,
        @NotNull ChildPlayContextDecoder<PARENT, CHILD, PAYLOAD> decoder,
        @NotNull ContextEncoder<CHILD, PAYLOAD> encoder, @NotNull ParentContextFinder<PARENT, CHILD> parentFinder) {
        return new ChildPlayChannelContext<>(null, parentChannelContext, codec.mapStream(NetBufs::registryNetOf),
            decoder,
            encoder,
            parentFinder);
    }

    /**
     * Creates a new child channel context that is capable of finding a child context based on a parent context.
     * This accepts a {@link NetRegistryByteBuf} codec or {@link RegistryFriendlyByteBuf} codec.
     *
     * @param parentChannelContext the channel context this wraps and who supplies the parent context.
     * @param codec                the codec for the payload holding information about the child.
     * @param decoder              the decoder for finding the child in the parent based on the information supplied by the payload.
     * @param encoder              the encoder for encoding child-specific information into a payload.
     * @param parentFinder         the way to get the parent when given the child.
     * @param <PARENT>             the parent context type.
     * @param <CHILD>              the child context type.
     * @param <PAYLOAD>            the payload type.
     * @return a new child channel context.
     */
    public static <PARENT, CHILD, PAYLOAD> ChildPlayChannelContext<PARENT, CHILD, PAYLOAD> ofRegistryCodec(
        @NotNull PlayChannelContext<PARENT> parentChannelContext,
        @NotNull StreamCodec<? super NetRegistryByteBuf, PAYLOAD> codec,
        @NotNull ChildPlayContextDecoder<PARENT, CHILD, PAYLOAD> decoder,
        @NotNull ContextEncoder<CHILD, PAYLOAD> encoder, @NotNull ParentContextFinder<PARENT, CHILD> parentFinder) {
        return new ChildPlayChannelContext<>(null, parentChannelContext, codec, decoder, encoder,
            parentFinder);
    }

    private ChildPlayChannelContext(@Nullable String prefix, @NotNull PlayChannelContext<PARENT> parentChannelContext,
                                    @NotNull StreamCodec<? super NetRegistryByteBuf, PAYLOAD> codec,
                                    @NotNull ChildPlayContextDecoder<PARENT, CHILD, PAYLOAD> decoder,
                                    @NotNull ContextEncoder<CHILD, PAYLOAD> encoder,
                                    @NotNull ParentContextFinder<PARENT, CHILD> parentFinder) {
        this.prefix = prefix;
        this.parentChannelContext = parentChannelContext;
        this.codec = codec;
        this.decoder = decoder;
        this.encoder = encoder;
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
    public @NotNull Object decodePayload(@NotNull NetRegistryByteBuf buf) {
        Object parentPayload = parentChannelContext.decodePayload(buf);
        PAYLOAD payload = codec.decode(buf);
        return new Payload(parentPayload, payload);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void encodePayload(@NotNull Object payload, @NotNull NetRegistryByteBuf buf) {
        Payload myPayload = (Payload) payload;
        parentChannelContext.encodePayload(myPayload.parentPayload, buf);
        codec.encode(buf, myPayload.payload);
    }

    @SuppressWarnings("unchecked")
    @Override
    public @NotNull CHILD decodeContext(@NotNull Object payload, @NotNull PlayPayloadHandlingContext ctx)
        throws PayloadHandlingException {
        Payload myPayload = (Payload) payload;
        PARENT parent = parentChannelContext.decodeContext(myPayload.parentPayload, ctx);
        return decoder.decode(parent, myPayload.payload, ctx);
    }

    @Override
    public @NotNull Object encodeContext(@NotNull CHILD context) {
        PARENT parent = parentFinder.getParent(context);
        Object parentPayload = parentChannelContext.encodeContext(parent);
        PAYLOAD payload = encoder.encode(context);
        return new Payload(parentPayload, payload);
    }

    private class Payload {
        private final Object parentPayload;
        private final PAYLOAD payload;

        private Payload(Object parentPayload, PAYLOAD payload) {
            this.parentPayload = parentPayload;
            this.payload = payload;
        }

        public Object getParentPayload() {
            return parentPayload;
        }

        public PAYLOAD getPayload() {
            return payload;
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Payload payload1 = (Payload) o;

            if (!parentPayload.equals(payload1.parentPayload)) return false;
            return payload.equals(payload1.payload);
        }

        @Override
        public int hashCode() {
            int result = parentPayload.hashCode();
            result = 31 * result + payload.hashCode();
            return result;
        }

        @Override
        public String toString() {
            return "ChildChannelContext.Payload{" +
                "parentPayload=" + parentPayload +
                ", payload=" + payload +
                '}';
        }
    }
}
