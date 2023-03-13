package io.kotest.extensions.ktor.openapi

import io.swagger.v3.core.util.Yaml
import java.nio.file.Files
import java.nio.file.Path

class OpenApiWriter(private val path: Path) {

   fun write(builder: OpenApiBuilder) {
      val yaml = Yaml.pretty().writeValueAsString(builder.openapi)
      Files.write(path, yaml.encodeToByteArray())
   }
}

