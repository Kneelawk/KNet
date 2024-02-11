plugins {
    `maven-publish`
    id("architectury-plugin")
    id("dev.architectury.loom")
    id("com.github.johnrengelman.shadow")
    id("com.kneelawk.versioning")
}

evaluationDependsOn(":xplat")

val maven_group: String by project
group = maven_group

val archives_base_name: String by project
base {
    archivesName.set("$archives_base_name-${project.name}")
}

base.libsDirectory.set(rootProject.layout.buildDirectory.map { it.dir("libs") })
java.docsDir.set(rootProject.layout.buildDirectory.map { it.dir("docs").dir("${rootProject.name}-${project.name}") })

architectury {
    neoForge()
}

configurations {
    val common = create("common")
    create("shadowCommon")
    getByName("compileClasspath").extendsFrom(common)
    getByName("runtimeClasspath").extendsFrom(common)
    getByName("developmentNeoForge").extendsFrom(common)
    create("dev") {
        isCanBeConsumed = true
        isCanBeResolved = false
    }
}

repositories {
    mavenCentral()
    maven("https://maven.quiltmc.org/repository/release") { name = "Quilt" }
    maven("https://maven.neoforged.net/releases/") { name = "NeoForged" }

    mavenLocal()
}

dependencies {
    val minecraft_version: String by project
    minecraft("com.mojang:minecraft:$minecraft_version")
    val yarn_mappings: String by project
    mappings("net.fabricmc:yarn:$yarn_mappings:v2")

    val neoforge_version: String by project
    neoForge("net.neoforged:neoforge:$neoforge_version")

    "common"(project(path = ":xplat", configuration = "namedElements")) { isTransitive = false }
    "shadowCommon"(project(path = ":xplat", configuration = "transformProductionNeoForge")) {
        isTransitive = false
    }
}

tasks {
    processResources {
        from(project(":xplat").sourceSets.main.map { it.resources.asFileTree })

        inputs.property("version", project.version)

        filesMatching("META-INF/mods.toml") {
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

        withJavadocJar()
        withSourcesJar()
    }

    jar {
        from(rootProject.file("LICENSE")) {
            rename { "${it}_${archives_base_name}" }
        }
    }

    javadoc {
        source(project(":xplat").sourceSets.main.get().allJava)
        exclude("com/kneelawk/knet/impl")
        exclude("com/kneelawk/knet/neoforge/impl")

        val yarn_mappings: String by project
        val jetbrains_annotations_version: String by project
        (options as? StandardJavadocDocletOptions)?.links =
            listOf(
                "https://maven.fabricmc.net/docs/yarn-${yarn_mappings}/",
                "https://javadoc.io/doc/org.jetbrains/annotations/${jetbrains_annotations_version}/"
            )

        options.optionFiles(rootProject.file("javadoc-options.txt"))
    }

    named("sourcesJar", Jar::class) {
        val xplatSources = project(":xplat").tasks.named("sourcesJar", Jar::class)
        dependsOn(xplatSources)
        from(xplatSources.flatMap { task -> task.archiveFile.map { zipTree(it) } })
    }

    afterEvaluate {
        named("genSources") {
            setDependsOn(listOf("genSourcesWithVineflower"))
        }
    }
}

artifacts {
    add("dev", tasks.shadowJar)
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }

    repositories {
        if (System.getenv("PUBLISH_REPO") != null) {
            maven {
                name = "publishRepo"
                url = uri(rootProject.file(System.getenv("PUBLISH_REPO")))
            }
        }
    }
}
