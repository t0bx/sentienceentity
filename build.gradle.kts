plugins {
    java
    id("com.gradleup.shadow") version "8.3.8"
    id("maven-publish")
}

group = "de.t0bx"
version = "1.9"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

repositories {
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://oss.sonatype.org/content/groups/public/")
    maven("https://repo.codemc.io/repository/maven-releases/")
    maven("https://repo.codemc.io/repository/maven-snapshots/")
    maven("https://repository.t0bx.de/repository/spigotmc-releases/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.7-R0.1-SNAPSHOT")
    compileOnly("org.projectlombok:lombok:1.18.34")
    annotationProcessor("org.projectlombok:lombok:1.18.34")

    implementation("io.netty:netty-transport:4.1.115.Final")
    implementation("io.netty:netty-codec:4.1.115.Final")

    implementation("net.kyori:adventure-text-minimessage:4.23.0")
    implementation("net.kyori:adventure-text-serializer-legacy:4.23.0")
    implementation("net.kyori:adventure-text-serializer-gson:4.23.0")

    implementation("org.bstats:bstats-bukkit:3.0.2")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.19.0")
}

tasks.processResources {
    from("LICENSE")
}

tasks.jar {
    manifest {
        attributes(
            "Implementation-Title" to project.name,
            "Implementation-Version" to project.version
        )
    }
}

tasks.shadowJar {
    relocate("org.bstats", "de.t0bx.sentienceEntity.bstats")

    archiveBaseName.set("SentienceEntity")
    archiveVersion.set(project.version.toString())
    archiveClassifier.set("")
    archiveExtension.set("jar")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
    repositories {
        maven {
            url = uri("https://repository.t0bx.de/repository/spigotmc-releases/")
            credentials {
                username = System.getenv("REPO_USER")
                password = System.getenv("REPO_PASSWORD")
            }
        }
    }
}
