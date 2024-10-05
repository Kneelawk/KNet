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

import java.lang.reflect.InvocationTargetException;
import java.util.function.BooleanSupplier;

import com.kneelawk.commonevents.api.Event;
import com.kneelawk.knet.impl.KNetLog;
import com.kneelawk.knet.impl.platform.Platform;

/**
 * Callback for registering backends.
 */
public interface BackendRegistrationCallback {

    /**
     * Event for this callback.
     * <p>
     * You can listen for this callback via registering directly or via Common Events' mod-scanning.
     */
    Event<BackendRegistrationCallback> EVENT = Event.create(BackendRegistrationCallback.class, callbacks -> ctx -> {
        for (BackendRegistrationCallback callback : callbacks) {
            try {
                callback.registerBackends(ctx);
            } catch (Exception e) {
                KNetLog.LOG.error("Error invoking backend registration callback", e);
            }
        }
    });

    /**
     * Called on a registration listener to allow it to register one or more backends.
     *
     * @param ctx the context to register the listeners to.
     */
    void registerBackends(Context ctx);

    /**
     * Context supplied to backend registration listeners.
     */
    interface Context {
        /**
         * Registers the given backend instance.
         *
         * @param backend the backend instance to register.
         */
        void registerBackend(KNetBackend backend);

        /**
         * Registers the given backend by its class name.
         * <p>
         * The specified class must have a public no-arguments constructor.
         *
         * @param className the name of the backend implementation class. this must implement {@link KNetBackend}.
         */
        default void registerBackend(String className) {
            KNetBackend backend;
            try {
                backend = (KNetBackend) Class.forName(className).getConstructor().newInstance();
            } catch (ClassNotFoundException | InvocationTargetException | InstantiationException |
                     IllegalAccessException | NoSuchMethodException e) {
                throw new RuntimeException("Unable to load KNet backend: " + className, e);
            }
            registerBackend(backend);
        }

        /**
         * Registers the given backend by its class name, checking a predicate first.
         * <p>
         * The specified classes must have public no-arguments constructors.
         *
         * @param predicateClassName the name of the backend predicate class. This must implement {@link BooleanSupplier}.
         * @param backendClassName   the name of the backend implementation class. This must implement {@link KNetBackend}.
         */
        default void registerBackend(String predicateClassName, String backendClassName) {
            BooleanSupplier predicate;
            try {
                predicate = (BooleanSupplier) Class.forName(predicateClassName).getConstructor().newInstance();
            } catch (ClassNotFoundException | InvocationTargetException | InstantiationException |
                     IllegalAccessException | NoSuchMethodException e) {
                throw new RuntimeException("Unable to load KNet backend predicate: " + predicateClassName, e);
            }

            try {
                if (!predicate.getAsBoolean()) return;
            } catch (NoClassDefFoundError ignored) {
                // this exception would generally be thrown by a failed predicate
                return;
            }

            KNetBackend backend;
            try {
                backend = (KNetBackend) Class.forName(backendClassName).getConstructor().newInstance();
            } catch (ClassNotFoundException | InvocationTargetException | InstantiationException |
                     IllegalAccessException | NoSuchMethodException e) {
                throw new RuntimeException("Unable to load KNet backend: " + backendClassName, e);
            }
            registerBackend(backend);
        }

        /**
         * Convenience method to check if the given mod exists on the current platform.
         *
         * @param modId the mod-id of the mod to check for.
         * @return whether the given mod is present in this game instance.
         */
        default boolean isModLoaded(String modId) {
            return Platform.INSTANCE.isModLoaded(modId);
        }
    }
}
