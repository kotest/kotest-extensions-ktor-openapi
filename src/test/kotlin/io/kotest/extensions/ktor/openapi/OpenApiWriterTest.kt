package io.kotest.extensions.ktor.openapi

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.string.shouldInclude
import java.nio.file.Files
import kotlin.io.path.readText

class OpenApiWriterTest : FunSpec({

   test("writer should include service name") {
      val file = Files.createTempFile("openapi", "test")
      val writer = OpenApiWriter(OpenApiConfig(path = file, serviceTitle = "foo service"))
      writer.write()
      file.readText() shouldInclude "title: foo service"
   }

   test("writer should include service version") {
      val file = Files.createTempFile("openapi", "test")
      val writer = OpenApiWriter(OpenApiConfig(path = file, serviceVersion = "13.4.4"))
      writer.write()
      file.readText() shouldInclude "version: 13.4.4"
   }

   test("writer should include service description") {
      val file = Files.createTempFile("openapi", "test")
      val writer = OpenApiWriter(OpenApiConfig(path = file, serviceDescription = "big bad service"))
      writer.write()
      file.readText() shouldInclude "description: big bad service"
   }
})
