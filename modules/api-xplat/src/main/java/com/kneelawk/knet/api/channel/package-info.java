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
 * KNet channel package, holding the main body of the KNet API.
 * <p>
 * This package contains the no-context channels. See {@link com.kneelawk.knet.api.channel.context} for the contextual
 * channels.
 * <p>
 * Channels are designed to be static fields in the classes where they are used.
 * <p>
 * You can create a simple no-context play channel like so:
 * <pre>{@code
 * public static final NoContextPlayChannel<MyPayload> MY_CHANNEL = NoContextPlayChannel.of(MyPayload.ID, MyPayload.CODEC).recvClient(ThisClass::myPayloadClientHandler).recvServer(ThisClass:myPayloadServerHandler);
 * }</pre>
 * <p>
 * Calling {@code recvClient} or {@code recvServer} is optional and depends on which side you want to receive payloads
 * through the channel on.
 * <p>
 * {@link com.kneelawk.knet.api.channel.NoContextPlayChannel} has several send methods you can use to send to specific
 * players depending on where they are and what entities or chunks they are tracking.
 *
 * @see com.kneelawk.knet.api.channel.context
 */
@FieldsAreNonnullByDefault
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
package com.kneelawk.knet.api.channel;

import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
