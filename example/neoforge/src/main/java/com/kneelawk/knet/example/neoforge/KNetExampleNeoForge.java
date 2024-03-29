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

package com.kneelawk.knet.example.neoforge;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlerEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import com.mojang.serialization.MapCodec;

import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.screen.ScreenHandlerType;

import com.kneelawk.knet.example.KNetExample;
import com.kneelawk.knet.neoforge.api.KNetRegistrarNeoForge;

@Mod(KNetExample.MOD_ID)
public class KNetExampleNeoForge {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(KNetExample.MOD_ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(KNetExample.MOD_ID);
    public static final DeferredRegister<MapCodec<? extends Block>> BLOCK_TYPES =
        DeferredRegister.create(RegistryKeys.BLOCK_TYPE, KNetExample.MOD_ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
        DeferredRegister.create(RegistryKeys.BLOCK_ENTITY_TYPE, KNetExample.MOD_ID);
    public static final DeferredRegister<ScreenHandlerType<?>> SCREEN_HANDLERS =
        DeferredRegister.create(RegistryKeys.SCREEN_HANDLER, KNetExample.MOD_ID);

    public KNetExampleNeoForge(IEventBus modBus) {
        KNetExample.init();

        BLOCKS.register(modBus);
        ITEMS.register(modBus);
        BLOCK_TYPES.register(modBus);
        BLOCK_ENTITY_TYPES.register(modBus);
        SCREEN_HANDLERS.register(modBus);

        modBus.addListener(this::onRegisterPayloadHandler);
    }

    private void onRegisterPayloadHandler(RegisterPayloadHandlerEvent event) {
        KNetExample.registerChannels(new KNetRegistrarNeoForge(event.registrar(KNetExample.MOD_ID)));
    }
}
