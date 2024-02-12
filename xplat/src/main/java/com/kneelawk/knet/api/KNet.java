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

package com.kneelawk.knet.api;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import com.kneelawk.knet.api.channel.context.ChannelContext;
import com.kneelawk.knet.api.channel.context.PayloadCodec;
import com.kneelawk.knet.api.channel.context.RootChannelContext;
import com.kneelawk.knet.api.handling.PayloadHandlingErrorException;

/**
 * KNet xplat public interface.
 */
public class KNet {
    private KNet() {}

    /**
     * Channel context used for associating a channel with a block entity
     * so that messages can be sent between client and server instances of that block entity.
     * <p>
     * In order to use this in your own channel, use:
     * <pre>{@code
     * new ContextualChannel<>(channelId, KNet.BLOCK_ENTITY_CONTEXT.cast(MyBlockEntity.class), myPayloadCodec);
     * }</pre>
     */
    public static final ChannelContext<BlockEntity> BLOCK_ENTITY_CONTEXT =
        new RootChannelContext<>(BlockEntityPayload.CODEC, (payload, ctx) -> {
            World world = ctx.mustGetWorld();
            BlockEntity be = world.getBlockEntity(payload.pos());
            if (be == null) throw new PayloadHandlingErrorException(
                "Attempted to get block entity at: " + payload.pos() + " in " + world.getRegistryKey().getValue() +
                    " but non exist at that position.");

            return be;
        }, context -> new BlockEntityPayload(context.getPos()));

    private record BlockEntityPayload(BlockPos pos) {
        public static final PayloadCodec<BlockEntityPayload> CODEC =
            new PayloadCodec<>((buf, obj) -> buf.writeBlockPos(obj.pos()),
                buf -> new BlockEntityPayload(buf.readBlockPos()));
    }

    /**
     * Channel context used for associating a channel with an entity
     * so that messages can be sent between client and server instances of that entity.
     * <p>
     * In order to use this in your own channel, use:
     * <pre>{@code
     * new ContextualChannel<>(channelId, KNet.ENTITY_CONTEXT.cast(MyEntity.class), myPayloadCodec);
     * }</pre>
     */
    public static final ChannelContext<Entity> ENTITY_CONTEXT =
        new RootChannelContext<>(EntityPayload.CODEC, (payload, ctx) -> {
            World world = ctx.mustGetWorld();
            Entity entity = world.getEntityById(payload.entityId());
            if (entity == null) throw new PayloadHandlingErrorException(
                "Attempted to get entity with id: " + payload.entityId() + " in " + world.getRegistryKey().getValue() +
                    " but no entity exists with that id.");
            return entity;
        }, context -> new EntityPayload(context.getId()));

    private record EntityPayload(int entityId) {
        public static final PayloadCodec<EntityPayload> CODEC =
            new PayloadCodec<>((buf, obj) -> buf.writeInt(obj.entityId()), buf -> new EntityPayload(buf.readInt()));
    }

    /**
     * Channel context used for associating a channel with a screen handler
     * so that messages can be sent between client and server instances of that screen handler.
     * <p>
     * In order to use this in your own channel, use:
     * <pre>{@code
     * new ContextualChannel<>(channelId, KNet.SCREEN_HANDLER_CONTEXT.cast(MyScreenHandler.class), myPayloadCodec);
     * }</pre>
     */
    public static final ChannelContext<ScreenHandler> SCREEN_HANDLER_CONTEXT =
        new RootChannelContext<>(ScreenHandlerPayload.CODEC, (payload, ctx) -> {
            PlayerEntity player = ctx.mustGetPlayer();
            ScreenHandler screenHandler = player.currentScreenHandler;
            if (screenHandler == null) {
                throw new PayloadHandlingErrorException(
                    "Received screen-handler payload for player " + player.getGameProfile().getName() +
                        " but this player does not have any current screen handler.");
            }
            if (screenHandler.syncId != payload.syncId()) {
                throw new PayloadHandlingErrorException(
                    "Received screen-handler payload for player " + player.getGameProfile().getName() +
                        ", for a screen " + payload.syncId() + ", but the player's current screen handler is " +
                        screenHandler.syncId);
            }
            return screenHandler;
        }, context -> new ScreenHandlerPayload(context.syncId));

    private record ScreenHandlerPayload(int syncId) {
        public static final PayloadCodec<ScreenHandlerPayload> CODEC =
            new PayloadCodec<>((buf, obj) -> buf.writeInt(obj.syncId()),
                buf -> new ScreenHandlerPayload(buf.readInt()));
    }
}
