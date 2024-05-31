plugins {
    id("com.kneelawk.versioning")
    id("com.kneelawk.submodule")
}

submodule {
    setRefmaps("knet-example")
    applyFabricLoaderDependency()
    forceRemap()
    xplatProjectDependency(":")
}

loom {
    accessWidenerPath.set(file("src/main/resources/knet_example.accesswidener"))
}
