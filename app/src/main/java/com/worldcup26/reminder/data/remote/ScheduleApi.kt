package com.worldcup26.reminder.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Fetches the raw schedule JSON from GitHub. No API key, no auth — just a GET on a
 * static file that the maintainer refreshes roughly daily.
 */
class ScheduleApi(
    private val sourceUrl: String = DEFAULT_SOURCE_URL,
) {
    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) { json(json) }
    }

    suspend fun fetchSchedule(): ScheduleDto {
        // Decode from text so a content-type of text/plain (raw.githubusercontent)
        // doesn't trip content negotiation.
        val body = client.get(sourceUrl).bodyAsText()
        return json.decodeFromString(ScheduleDto.serializer(), body)
    }

    companion object {
        const val DEFAULT_SOURCE_URL =
            "https://raw.githubusercontent.com/openfootball/worldcup.json/master/2026/worldcup.json"
    }
}
