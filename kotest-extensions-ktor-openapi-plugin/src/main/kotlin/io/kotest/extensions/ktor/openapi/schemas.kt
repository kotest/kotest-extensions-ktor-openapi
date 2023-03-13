package io.kotest.extensions.ktor.openapi

import io.swagger.v3.oas.models.media.Schema
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties
import kotlin.reflect.typeOf

object SwaggerSchemas {
   val string = Schema<String>().apply { type = "string" }
   val integer = Schema<String>().apply { type = "integer" }
   val number = Schema<String>().apply { type = "number" }
   val boolean = Schema<String>().apply { type = "boolean" }
   val obj = Schema<String>().apply { type = "object" }
}

fun KClass<*>.toSchema(): Schema<Any> {
   require(isData)
   val schema = Schema<Any>()
   schema.name = this.java.name
   schema.type = "object"
   memberProperties.map { prop ->
      val propSchema = when (prop.returnType) {
         typeOf<String>() -> SwaggerSchemas.string
         typeOf<Int>() -> SwaggerSchemas.integer
         typeOf<Long>() -> SwaggerSchemas.integer
         typeOf<Float>() -> SwaggerSchemas.number
         typeOf<Double>() -> SwaggerSchemas.number
         typeOf<Boolean>() -> SwaggerSchemas.boolean
         else -> null
      }
      schema.addProperty(prop.name, propSchema)
   }
   return schema
}
