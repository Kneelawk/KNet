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
import com.kneelawk.knet.api.KNetRegistrar;

/**
 * Used for when a backend is ready to register channels.
 */
public interface ChannelRegistrationCallback {
    /**
     * Called to register channels.
     *
     * @param ctx the channel registration context, from which the registrar can be obtained.
     */
    void registerChannels(Context ctx);

    /**
     * The context provided to channel registration.
     */
    interface Context {
        /**
         * {@return the KNet impl associated with this registration event}
         */
        KNet getKNet();

        /**
         * Gets a registrar for the given mod and network version.
         * <p>
         * Note: network version checks are only performed on some platforms. If network compatibility checks are
         * important to the mod's function, then a {@link com.kneelawk.knet.api.phase.config.ConnectionConfigTask}
         * should be used.
         *
         * @param modId      the mod id of the mod requesting a registrar.
         * @param netVersion the networking version string used to check that the version of the mod on client and
         *                   server are compatible.
         * @return the registrar for the current mod and net version.
         */
        KNetRegistrar getRegistrar(String modId, String netVersion);
    }
}
