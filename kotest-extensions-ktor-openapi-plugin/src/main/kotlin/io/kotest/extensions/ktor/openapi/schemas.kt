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

fun KClass<*>.toSchema(): Schema<*> {
   val builder = SchemaRegistry()
   builder.register(this)
   return builder.schemas[this.java.name]!!
}

class SchemaRegistry {

   val schemas = mutableMapOf<String, Schema<*>>()

   fun register(kclass: KClass<*>): Schema<*> = schema(kclass.createType())

   private fun schema(type: KType): Schema<*> {
      return when (type) {
         typeOf<String>() -> SwaggerSchemas.string
         typeOf<Byte>() -> SwaggerSchemas.integer
         typeOf<Short>() -> SwaggerSchemas.integer
         typeOf<Int>() -> SwaggerSchemas.integer
         typeOf<Long>() -> SwaggerSchemas.integer
         typeOf<Float>() -> SwaggerSchemas.number
         typeOf<Double>() -> SwaggerSchemas.number
         typeOf<Boolean>() -> SwaggerSchemas.boolean
         else -> {
            when (val classifier = type.classifier) {
               is KClass<*> -> {
                  when {


                     classifier == String::class -> SwaggerSchemas.string
                     classifier == Byte::class -> SwaggerSchemas.integer
                     classifier == Short::class -> SwaggerSchemas.integer
                     classifier == Int::class -> SwaggerSchemas.integer
                     classifier == Long::class -> SwaggerSchemas.integer
                     classifier == Float::class -> SwaggerSchemas.number
                     classifier == Double::class -> SwaggerSchemas.number
                     classifier == Boolean::class -> SwaggerSchemas.boolean

                     classifier == List::class -> buildLists(type)
                     classifier == Set::class -> buildLists(type)

                     classifier == Map::class -> buildMaps(type)
                     classifier.isData -> {

                        // need to put object first, then build it, to stop stack overflow
                        if (!schemas.containsKey(classifier.java.name)) {
                           val schema = Schema<Any>()
                           schemas[classifier.java.name] = schema
                           buildData(classifier, schema)
                        }

                        val ref = Schema<Any>()
                        ref.`$ref` = classifier.ref()
                        ref
                     }

                     classifier.isSealed -> {

                        // need to put object first, then build it, to stop stack overflow
                        if (!schemas.containsKey(classifier.java.name)) {
                           val schema = Schema<Any>()
                           schemas[classifier.java.name] = schema
                           buildSealed(classifier, schema)
                        }

                        val ref = Schema<Any>()
                        ref.`$ref` = classifier.ref()
                        ref
                     }

                     classifier.objectInstance != null -> {

                        // need to put object first, then build it, to stop stack overflow
                        if (!schemas.containsKey(classifier.java.name)) {
                           val schema = Schema<Any>()
                           schema.type = "object"
                           schema.name = classifier.java.name
                           schemas[classifier.java.name] = schema
                        }

                        val ref = Schema<Any>()
                        ref.`$ref` = classifier.ref()
                        ref
                     }

                     else -> throw UnsupportedOperationException("Unsupported kclass $classifier")
                  }
               }

               else -> throw UnsupportedOperationException("Unsupported classifier $classifier")
            }
         }
      }
   }

   internal fun buildData(kclass: KClass<*>, schema: Schema<Any>): Schema<Any> {
      schema.name = kclass.java.name
      schema.type = "object"
      kclass.memberProperties.map { prop ->
         val propSchema = schema(prop.returnType)
         schema.addProperty(prop.name, propSchema)
      }
      return schema
   }

   internal fun buildSealed(kclass: KClass<*>, schema: Schema<Any>): Schema<Any> {
      val subschemas = kclass.sealedSubclasses.map { register(it) }
      schema.name = kclass.java.name
      schema.type = "object"
      schema.anyOf(subschemas)
      return schema
   }

   internal fun buildLists(type: KType): Schema<Any> {
      val valueType = schema(type.arguments[0].type!!)
      val schema = Schema<Any>()
      schema.type = "array"
      schema.items = valueType
      return schema
   }

   internal fun buildMaps(type: KType): Schema<Any> {
      val valueType = schema(type.arguments[1].type!!)
      val schema = Schema<Any>()
      schema.type = "object"
      schema.additionalProperties = valueType
      return schema
   }
}

internal fun KClass<*>.ref() = "#/components/schemas/" + java.name
internal fun KClass<*>.schema(): Schema<Any> {
   val schema = Schema<Any>()
   schema.`$ref` = this.ref()
   return schema
}

