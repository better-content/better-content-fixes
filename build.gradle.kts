plugins {
    idea
    `maven-publish`
    jacoco
    id("net.minecraftforge.gradle") version "[6.0.24,6.2)"
    id("org.parchmentmc.librarian.forgegradle") version "1.2.0"
    id("org.spongepowered.mixin") version "0.7.+"
}

group = property("mod_group_id") as String
version = property("mod_version") as String
val mixinExtrasVersion = "0.5.0"
val hyleCompileJar = System.getenv("BC_HYLE_JAR")
    ?: "../../cache/packwiz-downloads/mods/Hyle-2.0.0-1.20.1-forge-all.jar"
val thirstCompileJar = System.getenv("BC_THIRST_JAR")
    ?: "../../cache/packwiz-downloads/mods/ThirstWasTaken-1.20.1-1.4.0.jar"
val weather2CompileJar = System.getenv("BC_WEATHER2_JAR")
    ?: "../../cache/packwiz-downloads/mods/weather2-1.20.1-2.8.3.jar"
val curiosCompileJar = System.getenv("BC_CURIOS_JAR")
    ?: "../../cache/packwiz-downloads/mods/curios-forge-5.14.1+1.20.1.jar"
val tconCompileJar = System.getenv("BC_TCON_JAR")
    ?: "../../cache/packwiz-downloads/mods/TConstruct-1.20.1-3.11.2.166.jar"
val mantleCompileJar = System.getenv("BC_MANTLE_JAR")
    ?: "../../cache/packwiz-downloads/mods/Mantle-1.20.1-1.11.104.jar"

base {
    archivesName.set(property("mod_id") as String)
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(17))
}

minecraft {
    mappings("official", property("minecraft_version") as String)
    copyIdeResources = true
    jarJar.enable()

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
        create("gameTestServer") {
            workingDirectory(project.file("run-gametest"))
            property("forge.enableGameTest", "true")
            property("forge.gameTestServer", "true")
            property("forge.enabledGameTestNamespaces", property("mod_id") as String)
            arg("--nogui")
        }
    }
}

repositories {
    maven("https://maven.minecraftforge.net")
    maven("https://harleyoconnor.com/maven")
    maven("https://repo.spongepowered.org/repository/maven-public/")
    maven("https://maven.llamalad7.mixinextras.org/releases/")
    mavenCentral()
}

dependencies {
    minecraft("net.minecraftforge:forge:${property("minecraft_version")}-${property("forge_version")}")
    compileOnly(files(hyleCompileJar))
    compileOnly(files(thirstCompileJar))
    compileOnly(files(weather2CompileJar))
    compileOnly(files(curiosCompileJar))
    compileOnly(fg.deobf(files(mantleCompileJar)))
    compileOnly(fg.deobf(files(tconCompileJar)))
    runtimeOnly(fg.deobf("com.ferreusveritas.dynamictrees:DynamicTrees-1.20.1:1.4.9"))
    compileOnly(annotationProcessor("io.github.llamalad7:mixinextras-common:$mixinExtrasVersion")!!)
    implementation(jarJar("io.github.llamalad7:mixinextras-forge:[$mixinExtrasVersion,0.6.0)")!!)
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testImplementation("com.google.code.gson:gson:2.10.1")
}

tasks.named<Jar>("jar") {
    finalizedBy("reobfJar")
}

val stageRuntimeJar by tasks.registering(Copy::class) {
    group = "build"
    description = "Stages the reobfuscated runtime jar into build/libs using the canonical release filename."
    dependsOn(tasks.named("reobfJar"))
    mustRunAfter(tasks.named("jarJar"))
    mustRunAfter(tasks.named("reobfJarJar"))
    from(layout.buildDirectory.file("reobfJar/output.jar"))
    into(layout.buildDirectory.dir("libs"))
    rename { "${base.archivesName.get()}-$version.jar" }
}

tasks.named("assemble") {
    dependsOn(stageRuntimeJar)
}

tasks.withType<JavaCompile>().configureEach {
    options.release.set(17)
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

tasks.register("headlessGameTest") {
    group = "verification"
    description = "Runs Forge game tests in a headless dedicated server."
    dependsOn(tasks.named("runGameTestServer"))
}

tasks.register("verifyFast") {
    group = "verification"
    description = "Runs deterministic unit/resource checks without Forge game tests."
    dependsOn(tasks.named("check"))
}

tasks.register("verifyFull") {
    group = "verification"
    description = "Runs the full verification lane including headless Forge game tests."
    dependsOn(tasks.named("verifyFast"))
    dependsOn(tasks.named("headlessGameTest"))
}

val resetGameTestMods = tasks.register<Delete>("resetGameTestMods") {
    delete(layout.projectDirectory.dir("run-gametest/mods"))
}

val syncGameTestStructures = tasks.register<Sync>("syncGameTestStructures") {
    from(layout.projectDirectory.dir("src/main/resources/gameteststructures"))
    into(layout.projectDirectory.dir("run-gametest/gameteststructures"))
}

tasks.matching { it.name.startsWith("prepareRunGameTestServer") }.configureEach {
    dependsOn(resetGameTestMods)
    dependsOn(syncGameTestStructures)
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
    config("bcfixes.mixins.json")
}

jacoco {
    toolVersion = "0.8.12"
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
    classDirectories.setFrom(
        files(classDirectories.files.map {
            fileTree(it) {
                include(
                    "io/github/bcfixes/compat/BurntGrassReplacementDefinitions*"
                )
            }
        })
    )
}

tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.jacocoTestReport)
    classDirectories.setFrom(tasks.jacocoTestReport.map { it.classDirectories })
    violationRules {
        rule {
            element = "CLASS"
            includes = listOf("io.github.bcfixes.compat.BurntGrassReplacementDefinitions")
            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = "0.90".toBigDecimal()
            }
            limit {
                counter = "BRANCH"
                value = "COVEREDRATIO"
                minimum = "0.75".toBigDecimal()
            }
        }
    }
}

tasks.check {
    dependsOn(tasks.jacocoTestCoverageVerification)
}
