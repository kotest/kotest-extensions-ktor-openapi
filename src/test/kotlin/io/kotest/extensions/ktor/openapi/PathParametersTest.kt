package io.kotest.extensions.ktor.openapi

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.testing.testApplication

class PathParametersTest : FunSpec() {
   init {
      test("Route.pathParameters should return all path parameters from parameter selectors") {
         testApplication {
            routing {
               route("/a") {
                  route("/{userId}") {
                     val r = get("/c/{locale}") { }
                     r.pathParameters() shouldBe listOf("userId", "locale")
                  }
               }
            }
         }
      }
   }
}
