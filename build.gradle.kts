@Suppress("PropertyName")
var VERSION = "2.1.7"

plugins { // needed for the subprojects section to work
    id("java")
    id("java-library")
    id("maven-publish")
    id("io.github.goooler.shadow") version "8.1.7"
}

ext {
    set("lombokDep", "org.projectlombok:lombok:1.18.32")
    set("jetbrainsDep", "org.jetbrains:annotations:24.1.0")

    // reduced is just a re-zipped version of the original, without some conflicting libraries
    //  gson, org.json, com.yaml.snakeyaml
    set("lowestSpigotDep", "net.techcable.tacospigot:server:1.8.8-R0.2-REDUCED")    // luxious nexus (public)
}

allprojects {
    group = "com.kamikazejam"
    version = VERSION
    description = "Utility Jar to help plugins be compatible between various Factions Jars"

    apply(plugin = "java")
    apply(plugin = "java-library")

    // Provision Java 21 all subprojects
    java {
        toolchain.languageVersion.set(JavaLanguageVersion.of(21))
    }

    repositories {
        mavenLocal()
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://repo.luxiouslabs.net/repository/maven-public/")
        maven {
            name = "luxiousFactionsLibs"
            url = uri("https://repo.luxiouslabs.net/repository/luxious-private/")
            credentials {
                username = System.getenv("LUXIOUS_NEXUS_USER")
                password = System.getenv("LUXIOUS_NEXUS_PASS")
            }
        }
        maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        gradlePluginPortal()
    }

    dependencies {
        // Spigot
        compileOnly(project.property("lowestSpigotDep") as String)
        // JetBrains Annotations
        implementation(project.property("jetbrainsDep") as String)

        // Lombok
        compileOnly(project.property("lombokDep") as String)
        annotationProcessor(project.property("lombokDep") as String)
        testAnnotationProcessor(project.property("lombokDep") as String)

        // Vault
        compileOnly("com.github.MilkBowl:VaultAPI:1.7.3")
    }

    // We want UTF-8 for everything
    tasks.withType<JavaCompile> {
        options.encoding = Charsets.UTF_8.name()
    }
}

dependencies {
    // Modules
    shadow(project(":common"))
    shadow(project(":factions-atlas"))
    shadow(project(":factions-jartex"))
    shadow(project(":factions-jerry"))
    shadow(project(":factions-joseph"))
    shadow(project(":factions-mc1.20"))
    shadow(project(":factions-newuuid"))
    shadow(project(":factions-saber"))
    shadow(project(":factions-saberx"))
    shadow(project(":factions-stellar"))
    shadow(project(":factions-uuid"))
}



publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            groupId = rootProject.group.toString()
            artifactId = project.name
            version = rootProject.version.toString()
            from(components["java"])
        }
    }

    repositories {
        maven {
            credentials {
                username = System.getenv("LUXIOUS_NEXUS_USER")
                password = System.getenv("LUXIOUS_NEXUS_PASS")
            }
            // Select URL based on version (if it's a snapshot or not)
            url = if (project.version.toString().endsWith("-SNAPSHOT")) {
                uri("https://repo.luxiouslabs.net/repository/maven-snapshots/")
            }else {
                uri("https://repo.luxiouslabs.net/repository/maven-releases/")
            }
        }
    }
}


tasks {
    publish.get().dependsOn(build)
    build.get().dependsOn(shadowJar)

    shadowJar {
        archiveClassifier.set("")
        configurations = listOf(project.configurations.shadow.get())
    }
}
