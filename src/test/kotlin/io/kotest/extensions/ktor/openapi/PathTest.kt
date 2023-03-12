package io.kotest.extensions.ktor.openapi

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.testing.testApplication

class PathTest : FunSpec() {
   init {
      test("Route.path should support constant elements") {
         testApplication {
            routing {
               route("/a") {
                  route("/b") {
                     val r = get("/c") { }
                     r.path() shouldBe "/a/b/c"
                  }
               }
            }
         }
      }

      test("Route.path should support parameter elements") {
         testApplication {
            routing {
               route("/a") {
                  route("/{userId}") {
                     val r = get("/c") { }
                     r.path() shouldBe "/a/{userId}/c"
                  }
               }
            }
         }
      }

      test("Route.path should support and remove trailing slash") {
         testApplication {
            routing {
               route("/a") {
                  route("/b") {
                     val r = get("/c/") { }
                     r.path() shouldBe "/a/b/c"
                  }
               }
            }
         }
      }

      test("Route.path should support wildcard") {
         testApplication {
            routing {
               route("/a") {
                  route("/b") {
                     val r = get("/*") { }
                     r.path() shouldBe "/a/b/*"
                  }
               }
            }
         }
      }
   }
}
