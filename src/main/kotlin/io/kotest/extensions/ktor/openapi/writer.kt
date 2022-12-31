package io.kotest.extensions.ktor.openapi

import io.ktor.http.HttpMethod
import io.swagger.v3.core.util.Yaml
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.media.Content
import io.swagger.v3.oas.models.media.MediaType
import io.swagger.v3.oas.models.parameters.Parameter
import io.swagger.v3.oas.models.responses.ApiResponse
import io.swagger.v3.oas.models.responses.ApiResponses
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import java.nio.file.Files
import java.nio.file.Path

class OpenApiGenerator(config: OpenApiConfig) {

   private val openapi = OpenAPI().also { api ->
      api.info = Info()
      api.info.description = "my-service"
      api.info.title = "my-service"
      api.info.version = "1.0.0"
      api.components = Components()
      config.authentication.forEach { authenticator ->
         api.components.addSecuritySchemes(authenticator.key, SecurityScheme().also {
            when (val auth = authenticator.value) {
               is Authenticator.Header -> {
                  it.type = SecurityScheme.Type.APIKEY
                  it.`in` = SecurityScheme.In.HEADER
                  it.name = auth.name
               }
            }
         })
      }
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

      if (trace.authentications.isNotEmpty()) {
         trace.authentications.forEach {
            val sec = SecurityRequirement()
            sec.addList(it)
            op.addSecurityItem(sec)
         }
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
