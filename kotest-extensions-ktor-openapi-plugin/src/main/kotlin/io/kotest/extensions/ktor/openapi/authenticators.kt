package io.kotest.extensions.ktor.openapi

sealed interface AuthenticationMethod {
   data class Header(val name: String) : AuthenticationMethod
   data class Bearer(val name: String) : AuthenticationMethod
}
