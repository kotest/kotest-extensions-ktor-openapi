package io.kotest.extensions.ktor.openapi

sealed interface AuthenticationMethod {
   data class Header(val headerName: String) : AuthenticationMethod
   object Bearer : AuthenticationMethod
   data class Basic(val realm: String) : AuthenticationMethod
   data class CustomAuthorization(val scheme: String) : AuthenticationMethod
}
