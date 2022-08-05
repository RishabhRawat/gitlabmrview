plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "1.7.10"
    id("org.jetbrains.intellij") version "1.8.0"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.7.10"
    id("com.apollographql.apollo3") version "3.5.0"
}

group = "com.rrawat"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

// Configure Gradle IntelliJ Plugin
// Read more: https://plugins.jetbrains.com/docs/intellij/tools-gradle-intellij-plugin.html
intellij {
    version.set("2022.2")
    type.set("PY") // Target IDE Platform

    plugins.set(listOf("git4idea"))
}

apollo {
    packageName.set("com.rrawat.gitlabmrview")
    generateOptionalOperationVariables.set(false)
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    patchPluginXml {
        sinceBuild.set("220")
        untilBuild.set("223.*")
    }

    test {
        useJUnitPlatform()
    }

    signPlugin {
        certificateChain.set(System.getenv("CERTIFICATE_CHAIN"))
        privateKey.set(System.getenv("PRIVATE_KEY"))
        password.set(System.getenv("PRIVATE_KEY_PASSWORD"))
    }

    publishPlugin {
        token.set(System.getenv("PUBLISH_TOKEN"))
    }
}

dependencies {
    implementation(kotlin("script-runtime"))
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
    implementation("com.apollographql.apollo3:apollo-runtime:3.5.0")
    testImplementation(kotlin("test"))
}