plugins {
    id("com.kneelawk.versioning")
    id("com.kneelawk.submodule")
}

submodule {
    applyXplatConnection(":simple-xplat")
    generateRuns()
}

loom {
    accessWidenerPath = project(":simple-xplat").loom.accessWidenerPath
}

//dependencies {
//    // Mod Menu
//    val mod_menu_version: String by project
//    modLocalRuntime("com.terraformersmc:modmenu:$mod_menu_version") {
//        exclude(group = "net.fabricmc")
//        exclude(group = "net.fabricmc.fabric-api")
//    }
//}
