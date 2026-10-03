package com.example.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import java.util.Locale
import kotlin.math.*

data class GeoLocationResult(
    val latitude: Double,
    val longitude: Double,
    val formattedAddress: String,
    val locality: String,
    val city: String,
    val state: String,
    val postalCode: String
)

object LocationHelper {

    // Default Fallback Location: Purulia Main Hub, West Bengal
    const val DEFAULT_LAT = 23.3322
    const val DEFAULT_LNG = 86.3652
    const val DEFAULT_ADDRESS = "Main Market Road, Purulia Town, West Bengal 723101"
    const val DEFAULT_LOCALITY = "Purulia Town"
    const val DEFAULT_CITY = "Purulia"
    const val DEFAULT_STATE = "West Bengal"
    const val DEFAULT_PINCODE = "723101"

    /**
     * Calculates distance between two GPS coordinates using the Haversine formula
     * Returns distance in kilometers (km)
     */
    fun calculateDistanceKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        if (lat1 == 0.0 || lon1 == 0.0 || lat2 == 0.0 || lon2 == 0.0) return 0.0
        val earthRadiusKm = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val distance = earthRadiusKm * c
        return (distance * 10.0).roundToInt() / 10.0 // 1 decimal place precision
    }

    /**
     * Formats distance into a clean string (e.g., "1.4 km", "600 m")
     */
    fun formatDistance(distanceKm: Double): String {
        return if (distanceKm < 1.0) {
            "${(distanceKm * 1000).toInt()} m"
        } else {
            "${String.format(Locale.ENGLISH, "%.1f", distanceKm)} km"
        }
    }

    /**
     * Estimates delivery duration in minutes based on distance
     */
    fun estimateDeliveryMinutes(distanceKm: Double): Int {
        val basePrepTime = 15
        val travelTime = (distanceKm * 3.0).toInt().coerceAtLeast(5)
        return (basePrepTime + travelTime).coerceIn(20, 60)
    }

    /**
     * Calculates delivery charges dynamically
     */
    fun calculateDeliveryFee(
        distanceKm: Double,
        orderTotal: Double,
        baseFee: Double = 25.0,
        freeThreshold: Double = 499.0,
        perKmRate: Double = 6.0,
        baseRadiusKm: Double = 2.0,
        surgeFee: Double = 0.0
    ): Double {
        if (orderTotal >= freeThreshold) return 0.0
        val extraDistance = (distanceKm - baseRadiusKm).coerceAtLeast(0.0)
        val fee = baseFee + (extraDistance * perKmRate) + surgeFee
        return (fee.roundToInt()).toDouble()
    }

    /**
     * Reverse geocodes coordinates to a human-readable Address object using Android Geocoder
     */
    fun reverseGeocode(context: Context, latitude: Double, longitude: Double): GeoLocationResult {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val addr = addresses[0]
                val fullAddress = (0..addr.maxAddressLineIndex).mapNotNull { addr.getAddressLine(it) }.joinToString(", ")
                val locality = addr.subLocality ?: addr.locality ?: "Local Area"
                val city = addr.locality ?: addr.subAdminArea ?: addr.adminArea ?: "Purulia"
                val state = addr.adminArea ?: "West Bengal"
                val postalCode = addr.postalCode ?: "723101"

                return GeoLocationResult(
                    latitude = latitude,
                    longitude = longitude,
                    formattedAddress = if (fullAddress.isNotBlank()) fullAddress else "$locality, $city, $state $postalCode",
                    locality = locality,
                    city = city,
                    state = state,
                    postalCode = postalCode
                )
            }
        } catch (e: Exception) {
            // Geocoder service may not be available on all devices/networks
        }

        // Return nearest matched city location
        val matched = getNearestKnownCity(latitude, longitude)
        return GeoLocationResult(
            latitude = latitude,
            longitude = longitude,
            formattedAddress = "${matched.name}, ${matched.state} ${matched.pincode}",
            locality = matched.name,
            city = matched.city,
            state = matched.state,
            postalCode = matched.pincode
        )
    }

    /**
     * Retrieves current device location using Android LocationManager
     */
    @SuppressLint("MissingPermission")
    fun getDeviceLocation(context: Context): Location? {
        try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return null
            val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

            var bestLocation: Location? = null
            if (isGpsEnabled) {
                bestLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            }
            if (bestLocation == null && isNetworkEnabled) {
                bestLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
            }
            if (bestLocation == null && locationManager.isProviderEnabled(LocationManager.PASSIVE_PROVIDER)) {
                bestLocation = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER)
            }
            return bestLocation
        } catch (e: Exception) {
            return null
        }
    }

    data class KnownLocation(
        val name: String,
        val city: String,
        val state: String,
        val pincode: String,
        val latitude: Double,
        val longitude: Double
    )

    val POPULAR_LOCATIONS = listOf(
        KnownLocation("Main Market Purulia", "Purulia", "West Bengal", "723101", 23.3322, 86.3652),
        KnownLocation("Station Road Purulia", "Purulia", "West Bengal", "723102", 23.3385, 86.3712),
        KnownLocation("Bhatbundh", "Purulia", "West Bengal", "723101", 23.3290, 86.3580),
        KnownLocation("City Center Bokaro", "Bokaro", "Jharkhand", "827004", 23.6693, 86.1511),
        KnownLocation("Sector 4 Bokaro", "Bokaro", "Jharkhand", "827004", 23.6730, 86.1420),
        KnownLocation("Bank More Dhanbad", "Dhanbad", "Jharkhand", "826001", 23.7957, 86.4304),
        KnownLocation("Saraidhela Dhanbad", "Dhanbad", "Jharkhand", "828127", 23.8120, 86.4460),
        KnownLocation("Main Road Ranchi", "Ranchi", "Jharkhand", "834001", 23.3441, 85.3096),
        KnownLocation("Lalpur Ranchi", "Ranchi", "Jharkhand", "834001", 23.3700, 85.3340),
        KnownLocation("Park Street Kolkata", "Kolkata", "West Bengal", "700016", 22.5510, 88.3524),
        KnownLocation("Salt Lake Kolkata", "Kolkata", "West Bengal", "700091", 22.5867, 88.4178),
        KnownLocation("Asansol Court Area", "Asansol", "West Bengal", "713304", 23.6889, 86.9661),
        KnownLocation("City Center Durgapur", "Durgapur", "West Bengal", "713216", 23.5204, 87.3119),
        KnownLocation("Bankura Town Market", "Bankura", "West Bengal", "722101", 23.2325, 87.0718)
    )

    private fun getNearestKnownCity(lat: Double, lng: Double): KnownLocation {
        return POPULAR_LOCATIONS.minByOrNull {
            calculateDistanceKm(lat, lng, it.latitude, it.longitude)
        } ?: POPULAR_LOCATIONS.first()
    }
}
