package io.kotest.extensions.ktor.openapi

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.swagger.v3.oas.models.media.Schema

class SchemaBuilderTest : FunSpec() {
   init {

      test("basic property types") {
         data class Foo(val a: String, val b: Boolean, val c: Int, val d: Long, val e: Float, val f: Double)

         val expected = Schema<Foo>()
         expected.type = "object"
         expected.addProperty("a", SwaggerSchemas.string)
         expected.addProperty("b", SwaggerSchemas.boolean)
         expected.addProperty("c", SwaggerSchemas.integer)
         expected.addProperty("d", SwaggerSchemas.integer)
         expected.addProperty("e", SwaggerSchemas.number)
         expected.addProperty("f", SwaggerSchemas.number)
         expected.name = Foo::class.java.name
         Foo::class.toSchema() shouldBe expected
      }

      test("support primitive maps") {
         data class Foo(val a: Map<String, String>)

         val mapSchema = Schema<Any>()
         mapSchema.type = "object"
         mapSchema.additionalProperties = SwaggerSchemas.string

         val expected = Schema<Foo>()
         expected.type = "object"
         expected.addProperty("a", mapSchema)
         expected.name = Foo::class.java.name
         Foo::class.toSchema() shouldBe expected
      }

      test("support complex maps") {
         data class Bar(val b: Boolean)
         data class Foo(val a: Map<String, Bar>)

         val mapSchema = Schema<Any>()
         mapSchema.type = "object"
         mapSchema.additionalProperties = Bar::class.toSchema()

         val expected = Schema<Foo>()
         expected.type = "object"
         expected.addProperty("a", mapSchema)
         expected.name = Foo::class.java.name
         Foo::class.toSchema() shouldBe expected
      }
   }
}
