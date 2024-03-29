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

package com.kneelawk.knet.example;

import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import com.kneelawk.knet.api.KNetRegistrar;
import com.kneelawk.knet.example.block.KNEBlocks;
import com.kneelawk.knet.example.blockentity.FancyLightBlockEntity;
import com.kneelawk.knet.example.blockentity.KNEBlockEntities;
import com.kneelawk.knet.example.screen.FancyLightScreenHandler;
import com.kneelawk.knet.example.screen.KNEScreenHandlers;

public class KNetExample {
    public static final String MOD_ID = "knet_example";

    public static void init() {
        KNEBlocks.init();
        KNEBlockEntities.init();
        KNEScreenHandlers.init();
    }

    public static void registerChannels(KNetRegistrar registrar) {
        registrar.register(FancyLightBlockEntity.COLOR_UPDATE_CHANNEL);
        registrar.register(FancyLightScreenHandler.COLOR_UPDATE_CHANNEL);
    }

    public static Identifier id(String path) {
        return new Identifier(MOD_ID, path);
    }

    public static MutableText tt(String prefix, String suffix, Object... args) {
        return Text.translatable(prefix + "." + MOD_ID + "." + suffix, args);
    }
}
