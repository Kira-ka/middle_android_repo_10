package ru.yandex.buggyweatherapp.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.yandex.buggyweatherapp.domaine.model.LocationEntity
import ru.yandex.buggyweatherapp.domaine.model.WeatherDataEntity
import ru.yandex.buggyweatherapp.domaine.repository.LocationRepository
import ru.yandex.buggyweatherapp.domaine.repository.WeatherRepository
import ru.yandex.buggyweatherapp.presentation.state.AppUiAction
import ru.yandex.buggyweatherapp.presentation.state.AppUiState
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository,
    private val locationRepository: LocationRepository,
) : ViewModel() {

    private val _state = MutableStateFlow<AppUiState>(
        AppUiState(
            weatherData = null,
            currentLocation = LocationEntity(0.0, 0.0, null),
            isLoading = true,
            error = null,
            cityName = "",
            isFavorite = false,
        )
    )
    val state: StateFlow<AppUiState> = _state.asStateFlow()

    init {
        fetchCurrentLocationWeather()
        startAutoRefresh()
    }

    fun reduce(action: AppUiAction) {
        when (action) {
            AppUiAction.FetchCurrentLocationWeather -> fetchCurrentLocationWeather()
            AppUiAction.ToggleFavorite -> toggleFavorite()
            is AppUiAction.SearchWeatherByCity -> searchWeatherByCity(action.city)
        }
    }

   private fun fetchCurrentLocationWeather() {
        viewModelScope.launch {
            try {
                updateState { loading() }

                val location = locationRepository.getCurrentLocation()
                if (location != null) {
                    updateState { updateLocation(location) }

                    val cityNameFromLocation = locationRepository.getCityNameFromLocation(location)
                    updateState { updateCityName(cityNameFromLocation.orEmpty()) }

                    getWeatherForLocation(location)
                } else {
                    updateState { updateError("Unable to get current location") }
                }
            } catch (e: CancellationException) {
                throw e
            } catch (throwable: Throwable) {
                updateState { updateError(throwable.message ?: "Unknown error") }
            }
        }
    }

    private fun getWeatherForLocation(location: LocationEntity) {
        viewModelScope.launch {
            try {
                updateState { loading() }

                val data = weatherRepository.getWeatherData(location)
                updateState { idle(data) }

            } catch (e: CancellationException) {
                throw e
            } catch (throwable: Throwable) {
                updateState { updateError(throwable.message ?: "Unknown error") }
            }
        }
    }

   private fun searchWeatherByCity(city: String) {
        if (city.isBlank()) {
            updateState { updateError("City name cannot be empty") }
            return
        }
        viewModelScope.launch {
            try {
                updateState { loading() }

                val data = weatherRepository.getWeatherByCity(city)
                updateState { idle(data) }
                updateState { updateCityName(city) }

            } catch (e: CancellationException) {
                throw e
            } catch (throwable: Throwable) {
                updateState { updateError(throwable.message ?: "Unknown error") }
            }
        }
    }


    private fun startAutoRefresh() {
        viewModelScope.launch {
            while (true) {
                delay(60_000)
                state.value.currentLocation?.let { location ->
                    getWeatherForLocation(location)
                }
            }
        }
    }


    private fun toggleFavorite() {
        updateState { updateFavorite() }
    }

    fun updateState(modifier: AppUiState.() -> AppUiState) {
        _state.update { it.modifier() }
    }
}

fun AppUiState.loading() = copy(isLoading = true, error = null)

fun AppUiState.updateLocation(location: LocationEntity?) = copy(currentLocation = location)

fun AppUiState.updateCityName(name: String) = copy(cityName = name)

fun AppUiState.updateError(text: String) = copy(isLoading = false, error = text)

fun AppUiState.idle(data: WeatherDataEntity) =
    copy(weatherData = data, isLoading = false, error = null)

fun AppUiState.updateFavorite() =
    copy(isFavorite = !isFavorite)