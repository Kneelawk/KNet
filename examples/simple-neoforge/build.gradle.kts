plugins {
    id("com.kneelawk.versioning")
    id("com.kneelawk.submodule")
}

submodule {
    applyXplatConnection(":simple-xplat")
    generateRuns()
}

neoForge {
    accessTransformers {
        from(file("knet_example.accesstransformer.cfg"))
        publish(file("knet_example.accesstransformer.cfg"))
    }
}
