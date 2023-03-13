package io.kotest.extensions.ktor.openapi

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.put
import io.ktor.server.testing.testApplication

class RepeatedPathTest : FunSpec() {
   init {

      test("should support same path with different methods") {
         val t = Tracer()
         testApplication {
            install(KotestOpenApi) {
               tracer = t
            }
            routing {
               get("/a") {
                  call.respond(HttpStatusCode.OK)
               }
               put("/b") {
                  call.respond(HttpStatusCode.OK)
               }
            }
            client.get("/a") {}
            client.put("/b") {}
         }
         t.getTraces().map { Pair(it.path, it.method) } shouldBe listOf(
            Pair("/a", HttpMethod.Get),
            Pair("/b", HttpMethod.Put)
         )
      }
   }
}
