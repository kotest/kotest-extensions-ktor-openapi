package io.kotest.extensions.ktor.openapi

import io.ktor.http.HttpStatusCode
import io.ktor.http.content.ByteArrayContent
import io.ktor.http.content.OutgoingContent
import io.ktor.http.content.OutputStreamContent
import io.ktor.http.content.TextContent
import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.application.hooks.CallSetup
import io.ktor.server.application.hooks.ResponseBodyReadyForSend
import io.ktor.server.application.hooks.ResponseSent
import io.ktor.server.request.httpMethod
import io.ktor.server.routing.Routing
import io.ktor.util.AttributeKey
import io.ktor.utils.io.ByteChannel
import io.ktor.utils.io.jvm.javaio.copyTo
import java.io.ByteArrayOutputStream
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
   var serviceTitle: String? = null,
   var serviceVersion: String? = null,
   var serviceDescription: String? = null,
   var contact: String? = null,
   var servers: List<TargetServer> = emptyList(),
   var authentications: Map<String, AuthenticationMethod> = emptyMap(),
)

data class TargetServer(
   val url: String,
   val description: String,
)

class OpenApiPluginConfig(
   // can override the tracer here, used to test this plugin itself
   internal var tracer: Tracer = defaultTracer
)

val KotestOpenApi = createApplicationPlugin("OpenApi", createConfiguration = ::OpenApiPluginConfig) {

   // used to pass the trace object between hooks
   val traceKey = AttributeKey<Trace>("kotestOpenApiTrace")

   // this is called for each registered route
   environment!!.monitor.subscribe(Routing.RoutingCallStarted) { call ->
      val trace = call.attributes[traceKey]
      trace.path = call.route.path()
      trace.pathParameters = call.route.pathParameters()
      trace.authentication = call.route.authentication()
   }

//   on(AuthenticationChecked) {
//   }

   on(CallSetup) { call ->
      call.attributes.put(traceKey, Trace.default("", call.request.httpMethod, ""))
   }

   on(ResponseSent) { call ->
      if (call.response.status() != null && call.response.status() != HttpStatusCode.NotFound) {
         val trace = call.attributes[traceKey]
         trace.status = call.response.status()
         trace.description = call.attributes.getOrNull(DescriptionKey)
         trace.deprecated = call.attributes.getOrNull(DeprecatedKey) ?: false
//         trace.schema = call.attributes.getOrNull(SchemaKey)
         trace.pathParameterExamples = trace.pathParameters.associateWith { call.parameters[it] }
         this@createApplicationPlugin.pluginConfig.tracer.addTrace(trace)
      }
   }

   onCallRespond { call, body ->
      if (body::class.isData) {
         val trace = call.attributes[traceKey]
         trace.schema = body::class
      }
   }

   on(ResponseBodyReadyForSend) { call, content ->
      val trace = call.attributes[traceKey]
      when (content) {

         is TextContent -> {
            trace.contentType = content.contentType
            trace.responseBody = content.text
         }

         is OutgoingContent.ByteArrayContent -> {
            trace.contentType = content.contentType
            trace.responseBody = "<bytes>"
         }

         is OutputStreamContent -> {

            val channel = ByteChannel()
            content.writeTo(channel)

            val baos = ByteArrayOutputStream()
            channel.copyTo(baos)

            trace.contentType = content.contentType
            trace.responseBody = baos.toByteArray().decodeToString()

            transformBodyTo(
               ByteArrayContent(
                  bytes = baos.toByteArray(),
                  contentType = content.contentType,
                  status = content.status
               )
            )
         }

         else -> println(content::class.java)
      }
   }
}
