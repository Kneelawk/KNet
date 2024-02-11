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

java.docsDir.set(rootProject.layout.buildDirectory.map { it.dir("docs").dir("${rootProject.name}-${project.name}") })

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

        withJavadocJar()
        withSourcesJar()
    }

    jar {
        from(rootProject.file("LICENSE")) {
            rename { "${it}_${archives_base_name}" }
        }
    }

    javadoc {
        exclude("com/kneelawk/knet/impl")

        val yarn_mappings: String by project
        val jetbrains_annotations_version: String by project
        (options as? StandardJavadocDocletOptions)?.links =
            listOf(
                "https://maven.fabricmc.net/docs/yarn-${yarn_mappings}/",
                "https://javadoc.io/doc/org.jetbrains/annotations/${jetbrains_annotations_version}/"
            )

        options.optionFiles(rootProject.file("javadoc-options.txt"))
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

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = "${project.name}-intermediary"
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
