package io.kotest.extensions.ktor.openapi

import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.application.hooks.CallSetup
import io.ktor.server.application.hooks.ResponseSent
import io.ktor.server.auth.AuthenticationRouteSelector
import io.ktor.server.request.httpMethod
import io.ktor.server.routing.PathSegmentParameterRouteSelector
import io.ktor.server.routing.Route
import io.ktor.server.routing.RouteSelector
import io.ktor.server.routing.Routing
import io.ktor.util.AttributeKey
import io.ktor.util.pipeline.PipelineContext
import java.nio.file.Path
import java.nio.file.Paths

class OpenApiConfig(
   var path: Path = Paths.get("./openapi.yml"),
   var authentication: Map<String, Authenticator> = emptyMap(),
)

sealed interface Authenticator {
   data class Header(val name: String) : Authenticator
}

val OpenApiKey: AttributeKey<OpenApiConfig> = AttributeKey("OpenApiConfigAttributeKey")

val OpenApi = createApplicationPlugin("OpenApi", createConfiguration = ::OpenApiConfig) {

   val writer = OpenApiGenerator(pluginConfig)
   val traceKey = AttributeKey<Trace>("kotestOpenApiTrace")

//   this.onCall { call ->
//      println(call.isHandled)
//      writer.generate(call.request.uri, call.request.httpMethod)
//      writer.write(this.pluginConfig.path)
//   }

   fun Route.selectors(): List<RouteSelector> {
      return when (val parent = parent) {
         null -> listOf(selector)
         else -> parent.selectors() + selector
      }
   }

   fun Route.params(): List<String> {
      return selectors().filterIsInstance<PathSegmentParameterRouteSelector>().map { it.name }
   }

   fun Route.authentication(): List<String> {
      return selectors().filterIsInstance<AuthenticationRouteSelector>().flatMap { it.names }.filterNotNull()
   }

   environment!!.monitor.subscribe(Routing.RoutingCallStarted) { call ->
      val trace = call.attributes[traceKey]
      trace.path = call.route.parent.toString()
      trace.params = call.route.params()
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
