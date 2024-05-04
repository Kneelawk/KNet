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
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredBlock;

import com.mojang.serialization.MapCodec;
import com.kneelawk.knet.api.util.NetCodecs;
import com.kneelawk.knet.api.util.RegistryNetByteBuf;
import com.kneelawk.knet.example.KNEPlatform;
import com.kneelawk.knet.example.screen.ExtraScreenHandlerDecoder;
import com.kneelawk.knet.example.screen.ExtraScreenHandlerFactory;

public class KNEPlatformImpl implements KNEPlatform {
    @Override
    public <T extends Block> Supplier<T> registerBlockWithItem(String path, Supplier<T> creator,
                                                               MapCodec<? extends Block> codec) {
        DeferredBlock<T> block = KNetExampleNeoForge.BLOCKS.register(path, creator);
        KNetExampleNeoForge.ITEMS.register(path, () -> new BlockItem(block.get(), new Item.Properties()));
        KNetExampleNeoForge.BLOCK_TYPES.register(path, () -> codec);
        return block;
    }

    @Override
    public <T extends BlockEntity> Supplier<BlockEntityType<T>> registerBlockEntity(String path,
                                                                                    Supplier<BlockEntityType<T>> creator) {
        return KNetExampleNeoForge.BLOCK_ENTITY_TYPES.register(path, creator);
    }

    @Override
    public <T extends AbstractContainerMenu, P> Supplier<MenuType<T>> registerExtraScreenHandler(String path,
                                                                                                  ExtraScreenHandlerDecoder<T, P> factory,
                                                                                                  StreamCodec<? super RegistryNetByteBuf, P> codec) {
        return KNetExampleNeoForge.SCREEN_HANDLERS.register(path, () -> IMenuTypeExtension.create(
            (syncId, playerInv, buf) -> factory.create(syncId, playerInv,
                NetCodecs.regNetToVanilla(codec).decode(buf))));
    }

    @Override
    @SuppressWarnings("unchecked")
    public void openScreen(ServerPlayer player, MenuProvider factory) {
        if (factory instanceof ExtraScreenHandlerFactory<?> extra) {
            player.openMenu(extra,
                buf -> ((StreamCodec<RegistryFriendlyByteBuf, Object>) NetCodecs.regNetToVanilla(extra.getCodec())).encode(
                    buf, extra.getExtra(player)));
        } else {
            player.openMenu(factory);
        }
    }
}
