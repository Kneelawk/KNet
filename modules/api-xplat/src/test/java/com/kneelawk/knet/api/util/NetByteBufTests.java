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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NetByteBufTests {
    @Test
    void writeBoolean() {
        NetByteBuf buf = NetBufs.netBuf();
        buf.writeBoolean(true);
        buf.writeBoolean(false);
        buf.writeBoolean(true);
        assertEquals((byte) 0b101, buf.readByte(), "writeBoolean(true...false...true) should produce 0b101");
    }

    @Test
    void readBoolean() {
        NetByteBuf buf = NetBufs.netBuf();
        buf.writeByte(0b101);
        assertEquals(true, buf.readBoolean());
        assertEquals(false, buf.readBoolean());
        assertEquals(true, buf.readBoolean());
    }

    @Test
    void writeFixedBits() {
        NetByteBuf buf = NetBufs.netBuf();
        buf.writeFixedBits(0b10101010, 5);
        assertEquals((byte) 0b01010, buf.getByte(0));

        buf.writeFixedBits(0b11001100, 5);
        assertEquals((byte) 0b01101010, buf.getByte(0));
        assertEquals((byte) 0b00, buf.getByte(1));

        buf.writeFixedBits(0b11011011011011011, 17);
        assertEquals((byte) 0b11011000, buf.getByte(1));
        assertEquals((byte) 0b11011011, buf.getByte(2));
        assertEquals((byte) 0b011, buf.getByte(3));
    }

    @Test
    void readFixedBits() {
        NetByteBuf buf = NetBufs.netBuf();
        buf.writeByte(0b11001100);
        buf.writeByte(0b10101010);
        buf.writeByte(0b11100111);
        buf.writeByte(0b01100110);
        assertEquals(0b01100, buf.readFixedBits(5));
        assertEquals(0b11010, buf.readFixedBits(5));
        assertEquals(0b10101011100111110, buf.readFixedBits(17));
    }

    @Test
    void writeVarInt() {
        NetByteBuf buf = NetBufs.netBuf();
        buf.writeVarInt(0b0001001000110100);
        assertEquals((byte) 0b10110100, buf.getByte(0));
        assertEquals((byte) 0b00100100, buf.getByte(1));
    }

    @Test
    void readVarInt() {
        NetByteBuf buf = NetBufs.netBuf();
        buf.writeByte(0b11010101);
        buf.writeByte(0b10001011);
        buf.writeByte(0b01110010);
        assertEquals(0b11111111111100110111101000101010, buf.readVarInt());
    }
}
