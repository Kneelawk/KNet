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
        maven("https://maven.kneelawk.com/releases/") {
            name = "Kneelawk"
        }
        gradlePluginPortal()
        mavenLocal()
    }
    plugins {
        val loom_version: String by settings
        id("fabric-loom") version loom_version
        val moddev_version: String by settings
        id("net.neoforged.moddev") version moddev_version
        val remapcheck_version: String by settings
        id("com.kneelawk.remapcheck") version remapcheck_version
        val versioning_version: String by settings
        id("com.kneelawk.versioning") version versioning_version
        val kpublish_version: String by settings
        id("com.kneelawk.kpublish") version kpublish_version
        val submodule_version: String by settings
        id("com.kneelawk.submodule") version submodule_version
    }
}

rootProject.name = "knet"

fun add(enabled: Boolean, name: String, path: String) {
    if (!enabled) return
    include(name)
    project(name).projectDir = file(path)
}

val xplat = true
val mojmap = true
val fabric = true
val neoforge = true

add(xplat, ":xplat", "xplat")
add(mojmap, ":xplat-mojmap", "xplat-mojmap")
add(fabric, ":fabric", "fabric")
add(fabric, ":fabric:remapCheck", "fabric/remapCheck")
add(neoforge, ":neoforge", "neoforge")

add(xplat, ":example-xplat", "example/xplat")
add(fabric, ":example-fabric", "example/fabric")
add(neoforge, ":example-neoforge", "example/neoforge")
