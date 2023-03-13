package io.kotest.extensions.ktor.openapi

import io.swagger.v3.oas.models.media.Schema
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.createType
import kotlin.reflect.full.memberProperties
import kotlin.reflect.typeOf

object SwaggerSchemas {
   val string = Schema<String>().apply { type = "string" }
   val integer = Schema<String>().apply { type = "integer" }
   val number = Schema<String>().apply { type = "number" }
   val boolean = Schema<String>().apply { type = "boolean" }
   val obj = Schema<String>().apply { type = "object" }
}

fun KClass<*>.toSchema() = this.createType().toSchema()

fun KType.toSchema(): Schema<*>? {
   return when (this) {
      typeOf<String>() -> SwaggerSchemas.string
      typeOf<Int>() -> SwaggerSchemas.integer
      typeOf<Long>() -> SwaggerSchemas.integer
      typeOf<Float>() -> SwaggerSchemas.number
      typeOf<Double>() -> SwaggerSchemas.number
      typeOf<Boolean>() -> SwaggerSchemas.boolean
      else -> {
         when (val classifier = this.classifier) {
            is KClass<*> -> {
               when (classifier) {

                  List::class -> {
                     val valueType = arguments[0].type?.classifier as KClass<*>
                     val s = Schema<Any>()
                     s.type = "array"
                     s.items = valueType.toSchema()
                     s
                  }

                  Map::class -> {
                     val valueType = arguments[1].type?.classifier as KClass<*>
                     val s = Schema<Any>()
                     s.type = "object"
                     s.additionalProperties = valueType.toSchema()
                     s
                  }

                  String::class -> SwaggerSchemas.string
                  Boolean::class -> SwaggerSchemas.boolean
                  Integer::class -> SwaggerSchemas.integer
                  Long::class -> SwaggerSchemas.integer
                  Float::class -> SwaggerSchemas.number
                  Double::class -> SwaggerSchemas.number

                  else -> {
                     if (classifier.isData) {
                        val schema = Schema<Any>()
                        schema.name = classifier.java.name
                        schema.type = "object"
                        classifier.memberProperties.map { prop ->
                           val propSchema = prop.returnType.toSchema()
                           schema.addProperty(prop.name, propSchema)
                        }
                        schema
                     } else {
                       null
                     }
                  }
               }
            }

            else -> null
         }
      }
   }
}
