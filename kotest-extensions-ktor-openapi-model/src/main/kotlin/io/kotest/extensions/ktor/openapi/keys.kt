package io.kotest.extensions.ktor.openapi

import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.util.AttributeKey
import io.ktor.util.pipeline.PipelineContext

val DescriptionKey: AttributeKey<String> = AttributeKey("KotestOpenApiDescriptionKey")
val DeprecatedKey: AttributeKey<Boolean> = AttributeKey("KotestOpenApiDeprecatedKey")

fun PipelineContext<*, ApplicationCall>.description(desc: String) {
   call.attributes.put(DescriptionKey, desc)
}

fun PipelineContext<*, ApplicationCall>.deprecated(deprecated: Boolean) {
   if (deprecated) call.attributes.put(DeprecatedKey, deprecated)
}
