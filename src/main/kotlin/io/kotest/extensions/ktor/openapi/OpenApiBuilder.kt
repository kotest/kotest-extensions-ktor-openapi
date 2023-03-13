package io.kotest.extensions.ktor.openapi

import io.ktor.http.HttpMethod
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

class OpenApiBuilder(private val config: OpenApiConfig) {

   val openapi = OpenAPI().also { api ->
      api.info = Info()
      api.info.description = config.serviceDescription
      api.info.title = config.serviceTitle ?: "service-title"
      api.info.version = config.serviceVersion ?: "0.0.0"
      api.components = Components()
      config.authentications.forEach { authenticator ->
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

      trace.pathParameters.forEach {
         val p = Parameter()
         p.name = it
         p.`in` = "path"
         p.example = trace.pathParameterExamples[it]
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


      trace.status?.let { status ->

         val resp = ApiResponse()
         resp.description = status.description
         op.responses.addApiResponse(status.value.toString(), resp)

         trace.contentType?.let { contentType ->
            trace.responseBody?.let { body ->

               val mediaType = MediaType()
               mediaType.example = body

               resp.content = Content()
               resp.content.addMediaType(contentType.toString(), mediaType)
            }
         }
      }

      trace.authentications.forEach {
         val sec = SecurityRequirement()
         sec.addList(it)
         op.addSecurityItem(sec)
      }

      openapi.path(trace.path, item)
   }
}
