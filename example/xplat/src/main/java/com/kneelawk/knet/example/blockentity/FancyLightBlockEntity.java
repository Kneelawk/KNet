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

package com.kneelawk.knet.example.blockentity;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import com.kneelawk.knet.api.KNet;
import com.kneelawk.knet.api.channel.context.ContextualPlayChannel;
import com.kneelawk.knet.api.handling.PayloadHandlingErrorException;
import com.kneelawk.knet.api.handling.PlayPayloadHandlingContext;
import com.kneelawk.knet.api.util.RegistryNetByteBuf;
import com.kneelawk.knet.example.KNetExample;
import com.kneelawk.knet.example.net.BlockPosPayload;
import com.kneelawk.knet.example.net.ColorUpdatePayload;
import com.kneelawk.knet.example.screen.ExtraScreenHandlerFactory;
import com.kneelawk.knet.example.screen.FancyLightScreenHandler;

import static com.kneelawk.knet.example.KNetExample.id;
import static com.kneelawk.knet.example.KNetExample.tt;

public class FancyLightBlockEntity extends BlockEntity implements ExtraScreenHandlerFactory<BlockPosPayload> {
    private static final Component CONTAINER_NAME = tt("container", "fancy_light");

    public static final ContextualPlayChannel<FancyLightBlockEntity, ColorUpdatePayload> COLOR_UPDATE_CHANNEL =
        ContextualPlayChannel.ofNetCodec(id("fancy_light_color_update"),
                KNet.BLOCK_ENTITY_CONTEXT.cast(FancyLightBlockEntity.class), ColorUpdatePayload.CODEC)
            .recvClient(FancyLightBlockEntity::recv);

    private int red = 255;
    private int green = 255;
    private int blue = 255;

    public FancyLightBlockEntity(BlockPos pos, BlockState state) {
        super(KNEBlockEntities.FANCY_LIGHT.get(), pos, state);
    }

    public int getRed() {
        return red;
    }

    public int getGreen() {
        return green;
    }

    public int getBlue() {
        return blue;
    }

    public void updateRed(int newRed) {
        red = newRed & 0xFF;
        COLOR_UPDATE_CHANNEL.sendToTracking(this, this, new ColorUpdatePayload((byte) red, (byte) 0));
        setChanged();
    }

    public void updateGreen(int newGreen) {
        green = newGreen & 0xFF;
        COLOR_UPDATE_CHANNEL.sendToTracking(this, this, new ColorUpdatePayload((byte) green, (byte) 1));
        setChanged();
    }

    public void updateBlue(int newBlue) {
        blue = newBlue & 0xFF;
        COLOR_UPDATE_CHANNEL.sendToTracking(this, this, new ColorUpdatePayload((byte) blue, (byte) 2));
        setChanged();
    }

    private void recv(ColorUpdatePayload payload, PlayPayloadHandlingContext ctx) throws PayloadHandlingErrorException {
        KNetExample.LOGGER.info("BlockEntity received payload: {}", payload);
        switch (payload.index()) {
            case 0 -> red = payload.value() & 0xFF;
            case 1 -> green = payload.value() & 0xFF;
            case 2 -> blue = payload.value() & 0xFF;
            default -> throw new PayloadHandlingErrorException("Invalid color-update index: " + payload.index());
        }
    }

    @Override
    public void loadAdditional(CompoundTag nbt, HolderLookup.Provider registryLookup) {
        red = nbt.getByte("red") & 0xFF;
        green = nbt.getByte("green") & 0xFF;
        blue = nbt.getByte("blue") & 0xFF;
    }

    @Override
    protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registryLookup) {
        nbt.putByte("red", (byte) red);
        nbt.putByte("green", (byte) green);
        nbt.putByte("blue", (byte) blue);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registryLookup) {
        return saveWithoutMetadata(registryLookup);
    }

    @Override
    public BlockPosPayload getExtra(ServerPlayer player) {
        return new BlockPosPayload(getBlockPos());
    }

    @Override
    public StreamCodec<? super RegistryNetByteBuf, BlockPosPayload> getCodec() {
        return BlockPosPayload.CODEC;
    }

    @Override
    public Component getDisplayName() {
        return CONTAINER_NAME;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player player) {
        return new FancyLightScreenHandler(syncId, ContainerLevelAccess.create(getLevel(), getBlockPos()), this);
    }
}
