package io.kotest.extensions.ktor.openapi

import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode

/**
 * The tracer is used to collect individual route traces
 */
class Tracer {

   private val traces = mutableListOf<Trace>()

   fun addTrace(trace: Trace) {
      this.traces.add(trace)
   }

   fun getTraces(): List<Trace> {
      return traces.toList()
   }
}

val defaultTracer = Tracer()

/**
 *
 * @pathParameters the path parameter names.
 * @pathParameterExamples ps path parameter values used
 */
data class Trace(
   val method: HttpMethod,
   var path: String,
   var pathParameters: List<String>,
   var authentications: List<String>,
   var status: HttpStatusCode?,
   var contentType: ContentType? = null,
   var responseBody: String? = null,
   var description: String?,
   var pathParameterExamples: Map<String, String?>,
) {
   companion object {
      fun default(method: HttpMethod, path: String) =
         Trace(
            method = method,
            path = path,
            pathParameters = emptyList(),
            authentications = emptyList(),
            status = null,
            contentType = null,
            responseBody = null,
            description = null,
            pathParameterExamples = emptyMap()
         )
   }
}
