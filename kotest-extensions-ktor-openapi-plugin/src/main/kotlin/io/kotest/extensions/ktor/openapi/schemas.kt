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

fun KClass<*>.toSchema() = createSchema(this.createType(), mutableMapOf())

fun createSchema(type: KType, schemas: MutableMap<String, Schema<*>>): Schema<*>? {
   return when (type) {
      typeOf<String>() -> SwaggerSchemas.string
      typeOf<Int>() -> SwaggerSchemas.integer
      typeOf<Long>() -> SwaggerSchemas.integer
      typeOf<Float>() -> SwaggerSchemas.number
      typeOf<Double>() -> SwaggerSchemas.number
      typeOf<Boolean>() -> SwaggerSchemas.boolean
      else -> {
         when (val classifier = type.classifier) {
            is KClass<*> -> {
               val seen = schemas[classifier.java.name]
               if (seen != null) {
                  val s = Schema<Any>()
                  s.`$ref` = "#/components/schemas/" + classifier.java.name
                  s
               } else when (classifier) {
                  List::class -> {
                     val valueType = type.arguments[0].type?.classifier as KClass<*>
                     val s = Schema<Any>()
                     s.type = "array"
                     s.items = createSchema(valueType.createType(), schemas)
                     s
                  }

                  Map::class -> {
                     val valueType = type.arguments[1].type?.classifier as KClass<*>
                     val s = Schema<Any>()
                     s.type = "object"
                     s.additionalProperties = createSchema(valueType.createType(), schemas)
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
                        schemas[classifier.java.name] = schema
                        classifier.memberProperties.map { prop ->
                           val propSchema = createSchema(prop.returnType, schemas)
                           schema.addProperty(prop.name, propSchema)
                        }
                        schema
                     } else if (classifier.isSealed) {
                        val subschemas = classifier.sealedSubclasses.map {
                           createSchema(it.createType(), schemas)
                        }
                        val schema = Schema<Any>()
                        schema.name = classifier.java.name
                        schema.type = "object"
                        schema.anyOf(subschemas)
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
