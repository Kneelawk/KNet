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
 * KNet utils package. This contains the optimized byte buffers and other utilities.
 * <p>
 * {@link com.kneelawk.knet.api.util.NetByteBuf}s are based on LibNetworkStack's {@code NetByteBuf}s, but with some
 * added optimizations and compatibilities. These buffers support registries in two different forms:
 * {@link com.kneelawk.knet.api.util.NetRegistryByteBuf} which extends {@link net.minecraft.network.RegistryFriendlyByteBuf},
 * and {@link com.kneelawk.knet.api.util.RegistryNetByteBuf} which extends {@link com.kneelawk.knet.api.util.NetByteBuf}.
 * These registry buffers can be converted from one type to another easily.
 * <p>
 * Another noteworthy utility is the {@link com.kneelawk.knet.api.util.Palette} which allows mods to convert repeated
 * objects, like {@link net.minecraft.resources.ResourceLocation}s, into integer values for encoding in a packet, only
 * needing to encode the whole object once when it is first mapped to an integer.
 * <p>
 * {@link com.kneelawk.knet.api.util.PlayerUtils} helps to gather players who are related to or tracking various
 * Minecraft objects.
 */
@FieldsAreNonnullByDefault
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
package com.kneelawk.knet.api.util;

import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
