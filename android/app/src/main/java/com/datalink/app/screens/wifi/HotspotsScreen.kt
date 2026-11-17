package com.datalink.app.screens.wifi

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.datalink.app.R
import com.datalink.app.data.Hotspot
import com.datalink.app.data.HotspotRepository
import com.datalink.app.data.HotspotSnapshot
import com.datalink.app.data.currentDeviceLocation
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.text.DateFormat
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HotspotsScreen() {
    val context = LocalContext.current
    val repository = remember { HotspotRepository(context.applicationContext) }
    val scope = rememberCoroutineScope()
    val permissionDeniedMessage = stringResource(R.string.location_permission_required)
    var snapshot by remember { mutableStateOf<HotspotSnapshot?>(null) }
    var isRefreshing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(repository) {
        snapshot = withContext(Dispatchers.IO) { repository.loadCachedSnapshot() }
    }

    fun refreshHotspots() {
        scope.launch {
            isRefreshing = true
            errorMessage = null
            try {
                val location = withContext(Dispatchers.IO) {
                    currentDeviceLocation(context)
                }
                snapshot = repository.refresh(location.latitude, location.longitude)
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: IOException) {
                errorMessage = exception.message ?: "Could not load hotspot listings."
            } catch (exception: IllegalStateException) {
                errorMessage = exception.message ?: "Could not get your location."
            } catch (exception: SecurityException) {
                errorMessage = "Location permission is needed to search nearby."
            } finally {
                isRefreshing = false
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.values.any { it }) {
            refreshHotspots()
        } else {
            errorMessage = permissionDeniedMessage
        }
    }

    fun onRefresh() {
        val hasLocationPermission =
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
        if (hasLocationPermission) {
            refreshHotspots()
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text(stringResource(R.string.app_name)) })
        }
    ) { insets ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(insets)
                .padding(horizontal = 16.dp)
        ) {
            Text(
                stringResource(R.string.hotspots_title),
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(Modifier.height(6.dp))
            Text(
                stringResource(R.string.hotspots_description),
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = ::onRefresh,
                enabled = !isRefreshing,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isRefreshing) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(18.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.searching_hotspots))
                } else {
                    Text(stringResource(R.string.refresh_hotspots))
                }
            }
            errorMessage?.let { message ->
                Spacer(Modifier.height(8.dp))
                Text(message, color = MaterialTheme.colorScheme.error)
            }
            snapshot?.let { saved ->
                Spacer(Modifier.height(8.dp))
                Text(
                    stringResource(
                        R.string.saved_offline_listings,
                        DateFormat.getDateTimeInstance().format(Date(saved.savedAt))
                    ),
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    stringResource(R.string.openstreetmap_attribution),
                    style = MaterialTheme.typography.labelSmall
                )
            }
            Spacer(Modifier.height(8.dp))

            val savedHotspots = snapshot?.hotspots.orEmpty().sortedBy { hotspot ->
                hotspot.distanceFrom(snapshot)
            }
            if (savedHotspots.isEmpty() && !isRefreshing) {
                Text(
                    if (snapshot == null) {
                        stringResource(R.string.empty_hotspot_cache)
                    } else {
                        stringResource(R.string.empty_hotspot_search)
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(savedHotspots, key = Hotspot::id) { hotspot ->
                    HotspotCard(hotspot, snapshot)
                }
            }
        }
    }
}

@Composable
private fun HotspotCard(hotspot: Hotspot, snapshot: HotspotSnapshot?) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(hotspot.name, style = MaterialTheme.typography.titleMedium)
            hotspot.address?.let {
                Spacer(Modifier.height(4.dp))
                Text(it, style = MaterialTheme.typography.bodyMedium)
            }
            Spacer(Modifier.height(4.dp))
            Row {
                Text(
                    hotspot.accessFee?.let { stringResource(R.string.hotspot_access_fee, it) }
                        ?: stringResource(R.string.hotspot_access_fee_unknown),
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(Modifier.weight(1f))
                Text(
                    hotspot.distanceFrom(snapshot)?.let { distance ->
                        if (distance < 1_000) {
                            stringResource(R.string.hotspot_distance_meters, distance.toInt())
                        } else {
                            stringResource(R.string.hotspot_distance_kilometers, distance / 1_000)
                        }
                    }.orEmpty(),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Button(
                onClick = {
                    val coordinates = "${hotspot.latitude},${hotspot.longitude}"
                    val location = Uri.parse(
                        "geo:${hotspot.latitude},${hotspot.longitude}?q=" +
                            Uri.encode("${hotspot.latitude},${hotspot.longitude}(${hotspot.name})")
                    )
                    try {
                        context.startActivity(Intent(Intent.ACTION_VIEW, location))
                    } catch (_: ActivityNotFoundException) {
                        val mapUrl = Uri.parse(
                            "https://www.openstreetmap.org/?mlat=${hotspot.latitude}&mlon=${hotspot.longitude}#map=18/$coordinates"
                        )
                        try {
                            context.startActivity(Intent(Intent.ACTION_VIEW, mapUrl))
                        } catch (_: ActivityNotFoundException) {
                            Toast.makeText(
                                context,
                                R.string.maps_app_not_found,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            ) {
                Text(stringResource(R.string.open_directions))
            }
        }
    }
}

private fun Hotspot.distanceFrom(snapshot: HotspotSnapshot?): Float? {
    if (snapshot == null) return null
    val result = FloatArray(1)
    Location.distanceBetween(
        snapshot.latitude,
        snapshot.longitude,
        latitude,
        longitude,
        result
    )
    return result[0]
}
