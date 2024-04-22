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

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import com.kneelawk.knet.api.KNet;
import com.kneelawk.knet.api.channel.context.ContextualPlayChannel;
import com.kneelawk.knet.api.handling.PlayPayloadHandlingContext;
import com.kneelawk.knet.api.handling.PayloadHandlingErrorException;
import com.kneelawk.knet.api.util.RegistryNetByteBuf;
import com.kneelawk.knet.example.KNetExample;
import com.kneelawk.knet.example.net.BlockPosPayload;
import com.kneelawk.knet.example.net.ColorUpdatePayload;
import com.kneelawk.knet.example.screen.ExtraScreenHandlerFactory;
import com.kneelawk.knet.example.screen.FancyLightScreenHandler;

import static com.kneelawk.knet.example.KNetExample.id;
import static com.kneelawk.knet.example.KNetExample.tt;

public class FancyLightBlockEntity extends BlockEntity implements ExtraScreenHandlerFactory<BlockPosPayload> {
    private static final Text CONTAINER_NAME = tt("container", "fancy_light");

    public static final ContextualPlayChannel<FancyLightBlockEntity, ColorUpdatePayload> COLOR_UPDATE_CHANNEL =
        new ContextualPlayChannel<>(id("fancy_light_color_update"),
            KNet.BLOCK_ENTITY_CONTEXT.cast(FancyLightBlockEntity.class), ColorUpdatePayload.CODEC).recvClient(
            FancyLightBlockEntity::recv);

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
        markDirty();
    }

    public void updateGreen(int newGreen) {
        green = newGreen & 0xFF;
        COLOR_UPDATE_CHANNEL.sendToTracking(this, this, new ColorUpdatePayload((byte) green, (byte) 1));
        markDirty();
    }

    public void updateBlue(int newBlue) {
        blue = newBlue & 0xFF;
        COLOR_UPDATE_CHANNEL.sendToTracking(this, this, new ColorUpdatePayload((byte) blue, (byte) 2));
        markDirty();
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
    public void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        red = nbt.getByte("red") & 0xFF;
        green = nbt.getByte("green") & 0xFF;
        blue = nbt.getByte("blue") & 0xFF;
    }

    @Override
    protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        nbt.putByte("red", (byte) red);
        nbt.putByte("green", (byte) green);
        nbt.putByte("blue", (byte) blue);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
        return createNbt(registryLookup);
    }

    @Override
    public BlockPosPayload getExtra(ServerPlayerEntity player) {
        return new BlockPosPayload(getPos());
    }

    @Override
    public PacketCodec<? super RegistryNetByteBuf, BlockPosPayload> getCodec() {
        return BlockPosPayload.CODEC;
    }

    @Override
    public Text getDisplayName() {
        return CONTAINER_NAME;
    }

    @Nullable
    @Override
    public ScreenHandler createMenu(int syncId, PlayerInventory playerInventory, PlayerEntity player) {
        return new FancyLightScreenHandler(syncId, ScreenHandlerContext.create(getWorld(), getPos()), this);
    }
}
