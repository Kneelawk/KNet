plugins {
    id("com.kneelawk.versioning")
    id("com.kneelawk.submodule")
    id("com.kneelawk.kpublish")
}

submodule {
    setLibsDirectory()
    applyFabricLoaderDependency()
    applyFabricApiDependency()
    applyXplatConnection(":xplat", "fabric")
    setupJavadoc()
}

dependencies {
    // Common Events
    val common_events_version: String by project
    modApi("com.kneelawk.common-events:common-events-fabric:$common_events_version")
}

kpublish {
    createPublication()
}
