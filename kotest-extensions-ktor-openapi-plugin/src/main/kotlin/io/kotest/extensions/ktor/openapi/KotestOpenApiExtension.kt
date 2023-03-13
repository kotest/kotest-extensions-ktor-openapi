package io.kotest.extensions.ktor.openapi

import io.kotest.core.listeners.AfterProjectListener

/**
 * A Kotest extension which will write out the open-api definition file after all tests have completed.
 * This extension must be installed in your test suite, via project config.
 */
class KotestOpenApiExtension(private val config: OpenApiConfig) : AfterProjectListener {
   override suspend fun afterProject() {
      val builder = OpenApiBuilder(config)
      defaultTracer.getTraces().groupBy { it.path }.forEach { (path, traces) -> builder.addTraces(path, traces) }
      OpenApiWriter(config.path).write(builder)
   }
}
