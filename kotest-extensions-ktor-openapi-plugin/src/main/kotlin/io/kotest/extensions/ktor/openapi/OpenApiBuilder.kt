package io.kotest.extensions.ktor.openapi

import io.ktor.http.HttpMethod
import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.PathItem
import io.swagger.v3.oas.models.examples.Example
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.media.Content
import io.swagger.v3.oas.models.media.MediaType
import io.swagger.v3.oas.models.media.Schema
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
         val scheme = SecurityScheme()
         when (val auth = authenticator.value) {

            is AuthenticationMethod.Header -> {
               scheme.type = SecurityScheme.Type.APIKEY
               scheme.`in` = SecurityScheme.In.HEADER
               scheme.name = auth.name
            }

            is AuthenticationMethod.Bearer -> {
               scheme.type = SecurityScheme.Type.HTTP
               scheme.scheme = "bearer"
               scheme.name = auth.name
            }
         }
         api.components.addSecuritySchemes(authenticator.key, scheme)
      }
   }

   /**
    * Adds traces grouped by an endpoint path
    */
   fun addTraces(path: String, traces: List<Trace>) {

      val item = PathItem()
      openapi.path(path, item)

      // add all schemas at the top level
      traces.mapNotNull { it.schema }.forEach { schema ->
         openapi.components.addSchemas(schema.java.name, schema.toSchema())
      }

      // each http method is an operation
      traces.groupBy { it.method }.forEach { (method, tracesByMethod) ->

         val op = Operation()
         op.description = tracesByMethod.firstNotNullOfOrNull { it.description }
         op.deprecated = tracesByMethod.any { it.deprecated }
         op.responses = ApiResponses()
         op.parameters = mutableListOf()

         when (method) {
            HttpMethod.Delete -> item.delete = op
            HttpMethod.Get -> item.get = op
            HttpMethod.Head -> item.head = op
            HttpMethod.Options -> item.options = op
            HttpMethod.Patch -> item.patch = op
            HttpMethod.Post -> item.post = op
            HttpMethod.Put -> item.put = op
         }

         tracesByMethod.groupBy { it.status }.forEach { (status, tracesByStatus) ->
            if (status != null) {

               // open api uses one ApiResponse per status code
               val resp = ApiResponse()
               resp.description = status.description
               op.responses.addApiResponse(status.value.toString(), resp)

               tracesByStatus.groupBy { it.contentType }.forEach { (contentType, tracesByContentType) ->
                  if (contentType != null) {

                     val mediaType = MediaType()
                     tracesByContentType.firstNotNullOfOrNull { it.schema }?.let {
                        mediaType.schema = Schema<Any>()
                        mediaType.schema.`$ref` = "#/components/schemas/" + it.java.name
                     }

                     // for each content type that is the same, they are added as multiple examples
                     // to the same MediaType in the response content
                     val bodies = tracesByContentType.mapNotNull { it.responseBody }.distinct()
                     if (bodies.size == 1) {
                        mediaType.example = bodies.first()
                     } else if (bodies.size > 1) {
                        bodies.withIndex().forEach { (index, value) ->
                           mediaType.addExamples(
                              "Example ${index + 1}",
                              Example().value(value)
                           )
                        }
                     }

                     if (resp.content == null) resp.content = Content()
                     resp.content.addMediaType(contentType.toString(), mediaType)
                  }
               }
            }
         }

         // path parameters must be consistent across all traces since the endpoint is the same,
         // so we can add just once
         tracesByMethod.first().pathParameters.forEach {
            op.addParametersItem(
               Parameter()
                  .name(it)
                  .`in`("path")
            )
         }

         tracesByMethod.forEach { trace ->

            trace.pathParameterExamples.forEach { (param, example) ->
               val p = op.parameters.find { it.name == param && it.`in` == "path" }
               if (p != null) {
                  val examples = p.examples ?: emptyMap()
                  val key = "Example " + (examples.size + 1)
                  p.addExample(key, Example().value(example))
               }
            }
         }

         tracesByMethod.flatMap { it.authentication }.distinct().forEach {
            val sec = SecurityRequirement()
            sec.addList(it)
            op.addSecurityItem(sec)
         }
      }
   }
}
