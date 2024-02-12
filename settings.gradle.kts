pluginManagement {
    repositories {
        maven("https://maven.quiltmc.org/repository/release") {
            name = "Quilt"
        }
        maven("https://maven.fabricmc.net/") {
            name = "Fabric"
        }
        maven("https://maven.architectury.dev/") {
            name = "Architectury"
        }
        maven("https://maven.neoforged.net/releases/") {
            name = "NeoForged"
        }
        gradlePluginPortal()
    }
    plugins {
        val loom_version: String by settings
        id("fabric-loom") version loom_version
        val architectury_version: String by settings
        id("architectury-plugin") version architectury_version
        val architectury_loom_version: String by settings
        id("dev.architectury.loom") version architectury_loom_version
        val shadow_version: String by settings
        id("com.github.johnrengelman.shadow") version shadow_version
    }
}

include(":xplat")
include(":fabric")
include(":neoforge")

include(":xplat:mojmap")
project(":xplat:mojmap").projectDir = file("xplat/mojmap")

include(":example-xplat")
project(":example-xplat").projectDir = file("example/xplat")
include(":example-fabric")
project(":example-fabric").projectDir = file("example/fabric")
include(":example-neoforge")
project(":example-neoforge").projectDir = file("example/neoforge")

rootProject.name = "knet"
