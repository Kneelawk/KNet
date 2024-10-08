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

/**
 * KNet contextual channel package.
 * <p>
 * This channel contains the contextual channels and the channel context systems.
 * <p>
 * Channel context is a mechanism by which senders can send extra context (e.g. the block entity, menu, block, entity,
 * etc. sending the message) with the message being sent, allowing the object on the receiver that corresponds to the
 * sender object to be the object that receives the message.
 * <p>
 * There are various context implementations that can provide context for many of Minecraft's built-in types. These
 * built-in context types can be accessed from {@link com.kneelawk.knet.api.KNet}. You can make contexts for custom
 * types by creating a new instance of {@link com.kneelawk.knet.api.channel.context.RootPlayChannelContext},
 * or using {@link com.kneelawk.knet.api.channel.context.ChildPlayChannelContext} if your custom type extends from a
 * type that already has a context implementation. You can use a {@link com.kneelawk.knet.api.channel.context.CastPlayChannelContext}
 * to cast from a superclass context type to a subclass context type. Most derivative context types can be created via
 * utility method on {@link com.kneelawk.knet.api.channel.context.PlayChannelContext} itself.
 * <p>
 * An example of a contextual channel might be:
 * <pre>{@code
 * public static final ContextualPlayChannel<MyBlockEntity, MyUpdatePayload> UPDATE = ContextualPlayChannel.ofNetCodec(ResourceLocation.parse("my_mod:my_update"), KNet.BLOCK_ENTITY_CONTEXT.cast(MyBlockEntity.class), MyUpdatePayload.CODEC).recvClient(MyBlockEntity::recv);
 * }</pre>
 */
@FieldsAreNonnullByDefault
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
package com.kneelawk.knet.api.channel.context;

import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
