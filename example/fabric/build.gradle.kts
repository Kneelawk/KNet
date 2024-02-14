plugins {
    `maven-publish`
    id("architectury-plugin")
    id("dev.architectury.loom")
    id("com.github.johnrengelman.shadow")
    id("com.kneelawk.versioning")
}

evaluationDependsOn(":example-xplat")

val maven_group: String by project
group = maven_group

val archives_base_name: String by project
base {
    archivesName.set("$archives_base_name-${project.name}")
}

base.libsDirectory.set(rootProject.layout.buildDirectory.map { it.dir("libs") })

loom {
    accessWidenerPath = project(":example-xplat").loom.accessWidenerPath

    runs {
        named("client") {
            programArgs("--width", "1280", "--height", "720")
        }
    }
}

architectury {
    platformSetupLoomIde()
    fabric()
}

configurations {
    val common = create("common")
    create("shadowCommon")
    getByName("compileClasspath").extendsFrom(common)
    getByName("runtimeClasspath").extendsFrom(common)
    getByName("developmentFabric").extendsFrom(common)
}

repositories {
    mavenCentral()
    maven("https://maven.quiltmc.org/repository/release") { name = "Quilt" }
    maven("https://maven.terraformersmc.com/releases/") { name = "TerraformersMC" }

    mavenLocal()
}

dependencies {
    val minecraft_version: String by project
    minecraft("com.mojang:minecraft:$minecraft_version")
    val yarn_mappings: String by project
    mappings("net.fabricmc:yarn:$yarn_mappings:v2")

    // Using modCompileOnly & modLocalRuntime so that these dependencies don't get brought into any projects that depend
    // on this one.

    // Fabric Loader
    val fabric_loader_version: String by project
    modCompileOnly("net.fabricmc:fabric-loader:$fabric_loader_version")
    modLocalRuntime("net.fabricmc:fabric-loader:$fabric_loader_version")

    // Fabric Api
    val fapi_version: String by project
    modCompileOnly("net.fabricmc.fabric-api:fabric-api:$fapi_version")
    modLocalRuntime("net.fabricmc.fabric-api:fabric-api:$fapi_version")

    "common"(project(path = ":example-xplat", configuration = "namedElements")) { isTransitive = false }
    "shadowCommon"(project(path = ":example-xplat", configuration = "transformProductionFabric")) {
        isTransitive = false
    }

    // KNet
    compileOnly(project(":xplat", configuration = "namedElements"))
    localRuntime(project(":xplat", configuration = "namedElements"))
    implementation(project(":fabric", configuration = "namedElements"))
    include(project(":fabric"))

    // Mod Menu
    val mod_menu_version: String by project
    modLocalRuntime("com.terraformersmc:modmenu:$mod_menu_version")
}

tasks {
    processResources {
        from(project(":example-xplat").sourceSets.main.map { it.resources.asFileTree })

        inputs.property("version", project.version)

        filesMatching("quilt.mod.json") {
            expand(mapOf("version" to project.version))
        }
        filesMatching("fabric.mod.json") {
            expand(mapOf("version" to project.version))
        }
    }

    shadowJar {
        exclude("architectury.common.json")
        configurations = listOf(project.configurations["shadowCommon"])
        archiveClassifier = "dev-shadow"
    }

    remapJar {
        injectAccessWidener = true
        inputFile.set(shadowJar.flatMap { it.archiveFile })
        dependsOn(shadowJar)
    }

    withType<JavaCompile> {
        options.encoding = "UTF-8"
        options.release.set(17)
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17

        withSourcesJar()
    }

    jar {
        from(rootProject.file("LICENSE")) {
            rename { "${it}_${archives_base_name}" }
        }
    }

    named("sourcesJar", Jar::class) {
        val xplatSources = project(":example-xplat").tasks.named("sourcesJar", Jar::class)
        dependsOn(xplatSources)
        from(xplatSources.flatMap { task -> task.archiveFile.map { zipTree(it) } })
    }

    afterEvaluate {
        named("genSources") {
            setDependsOn(listOf("genSourcesWithVineflower"))
        }
    }
}
