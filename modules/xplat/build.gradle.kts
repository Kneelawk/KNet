plugins {
    id("com.kneelawk.versioning")
    id("com.kneelawk.submodule")
    id("com.kneelawk.kpublish")
}

submodule {
    setRefmaps("knet")
    setupJavadoc()
    xplatProjectDependency(":api")
    xplatProjectDependency(":backend-badpackets")
}

kpublish {
    createPublication("intermediary")
}
