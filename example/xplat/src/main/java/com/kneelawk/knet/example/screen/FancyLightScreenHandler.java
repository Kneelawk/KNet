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

package com.kneelawk.knet.example.screen;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import com.kneelawk.knet.example.block.KNEBlocks;
import com.kneelawk.knet.example.blockentity.FancyLightBlockEntity;

public class FancyLightScreenHandler extends ScreenHandler {
    private final ScreenHandlerContext context;
    private final FancyLightBlockEntity entity;

    public static FancyLightScreenHandler fromNetwork(int syncId, PlayerInventory playerInv, PacketByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        World world = playerInv.player.getWorld();

        if (!(world.getBlockEntity(pos) instanceof FancyLightBlockEntity entity)) throw new IllegalArgumentException(
            "Tried to open screen at " + pos + " but there was no FancyLightBlockEntity there");

        return new FancyLightScreenHandler(syncId, ScreenHandlerContext.create(world, pos), entity);
    }

    public FancyLightScreenHandler(int syncId, ScreenHandlerContext context, FancyLightBlockEntity entity) {
        super(KNEScreenHandlers.FANCY_LIGHT.get(), syncId);
        this.context = context;
        this.entity = entity;
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int slot) {
        return null;
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return canUse(context, player, KNEBlocks.FANCY_LIGHT.get());
    }
}
