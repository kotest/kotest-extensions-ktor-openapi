package io.kotest.extensions.ktor.openapi

import io.kotest.core.listeners.AfterProjectListener

class KotestOpenApiExtension(private val config: OpenApiConfig) : AfterProjectListener {
   override suspend fun afterProject() {
      val writer = OpenApiWriter(config)
      Tracer.getTraces().forEach { writer.addTrace(it) }
      writer.write(config.path)
   }
}
