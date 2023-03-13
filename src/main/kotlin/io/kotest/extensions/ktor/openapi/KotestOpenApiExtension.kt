package io.kotest.extensions.ktor.openapi

import io.kotest.core.listeners.AfterProjectListener

/**
 * A Kotest extension which will write out the open-api definition file after all tests have completed.
 * This extension must be installed in your test suite, via project config.
 */
class KotestOpenApiExtension(private val config: OpenApiConfig) : AfterProjectListener {
   override suspend fun afterProject() {
      val builder = OpenApiBuilder(config)
      defaultTracer.getTraces().forEach { builder.addTrace(it) }
      OpenApiWriter(config.path).write(builder)
   }
}
