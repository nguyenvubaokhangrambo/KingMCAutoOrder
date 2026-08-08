plugins {
    id("fabric-loom") version "1.9-SNAPSHOT"
    id("maven-publish")
    java
}

version = "1.0.0"
group = "com.kingmc.autoorder"

repositories {
    mavenCentral()
    maven("https://maven.meteordev.org/releases")
    maven("https://maven.meteordev.org/snapshots")
    maven("https://meteordev.github.io/baritone")
}

dependencies {
    minecraft("com.mojang:minecraft:${property("minecraft_version")}")
    mappings("net.fabricmc:yarn:${property("yarn_mappings")}:v2")
    modImplementation("net.fabricmc:fabric-loader:${property("loader_version")}")
    
    // Meteor Client & Baritone
    modImplementation("meteordevelopment:meteor-client:${property("meteor_version")}")
    modImplementation("baritone:baritone-unoptimized-fabric:${property("minecraft_version")}-SNAPSHOT")
}

tasks.processResources {
    inputs.property("version", project.version)
    filesMatching("fabric.mod.json") {
        expand("version" to project.version)
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(21)
}