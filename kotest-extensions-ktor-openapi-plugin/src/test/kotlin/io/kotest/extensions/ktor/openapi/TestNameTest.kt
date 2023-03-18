package io.kotest.extensions.ktor.openapi

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.testing.testApplication

class TestNameTest : FunSpec() {
   init {
      test("pass test path to tracer") {
         val t = Tracer()
         testApplication {
            install(KotestOpenApi) {
               tracer = t
            }
            routing {
               get("/a") {
                  call.respond(HttpStatusCode.OK)
               }
            }
            val resp = client.get("/a")
            resp.status shouldBe HttpStatusCode.OK
         }
         t.getTraces().single().testName shouldBe ""
      }
   }
}
