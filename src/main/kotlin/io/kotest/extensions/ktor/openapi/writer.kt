package io.kotest.extensions.ktor.openapi

import io.ktor.http.HttpMethod
import io.swagger.v3.core.util.Yaml
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.media.Content
import io.swagger.v3.oas.models.media.MediaType
import io.swagger.v3.oas.models.parameters.Parameter
import io.swagger.v3.oas.models.responses.ApiResponse
import io.swagger.v3.oas.models.responses.ApiResponses
import java.nio.file.Files
import java.nio.file.Path

class OpenApiGenerator {

   private val openapi = OpenAPI().also {
      it.info = Info()
      it.info.description = "my-service"
      it.info.title = "my-service"
      it.info.version = "1.0.0"
   }

   fun addTrace(trace: Trace) {
      val item = PathItem()
      val op = Operation()
      op.description = trace.description
      op.responses = ApiResponses()
      trace.params.forEach {
         val p = Parameter()
         p.name = it
         p.`in` = "path"
         p.example = trace.ps[it]
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
      val resp = ApiResponse()
      resp.description = trace.response!!.description
      resp.content = Content()
      val mediaType = MediaType()
      mediaType.example = """{"name":"foo"}"""
      resp.content.addMediaType("application/json", mediaType)
      op.responses.addApiResponse(trace.response!!.value.toString(), resp)
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
