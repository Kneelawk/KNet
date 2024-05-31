plugins {
    id("com.kneelawk.versioning")
    id("com.kneelawk.submodule")
    id("com.kneelawk.kpublish")
}

submodule {
    setLibsDirectory()
    setRefmaps("knet")
    applyFabricLoaderDependency()
    forceRemap()
    setupJavadoc()
}

dependencies {
    // Common Events
    val common_events_version: String by project
    modApi("com.kneelawk:common-events-xplat-intermediary:$common_events_version")
}

kpublish {
    createPublication("intermediary")
}
