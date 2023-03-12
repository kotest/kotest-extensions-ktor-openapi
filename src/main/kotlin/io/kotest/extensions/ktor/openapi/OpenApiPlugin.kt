package io.kotest.extensions.ktor.openapi

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
   var path: Path = Paths.get("./myopenapi.yml"),
   var serviceTitle: String? = null,
   var serviceVersion: String? = null,
   var serviceDescription: String? = null,
   var authentications: Map<String, Authenticator> = emptyMap(),
)

val OpenApiKey: AttributeKey<OpenApiConfig> = AttributeKey("OpenApiConfigAttributeKey")

val KotestOpenApi = createApplicationPlugin("OpenApi") {

   val traceKey = AttributeKey<Trace>("kotestOpenApiTrace")

//   this.onCall { call ->
//      println(call.isHandled)
//      writer.generate(call.request.uri, call.request.httpMethod)
//      writer.write(this.pluginConfig.path)
//   }

   // this is called for each registeted route
   environment!!.monitor.subscribe(Routing.RoutingCallStarted) { call ->
      val trace = call.attributes[traceKey]
      trace.path = call.route.path()
      trace.pathParameters = call.route.pathParameters()
      trace.authentications = call.route.authentication()
   }

   on(CallSetup) { call ->
      call.attributes.put(
         traceKey, Trace(call.request.httpMethod, "", emptyList(), emptyList(), null, null, emptyMap())
      )
   }

   on(ResponseSent) { call ->
      if (call.response.status() != null && call.response.status() != HttpStatusCode.NotFound) {
         val trace = call.attributes[traceKey]
         trace.response = call.response.status()
         trace.description = call.attributes.getOrNull(DescriptionKey)
         trace.pathParameterExamples = trace.pathParameters.associateWith { call.parameters[it] }
         Tracer.addTrace(trace)
      }
   }
}

val DescriptionKey: AttributeKey<String> = AttributeKey("KotestOpenApiDescriptionKey")

fun PipelineContext<*, ApplicationCall>.description(desc: String) {
   call.attributes.put(DescriptionKey, desc)
}
