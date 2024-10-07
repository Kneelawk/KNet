package com.kneelawk.knet.impl.backend;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

import org.jetbrains.annotations.Nullable;

import com.kneelawk.commonevents.api.Scan;
import com.kneelawk.knet.api.KNet;
import com.kneelawk.knet.api.backend.BackendRegistrationCallback;
import com.kneelawk.knet.api.backend.KNetBackend;
import com.kneelawk.knet.impl.KNetLog;

@Scan(side = Scan.Side.CLIENT)
public class BackendManager {
    private static Map<String, KNet> knetsByName;
    private static List<KNet> knetsByPriority;
    private static KNet defaultKNet;

    static {
        loadBackends();
    }

    private static void loadBackends() {
        KNetLog.LOG.info("[KNet] Loading KNet backends...");

        BackendRegistrationContext ctx = new BackendRegistrationContext();
        BackendRegistrationCallback.EVENT.invoker().registerBackends(ctx);
        Map<String, KNetBackend> backends = ctx.getBackends();

        KNetLog.LOG.info("[KNet] KNet backends: {}", backends.keySet());

        Map<String, Integer> priorities = backends.entrySet().stream()
            .collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, e -> e.getValue().getPriority()));
        knetsByName = backends.entrySet().stream()
            .collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, e -> e.getValue().getKNet()));

        if (backends.isEmpty()) {
            KNetLog.LOG.error("[KNet] No KNet backends present! KNet will not work.");
            knetsByPriority = List.of();
        } else {
            String bestRenderer = priorities.entrySet().stream().min(Map.Entry.comparingByValue()).get().getKey();
            KNetLog.LOG.info("[KNet] Best KNet backend: {}", bestRenderer);

            Map<String, Integer> newPriorities = BackendConfig.getDefaultBackend(priorities);
            ArrayList<String> sortedBackends = new ArrayList<>(backends.keySet());
            sortedBackends.sort(Comparator.comparing(newPriorities::get));
            String defaultName = sortedBackends.getFirst();

            KNetLog.LOG.info("[KNet] Sorted KNet Backends: {}", sortedBackends);

            KNetLog.LOG.info("[KNet] Default KNet backend: {}", defaultName);

            knetsByPriority =
                sortedBackends.stream().map(knetsByName::get).collect(ImmutableList.toImmutableList());
            defaultKNet = knetsByPriority.getFirst();

            KNetLog.LOG.info("[KNet] KNet backends loaded.");

            KNet.LOADED.invoker().onLoaded(BackendLoadedProvider.INSTANCE);
        }
    }

    public static void load() {
        // Statically loads this class
    }

    public static KNet getDefault() {
        KNet defaultKNet = BackendManager.defaultKNet;
        if (defaultKNet == null) throw new RuntimeException("No KNet backends are present");
        return defaultKNet;
    }

    public static @Nullable KNet tryGetDefault() {
        return defaultKNet;
    }

    public static KNet get(String name) {
        KNet knet = knetsByName.get(name);
        if (knet == null) throw new RuntimeException("No KNet backend with name: " + name);
        return knet;
    }

    public static @Nullable KNet tryGet(String name) {
        return knetsByName.get(name);
    }
}
