package nl.connectplay.scoreplay.utilities

/**
 * Some utility functions to make getting defaults for our standard query parameters slightly easier
 */

import io.ktor.server.request.ApplicationRequest

/**
 * Gets default limit of 25 if the query parameter is not provided
 */
fun ApplicationRequest.getLimitQueryParameter(): Int = this.queryParameters["limit"]?.toInt() ?: 25

/**
 * Gets default offset of 0 if the query parameter is not provided
 */
fun ApplicationRequest.getOffsetQueryParameter(): Int = this.queryParameters["offset"]?.toInt() ?: 0

/**
 * Gets the query string
 */
fun ApplicationRequest.getSearchQueryParameter(): String? = this.queryParameters["query"]