package io.kotest.extensions.ktor.openapi

import io.kotest.core.spec.style.FunSpec
import io.kotest.inspectors.forOne
import io.kotest.matchers.shouldBe
import io.ktor.client.request.get
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.testing.testApplication

class SchemaDetectionTest : FunSpec() {
   init {
      test("response objects should be added as schemas") {

         data class MyTestClass(val a: String)

         val t = Tracer()
         testApplication {
            install(KotestOpenApi) {
               tracer = t
            }
            routing {
               get("/a") {
                  call.respond(MyTestClass("foo"))
               }
            }
            client.get("a")
         }
         t.getTraces().forOne { it.schema shouldBe MyTestClass::class }
      }
   }
}
