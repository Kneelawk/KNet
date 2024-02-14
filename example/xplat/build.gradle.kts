plugins {
    `maven-publish`
    id("architectury-plugin")
    id("dev.architectury.loom")
    id("com.kneelawk.versioning")
}

val maven_group: String by project
group = maven_group

val archives_base_name: String by project
base {
    archivesName.set("$archives_base_name-${project.name}-intermediary")
}

loom {
    accessWidenerPath.set(file("src/main/resources/knet_example.accesswidener"))
}

architectury {
    val enabled_platforms: String by project
    common(enabled_platforms.split(','))
}

repositories {
    mavenCentral()
    maven("https://maven.quiltmc.org/repository/release") { name = "Quilt" }

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

    // KNet
    compileOnly(project(":xplat", configuration = "namedElements"))

    testImplementation("junit:junit:4.13.2")
}

tasks {
    processResources {
        inputs.property("version", project.version)

        filesMatching("quilt.mod.json") {
            expand(mapOf("version" to project.version))
        }
        filesMatching("fabric.mod.json") {
            expand(mapOf("version" to project.version))
        }
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

    test {
        useJUnit()
    }

    afterEvaluate {
        named("genSources") {
            setDependsOn(listOf("genSourcesWithVineflower"))
        }
    }
}
