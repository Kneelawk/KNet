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

import java.util.ServiceLoader;
import java.util.function.Supplier;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import com.mojang.serialization.MapCodec;
import com.kneelawk.knet.api.util.NetByteBuf;
import com.kneelawk.knet.api.util.RegistryNetByteBuf;
import com.kneelawk.knet.example.screen.ExtraScreenHandlerDecoder;

public interface KNEPlatform {
    KNEPlatform INSTANCE = ServiceLoader.load(KNEPlatform.class).findFirst()
        .orElseThrow(() -> new RuntimeException("Unable to find KNetExample platform"));

    <T extends Block> Supplier<T> registerBlockWithItem(String path, Supplier<T> creator,
                                                        MapCodec<? extends Block> codec);

    <T extends BlockEntity> Supplier<BlockEntityType<T>> registerBlockEntity(String path,
                                                                             Supplier<BlockEntityType<T>> creator);

    <T extends AbstractContainerMenu, P> Supplier<MenuType<T>> registerExtraScreenHandler(String path, ExtraScreenHandlerDecoder<T, P> factory, StreamCodec<? super RegistryNetByteBuf, P> codec);

    void openScreen(ServerPlayer player, MenuProvider factory);
}
