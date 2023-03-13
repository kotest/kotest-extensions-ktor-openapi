enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
rootProject.name = "kotest-extensions-ktor-openapi"

pluginManagement {
  repositories {
    mavenCentral()
    gradlePluginPortal()
  }
}

include("kotest-extensions-ktor-openapi-model")
include("kotest-extensions-ktor-openapi-plugin")
