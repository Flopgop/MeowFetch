import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose") version "1.9.0-beta03"
    id("org.jetbrains.kotlin.plugin.compose")
}

group = "net.flamgop"
version = "3.0"

repositories {
    maven("https://packages.jetbrains.team/maven/p/kpm/public/")
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("--enable-preview")
    sourceCompatibility = "21"
    targetCompatibility = "21"
}

tasks.withType<JavaExec>().configureEach {
    jvmArgs("--enable-preview")
}

dependencies {
    implementation("org.jetbrains.jewel:jewel-int-ui-standalone:0.30.0-252.26252")
    implementation("org.jetbrains.jewel:jewel-int-ui-decorated-window:0.30.0-252.26252")
    implementation(compose.desktop.currentOs)
    implementation(compose.components.resources)
    implementation(compose.materialIconsExtended)
    implementation(compose.material3)

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.10.2")

//    implementation("dev.mobile:dadb:1.2.10")

    implementation("io.github.vinceglb:filekit-core:0.11.0")
    implementation("io.github.vinceglb:filekit-dialogs:0.11.0")
    implementation("io.github.vinceglb:filekit-dialogs-compose:0.11.0")
    implementation("io.github.vinceglb:filekit-coil:0.11.0")

    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
}

compose.resources {
    publicResClass = false
    packageOfResClass = "net.flamgop.meowfetch.resources"
    generateResClass = auto
}

compose.desktop {
    application {
        mainClass = "net.flamgop.meowfetch.AppKt"
        args += "--enable-preview"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Exe, TargetFormat.Deb)
            packageName = "meowfetch"
            packageVersion = "1.0.0"
            includeAllModules = true
        }
    }
}
