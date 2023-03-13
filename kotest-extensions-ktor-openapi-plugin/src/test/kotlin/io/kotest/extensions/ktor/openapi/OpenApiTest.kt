package io.kotest.extensions.ktor.openapi

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.call
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.basic
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respond
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.patch
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.testing.testApplication

class OpenApiTest : FunSpec() {

//   val collector = OpenApiCollector(
//      path = Paths.get("/home/sam/development/workspace/kotest/kotest-extensions-ktor-openapi/opentest.yml"),
//      authentications = mapOf("internal" to Authenticator.Header("My-Api-Key"))
//   )

   init {
      test("should generate routes for all method types") {
         testApplication {
            install(KotestOpenApi)
            install(ContentNegotiation) {
               jackson()
            }
            install(Authentication) {
               basic("auth2") {
                  this.realm = "myrealm"
               }
               basic("auth1") {
                  this.realm = "myrealm"
               }
            }
            routing {
               route("/internal") {
                  get("/foo1") {
                     description("Returns the user identified by the foo param")
                     call.respond(User("sammy", "chicago"))
                  }
                  patch("/patchme") {
                     deprecated(true)
                     call.respond(HttpStatusCode.OK)
                  }
               }
               authenticate("auth1", "auth2") {
                  get("/users/{foo}") { call.respond(HttpStatusCode.OK) }
                  delete("/with/param1/{foo}/even/{param2}") {
                     call.respond(HttpStatusCode.OK)
                  }
               }
               post("/bar2") { call.respond(HttpStatusCode.OK, "some response body") }
            }
            client.get("/internal/foo1").bodyAsText() shouldBe """{"name":"sammy","location":"chicago"}"""
            client.patch("/internal/patchme")
            client.post("/bar2").status
            client.get("/users/154363")
            client.delete("/with/param1/foo/even/param2")
         }
      }
   }
}

data class User(
   val name: String,
   val location: String,
)
