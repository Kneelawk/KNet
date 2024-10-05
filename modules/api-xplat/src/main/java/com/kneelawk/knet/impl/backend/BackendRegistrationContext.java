package com.kneelawk.knet.impl.backend;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;

import com.kneelawk.knet.api.backend.BackendRegistrationCallback;
import com.kneelawk.knet.api.backend.KNetBackend;

public class BackendRegistrationContext implements BackendRegistrationCallback.Context {
    private final Map<String, KNetBackend> renderers = new LinkedHashMap<>();

    @Override
    public void registerBackend(@NotNull KNetBackend backend) {
        Objects.requireNonNull(backend, "backend should never be null");

        if (renderers.containsKey(backend.getName())) {
            throw new IllegalStateException(
                "Two renderers are present with the same name: " + backend.getName() + " (" +
                    renderers.get(backend.getName()).getClass() + " and " + backend.getKNet().getClass() + ")");
        }

        renderers.put(backend.getName(), backend);
    }

    public Map<String, KNetBackend> getBackends() {
        return renderers;
    }
}
