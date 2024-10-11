plugins {
    id("com.kneelawk.versioning")
    id("com.kneelawk.submodule")
}

submodule {
    xplatProjectDependency(":")
    xplatProjectDependency(":api", api = false, include = false, addMods = false)
}

loom {
    accessWidenerPath.set(file("src/main/resources/knet_example.accesswidener"))
}
