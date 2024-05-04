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

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;

import com.mojang.serialization.MapCodec;

import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Tuple;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

import com.kneelawk.knet.api.util.NetCodecs;
import com.kneelawk.knet.api.util.RegistryNetByteBuf;
import com.kneelawk.knet.example.KNEPlatform;
import com.kneelawk.knet.example.screen.ExtraScreenHandlerDecoder;
import com.kneelawk.knet.example.screen.ExtraScreenHandlerFactory;

import static com.kneelawk.knet.example.KNetExample.id;

public class KNEPlatformImpl implements KNEPlatform {
    @Override
    public <T extends Block> Supplier<T> registerBlockWithItem(String path, Supplier<T> creator,
                                                               MapCodec<? extends Block> codec) {
        ResourceLocation id = id(path);
        T block = creator.get();
        KNetExampleFabric.BLOCKS.add(new Tuple<>(id, block));
        KNetExampleFabric.ITEMS.add(new Tuple<>(id, new BlockItem(block, new Item.Properties())));
        KNetExampleFabric.BLOCK_TYPES.add(new Tuple<>(id, codec));
        return () -> block;
    }

    @Override
    public <T extends BlockEntity> Supplier<BlockEntityType<T>> registerBlockEntity(String path,
                                                                                    Supplier<BlockEntityType<T>> creator) {
        BlockEntityType<T> type = creator.get();
        KNetExampleFabric.BLOCK_ENTITY_TYPES.add(new Tuple<>(id(path), type));
        return () -> type;
    }

    @Override
    public <T extends AbstractContainerMenu, P> Supplier<MenuType<T>> registerExtraScreenHandler(String path,
                                                                                                 ExtraScreenHandlerDecoder<T, P> factory,
                                                                                                 StreamCodec<? super RegistryNetByteBuf, P> codec) {
        MenuType<T> type =
            new ExtendedScreenHandlerType<>(factory::create, NetCodecs.regNetToVanilla(codec));
        KNetExampleFabric.SCREEN_HANDLERS.add(new Tuple<>(id(path), type));
        return () -> type;
    }

    @Override
    public void openScreen(ServerPlayer player, MenuProvider factory) {
        if (factory instanceof ExtraScreenHandlerFactory<?> extra) {
            player.openMenu(new ExtendedScreenHandlerFactory<>() {
                @Override
                public Object getScreenOpeningData(ServerPlayer player) {
                    return extra.getExtra(player);
                }

                @Override
                public Component getDisplayName() {
                    return extra.getDisplayName();
                }

                @Nullable
                @Override
                public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
                    return extra.createMenu(syncId, playerInventory, player);
                }
            });
        } else {
            player.openMenu(factory);
        }
    }
}
