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
    api(project(":backend-neoforge"))
    jarJar(project(":backend-neoforge"))
}

kpublish {
    createPublication()
}
