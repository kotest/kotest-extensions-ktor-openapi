package io.kotest.extensions.ktor.openapi

import io.ktor.http.HttpMethod
import io.swagger.v3.core.util.Yaml
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.parameters.Parameter
import java.nio.file.Files
import java.nio.file.Path

class OpenApiGenerator {

   private val openapi = OpenAPI()

   fun generate(trace: Trace) {
      val item = PathItem()
      val op = Operation()
      trace.params.forEach {
         val p = Parameter()
         p.name = it
         op.addParametersItem(p)
      }
      when (trace.method) {
         HttpMethod.Delete -> item.delete = op
         HttpMethod.Get -> item.get = op
         HttpMethod.Head -> item.head = op
         HttpMethod.Options -> item.options = op
         HttpMethod.Patch -> item.patch = op
         HttpMethod.Post -> item.post = op
         HttpMethod.Put -> item.put = op
      }
      openapi.path(trace.path, item)
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
