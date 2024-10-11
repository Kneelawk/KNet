plugins {
    id("com.kneelawk.versioning")
    id("com.kneelawk.submodule")
    id("com.kneelawk.kpublish")
}

submodule {
    setRefmaps("knet_backend_badpackets")
    xplatProjectDependency(":api", include = false)
    setupJavadoc()
}

repositories {
    maven("https://maven2.bai.lol") { name = "BadPackets" }
}

dependencies {
    val badpackets_version: String by project
    modCompileOnly("lol.bai:badpackets:fabric-${badpackets_version}")
    modLocalRuntime("lol.bai:badpackets:fabric-${badpackets_version}")
}

kpublish {
    createPublication("intermediary")
}
