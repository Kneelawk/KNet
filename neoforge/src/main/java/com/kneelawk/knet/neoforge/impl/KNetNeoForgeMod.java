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

package com.kneelawk.knet.neoforge.impl;

import java.util.function.Consumer;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.network.configuration.ICustomConfigurationTask;
import net.neoforged.neoforge.network.event.RegisterConfigurationTasksEvent;

import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

import com.kneelawk.knet.impl.KNetImpl;
import com.kneelawk.knet.impl.KNetLog;

@Mod(KNetImpl.MOD_ID)
public class KNetNeoForgeMod {
    public KNetNeoForgeMod(IEventBus modBus) {
        KNetLog.LOG.info("Initializing KNet {}",
            FMLLoader.getLoadingModList().getModFileById(KNetImpl.MOD_ID).versionString());
        
        modBus.addListener(this::onRegisterConfigurationTasks);
    }
    
    private void onRegisterConfigurationTasks(RegisterConfigurationTasksEvent event) {
        event.register(new ICustomConfigurationTask() {
            @Override
            public void run(Consumer<CustomPayload> consumer) {
                event.getListener().onTaskFinished(getKey());
            }

            @Override
            public Key getKey() {
                return new Key(new Identifier("test"));
            }
        });
    }
}
