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

package com.kneelawk.knet.api.handling;

/**
 * Exception thrown when a payload cannot be handled.
 */
public abstract class PayloadHandlingException extends Exception {
    /**
     * Creates an empty payload handling exception.
     */
    public PayloadHandlingException() {
    }

    /**
     * Creates a payload handling exception with a message.
     *
     * @param message the message.
     */
    public PayloadHandlingException(String message) {
        super(message);
    }

    /**
     * Creates a payload handling exception with a message and a cause.
     *
     * @param message the message.
     * @param cause   the cause.
     */
    public PayloadHandlingException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a payload handling exception with a cause.
     *
     * @param cause the cause.
     */
    public PayloadHandlingException(Throwable cause) {
        super(cause);
    }
}
