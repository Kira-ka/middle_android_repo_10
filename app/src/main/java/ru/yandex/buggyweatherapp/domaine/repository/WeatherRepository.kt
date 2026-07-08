package ru.yandex.buggyweatherapp.domaine.repository

import ru.yandex.buggyweatherapp.domaine.model.LocationEntity
import ru.yandex.buggyweatherapp.domaine.model.WeatherDataEntity

interface WeatherRepository {

    suspend fun getWeatherData(location: LocationEntity): WeatherDataEntity

    suspend fun getWeatherByCity(cityName: String): WeatherDataEntity
}