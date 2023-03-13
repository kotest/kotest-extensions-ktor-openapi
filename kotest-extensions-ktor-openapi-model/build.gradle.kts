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
   api("io.ktor:ktor-server-core:2.2.4")
}

tasks.withType<KotlinCompile> {
   kotlinOptions.jvmTarget = "11"
}
