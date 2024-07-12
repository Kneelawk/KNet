plugins {
    id("com.kneelawk.versioning")
    id("com.kneelawk.submodule")
    id("com.kneelawk.kpublish")
}

submodule {
    setLibsDirectory()
    applyNeoforgeDependency()
    applyXplatConnection(":xplat", "neoforge")
    setupJavadoc()
    createDevExport()
}

dependencies {
    // Common Events
    val common_events_version: String by project
    modApi("com.kneelawk.common-events:common-events-neoforge:$common_events_version")
    include("com.kneelawk.common-events:common-events-neoforge:$common_events_version")
}

kpublish {
    createPublication()
}
