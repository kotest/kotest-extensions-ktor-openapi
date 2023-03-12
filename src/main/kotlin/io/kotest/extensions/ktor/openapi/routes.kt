package io.kotest.extensions.ktor.openapi

import io.ktor.server.routing.PathSegmentConstantRouteSelector
import io.ktor.server.routing.PathSegmentOptionalParameterRouteSelector
import io.ktor.server.routing.PathSegmentParameterRouteSelector
import io.ktor.server.routing.PathSegmentTailcardRouteSelector
import io.ktor.server.routing.PathSegmentWildcardRouteSelector
import io.ktor.server.routing.Route
import io.ktor.server.routing.RouteSelector
import io.ktor.server.routing.TrailingSlashRouteSelector

/**
 * Returns all the [RouteSelector]s for the given [Route], with the root selector
 * returned first in the list
 */
fun Route.selectors(): List<RouteSelector> {
   return when (val parent = parent) {
      null -> listOf(selector)
      else -> parent.selectors() + selector
   }
}

/**
 * Extracts the [RouteSelector]s and filters to those which contribute to the path, then
 * concats those together to return the full endpoint path.
 */
fun Route.path() = selectors().mapNotNull {
   when (it) {
      is PathSegmentParameterRouteSelector -> it.toString()
      is PathSegmentConstantRouteSelector -> it.toString()
      is PathSegmentOptionalParameterRouteSelector -> it.toString()
      is PathSegmentTailcardRouteSelector -> it.toString()
      is PathSegmentWildcardRouteSelector -> it.toString()
      is TrailingSlashRouteSelector -> ""
      else -> null
   }
}.joinToString("/", prefix = "/").removeSuffix("/")
