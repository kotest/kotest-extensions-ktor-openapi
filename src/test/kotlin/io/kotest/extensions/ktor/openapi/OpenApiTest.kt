package io.kotest.extensions.ktor.openapi

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.response.respond
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.testing.testApplication
import java.nio.file.Paths

class OpenApiTest : FunSpec() {
   init {
      test("should generate routes for all method types") {
         val apiPath = Paths.get("/home/sam/development/workspace/kotest/kotest-extensions-ktor-openapi/opentest.yml")
         testApplication {
            install(OpenApi) {
               path = apiPath
            }
            routing {
               route("/internal") {
                  get("/foo1") {
                     description("Returns the user identified by the foo param")
                     call.respond(HttpStatusCode.OK)
                  }
               }
               get("/with/param/{foo}") { call.respond(HttpStatusCode.OK) }
               delete("/with/param1/{foo}/even/{param2}") { call.respond(HttpStatusCode.OK) }
               post("/bar2") { call.respond(HttpStatusCode.OK) }
            }
            client.get("/internal/foo1")
            client.post("/bar2").status shouldBe HttpStatusCode.OK
            client.get("/with/param/abc").status shouldBe HttpStatusCode.OK
            client.delete("/with/param1/foo/even/param2").status shouldBe HttpStatusCode.OK
         }
      }
   }
}
