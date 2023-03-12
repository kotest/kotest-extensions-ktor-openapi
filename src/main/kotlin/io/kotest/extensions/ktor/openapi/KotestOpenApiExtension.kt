package io.kotest.extensions.ktor.openapi

import io.kotest.core.listeners.AfterProjectListener

/**
 * A Kotest extension which will write out the open-api definition file after all tests have completed.
 * This extension must be installed in your test suite, via project config.
 */
class KotestOpenApiExtension(private val config: OpenApiConfig) : AfterProjectListener {
   override suspend fun afterProject() {
      val writer = OpenApiWriter(config)
      Tracer.getTraces().forEach { writer.addTrace(it) }
      writer.write()
   }
}
