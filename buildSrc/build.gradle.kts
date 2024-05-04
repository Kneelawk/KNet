plugins {
    id("java-gradle-plugin")
}

repositories {
    mavenCentral()
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

dependencies {
    val architectury_loom_version: String by project
    implementation("dev.architectury.loom:dev.architectury.loom.gradle.plugin:$architectury_loom_version")
}

gradlePlugin {
    plugins {
        create("versioningPlugin") {
            id = "com.kneelawk.versioning"
            implementationClass = "com.kneelawk.versioning.VersioningPlugin"
        }
        create("remapCheckPlugin") {
            id = "com.kneelawk.remapcheck"
            implementationClass = "com.kneelawk.remapcheck.RemapCheckPlugin"
        }
    }
}
