package io.kotest.extensions.ktor.openapi

import io.ktor.http.HttpMethod
import io.swagger.v3.core.util.Yaml
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.PathItem
import java.nio.file.Files
import java.nio.file.Path

class OpenApiGenerator {

   private val openapi = OpenAPI()

   fun generate(uri: String, method: HttpMethod) {
      val item = PathItem()
      when (method) {
         HttpMethod.Delete -> item.delete = Operation()
         HttpMethod.Get -> item.get = Operation()
         HttpMethod.Head -> item.head = Operation()
         HttpMethod.Options -> item.options = Operation()
         HttpMethod.Patch -> item.patch = Operation()
         HttpMethod.Post -> item.post = Operation()
         HttpMethod.Put -> item.put = Operation()
      }
      openapi.path(uri, item)
   }

   fun write(path: Path) {
      val factory = Yaml.mapper().factory
//      factory.disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER)
//         .enable(YAMLGenerator.Feature.MINIMIZE_QUOTES)
//         .enable(YAMLGenerator.Feature.ALWAYS_QUOTE_NUMBERS_AS_STRINGS)
//
      val yaml = Yaml.pretty().writeValueAsString(openapi)
      Files.write(path, yaml.encodeToByteArray())
   }
}
