package io.kotest.extensions.ktor.openapi

import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.application.hooks.CallSetup
import io.ktor.server.application.hooks.ResponseSent
import io.ktor.server.request.httpMethod
import io.ktor.server.routing.PathSegmentParameterRouteSelector
import io.ktor.server.routing.Route
import io.ktor.server.routing.Routing
import io.ktor.util.AttributeKey
import java.nio.file.Path
import java.nio.file.Paths

class OpenApiConfig(
   var path: Path = Paths.get("./openapi.yml")
)

val OpenApiKey: AttributeKey<OpenApiConfig> = AttributeKey("OpenApiConfigAttributeKey")

val OpenApi = createApplicationPlugin("OpenApi", createConfiguration = ::OpenApiConfig) {

   val writer = OpenApiGenerator()
   val traceKey = AttributeKey<Trace>("kotestOpenApiTrace")

//   this.onCall { call ->
//      println(call.isHandled)
//      writer.generate(call.request.uri, call.request.httpMethod)
//      writer.write(this.pluginConfig.path)
//   }

   fun Route.params(): List<String> {
      val params = parent?.params() ?: emptyList()
      return when (val s = selector) {
         is PathSegmentParameterRouteSelector -> params + s.name
         else -> params
      }
   }

   environment!!.monitor.subscribe(Routing.RoutingCallStarted) { call ->
      val trace = call.attributes[traceKey]
      trace.path = call.route.parent.toString()
      trace.params = call.route.params()
   }

   on(CallSetup) { call ->
      call.attributes.put(traceKey, Trace(null, emptyList(), call.request.httpMethod))
   }

   on(ResponseSent) { call ->
      if (call.response.status() != null && call.response.status() != HttpStatusCode.NotFound) {
         val trace = call.attributes[traceKey]
         writer.generate(trace)
         writer.write(this.pluginConfig.path)
      }
   }
}

data class Trace(
   var path: String?,
   var params: List<String>,
   val method: HttpMethod,
)