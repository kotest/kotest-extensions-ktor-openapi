package io.kotest.extensions.ktor.openapi

import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.request.httpMethod
import io.ktor.server.request.uri
import io.ktor.util.AttributeKey
import java.nio.file.Path
import java.nio.file.Paths

class OpenApiConfig(
   var path: Path = Paths.get("./openapi.yml")
)

val OpenApiKey: AttributeKey<OpenApiConfig> = AttributeKey("OpenApiConfigAttributeKey")

val OpenApi = createApplicationPlugin("OpenApi", createConfiguration = ::OpenApiConfig) {

   val config: OpenApiConfig = this@createApplicationPlugin.pluginConfig
   application.attributes.put(OpenApiKey, config)
   val writer = OpenApiGenerator()

   this.onCall {
      writer.generate(it.request.uri, it.request.httpMethod)
      writer.write(config.path)
   }
}
