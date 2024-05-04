plugins {
    id("dev.architectury.loom")
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
        named("client").configure {
            ideConfigGenerated(true)
            programArgs("--width", "1280", "--height", "720")
        }
        named("server").configure {
            ideConfigGenerated(true)
        }
    }
}

repositories {
    mavenCentral()
    maven("https://maven.quiltmc.org/repository/release") { name = "Quilt" }
    maven("https://maven.neoforged.net/releases/") { name = "NeoForged" }
    maven("https://maven.firstdark.dev/snapshots") { name = "FirstDark" }
    maven("https://kneelawk.com/maven") { name = "Kneelawk" }
    maven("https://maven.parchmentmc.org") { name = "ParchmentMC" }

    mavenLocal()
}

dependencies {
    val minecraft_version: String by project
    minecraft("com.mojang:minecraft:$minecraft_version")
    val parchment_version: String by project
    mappings(loom.layered {
        officialMojangMappings()
        parchment("org.parchmentmc.data:parchment-$minecraft_version:$parchment_version@zip")
    })

    val neoforge_version: String by project
    neoForge("net.neoforged:neoforge:$neoforge_version")

    compileOnly(project(path = ":example-xplat", configuration = "namedElements"))

    // KNet
    compileOnly(project(":xplat", configuration = "namedElements"))
    compileOnly(project(":neoforge", configuration = "namedElements"))
    runtimeOnly(project(":neoforge", configuration = "dev"))
    include(project(":neoforge"))

    // :neoforge:dev doesn't export deps
    val common_events_version: String by project
    modRuntimeOnly("com.kneelawk:common-events-neoforge:$common_events_version")

    testCompileOnly(project(":neoforge", configuration = "namedElements"))
    testRuntimeOnly(project(":neoforge", configuration = "dev"))
}

java {
    val java_version: String by project
    val javaVersion = JavaVersion.toVersion(java_version)
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion

    withSourcesJar()
}

tasks {
    processResources.configure {
        from(project(":example-xplat").sourceSets.main.map { it.resources })

        inputs.property("version", project.version)

        filesMatching("META-INF/neoforge.mods.toml") {
            expand(mapOf("version" to project.version))
        }
    }

    withType<JavaCompile>().configureEach {
        source(project(":example-xplat").sourceSets.main.map { it.allSource })
        options.encoding = "UTF-8"
        val java_version: String by project
        options.release.set(java_version.toInt())
    }

    jar.configure {
        from(rootProject.file("LICENSE")) {
            rename { "${it}_${archives_base_name}" }
        }
    }

    named("sourcesJar", Jar::class).configure {
        from(project(":example-xplat").sourceSets.main.map { it.allSource })
        from(rootProject.file("LICENSE")) {
            rename { "${it}_${rootProject.name}" }
        }
    }

    afterEvaluate {
        named("genSources") {
            setDependsOn(listOf("genSourcesWithVineflower"))
        }
    }
}
