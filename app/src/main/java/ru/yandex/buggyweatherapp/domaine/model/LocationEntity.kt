package ru.yandex.buggyweatherapp.domaine.model

data class LocationEntity(
    val latitude: Double,
    val longitude: Double,
    val name: String? = null
)