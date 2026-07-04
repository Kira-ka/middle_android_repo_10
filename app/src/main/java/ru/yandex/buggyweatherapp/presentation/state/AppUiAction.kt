package ru.yandex.buggyweatherapp.presentation.state

sealed interface AppUiAction {

    data class SearchWeatherByCity(val city: String) : AppUiAction
    data object ToggleFavorite : AppUiAction
    data object FetchCurrentLocationWeather : AppUiAction
}