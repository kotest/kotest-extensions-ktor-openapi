package io.kotest.extensions.ktor.openapi

import io.kotest.core.spec.style.FunSpec
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.testing.testApplication
import java.nio.file.Files

class OpenApiTest : FunSpec() {
   init {
      test("should generate routes for all method types") {
         val apiPath = Files.createTempFile("openapi", "test")
         testApplication {
            install(OpenApi) {
               path = apiPath
            }
            routing {
               get("/foo") { call.respond(HttpStatusCode.OK) }
               post("/bar") { call.respond(HttpStatusCode.OK) }
            }
            client.get("foo")
            client.post("/bar")

         }
      }
   }
}
