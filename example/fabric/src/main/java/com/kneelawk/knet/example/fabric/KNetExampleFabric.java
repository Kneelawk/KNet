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

package com.kneelawk.knet.example.fabric;

import java.util.List;

import net.fabricmc.api.ModInitializer;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import com.mojang.serialization.MapCodec;

import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

import com.kneelawk.knet.example.KNetExample;
import com.kneelawk.knet.example.blockentity.FancyLightBlockEntity;
import com.kneelawk.knet.fabric.api.KNetFabric;

public class KNetExampleFabric implements ModInitializer {
    public static final List<Pair<Identifier, Block>> BLOCKS = new ObjectArrayList<>();
    public static final List<Pair<Identifier, Item>> ITEMS = new ObjectArrayList<>();
    public static final List<Pair<Identifier, MapCodec<? extends Block>>> BLOCK_TYPES = new ObjectArrayList<>();
    public static final List<Pair<Identifier, BlockEntityType<?>>> BLOCK_ENTITY_TYPES = new ObjectArrayList<>();
    public static final List<Pair<Identifier, ScreenHandlerType<?>>> SCREEN_HANDLERS = new ObjectArrayList<>();

    @Override
    public void onInitialize() {
        KNetExample.init();

        register(BLOCKS, Registries.BLOCK);
        register(ITEMS, Registries.ITEM);
        register(BLOCK_TYPES, Registries.BLOCK_TYPE);
        register(BLOCK_ENTITY_TYPES, Registries.BLOCK_ENTITY_TYPE);
        register(SCREEN_HANDLERS, Registries.SCREEN_HANDLER);

        KNetFabric.registerPlay(FancyLightBlockEntity.COLOR_UPDATE_CHANNEL);
    }

    private static <T> void register(List<Pair<Identifier, T>> list, Registry<T> registry) {
        for (var pair : list) {
            Registry.register(registry, pair.getLeft(), pair.getRight());
        }
    }
}
