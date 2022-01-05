import net.md_5.specialsource.JarMapping
import net.md_5.specialsource.JarRemapper
import net.md_5.specialsource.provider.JarProvider
import net.md_5.specialsource.provider.JointProvider
import org.gradle.api.tasks.bundling.Jar
import net.md_5.specialsource.Jar as SpecialJar

plugins {
    id("org.jetbrains.dokka") version "1.6.10"
    `maven-publish`
    signing
}


val api = project(":${rootProject.name}-api")

dependencies {
    implementation(api)
}

subprojects {
    configurations {
        create("mojangMapping")
        create("spigotMapping")
    }

    repositories {
        maven("https://libraries.minecraft.net")
        mavenLocal()
    }

    dependencies {
        implementation(api)
        implementation(requireNotNull(parent)) // core

        if (project.name.startsWith("v")) {
            compileOnly("com.mojang:brigadier:1.0.18")

            val nmsVersion = project.name.removePrefix("v")

            // source
            compileOnly("io.papermc.paper:paper-api:$nmsVersion-R0.1-SNAPSHOT")
            compileOnly("io.papermc.paper:paper-mojangapi:$nmsVersion-R0.1-SNAPSHOT")

            // binary
//            if (nmsVersion.startsWith("1.18")) {
//                implementation("io.papermc.paper:paper-server:$nmsVersion-R0.1-SNAPSHOT:mojang-mapped")
//                implementation("org.spigotmc:spigot:$nmsVersion-R0.1-SNAPSHOT:remapped-mojang")
//            } else {
//                compileOnly("io.papermc.paper:paper:$nmsVersion-R0.1-SNAPSHOT:mojang-mapped")
//            }
            compileOnly("org.spigotmc:spigot:$nmsVersion-R0.1-SNAPSHOT:remapped-mojang")
            mojangMapping("org.spigotmc:minecraft-server:$nmsVersion-R0.1-SNAPSHOT:maps-mojang@txt")
            spigotMapping("org.spigotmc:minecraft-server:$nmsVersion-R0.1-SNAPSHOT:maps-spigot@csrg")
        }
    }

    tasks {
        jar {
            doLast {
                fun remap(jarFile: File, outputFile: File, mappingFile: File, reversed: Boolean = false) {
                    val inputJar = SpecialJar.init(jarFile)

                    val mapping = JarMapping()
                    mapping.loadMappings(mappingFile.canonicalPath, reversed, false, null, null)

                    val provider = JointProvider()
                    provider.add(JarProvider(inputJar))
                    mapping.setFallbackInheritanceProvider(provider)

                    val mapper = JarRemapper(mapping)
                    mapper.remapJar(inputJar, outputFile)
                    inputJar.close()
                }

                val archiveFile = archiveFile.get().asFile
                val obfOutput = File(archiveFile.parentFile, "remapped-obf.jar")
                val spigotOutput = File(archiveFile.parentFile, "remapped-spigot.jar")

                val configurations = project.configurations
                val mojangMapping = configurations.named("mojangMapping").get().firstOrNull()
                val spigotMapping = configurations.named("spigotMapping").get().firstOrNull()

                if (mojangMapping != null && spigotMapping != null) {
                    remap(archiveFile, obfOutput, mojangMapping, true)
                    remap(obfOutput, spigotOutput, spigotMapping)

                    spigotOutput.copyTo(archiveFile, true)
                    obfOutput.delete()
                    spigotOutput.delete()
                } else {
                    throw IllegalStateException("Mojang and Spigot mapping should be specified for ${project.path}")
                }
            }
        }
    }
}

tasks {
    jar {
        archiveClassifier.set("core")
    }

    register<Jar>("paperJar") {
        from(sourceSets["main"].output)

        subprojects.forEach {
            val paperJar = it.tasks.jar.get()
            dependsOn(paperJar)
            from(zipTree(paperJar.archiveFile))
        }
    }

    register<Jar>("sourcesJar") {
        archiveClassifier.set("sources")
        (listOf(project) + subprojects).forEach { from(it.sourceSets["main"].allSource) }
    }

    register<Jar>("dokkaJar") {
        archiveClassifier.set("javadoc")
        dependsOn("dokkaHtml")

        from("$buildDir/dokka/html/") {
            include("**")
        }
    }
}

publishing {
    repositories {
        mavenLocal()

        maven {
            name = "debug"
            url = rootProject.uri(".debug/libraries")
        }

        maven {
            name = "central"

            credentials.runCatching {
                val nexusUsername: String by project
                val nexusPassword: String by project
                username = nexusUsername
                password = nexusPassword
            }.onFailure {
                logger.warn("Failed to load nexus credentials, Check the gradle.properties")
            }

            url = uri(
                if ("SNAPSHOT" in version as String) {
                    "https://s01.oss.sonatype.org/content/repositories/snapshots/"
                } else {
                    "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
                }
            )
        }
    }

    publications {
        register<MavenPublication>("core") {
            artifactId = rootProject.name

            from(components["java"])
            artifact(tasks["paperJar"])
            artifact(tasks["sourcesJar"])
            artifact(tasks["dokkaJar"])

            pom {
                name.set(rootProject.name)
                description.set("pom description")
                url.set("https://github.com/monun/${rootProject.name}")

                licenses {
                    license {
                        name.set("GNU General Public License version 3")
                        url.set("https://opensource.org/licenses/GPL-3.0")
                    }
                }

                developers {
                    developer {
                        id.set("monun")
                        name.set("Monun")
                        email.set("monun1010@gmail.com")
                        url.set("https://github.com/monun")
                        roles.addAll("developer")
                        timezone.set("Asia/Seoul")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/monun/${rootProject.name}.git")
                    developerConnection.set("scm:git:ssh://github.com:monun/${rootProject.name}.git")
                    url.set("https://github.com/monun/${rootProject.name}")
                }
            }
        }
    }
}

signing {
    isRequired = true
    sign(tasks.jar.get(), tasks["paperJar"], tasks["sourcesJar"], tasks["dokkaJar"])
    sign(publishing.publications["core"])
}
