package io.kotest.extensions.ktor.openapi

import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.application.hooks.CallSetup
import io.ktor.server.application.hooks.ResponseSent
import io.ktor.server.request.httpMethod
import io.ktor.server.routing.Routing
import io.ktor.util.AttributeKey
import io.ktor.util.pipeline.PipelineContext
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Config class used by the plugin.
 *
 * Ktor does not provide a means to have access to authentication implementation,
 * so we must add them here.
 */
class OpenApiConfig(
   var path: Path = Paths.get("./openapi.yml"),
   var authentication: Map<String, Authenticator> = emptyMap(),
)

val OpenApiKey: AttributeKey<OpenApiConfig> = AttributeKey("OpenApiConfigAttributeKey")

val KotestOpenApi = createApplicationPlugin("OpenApi", createConfiguration = ::OpenApiConfig) {

   val writer = OpenApiGenerator(pluginConfig)
   val traceKey = AttributeKey<Trace>("kotestOpenApiTrace")

//   this.onCall { call ->
//      println(call.isHandled)
//      writer.generate(call.request.uri, call.request.httpMethod)
//      writer.write(this.pluginConfig.path)
//   }

   environment!!.monitor.subscribe(Routing.RoutingCallStarted) { call ->
      val trace = call.attributes[traceKey]
      trace.path = call.route.path()
      trace.params = call.route.pathParameters()
      trace.authentications = call.route.authentication()
   }

   on(CallSetup) { call ->
      call.attributes.put(
         traceKey, Trace(call.request.httpMethod, null, emptyList(), emptyList(), null, null, emptyMap())
      )
   }

   on(ResponseSent) { call ->
      if (call.response.status() != null && call.response.status() != HttpStatusCode.NotFound) {
         val trace = call.attributes[traceKey]
         trace.response = call.response.status()
         trace.description = call.attributes.getOrNull(DescriptionKey)
         trace.ps = trace.params.associateWith { call.parameters[it] }
         writer.addTrace(trace)
         writer.write(this.pluginConfig.path)
      }
   }
}

val DescriptionKey: AttributeKey<String> = AttributeKey("KotestOpenApiDescriptionKey")

fun PipelineContext<*, ApplicationCall>.description(desc: String) {
   call.attributes.put(DescriptionKey, desc)
}

data class Trace(
   val method: HttpMethod,
   var path: String?,
   var params: List<String>,
   var authentications: List<String>,
   var response: HttpStatusCode?,
   var description: String?,
   var ps: Map<String, String?>,
)
