package io.kotest.extensions.ktor.openapi

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.basic
import io.ktor.server.routing.get
import io.ktor.server.testing.testApplication

class AuthenticationSelectorTest : FunSpec() {
   init {
      test("Route.authentication should return all applied authentication providers") {
         testApplication {
            install(Authentication) {
               basic("foo") {}
               basic("bar") {}
            }
            routing {
               authenticate("foo", "bar") {
                  val r = get("/a") { }
                  r.authentication() shouldBe listOf("foo", "bar")
               }
            }
         }
      }
   }
}
