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

package com.kneelawk.knet.api.event;

import com.kneelawk.knet.api.KNet;

/**
 * Called when all KNet backends have been loaded and are available to have callbacks registered to them.
 */
public interface KNetLoadedCallback {
    /**
     * Called when all KNet backends have been loaded.
     *
     * @param ctx the context for this event, from which the current backend can be retrieved.
     */
    void onLoaded(Context ctx);

    /**
     * Context for the loaded callback.
     */
    interface Context {
        /**
         * {@return the default KNet backend}
         */
        KNet getDefault();

        /**
         * Gets the KNet backend with the given name.
         *
         * @param name the name of the KNet backend to get.
         * @return the requested KNet backend.
         * @throws RuntimeException if a KNet backend with the given name could not be found.
         */
        KNet get(String name);

        /**
         * Tries to get the KNet backend with the given name.
         *
         * @param name the name of the backend to get.
         * @return the requested KNet backend or {@code null} if the requested backend was not found.
         */
        KNet tryGet(String name);
    }
}
