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

/**
 * Used for finding a child in a parent given the information in a payload.
 *
 * @param <CHILD>   the child to find.
 * @param <PARENT>  the parent.
 * @param <PAYLOAD> the payload.
 */
@FunctionalInterface
public interface ChildContextDecoder<PARENT, CHILD, PAYLOAD> {
    /**
     * Finds a child in a parent based on the information in the given payload.
     *
     * @param parent  the parent to find the child in.
     * @param payload the payload describing how to find the child.
     * @param ctx     the default payload handling context.
     * @return the found child.
     * @throws PayloadHandlingException if an error occurs while finding the child.
     */
    @NotNull CHILD decode(@NotNull PARENT parent, @NotNull PAYLOAD payload, @NotNull PayloadHandlingContext ctx)
        throws PayloadHandlingException;
}
