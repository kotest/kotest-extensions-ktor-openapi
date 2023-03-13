package io.kotest.extensions.ktor.openapi

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.ktor.http.HttpMethod
import io.ktor.server.routing.HttpMethodRouteSelector
import io.ktor.server.routing.PathSegmentConstantRouteSelector
import io.ktor.server.routing.RootRouteSelector
import io.ktor.server.routing.get
import io.ktor.server.routing.route
import io.ktor.server.testing.testApplication

class SelectorTest : FunSpec() {
   init {
      test("Route.selectors should return all selectors in depth first order") {
         testApplication {
            routing {
               route("/a") {
                  route("/b") {
                     val r = get("/c") { }
                     r.selectors()[0].shouldBeInstanceOf<RootRouteSelector>()
                     r.selectors()[1].shouldBeInstanceOf<PathSegmentConstantRouteSelector>().value shouldBe "a"
                     r.selectors()[2].shouldBeInstanceOf<PathSegmentConstantRouteSelector>().value shouldBe "b"
                     r.selectors()[3].shouldBeInstanceOf<PathSegmentConstantRouteSelector>().value shouldBe "c"
                     r.selectors()[4].shouldBeInstanceOf<HttpMethodRouteSelector>().method shouldBe HttpMethod.Get
                  }
               }
            }
         }
      }
   }
}
