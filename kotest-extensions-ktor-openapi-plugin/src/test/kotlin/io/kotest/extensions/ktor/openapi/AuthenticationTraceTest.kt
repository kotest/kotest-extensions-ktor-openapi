package io.kotest.extensions.ktor.openapi

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.UserIdPrincipal
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.basic
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.testing.testApplication

class AuthenticationTraceTest : FunSpec() {
   init {
      test("authentication providers set on a route should be traced") {

         val t = Tracer()
         testApplication {
            install(KotestOpenApi) {
               tracer = t
            }
            install(Authentication) {
               basic("foo") {
                  realm = "test"
                  validate { UserIdPrincipal("me") }
               }
               basic("bar") {
                  realm = "test"
                  validate { UserIdPrincipal("me") }
               }
            }
            routing {
               authenticate("foo", "bar") {
                  get("/a") {
                     call.respond(HttpStatusCode.OK, call.principal<UserIdPrincipal>()!!.name)
                  }
               }
            }
            val resp = client.get("/a") {
               header(HttpHeaders.Authorization, "Basic amV0YnJhaW5zOmZvb2Jhcg")
            }
            resp.status shouldBe HttpStatusCode.OK
            resp.bodyAsText() shouldBe "me"
         }
         t.getTraces().single().authentication shouldBe listOf("foo", "bar")
      }
   }
}
