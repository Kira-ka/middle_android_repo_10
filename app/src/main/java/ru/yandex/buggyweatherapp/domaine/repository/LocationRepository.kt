package ru.yandex.buggyweatherapp.domaine.repository

import ru.yandex.buggyweatherapp.domaine.model.LocationEntity

interface LocationRepository {

    suspend fun getCurrentLocation(): LocationEntity?

    fun getCityNameFromLocation(location: LocationEntity): String?

    fun startLocationTracking()
}