package com.datalink.app.data

data class Hotspot(
    val id: String,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val address: String?,
    val accessFee: String?,
    val website: String?
)

data class HotspotSnapshot(
    val latitude: Double,
    val longitude: Double,
    val savedAt: Long,
    val hotspots: List<Hotspot>
)
