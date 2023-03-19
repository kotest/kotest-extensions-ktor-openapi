package io.kotest.extensions.ktor.openapi

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.swagger.v3.oas.models.media.Schema

class SchemaBuilderTest : FunSpec() {
   init {

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
         foo.addProperty("b", Bar::class.schema())
         foo.name = Foo::class.java.name

         Foo::class.toSchema() shouldBe foo
      }

      test("support sealed interfaces") {
         data class Foo(val a: String, val b: Wibble)

         val bar = Schema<Wibble.Bar>()
         bar.type = "object"
         bar.addProperty("a", SwaggerSchemas.string)
         bar.addProperty("b", SwaggerSchemas.boolean)
         bar.name = Wibble.Bar::class.java.name

         val baz = Schema<Wibble.Baz>()
         baz.type = "object"
         baz.addProperty("c", SwaggerSchemas.number)
         baz.addProperty("d", SwaggerSchemas.number)
         baz.name = Wibble.Baz::class.java.name

         val wibble = Schema<Wibble>()
         wibble.type = "object"
         wibble.anyOf(listOf(Wibble.Bar::class.schema(), Wibble.Baz::class.schema()))
         wibble.name = Wibble::class.java.name

         val foo = Schema<Foo>()
         foo.type = "object"
         foo.addProperty("a", SwaggerSchemas.string)
         foo.addProperty("b", Wibble::class.schema())
         foo.name = Foo::class.java.name

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
         listSchema.items = Bar::class.schema()

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
         mapSchema.additionalProperties = Bar::class.schema()

         val expected = Schema<Foo>()
         expected.type = "object"
         expected.addProperty("a", mapSchema)
         expected.name = Foo::class.java.name
         Foo::class.toSchema() shouldBe expected
      }

      test("handle recursive types") {
         data class Foo(val a: List<Foo>)

         val list = Schema<Any>()
         list.type = "array"
         list.items = Foo::class.schema()

         val expected = Schema<Foo>()
         expected.type = "object"
         expected.addProperty("a", list)
         expected.name = Foo::class.java.name

         Foo::class.toSchema().properties["a"] shouldBe list
         Foo::class.toSchema() shouldBe expected
      }

      test("handle objects in sealed interfaces") {
         data class Foo(val a: BubbleBobble)

         val bubbleSchema = Schema<BubbleBobble.Bubble>()
         bubbleSchema.type = "object"
         bubbleSchema.addProperty("a", SwaggerSchemas.string)
         bubbleSchema.name = BubbleBobble.Bubble::class.java.name

         val bobbleSchema = Schema<BubbleBobble.Bobble>()
         bobbleSchema.type = "object"
         bobbleSchema.name = BubbleBobble.Bobble::class.java.name

         val bubbleBobbleSchema = Schema<BubbleBobble>()
         bubbleBobbleSchema.type = "object"
         bubbleBobbleSchema.anyOf(listOf(BubbleBobble.Bubble::class.schema(), BubbleBobble.Bobble::class.schema()))
         bubbleBobbleSchema.name = BubbleBobble::class.java.name

         val foo = Schema<Foo>()
         foo.type = "object"
         foo.addProperty("a", BubbleBobble::class.schema())
         foo.name = Foo::class.java.name

         Foo::class.toSchema() shouldBe foo
      }
   }
}

sealed interface Wibble {
   data class Bar(val a: String, val b: Boolean) : Wibble
   data class Baz(val c: Double, val d: Float) : Wibble
}

sealed interface BubbleBobble {
   data class Bubble(val a: String) : BubbleBobble
   object Bobble : BubbleBobble
}
