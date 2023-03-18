package io.kotest.extensions.ktor.openapi

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.string.shouldInclude
import java.nio.file.Files
import kotlin.io.path.readText

class WriterAuthenticationMethodTest : FunSpec() {
   init {

      test("support bearer authentications") {
         val file = Files.createTempFile("openapi", "test")
         val builder = OpenApiBuilder(
            OpenApiConfig(
               path = file,
               authentications = mapOf("foo" to AuthenticationMethod.Bearer("bar"))
            )
         )
         OpenApiWriter(file).write(builder)
         file.readText() shouldInclude """components:
  securitySchemes:
    foo:
      type: http
      name: bar
      scheme: bearer"""
      }
   }
}
