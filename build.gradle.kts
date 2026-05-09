plugins {
    idea
    `maven-publish`
    id("net.minecraftforge.gradle") version "[6.0.24,6.2)"
    id("org.parchmentmc.librarian.forgegradle") version "1.2.0"
    id("org.spongepowered.mixin") version "0.7.+"
}

group = property("mod_group_id") as String
version = property("mod_version") as String

base {
    archivesName.set(property("mod_id") as String)
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

minecraft {
    mappings("parchment", property("parchment_version") as String)
    copyIdeResources = true

    runs {
        configureEach {
            workingDirectory(project.file("run"))
            property("forge.logging.console.level", "debug")
            property("mixin.env.remapRefMap", "true")
            property("mixin.env.refMapRemappingFile", "${projectDir}/build/createSrgToMcp/output.srg")
            mods {
                create(property("mod_id") as String) {
                    source(sourceSets.main.get())
                }
            }
        }
        create("client")
        create("server") { arg("--nogui") }
    }
}

repositories {
    maven("https://maven.minecraftforge.net")
    maven("https://repo.spongepowered.org/repository/maven-public/")
    mavenCentral()
}

dependencies {
    minecraft("net.minecraftforge:forge:${property("minecraft_version")}-${property("forge_version")}")
}

tasks.named<Jar>("jar") {
    finalizedBy("reobfJar")
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(17)
}

tasks.processResources {
    val props = mapOf(
        "minecraft_version" to project.property("minecraft_version"),
        "forge_version" to project.property("forge_version"),
        "mod_id" to project.property("mod_id"),
        "mod_name" to project.property("mod_name"),
        "mod_version" to project.property("mod_version")
    )
    inputs.properties(props)
    filesMatching(listOf("META-INF/mods.toml", "pack.mcmeta")) {
        expand(props)
    }
}

mixin {
    config("btmfixes.mixins.json")
}
