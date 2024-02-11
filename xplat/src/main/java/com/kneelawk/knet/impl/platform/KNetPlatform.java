package com.kneelawk.knet.impl.platform;

import java.util.ServiceLoader;

public interface KNetPlatform {
    KNetPlatform INSTANCE = ServiceLoader.load(KNetPlatform.class).findFirst()
        .orElseThrow(() -> new RuntimeException("Unable to find KNet platform"));
}
