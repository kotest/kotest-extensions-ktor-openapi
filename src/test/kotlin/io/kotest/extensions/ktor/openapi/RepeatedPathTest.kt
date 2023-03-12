package io.kotest.extensions.ktor.openapi

import io.kotest.core.spec.style.FunSpec
import io.ktor.server.routing.get
import io.ktor.server.routing.put
import io.ktor.server.testing.testApplication

class RepeatedPathTest : FunSpec() {
   init {
      test("should support same path with different methods") {
         testApplication {
            install(KotestOpenApi)
            routing {
               get("/a") {}
               put("/b") {
               }
            }
         }
      }
   }
}
