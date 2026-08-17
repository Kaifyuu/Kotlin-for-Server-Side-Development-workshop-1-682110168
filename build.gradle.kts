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

// Run from any terminal (VS Code included).
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
}

tasks.register<JavaExec>("runWorkshop2") {
    group = "workshop"
    mainClass.set("org.example.Workshop2Kt")
    classpath = sourceSets["main"].runtimeClasspath
    javaLauncher.set(toolchainLauncher)
    systemProperty("stdout.encoding", "UTF-8")
}

// Prints the resolved runtime classpath so scripts/run-workshop.ps1 can invoke
// java.exe directly, bypassing Gradle's own stdout relay (which can corrupt
// non-ASCII output regardless of the child JVM's own encoding settings).
tasks.register("workshopClasspath") {
    doLast {
        println(sourceSets["main"].runtimeClasspath.asPath)
    }
}