package io.kotest.extensions.ktor.openapi

import io.kotest.core.config.AbstractProjectConfig
import io.kotest.core.extensions.Extension

class KotestProjectConfig : AbstractProjectConfig() {
   override fun extensions(): List<Extension> {
      return listOf(KotestOpenApiExtension(OpenApiConfig()))
   }
}
