package nl.connectplay.scoreplay.utilities

/**
 * Some utility functions to make getting defaults for our standard query parameters slightly easier
 */

import io.ktor.server.request.*

/**
 * Gets default limit of 25 if the query parameter is not provided
 */
fun ApplicationRequest.getLimitQueryParameter(default: Int = 25): Int = this.queryParameters["limit"]?.toInt() ?: default

/**
 * Gets default offset of 0 if the query parameter is not provided
 */
fun ApplicationRequest.getOffsetQueryParameter(default: Int = 0): Int = this.queryParameters["offset"]?.toInt() ?: default

/**
 * Gets the query string
 */
fun ApplicationRequest.getSearchQueryParameter(): String? = this.queryParameters["query"]