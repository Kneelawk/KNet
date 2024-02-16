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

import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;

import com.mojang.serialization.MapCodec;

import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;

import com.kneelawk.knet.example.KNEPlatform;
import com.kneelawk.knet.example.screen.ExtraScreenHandlerDecoder;
import com.kneelawk.knet.example.screen.ExtraScreenHandlerFactory;

import static com.kneelawk.knet.example.KNetExample.id;

public class KNEPlatformImpl implements KNEPlatform {
    @Override
    public <T extends Block> Supplier<T> registerBlockWithItem(String path, Supplier<T> creator,
                                                               MapCodec<? extends Block> codec) {
        Identifier id = id(path);
        T block = creator.get();
        KNetExampleFabric.BLOCKS.add(new Pair<>(id, block));
        KNetExampleFabric.ITEMS.add(new Pair<>(id, new BlockItem(block, new FabricItemSettings())));
        KNetExampleFabric.BLOCK_TYPES.add(new Pair<>(id, codec));
        return () -> block;
    }

    @Override
    public <T extends BlockEntity> Supplier<BlockEntityType<T>> registerBlockEntity(String path,
                                                                                    Supplier<BlockEntityType<T>> creator) {
        BlockEntityType<T> type = creator.get();
        KNetExampleFabric.BLOCK_ENTITY_TYPES.add(new Pair<>(id(path), type));
        return () -> type;
    }

    @Override
    public <T extends ScreenHandler> Supplier<ScreenHandlerType<T>> registerExtraScreenHandler(String path,
                                                                                               ExtraScreenHandlerDecoder<T> factory) {
        ScreenHandlerType<T> type = new ExtendedScreenHandlerType<>(factory::create);
        KNetExampleFabric.SCREEN_HANDLERS.add(new Pair<>(id(path), type));
        return () -> type;
    }

    @Override
    public void openScreen(ServerPlayerEntity player, NamedScreenHandlerFactory factory) {
        if (factory instanceof ExtraScreenHandlerFactory extra) {
            player.openHandledScreen(new ExtendedScreenHandlerFactory() {
                @Override
                public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
                    extra.writeExtra(player, buf);
                }

                @Override
                public Text getDisplayName() {
                    return extra.getDisplayName();
                }

                @Nullable
                @Override
                public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
                    return extra.createMenu(syncId, playerInventory, player);
                }
            });
        } else {
            player.openHandledScreen(factory);
        }
    }
}
