package io.kotest.extensions.ktor.openapi

import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.util.AttributeKey
import io.ktor.util.pipeline.PipelineContext
import kotlin.reflect.KClass

val DescriptionKey: AttributeKey<String> = AttributeKey("KotestOpenApiDescriptionKey")
val DeprecatedKey: AttributeKey<Boolean> = AttributeKey("KotestOpenApiDeprecatedKey")
val SchemaKey: AttributeKey<KClass<*>> = AttributeKey("KotestOpenApiSchemaKey")

fun PipelineContext<*, ApplicationCall>.description(desc: String) {
   call.attributes.put(DescriptionKey, desc)
}

fun PipelineContext<*, ApplicationCall>.deprecated(deprecated: Boolean) {
   if (deprecated) call.attributes.put(DeprecatedKey, deprecated)
}

inline fun <reified T : Any> PipelineContext<*, ApplicationCall>.schema() {
   call.attributes.put(SchemaKey, T::class)
}
