package io.kotest.extensions.ktor.openapi

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.swagger.v3.oas.models.media.Schema

class SchemaBuilderTest : FunSpec() {
   init {

      test("primitives") {
         String::class.toSchema() shouldBe SwaggerSchemas.string
         Long::class.toSchema() shouldBe SwaggerSchemas.integer
         Int::class.toSchema() shouldBe SwaggerSchemas.integer
         Float::class.toSchema() shouldBe SwaggerSchemas.number
         Double::class.toSchema() shouldBe SwaggerSchemas.number
         Boolean::class.toSchema() shouldBe SwaggerSchemas.boolean
      }

      test("basic property types in data classes") {
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

      test("support nested types") {
         data class Bar(val c: String, val d: Boolean)
         data class Foo(val a: String, val b: Bar)

         val bar = Schema<Foo>()
         bar.type = "object"
         bar.addProperty("c", SwaggerSchemas.string)
         bar.addProperty("d", SwaggerSchemas.boolean)
         bar.name = Bar::class.java.name

         val foo = Schema<Foo>()
         foo.type = "object"
         foo.addProperty("a", SwaggerSchemas.string)
         foo.addProperty("b", bar)
         foo.name = Bar::class.java.name

         Foo::class.toSchema() shouldBe foo
      }

      test("support primitive lists") {
         data class Foo(val a: List<String>)

         val listSchema = Schema<Any>()
         listSchema.type = "array"
         listSchema.items = SwaggerSchemas.string

         val expected = Schema<Foo>()
         expected.type = "object"
         expected.addProperty("a", listSchema)
         expected.name = Foo::class.java.name
         Foo::class.toSchema() shouldBe expected
      }

      test("support complex lists") {
         data class Bar(val b: Boolean)
         data class Foo(val a: List<Bar>)

         val listSchema = Schema<Any>()
         listSchema.type = "array"
         listSchema.items = Bar::class.toSchema()

         val expected = Schema<Foo>()
         expected.type = "object"
         expected.addProperty("a", listSchema)
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
         data class Bar(val b: Boolean, val c: String)
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
