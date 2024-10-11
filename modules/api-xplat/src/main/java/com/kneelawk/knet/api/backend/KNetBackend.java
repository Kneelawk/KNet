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

package com.kneelawk.knet.api.backend;

import com.kneelawk.knet.api.KNet;

/**
 * Provides access to a backend.
 */
public interface KNetBackend {
    /**
     * {@return the backend provided by this provider}
     */
    KNet getKNet();

    /**
     * Gets this backend's name.
     * <p>
     * Backend names allow users and mod authors to select which backend they would prefer to use instead of the
     * default one.
     *
     * @return this backend's name.
     */
    String getName();

    /**
     * Gets this backend's priority.
     * <p>
     * Backends with lower integer values represent higher priorities and will be selected over backends with higher
     * integer values.
     *
     * @return this backend's priority.
     */
    default int getPriority() {
        return 0;
    }
}
