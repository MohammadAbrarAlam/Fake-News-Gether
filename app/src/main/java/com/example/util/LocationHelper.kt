package com.example.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.location.Geocoder
import android.net.Uri
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

object LocationHelper {

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(context: Context): Pair<Double, Double>? = withContext(Dispatchers.IO) {
        try {
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
            val locationTask = fusedClient.lastLocation
            var result: Pair<Double, Double>? = null
            val latch = java.util.concurrent.CountDownLatch(1)

            locationTask.addOnSuccessListener { loc ->
                if (loc != null) {
                    result = Pair(loc.latitude, loc.longitude)
                }
                latch.countDown()
            }.addOnFailureListener {
                latch.countDown()
            }

            latch.await(3, java.util.concurrent.TimeUnit.SECONDS)
            result
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getAddressFromCoordinates(context: Context, lat: Double, lng: Double): String = withContext(Dispatchers.IO) {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(lat, lng, 1)
            if (!addresses.isNullOrEmpty()) {
                val addr = addresses[0]
                val parts = mutableListOf<String>()
                addr.featureName?.let { if (it.isNotBlank()) parts.add(it) }
                addr.locality?.let { if (!parts.contains(it)) parts.add(it) }
                addr.adminArea?.let { if (!parts.contains(it)) parts.add(it) }
                addr.countryName?.let { if (!parts.contains(it)) parts.add(it) }
                if (parts.isNotEmpty()) return@withContext parts.joinToString(", ")
            }
        } catch (e: Exception) {
            // Geocoder might fail without network or play services
        }
        return@withContext String.format(Locale.US, "%.4f, %.4f", lat, lng)
    }

    fun openInGoogleMaps(context: Context, lat: Double, lng: Double, label: String = "Location") {
        try {
            val encodedLabel = Uri.encode(label)
            val uri = Uri.parse("geo:$lat,$lng?q=$lat,$lng($encodedLabel)")
            val intent = Intent(Intent.ACTION_VIEW, uri)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            // Fallback to browser URL
            val browserUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$lat,$lng")
            val browserIntent = Intent(Intent.ACTION_VIEW, browserUri)
            browserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(browserIntent)
        }
    }

    fun openWebUrl(context: Context, url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            // Ignore if unable to open
        }
    }
}
