package com.worldcup26.reminder.data.remote

import android.content.Context
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
private data class BroadcastsDto(
    @SerialName("kinopoisk") val kinopoisk: List<String> = emptyList(),
)

/**
 * Provides the set of match ids that air on Kinopoisk (every other match airs on
 * Match TV). Ships with a bundled asset so it works offline/out of the box; if
 * [remoteUrl] is set, tries that first so the split can be updated without an app
 * release, falling back to the asset on any failure.
 */
class BroadcastsApi(
    private val context: Context,
    private val remoteUrl: String = REMOTE_URL,
) {
    private val json = Json { ignoreUnknownKeys = true; isLenient = true }
    private val client by lazy { HttpClient(OkHttp) }

    /** Returns the Kinopoisk match-id set, or empty if nothing could be loaded. */
    suspend fun kinopoiskIds(): Set<String> {
        val text = fetchRemote() ?: readAsset() ?: return emptySet()
        return runCatching {
            json.decodeFromString(BroadcastsDto.serializer(), text).kinopoisk.toSet()
        }.getOrDefault(emptySet())
    }

    private suspend fun fetchRemote(): String? {
        if (remoteUrl.isBlank()) return null
        return runCatching { client.get(remoteUrl).bodyAsText() }.getOrNull()
    }

    private fun readAsset(): String? = runCatching {
        context.assets.open(ASSET_NAME).bufferedReader().use { it.readText() }
    }.getOrNull()

    companion object {
        private const val ASSET_NAME = "broadcasts.json"

        // Point this at the raw broadcasts.json once it is hosted (e.g. GitHub raw)
        // to enable updates without shipping a new app version.
        const val REMOTE_URL = ""
    }
}
