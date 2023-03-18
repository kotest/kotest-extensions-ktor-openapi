package io.kotest.extensions.ktor.openapi

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.string.shouldInclude
import io.kotest.matchers.string.shouldNotInclude
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

   test("should support same endpoint with different methods") {
      val file = Files.createTempFile("openapi", "test")
      val builder = OpenApiBuilder(OpenApiConfig(path = file, serviceDescription = "big bad service"))
      builder.addTraces(
         "assbbb",
         listOf(
            Trace.default(HttpMethod.Get, "assbbb"),
            Trace.default(HttpMethod.Patch, "assbbb"),
         )
      )
      OpenApiWriter(file).write(builder)
      file.readText() shouldInclude "get"
      file.readText() shouldInclude "patch"
   }

   test("builder should support multiple path parameter examples") {
      val file = Files.createTempFile("openapi", "test")
      val builder = OpenApiBuilder(OpenApiConfig(path = file, serviceDescription = "big bad service"))
      builder.addTraces(
         "ghfgh",
         listOf(
            Trace.default(HttpMethod.Get, "ghfgh")
               .copy(pathParameters = listOf("a"), pathParameterExamples = mapOf("a" to "foo-param")),
            Trace.default(HttpMethod.Get, "ghfgh")
               .copy(pathParameters = listOf("a"), pathParameterExamples = mapOf("a" to "bar-param"))
         ),
      )
      OpenApiWriter(file).write(builder)
      file.readText().apply {
         shouldInclude("Example 1")
         shouldInclude("foo-param")
         shouldInclude("Example 2")
         shouldInclude("bar-param")
      }
   }

   test("builder should support multiple response bodies for a given content type") {
      val file = Files.createTempFile("openapi", "test")
      val builder = OpenApiBuilder(OpenApiConfig(path = file, serviceDescription = "big bad service"))
      builder.addTraces(
         "rtertret",
         listOf(
            Trace.default(HttpMethod.Get, "rtertret")
               .copy(
                  responseBody = "beam me up jim",
                  contentType = ContentType.Text.CSS,
                  status = HttpStatusCode.MovedPermanently,
               ),
            Trace.default(HttpMethod.Get, "rtertret")
               .copy(
                  responseBody = "beam me up scotty",
                  contentType = ContentType.Text.CSS,
                  status = HttpStatusCode.MovedPermanently,
               )
         )
      )
      OpenApiWriter(file).write(builder)
      file.readText().apply {
         shouldInclude("Example 1")
         shouldInclude("beam me up jim")
         shouldInclude("Example 2")
         shouldInclude("beam me up scotty")
      }
   }

   test("builder should include response body and content type") {
      val file = Files.createTempFile("openapi", "test")
      val builder = OpenApiBuilder(OpenApiConfig(path = file))
      builder.addTraces(
         "parth path",
         listOf(
            Trace.default(HttpMethod.Get, "parth path")
               .copy(
                  responseBody = "beam me up scotty",
                  contentType = ContentType.Text.CSS,
                  status = HttpStatusCode.MovedPermanently,
               )
         )
      )
      OpenApiWriter(file).write(builder)
      file.readText() shouldInclude "beam me up scotty"
      file.readText() shouldInclude "text/css"
   }

   test("builder should not set example key if only one response") {
      val file = Files.createTempFile("openapi", "test")
      val builder = OpenApiBuilder(OpenApiConfig(path = file))
      builder.addTraces(
         "parth path",
         listOf(
            Trace.default(HttpMethod.Get, "parth path")
               .copy(
                  responseBody = "beam me up scotty",
                  contentType = ContentType.Text.CSS,
                  status = HttpStatusCode.MovedPermanently,
               )
         )
      )
      OpenApiWriter(file).write(builder)
      file.readText() shouldInclude "beam me up scotty"
      file.readText() shouldNotInclude "Example 1"
   }

   test("builder should not include identical response bodies") {
      val file = Files.createTempFile("openapi", "test")
      val builder = OpenApiBuilder(OpenApiConfig(path = file))
      builder.addTraces(
         "parth path",
         listOf(
            Trace.default(HttpMethod.Get, "parth path")
               .copy(
                  responseBody = "beam me up scotty",
                  contentType = ContentType.Text.CSS,
                  status = HttpStatusCode.MovedPermanently,
               ),
            Trace.default(HttpMethod.Get, "parth path")
               .copy(
                  responseBody = "beam me up jimmy",
                  contentType = ContentType.Text.CSS,
                  status = HttpStatusCode.MovedPermanently,
               ),
            Trace.default(HttpMethod.Get, "parth path")
               .copy(
                  responseBody = "beam me up scotty",
                  contentType = ContentType.Text.CSS,
                  status = HttpStatusCode.MovedPermanently,
               )
         )
      )
      OpenApiWriter(file).write(builder)
      file.readText() shouldInclude "beam me up scotty"
      file.readText() shouldInclude "Example 1"
      file.readText() shouldInclude "Example 2"
      file.readText() shouldNotInclude "Example 3"
   }

   test("builder should distinct authentications by endpoint") {
      val file = Files.createTempFile("openapi", "test")
      val builder = OpenApiBuilder(OpenApiConfig(path = file))
      builder.addTraces(
         "party/path",
         listOf(
            Trace.default(HttpMethod.Get, "party/path")
               .copy(
                  responseBody = "beam me up scotty",
                  contentType = ContentType.Text.CSS,
                  status = HttpStatusCode.MovedPermanently,
                  authentication = listOf("foo", "bar")
               ),
            Trace.default(HttpMethod.Get, "party/path")
               .copy(
                  responseBody = "beam me up jimmy",
                  contentType = ContentType.Text.CSS,
                  status = HttpStatusCode.MovedPermanently,
                  authentication = listOf("foo", "bar")
               ),
         )
      )
      OpenApiWriter(file).write(builder)
      file.readText() shouldInclude """security:
      - foo: []
      - bar: []
components"""
   }
})
