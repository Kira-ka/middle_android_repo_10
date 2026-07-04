package ru.yandex.buggyweatherapp.data.repository

import android.location.Geocoder
import android.util.Log
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import kotlinx.coroutines.suspendCancellableCoroutine
import ru.yandex.buggyweatherapp.domaine.model.LocationEntity
import ru.yandex.buggyweatherapp.domaine.repository.LocationRepository
import ru.yandex.buggyweatherapp.data.location.LocationTracker
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class LocationRepositoryImpl @Inject constructor(
    private val fusedLocationClient: FusedLocationProviderClient,
    private val geocoder: Geocoder,
    private val locationTracker: LocationTracker
) : LocationRepository {

    override suspend fun getCurrentLocation(): LocationEntity? {
        return try {
            suspendCancellableCoroutine { continuation ->
                fusedLocationClient.getCurrentLocation(
                    Priority.PRIORITY_HIGH_ACCURACY,
                    null
                ).addOnSuccessListener { location ->
                    if (location != null) {
                        continuation.resume(
                            LocationEntity(location.latitude, location.longitude)
                        )
                    } else {
                        continuation.resume(null)
                    }
                }.addOnFailureListener { e ->
                    Log.e("LocationRepository", "Error getting location", e)
                    continuation.resumeWithException(e)
                }
            }
        } catch (e: SecurityException) {
            Log.e("LocationRepository", "Location permission not granted", e)
            null
        }
    }


    override fun getCityNameFromLocation(location: LocationEntity): String? {
        try {

            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)

            return if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                if (address.locality != null) {
                    address.locality
                } else if (address.subAdminArea != null) {
                    address.subAdminArea
                } else {
                    address.adminArea
                }
            } else {
                null
            }
        } catch (e: Exception) {
            Log.e("LocationRepository", "Error getting city name", e)
            return null
        }
    }


    override fun startLocationTracking() {
        locationTracker.startTracking()
    }
}