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

package com.kneelawk.knet.impl.backend;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.kneelawk.knet.impl.KNetImpl;
import com.kneelawk.knet.impl.KNetLog;
import com.kneelawk.knet.impl.platform.Platform;

public class BackendConfig {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final String CONFIG_NAME = "backends.json";
    private static final Path CONFIG_DIR = Platform.INSTANCE.getConfigDir().resolve(KNetImpl.CONFIG_DIR);
    private static final Path CONFIG_FILE = CONFIG_DIR.resolve(CONFIG_NAME);

    private static BackendConfig instance;

    public Map<String, Integer> backendPriorities;

    public static Map<String, Integer> getDefaultBackend(Map<String, Integer> priorities) {
        load(priorities);
        return instance.backendPriorities;
    }

    public static void load(Map<String, Integer> priorities) {
        BackendConfig config = loadImpl();
        initImpl(config, priorities);
        saveImpl(config);
        instance = config;
    }

    public static void save() {
        saveImpl(instance);
    }

    private static BackendConfig loadImpl() {
        if (Files.exists(CONFIG_FILE)) {
            try {
                BackendConfig config;
                try (BufferedReader reader = Files.newBufferedReader(CONFIG_FILE)) {
                    config = gson.fromJson(reader, BackendConfig.class);
                }
                if (config == null) return new BackendConfig();
                return config;
            } catch (Exception e) {
                KNetLog.LOG.warn("Error loading KNet Backend config", e);
                return new BackendConfig();
            }
        } else {
            return new BackendConfig();
        }
    }

    private static void initImpl(BackendConfig initTo, Map<String, Integer> priorities) {
        if (initTo.backendPriorities == null || initTo.backendPriorities.isEmpty()) {
            initTo.backendPriorities = new LinkedHashMap<>(priorities);
        } else {
            for (var entry : priorities.entrySet()) {
                initTo.backendPriorities.putIfAbsent(entry.getKey(), entry.getValue());
            }
        }
    }

    private static void saveImpl(BackendConfig toSave) {
        try {
            if (!Files.exists(CONFIG_DIR)) Files.createDirectories(CONFIG_DIR);
            try (BufferedWriter writer = Files.newBufferedWriter(CONFIG_FILE)) {
                gson.toJson(toSave, BackendConfig.class, writer);
            }
        } catch (Exception e) {
            KNetLog.LOG.warn("Error waving KNet Backend config", e);
        }
    }
}
