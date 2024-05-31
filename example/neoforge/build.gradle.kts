plugins {
    id("com.kneelawk.versioning")
    id("com.kneelawk.submodule")
}

submodule {
    applyNeoforgeDependency()
    applyXplatConnection(":example-xplat", "neoforge")
    generateRuns()
}

loom {
    accessWidenerPath = project(":example-xplat").loom.accessWidenerPath
}
