package io.kotest.extensions.ktor.openapi

sealed interface Authenticator {
   data class Header(val name: String) : Authenticator
}
