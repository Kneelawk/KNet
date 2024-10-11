plugins {
    id("com.kneelawk.versioning")
    id("com.kneelawk.submodule")
    id("com.kneelawk.kpublish")
}

submodule {
    setLibsDirectory()
    applyXplatConnection(":xplat")
    setupJavadoc()
}

dependencies {
    api(project(":backend-fabric", configuration = "namedElements"))
    include(project(":backend-fabric"))
}

kpublish {
    createPublication()
}
