package ru.yandex.buggyweatherapp.data.location

import android.content.Context
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import dagger.hilt.android.qualifiers.ApplicationContext
import ru.yandex.buggyweatherapp.domaine.model.LocationEntity
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationTracker @Inject constructor(
    @ApplicationContext  private val context: Context
) {
    private val locationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager


    private val listeners =
        CopyOnWriteArrayList<(LocationEntity) -> Unit>()


    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: android.location.Location) {

            val newLocation = LocationEntity(
                latitude = location.latitude,
                longitude = location.longitude
            )


            notifyListeners(newLocation)
        }

        @Deprecated("Deprecated in Java")
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {
        }

        override fun onProviderEnabled(provider: String) {}

        override fun onProviderDisabled(provider: String) {}
    }


    fun startTracking() {
        try {

            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                5000, // 5 секунд
                10f, // 10 метров
                locationListener
            )


        } catch (e: SecurityException) {
            Log.e("LocationTracker", "Permission denied", e)
        } catch (e: Exception) {
            Log.e("LocationTracker", "Error starting location tracking", e)
        }
    }


    fun addListener(listener: (LocationEntity) -> Unit) {
        listeners.add(listener)
    }

    private fun notifyListeners(location: LocationEntity) {
        for (listener in listeners) {
            listener(location)
        }
    }


}