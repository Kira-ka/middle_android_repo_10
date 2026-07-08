package ru.yandex.buggyweatherapp.presentation.state

import ru.yandex.buggyweatherapp.domaine.model.LocationEntity
import ru.yandex.buggyweatherapp.domaine.model.WeatherDataEntity

data class AppUiState(
    val weatherData: WeatherDataEntity?,
    val currentLocation: LocationEntity?,
    val isLoading: Boolean,
    val error: String?,
    val cityName: String,
    val isFavorite: Boolean,
)