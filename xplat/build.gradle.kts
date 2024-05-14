plugins {
    `maven-publish`
    id("dev.architectury.loom")
    id("com.kneelawk.versioning")
}

val maven_group: String by project
group = maven_group

val archives_base_name: String by project
base {
    archivesName.set("$archives_base_name-${project.name}-intermediary")
}

java.docsDir.set(rootProject.layout.buildDirectory.map { it.dir("docs").dir(project.name) })

repositories {
    mavenCentral()
    maven("https://maven.quiltmc.org/repository/release") { name = "Quilt" }
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

    // Using modCompileOnly & modLocalRuntime so that these dependencies don't get brought into any projects that depend
    // on this one.

    // Fabric Loader
    val fabric_loader_version: String by project
    modCompileOnly("net.fabricmc:fabric-loader:$fabric_loader_version")

    // Common Events
    val common_events_version: String by project
    modApi("com.kneelawk:common-events-xplat-intermediary:$common_events_version")

    testImplementation(platform("org.junit:junit-bom:5.10.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    val java_version: String by project
    val javaVersion = JavaVersion.toVersion(java_version)
    sourceCompatibility = javaVersion
    targetCompatibility = javaVersion

    withJavadocJar()
    withSourcesJar()
}

tasks {
    processResources.configure {
        inputs.property("version", project.version)

        filesMatching("quilt.mod.json") {
            expand(mapOf("version" to project.version))
        }
        filesMatching("fabric.mod.json") {
            expand(mapOf("version" to project.version))
        }
    }

    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
        val java_version: String by project
        options.release.set(java_version.toInt())
    }

    jar.configure {
        from(rootProject.file("LICENSE")) {
            rename { "${it}_${archives_base_name}" }
        }
        archiveClassifier.set("")
        manifest {
            attributes("Fabric-Loom-Remap" to true)
        }
    }

    named("sourcesJar", Jar::class).configure {
        from(rootProject.file("LICENSE")) {
            rename { "${it}_${rootProject.name}" }
        }
    }

    javadoc.configure {
        exclude("com/kneelawk/knet/impl")

//        val jetbrains_annotations_version: String by project
        (options as? StandardJavadocDocletOptions)?.links =
            listOf(
//                "https://javadoc.io/doc/org.jetbrains/annotations/${jetbrains_annotations_version}/"
            )

        options.optionFiles(rootProject.file("javadoc-options.txt"))
    }

    test.configure {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }

    afterEvaluate {
        named("genSources").configure {
            setDependsOn(listOf("genSourcesWithVineflower"))
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenIntermediary") {
            artifactId = "${rootProject.name}-${project.name}-intermediary"
            from(components["java"])
        }
        create<MavenPublication>("mavenMojmap") {
            artifactId = "${rootProject.name}-${project.name}-mojmap"
            artifact(tasks.named("jar"))
            artifact(tasks.named("sourcesJar"))
            artifact(tasks.named("javadocJar"))
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
