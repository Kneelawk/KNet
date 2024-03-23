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

import java.util.function.Supplier;

import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredBlock;

import com.mojang.serialization.MapCodec;

import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;

import com.kneelawk.knet.api.util.NetByteBuf;
import com.kneelawk.knet.example.KNEPlatform;
import com.kneelawk.knet.example.screen.ExtraScreenHandlerDecoder;
import com.kneelawk.knet.example.screen.ExtraScreenHandlerFactory;

public class KNEPlatformImpl implements KNEPlatform {
    @Override
    public <T extends Block> Supplier<T> registerBlockWithItem(String path, Supplier<T> creator,
                                                               MapCodec<? extends Block> codec) {
        DeferredBlock<T> block = KNetExampleNeoForge.BLOCKS.register(path, creator);
        KNetExampleNeoForge.ITEMS.register(path, () -> new BlockItem(block.get(), new Item.Settings()));
        KNetExampleNeoForge.BLOCK_TYPES.register(path, () -> codec);
        return block;
    }

    @Override
    public <T extends BlockEntity> Supplier<BlockEntityType<T>> registerBlockEntity(String path,
                                                                                    Supplier<BlockEntityType<T>> creator) {
        return KNetExampleNeoForge.BLOCK_ENTITY_TYPES.register(path, creator);
    }

    @Override
    public <T extends ScreenHandler> Supplier<ScreenHandlerType<T>> registerExtraScreenHandler(String path,
                                                                                               ExtraScreenHandlerDecoder<T> factory) {
        return KNetExampleNeoForge.SCREEN_HANDLERS.register(path, () -> IMenuTypeExtension.create(
            (syncId, playerInv, buf) -> factory.create(syncId, playerInv, NetByteBuf.asNetByteBuf(buf))));
    }

    @Override
    public void openScreen(ServerPlayerEntity player, NamedScreenHandlerFactory factory) {
        if (factory instanceof ExtraScreenHandlerFactory extra) {
            player.openMenu(extra, buf -> extra.writeExtra(player, NetByteBuf.asNetByteBuf(buf)));
        } else {
            player.openHandledScreen(factory);
        }
    }
}
