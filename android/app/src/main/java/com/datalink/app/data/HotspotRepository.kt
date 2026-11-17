package com.datalink.app.data

import android.content.Context
import android.util.Log
import com.datalink.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

class HotspotRepository(context: Context) {
    private val preferences =
        context.getSharedPreferences("hotspot-cache", Context.MODE_PRIVATE)

    fun loadCachedSnapshot(): HotspotSnapshot? {
        val saved = preferences.getString(CACHE_KEY, null) ?: return null
        return try {
            decodeSnapshot(JSONObject(saved))
        } catch (exception: JSONException) {
            Log.e(TAG, "Discarding unreadable saved hotspot data", exception)
            preferences.edit().remove(CACHE_KEY).apply()
            null
        }
    }

    suspend fun refresh(latitude: Double, longitude: Double): HotspotSnapshot =
        withContext(Dispatchers.IO) {
            val response = fetch(latitude, longitude)
            val snapshot = HotspotSnapshot(
                latitude = latitude,
                longitude = longitude,
                savedAt = System.currentTimeMillis(),
                hotspots = parseHotspots(response)
            )
            preferences.edit()
                .putString(CACHE_KEY, encodeSnapshot(snapshot).toString())
                .apply()
            snapshot
        }

    private fun fetch(latitude: Double, longitude: Double): JSONObject {
        val baseUrl = BuildConfig.HOTSPOT_API_BASE_URL.trimEnd('/')
        val query = """
            [out:json][timeout:10];
            (
              node(around:$SEARCH_RADIUS_METERS,$latitude,$longitude)["internet_access"~"^(wlan|wifi)$",i];
              way(around:$SEARCH_RADIUS_METERS,$latitude,$longitude)["internet_access"~"^(wlan|wifi)$",i];
              relation(around:$SEARCH_RADIUS_METERS,$latitude,$longitude)["internet_access"~"^(wlan|wifi)$",i];
            );
            out center tags;
        """.trimIndent()
        val encodedQuery = URLEncoder.encode(query, Charsets.UTF_8.name())
        val urls = buildList {
            if (baseUrl.isNotEmpty()) {
                add("$baseUrl/api/hotspots?lat=$latitude&lon=$longitude&radius=$SEARCH_RADIUS_METERS")
            }
            add("https://overpass-api.de/api/interpreter?data=$encodedQuery")
            add("https://overpass.kumi.systems/api/interpreter?data=$encodedQuery")
        }

        var lastFailure: IOException? = null
        for (url in urls) {
            try {
                return request(url)
            } catch (exception: IOException) {
                lastFailure = exception
            }
        }
        throw lastFailure ?: IOException("Hotspot data services are unavailable.")
    }

    private fun request(url: String): JSONObject {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = CONNECT_TIMEOUT_MILLIS
        connection.readTimeout = READ_TIMEOUT_MILLIS
        connection.setRequestProperty("Accept", "application/json")
        connection.setRequestProperty("User-Agent", "DataLinkUganda/1.0 (Android)")

        try {
            val statusCode = connection.responseCode
            val stream = if (statusCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }
            val body = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            if (statusCode !in 200..299) {
                throw IOException("Hotspot data service returned HTTP $statusCode.")
            }
            return try {
                JSONObject(body)
            } catch (exception: JSONException) {
                throw IOException("Hotspot data service returned invalid data.", exception)
            }
        } finally {
            connection.disconnect()
        }
    }

    private fun parseHotspots(response: JSONObject): List<Hotspot> {
        val entries = response.optJSONArray("hotspots")
            ?: response.optJSONArray("elements")
            ?: throw IOException("Hotspot data service returned an unexpected response.")

        return buildList {
            for (index in 0 until entries.length()) {
                val entry = entries.optJSONObject(index) ?: continue
                val tags = entry.optJSONObject("tags") ?: entry
                val center = entry.optJSONObject("center")
                val latitude = entry.optDouble("lat", center?.optDouble("lat", Double.NaN) ?: Double.NaN)
                val longitude = entry.optDouble("lon", center?.optDouble("lon", Double.NaN) ?: Double.NaN)
                if (!latitude.isFinite() || !longitude.isFinite()) continue

                val id = entry.optString("id").takeUnless { it.isBlank() || it == "null" }
                    ?: "${entry.optString("type", "hotspot")}-${index}"
                val name = entry.optString("name")
                    .takeUnless { it.isBlank() || it == "null" }
                    ?: "Mapped Wi-Fi location"
                val address = entry.optString("address")
                    .takeUnless { it.isBlank() || it == "null" }
                    ?: listOf("addr:street", "addr:suburb", "addr:city")
                        .mapNotNull { tags.optString(it).takeUnless(String::isBlank) }
                        .distinct()
                        .joinToString(", ")
                        .ifBlank { null }
                val accessFee = entry.optString("accessFee")
                    .takeUnless { it.isBlank() || it == "null" }
                    ?: tags.optString("internet_access:fee")
                    .takeUnless { it.isBlank() || it == "null" }
                val website = tags.optString("contact:website")
                    .takeUnless { it.isBlank() || it == "null" }
                    ?: tags.optString("website").takeUnless { it.isBlank() || it == "null" }

                add(Hotspot(id, name, latitude, longitude, address, accessFee, website))
            }
        }.distinctBy(Hotspot::id)
    }

    private fun encodeSnapshot(snapshot: HotspotSnapshot): JSONObject =
        JSONObject()
            .put("latitude", snapshot.latitude)
            .put("longitude", snapshot.longitude)
            .put("savedAt", snapshot.savedAt)
            .put("hotspots", JSONArray().apply {
                snapshot.hotspots.forEach { hotspot ->
                    put(
                        JSONObject()
                            .put("id", hotspot.id)
                            .put("name", hotspot.name)
                            .put("latitude", hotspot.latitude)
                            .put("longitude", hotspot.longitude)
                            .put("address", hotspot.address)
                            .put("accessFee", hotspot.accessFee)
                            .put("website", hotspot.website)
                    )
                }
            })

    private fun decodeSnapshot(json: JSONObject): HotspotSnapshot {
        val savedHotspots = json.getJSONArray("hotspots")
        val hotspots = buildList {
            for (index in 0 until savedHotspots.length()) {
                val hotspot = savedHotspots.getJSONObject(index)
                add(
                    Hotspot(
                        id = hotspot.getString("id"),
                        name = hotspot.getString("name"),
                        latitude = hotspot.getDouble("latitude"),
                        longitude = hotspot.getDouble("longitude"),
                        address = hotspot.optString("address").takeUnless { it.isBlank() || it == "null" },
                        accessFee = hotspot.optString("accessFee").takeUnless { it.isBlank() || it == "null" },
                        website = hotspot.optString("website").takeUnless { it.isBlank() || it == "null" }
                    )
                )
            }
        }
        return HotspotSnapshot(
            latitude = json.getDouble("latitude"),
            longitude = json.getDouble("longitude"),
            savedAt = json.getLong("savedAt"),
            hotspots = hotspots
        )
    }

    private companion object {
        const val TAG = "HotspotRepository"
        const val CACHE_KEY = "latest-snapshot"
        const val SEARCH_RADIUS_METERS = 5_000
        const val CONNECT_TIMEOUT_MILLIS = 15_000
        const val READ_TIMEOUT_MILLIS = 12_000
    }
}
