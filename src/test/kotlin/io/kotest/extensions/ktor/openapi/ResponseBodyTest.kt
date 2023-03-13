package io.kotest.extensions.ktor.openapi

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.request.get
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.withCharset
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.call
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.testing.testApplication
import java.nio.charset.Charset

class ResponseBodyTest : FunSpec() {
   init {

      test("should support text/plain response bodies") {
         val t = Tracer()
         testApplication {
            install(KotestOpenApi) {
               tracer = t
            }
            routing {
               get("/a") {
                  call.respond(HttpStatusCode.OK, "I love lucy")
               }
            }
            client.get("/a") {}
         }
         t.getTraces().first().apply {
            contentType shouldBe ContentType.Text.Plain.withCharset(Charset.defaultCharset())
            responseBody!!.array().decodeToString() shouldBe "I love lucy"
         }
      }

      test("should support json response bodies") {

         data class JsonResponse(val a: String, val b: Boolean, val c: Int)

         val t = Tracer()
         testApplication {
            install(KotestOpenApi) {
               tracer = t
            }
            install(ContentNegotiation) {
               jackson()
            }
            routing {
               get("/a") {
                  call.respond(HttpStatusCode.OK, JsonResponse("I love lucy", true, 1))
               }
            }
            client.get("/a") {}
         }
         t.getTraces().first().apply {
            contentType shouldBe ContentType.Application.Json.withCharset(Charset.defaultCharset())
            responseBody!!.array().decodeToString() shouldBe """{"a":"I love lucy","b":true,"c":1}"""
         }
      }
   }
}
