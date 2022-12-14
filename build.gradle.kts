import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
   `java-library`
   signing
   `maven-publish`
   kotlin("jvm") version "1.6.21"
}

group = "io.kotest.extensions"
version = Ci.version

repositories {
   mavenCentral()
   maven {
      url = uri("https://oss.sonatype.org/content/repositories/snapshots")
   }
}

dependencies {
   implementation("io.ktor:ktor-server-core:2.1.2")
   implementation("io.swagger.parser.v3:swagger-parser:2.1.3")
   testImplementation("io.ktor:ktor-client-apache:2.1.2")
   testImplementation("io.kotest:kotest-runner-junit5:5.5.1")
   testImplementation("io.kotest:kotest-assertions-core:5.5.1")
   testImplementation("io.ktor:ktor-server-test-host:2.1.2")
}

tasks.test {
   useJUnitPlatform()
   testLogging {
      showExceptions = true
      showStandardStreams = true
      exceptionFormat = TestExceptionFormat.FULL
   }
}

tasks.withType<KotlinCompile> {
   kotlinOptions.jvmTarget = "11"
}

val signingKey: String? by project
val signingPassword: String? by project

val publications: PublicationContainer = (extensions.getByName("publishing") as PublishingExtension).publications

signing {
   useGpgCmd()
   if (signingKey != null && signingPassword != null) {
      @Suppress("UnstableApiUsage")
      useInMemoryPgpKeys(signingKey, signingPassword)
   }
   if (Ci.isRelease) {
      sign(publications)
   }
}

java {
   withJavadocJar()
   withSourcesJar()
}

publishing {
   repositories {
      maven {
         val releasesRepoUrl = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
         val snapshotsRepoUrl = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
         name = "deploy"
         url = if (Ci.isRelease) releasesRepoUrl else snapshotsRepoUrl
         credentials {
            username = System.getenv("OSSRH_USERNAME") ?: ""
            password = System.getenv("OSSRH_PASSWORD") ?: ""
         }
      }
   }

   publications {
      register("mavenJava", MavenPublication::class) {
         from(components["java"])
         pom {
            name.set("kotest-extensions-ktor-openapi")
            description.set("Kotest extension for generating open-API docs")
            url.set("https://www.github.com/kotest/kotest-extensions-ktor-openapi")

            scm {
               connection.set("scm:git:http://www.github.com/kotest/kotest-extensions-ktor-openapi")
               developerConnection.set("scm:git:http://github.com/sksamuel")
               url.set("https://www.github.com/kotest/kotest-extensions-ktor-openapi")
            }

            licenses {
               license {
                  name.set("The Apache 2.0 License")
                  url.set("https://opensource.org/licenses/Apache-2.0")
               }
            }

            developers {
               developer {
                  id.set("sksamuel")
                  name.set("Stephen Samuel")
                  email.set("sam@sksamuel.com")
               }
            }
         }
      }
   }
}
