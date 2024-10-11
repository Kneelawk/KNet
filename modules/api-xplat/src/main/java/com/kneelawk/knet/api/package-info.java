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
 * KNet API root package, providing access to KNet's initialization and registration APIs.
 * <p>
 * Use common-events to listen for {@link com.kneelawk.knet.api.KNet.Loaded}, then register your channels with
 * {@link com.kneelawk.knet.api.KNet#getRegistrar(String)}:
 * <pre>{@code
 * @Scan
 * public class MyNetEventListeners {
 *     public static final String NETWORK_VERSION = "1";
 *
 *     @Listen(KNet.Loaded.class)
 *     public static void onLoad(KNet.Provider ctx) {
 *         KNet knet = ctx.getDefault();
 *         KNetRegistrar registrar = knet.getRegistrar(NETWORK_VERSION);
 *         registrar.register(MY_CHANNEL);
 *     }
 * }
 * }</pre>
 * Making sure to add the {@code common-events.json} to your resources:
 * <pre>{@code
 * {
 *     "scan": true
 * }
 * }</pre>
 *
 * @see com.kneelawk.knet.api.channel
 * @see com.kneelawk.commonevents.api.Event
 */
@FieldsAreNonnullByDefault
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
package com.kneelawk.knet.api;

import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
