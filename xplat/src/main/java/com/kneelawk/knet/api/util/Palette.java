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

package com.kneelawk.knet.api.util;

import org.jetbrains.annotations.NotNull;

import it.unimi.dsi.fastutil.ints.Int2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

/**
 * Maps objects to integers, allowing for smaller packet sizes when the same objects are referenced multiple times.
 *
 * @param <T> the type of object being mapped to an integer.
 */
public class Palette<T> {
    private final Int2ObjectMap<T> palette;
    private final Object2IntMap<T> reverse;

    /**
     * Decodes a palette from a {@link NetByteBuf}.
     *
     * @param buf    the buffer to decode from.
     * @param reader the function for decoding palette'd objects from the buffer.
     * @param <T>    the type of object this palette associates.
     * @return a filled palette.
     */
    public static <T> Palette<T> decode(@NotNull NetByteBuf buf, @NotNull NetByteBuf.PacketReader<T> reader) {
        int paletteLen = buf.readVarInt();
        Int2ObjectMap<T> palette = new Int2ObjectLinkedOpenHashMap<>(paletteLen);
        Object2IntMap<T> reverse = new Object2IntOpenHashMap<>(paletteLen);
        for (int i = 0; i < paletteLen; i++) {
            int key = buf.readVarInt();
            T obj = reader.apply(buf);
            palette.put(key, obj);
            reverse.put(obj, key);
        }

        return new Palette<>(palette, reverse);
    }

    private Palette(Int2ObjectMap<T> palette, Object2IntMap<T> reverse) {
        this.palette = palette;
        this.reverse = reverse;
    }

    /**
     * Creates a new palette.
     */
    public Palette() {
        palette = new Int2ObjectLinkedOpenHashMap<>();
        reverse = new Object2IntOpenHashMap<>();
    }

    /**
     * Gets an object from this palette.
     *
     * @param key the integer key to look up.
     * @return the object associated with the given integer key.
     */
    public @NotNull T get(int key) {
        return palette.get(key);
    }

    /**
     * Gets or associates an integer key for the given object.
     * <p>
     * This functions by first checking if the object already has an associated key, and returning that if so. If the
     * object does not have an associated key, a new integer key is created for the object and placed in the association
     * maps for future use.
     * <p>
     * Note: this method is not thread-safe. Key-association should only be performed on a single thread during payload
     * creation.
     *
     * @param obj the object to get the integer key for.
     * @return the integer key for the given object.
     */
    public int keyFor(@NotNull T obj) {
        if (reverse.containsKey(obj)) {
            return reverse.getInt(obj);
        } else {
            int key = reverse.size();
            reverse.put(obj, key);
            palette.put(key, obj);
            return key;
        }
    }

    /**
     * Encodes a palette to a {@link NetByteBuf}.
     *
     * @param buf    the buffer to write to.
     * @param writer the function for encoding palette'd objects into the buffer.
     */
    public void encode(@NotNull NetByteBuf buf, @NotNull NetByteBuf.PacketWriter<T> writer) {
        buf.writeVarInt(palette.size());
        for (Int2ObjectMap.Entry<T> entry : palette.int2ObjectEntrySet()) {
            buf.writeVarInt(entry.getIntKey());
            writer.accept(buf, entry.getValue());
        }
    }
}
