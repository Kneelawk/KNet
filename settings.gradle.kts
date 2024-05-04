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
        maven("https://kneelawk.com/maven/") {
            name = "Kneelawk"
        }
        gradlePluginPortal()
    }
    plugins {
        val architectury_loom_version: String by settings
        id("dev.architectury.loom") version architectury_loom_version
        val remapcheck_version: String by settings
        id("com.kneelawk.remapcheck") version remapcheck_version
    }
}

include(":xplat")
include(":fabric")
include(":fabric:remapCheck")
include(":neoforge")

include(":example-xplat")
project(":example-xplat").projectDir = file("example/xplat")
include(":example-fabric")
project(":example-fabric").projectDir = file("example/fabric")
include(":example-neoforge")
project(":example-neoforge").projectDir = file("example/neoforge")

rootProject.name = "knet"
