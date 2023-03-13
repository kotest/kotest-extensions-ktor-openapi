package io.kotest.extensions.ktor.openapi

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.string.shouldInclude
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import java.nio.file.Files
import kotlin.io.path.readText

class OpenApiWriterTest : FunSpec({

   test("writer should include service name") {
      val file = Files.createTempFile("openapi", "test")
      val builder = OpenApiBuilder(OpenApiConfig(path = file, serviceTitle = "foo service"))
      OpenApiWriter(file).write(builder)
      file.readText() shouldInclude "title: foo service"
   }

   test("writer should include service version") {
      val file = Files.createTempFile("openapi", "test")
      val builder = OpenApiBuilder(OpenApiConfig(path = file, serviceVersion = "13.4.4"))
      OpenApiWriter(file).write(builder)
      file.readText() shouldInclude "version: 13.4.4"
   }

   test("writer should include service description") {
      val file = Files.createTempFile("openapi", "test")
      val builder = OpenApiBuilder(OpenApiConfig(path = file, serviceDescription = "big bad service"))
      OpenApiWriter(file).write(builder)
      file.readText() shouldInclude "description: big bad service"
   }

   test("writer should include content types") {
      val file = Files.createTempFile("openapi", "test")
      val builder = OpenApiBuilder(OpenApiConfig(path = file, serviceDescription = "big bad service"))
      builder.addTrace(
         Trace.default(HttpMethod.Get, "path mc facepath")
            .copy(
               contentType = ContentType.Text.CSS,
               status = HttpStatusCode.MovedPermanently,
            )
      )
      OpenApiWriter(file).write(builder)
      file.readText() shouldInclude "text/css"
   }
})
