package com.datalink.app.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import androidx.core.content.ContextCompat
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

@Suppress("DEPRECATION")
suspend fun currentDeviceLocation(context: Context): Location {
    val hasLocationPermission =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
    check(hasLocationPermission) { "Allow location access to find nearby mapped Wi-Fi." }

    val locationManager = context.getSystemService(LocationManager::class.java)
    val enabledProviders = listOf(
        LocationManager.NETWORK_PROVIDER,
        LocationManager.GPS_PROVIDER
    ).filter(locationManager::isProviderEnabled)

    check(enabledProviders.isNotEmpty()) {
        "Turn on device location, then try again."
    }

    val recentLocation = enabledProviders
        .mapNotNull(locationManager::getLastKnownLocation)
        .filter { System.currentTimeMillis() - it.time <= MAX_LOCATION_AGE_MILLIS }
        .maxByOrNull(Location::getTime)
    if (recentLocation != null) return recentLocation

    for (provider in enabledProviders) {
        val location = withTimeoutOrNull(LOCATION_TIMEOUT_MILLIS) {
            suspendCancellableCoroutine { continuation ->
                val listener = object : LocationListener {
                    override fun onLocationChanged(location: Location) {
                        if (continuation.isActive) continuation.resume(location)
                    }

                    override fun onProviderEnabled(provider: String) = Unit
                    override fun onProviderDisabled(provider: String) = Unit
                    override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit
                }
                locationManager.requestSingleUpdate(provider, listener, Looper.getMainLooper())
                continuation.invokeOnCancellation { locationManager.removeUpdates(listener) }
            }
        }
        if (location != null) return location
    }

    error("Could not get your location. Check your signal and try again.")
}

private const val MAX_LOCATION_AGE_MILLIS = 5 * 60 * 1000L
private const val LOCATION_TIMEOUT_MILLIS = 15_000L
