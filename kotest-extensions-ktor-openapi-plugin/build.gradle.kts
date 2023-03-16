import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
   id("kotest-publishing-conventions")
   kotlin("jvm") version "1.6.21"
}

group = "io.kotest.extensions"
version = Ci.version

repositories {
   mavenLocal()
   mavenCentral()
   maven {
      url = uri("https://oss.sonatype.org/content/repositories/snapshots")
   }
}

dependencies {
   api(projects.kotestExtensionsKtorOpenapiModel)
   implementation("io.ktor:ktor-server-core:2.2.4")
   implementation("io.ktor:ktor-server-auth:2.2.4")
   implementation("io.swagger.parser.v3:swagger-parser:2.1.12")
   implementation("io.kotest:kotest-framework-api:5.5.4")
   testImplementation("io.ktor:ktor-client-apache:2.2.4")
   testImplementation("io.kotest:kotest-runner-junit5:5.5.5")
   testImplementation("io.kotest:kotest-assertions-core:5.5.5")
   testImplementation("io.ktor:ktor-server-test-host:2.2.4")
   testImplementation("io.ktor:ktor-serialization-jackson:2.2.3")
   testImplementation("io.ktor:ktor-server-content-negotiation:2.2.3")
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
