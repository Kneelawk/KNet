import org.jetbrains.kotlin.gradle.utils.extendsFrom

plugins {
    id("com.kneelawk.versioning")
    id("com.kneelawk.submodule")
    id("com.kneelawk.kpublish")
}

submodule {
    setLibsDirectory()
    applyXplatConnection(":backend-badpackets-xplat")
    setupJavadoc()
}

configurations {
    create("localRuntime")
    named("runtimeClasspath").extendsFrom(named("localRuntime"))
}

repositories {
    maven("https://maven2.bai.lol") { name = "BadPackets" }
}

dependencies {
    val badpackets_version: String by project
    compileOnly("lol.bai:badpackets:neo-${badpackets_version}")
    add("localRuntime", "lol.bai:badpackets:neo-${badpackets_version}")
}

kpublish {
    createPublication()
}
