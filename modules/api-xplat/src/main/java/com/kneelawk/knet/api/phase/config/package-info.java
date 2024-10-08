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
 * KNet config-phase package. This allows mods to send and use configuration-phase packets.
 * <p>
 * Typically, a {@link com.kneelawk.knet.api.phase.config.ConfigTask} is registered with
 * {@link com.kneelawk.knet.api.KNet#registerConfigTask(net.minecraft.resources.ResourceLocation, com.kneelawk.knet.api.phase.config.ConfigTask)}.
 * Then, {@link com.kneelawk.knet.api.phase.config.ConfigTask#start(com.kneelawk.knet.api.phase.config.ConfigTask.Context)}
 * will be called on each task registered with a backend whenever that backend is ready to start configuration tasks.
 */
@FieldsAreNonnullByDefault
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
package com.kneelawk.knet.api.phase.config;

import javax.annotation.ParametersAreNonnullByDefault;

import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
