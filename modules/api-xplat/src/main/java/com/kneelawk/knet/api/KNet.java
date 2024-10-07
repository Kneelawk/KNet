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

import org.jetbrains.annotations.Nullable;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import com.kneelawk.commonevents.api.Event;
import com.kneelawk.knet.api.channel.context.PlayChannelContext;
import com.kneelawk.knet.api.channel.context.RootPlayChannelContext;
import com.kneelawk.knet.api.handling.PayloadHandlingErrorException;
import com.kneelawk.knet.api.phase.config.ConfigTask;
import com.kneelawk.knet.impl.KNetLog;
import com.kneelawk.knet.impl.backend.BackendManager;
import com.kneelawk.knet.impl.payload.BlockEntityPayload;
import com.kneelawk.knet.impl.payload.EntityPayload;
import com.kneelawk.knet.impl.payload.ScreenHandlerPayload;

/**
 * KNet xplat public interface.
 */
public interface KNet {
    /**
     * Called by backends to ensure that all backends are loaded and users have registered channels with their
     * preferred backends.
     */
    static void load() {
        BackendManager.load();
    }

    /**
     * {@return the default KNet implementation}
     */
    static KNet getDefault() {
        return BackendManager.getDefault();
    }

    /**
     * {@return the default KNet implementation or null if there are no KNet implementations loaded}
     */
    static @Nullable KNet tryGetDefault() {
        return BackendManager.tryGetDefault();
    }

    /**
     * Gets the KNet implementation with the given name or throws an exception if it could not be found.
     *
     * @param name the name of the KNet implementation to get.
     * @return the requested KNet implementation.
     * @throws RuntimeException if the requested KNet implementation could not be found.
     */
    static KNet get(String name) {
        return BackendManager.get(name);
    }

    /**
     * Tries to get the KNet implementation with the given name or {@code null} if it could not be found.
     *
     * @param name the name of the KNet implementation to get.
     * @return the requested KNet implementation or {@code null} if it could not be found.
     */
    static @Nullable KNet tryGet(String name) {
        return BackendManager.tryGet(name);
    }

    /**
     * Event fired once all KNet backends have been loaded.
     * <p>
     * This event is not fired if no KNet backends could be loaded.
     */
    Event<Loaded> LOADED =
        Event.createSimple(Loaded.class, KNetLog.warn("Error in KNetLoaded event listener"));

    /**
     * Called when all KNet backends have been loaded and are available to have channels registered to them.
     */
    interface Loaded {
        /**
         * Called when all KNet backends have been loaded.
         *
         * @param ctx the context for this event, from which the current backend can be retrieved.
         */
        void onLoaded(Provider ctx);
    }

    /**
     * Context for the loaded callback.
     */
    interface Provider {
        /**
         * {@return the default KNet backend}
         */
        KNet getDefault();

        /**
         * Gets the KNet backend with the given name.
         *
         * @param name the name of the KNet backend to get.
         * @return the requested KNet backend.
         * @throws RuntimeException if a KNet backend with the given name could not be found.
         */
        KNet get(String name);

        /**
         * Tries to get the KNet backend with the given name.
         *
         * @param name the name of the backend to get.
         * @return the requested KNet backend or {@code null} if the requested backend was not found.
         */
        KNet tryGet(String name);
    }

    /**
     * Channel context used for associating a channel with a block entity
     * so that messages can be sent between client and server instances of that block entity.
     * <p>
     * In order to use this in your own channel, use:
     * <pre>{@code
     * new ContextualChannel<>(channelId, KNet.BLOCK_ENTITY_CONTEXT.cast(MyBlockEntity.class), myPayloadCodec);
     * }</pre>
     */
    PlayChannelContext<BlockEntity> BLOCK_ENTITY_CONTEXT =
        RootPlayChannelContext.ofNetCodec("knet_block_entity", BlockEntityPayload.CODEC, (payload, ctx) -> {
            Level level = ctx.mustGetLevel();
            BlockEntity be = level.getBlockEntity(payload.pos());
            if (be == null) throw new PayloadHandlingErrorException(
                "Attempted to get block entity at: " + payload.pos() + " in " + level.dimension().location() +
                    " but non exist at that position.");

            return be;
        }, context -> new BlockEntityPayload(context.getBlockPos()));

    /**
     * Channel context used for associating a channel with an entity
     * so that messages can be sent between client and server instances of that entity.
     * <p>
     * In order to use this in your own channel, use:
     * <pre>{@code
     * new ContextualChannel<>(channelId, KNet.ENTITY_CONTEXT.cast(MyEntity.class), myPayloadCodec);
     * }</pre>
     */
    PlayChannelContext<Entity> ENTITY_CONTEXT =
        RootPlayChannelContext.ofNetCodec("knet_entity", EntityPayload.CODEC, (payload, ctx) -> {
            Level level = ctx.mustGetLevel();
            Entity entity = level.getEntity(payload.entityId());
            if (entity == null) throw new PayloadHandlingErrorException(
                "Attempted to get entity with id: " + payload.entityId() + " in " + level.dimension().location() +
                    " but no entity exists with that id.");
            return entity;
        }, context -> new EntityPayload(context.getId()));

    /**
     * Channel context used for associating a channel with a screen handler
     * so that messages can be sent between client and server instances of that screen handler.
     * <p>
     * In order to use this in your own channel, use:
     * <pre>{@code
     * new ContextualChannel<>(channelId, KNet.SCREEN_HANDLER_CONTEXT.cast(MyScreenHandler.class), myPayloadCodec);
     * }</pre>
     */
    PlayChannelContext<AbstractContainerMenu> SCREEN_HANDLER_CONTEXT =
        RootPlayChannelContext.ofNetCodec("knet_container", ScreenHandlerPayload.CODEC, (payload, ctx) -> {
            Player player = ctx.mustGetPlayer();
            AbstractContainerMenu screenHandler = player.containerMenu;
            if (screenHandler == null) {
                throw new PayloadHandlingErrorException(
                    "Received screen-handler payload for player " + player.getGameProfile().getName() +
                        " but this player does not have any current screen handler.");
            }
            if (screenHandler.containerId != payload.syncId()) {
                throw new PayloadHandlingErrorException(
                    "Received screen-handler payload for player " + player.getGameProfile().getName() +
                        ", for a screen " + payload.syncId() + ", but the player's current screen handler is " +
                        screenHandler.containerId);
            }
            return screenHandler;
        }, context -> new ScreenHandlerPayload(context.containerId));

    /**
     * Gets a registrar for registering channels from this backend for the given mod and network version.
     * <p>
     * Some backends may require that this channels only be registered during a certain timeframe. Some backends may
     * have requirements for which threads channels are registered on. Using the {@link Loaded} event is generally
     * the best way to make sure that all requirements are met.
     * <p>
     * Note: only some backends check network version compatibility.
     *
     * @param modId          the mod id of the mod that the registrar will be associated with.
     * @param networkVersion the network version of the mod that the registrar will be associated with.
     * @return the requested registrar.
     */
    KNetRegistrar getRegistrar(String modId, String networkVersion);

    /**
     * Registers a config task that will be run every time a client connects.
     * <p>
     * If the task wishes to cancel early, it should return false from {@link ConfigTask#start(ConfigTask.Context)}.
     *
     * @param taskId the id of the task.
     * @param task   the task itself.
     */
    void registerConfigTask(ResourceLocation taskId, ConfigTask task);

    /**
     * {@return the sender used by all channels, associated with this backend}
     */
    KNetSender getSender();
}
