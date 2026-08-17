val ktorVersion = "3.0.3"
val exposedVersion = "1.0.0-beta-1"

plugins {
    kotlin("jvm") version "2.1.21"
    kotlin("plugin.serialization") version "2.1.21"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))

    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json:$ktorVersion")

    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
    testImplementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")

    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("com.h2database:h2:2.3.232")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(18)
}

// Run from any terminal (VS Code included) with correct Thai console output.
// stdout.encoding forces UTF-8 regardless of Windows console codepage detection.
val toolchainLauncher = javaToolchains.launcherFor {
    languageVersion.set(JavaLanguageVersion.of(18))
}

tasks.register<JavaExec>("runWorkshop1") {
    group = "workshop"
    mainClass.set("org.example.Workshop1Kt")
    classpath = sourceSets["main"].runtimeClasspath
    javaLauncher.set(toolchainLauncher)
    systemProperty("stdout.encoding", "UTF-8")
    standardInput = System.`in`
    // Gradle normally re-decodes child stdout through its own logging pipe,
    // which can corrupt non-ASCII text regardless of the child's own encoding.
    // Writing straight to the inherited console handle bypasses that relay.
    standardOutput = System.out
    errorOutput = System.err
}

tasks.register<JavaExec>("runWorkshop2") {
    group = "workshop"
    mainClass.set("org.example.Workshop2Kt")
    classpath = sourceSets["main"].runtimeClasspath
    javaLauncher.set(toolchainLauncher)
    systemProperty("stdout.encoding", "UTF-8")
    standardOutput = System.out
    errorOutput = System.err
}