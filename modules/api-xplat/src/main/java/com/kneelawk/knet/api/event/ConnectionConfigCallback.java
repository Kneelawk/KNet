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

import net.minecraft.resources.ResourceLocation;

import com.kneelawk.commonevents.api.Event;
import com.kneelawk.knet.api.KNet;
import com.kneelawk.knet.api.phase.config.ConfigTask;
import com.kneelawk.knet.api.phase.config.ConnectionConfigTaskQueue;

/**
 * Callback for initiating configure tasks from the server during the 'configure' phase of player connection.
 *
 * @deprecated Use {@link KNet#registerConfigTask(ResourceLocation, ConfigTask)} to register a config task once all
 * backends are loaded instead of re-applying tasks every time a client connects.
 */
@Deprecated
@FunctionalInterface
public interface ConnectionConfigCallback {
    /**
     * Used to listen for {@link ConnectionConfigCallback}s.
     * <p>
     * This event is fired on the server whenever a player connects to the server and enters the 'configure' stage.
     */
    Event<ConnectionConfigCallback> EVENT = Event.create(ConnectionConfigCallback.class, callbacks -> queue -> {
        for (ConnectionConfigCallback callback : callbacks) {
            callback.enqueueTasks(queue);
        }
    });

    /**
     * Used to enqueue tasks to the task queue.
     *
     * @param queue the queue to add tasks to.
     */
    void enqueueTasks(ConnectionConfigTaskQueue queue);
}
