package ru.yandex.buggyweatherapp.data.repository

import com.google.gson.JsonObject
import ru.yandex.buggyweatherapp.data.api.WeatherApiService
import ru.yandex.buggyweatherapp.domaine.model.LocationEntity
import ru.yandex.buggyweatherapp.domaine.model.WeatherDataEntity
import ru.yandex.buggyweatherapp.domaine.repository.WeatherRepository
import javax.inject.Inject

class WeatherRepositoryImpl @Inject constructor(private val weatherApi: WeatherApiService) :
    WeatherRepository {

    override suspend fun getWeatherData(location: LocationEntity): WeatherDataEntity {
        val data = weatherApi.getCurrentWeather(location.latitude, location.longitude)
        return parseWeatherData(data, location)
    }

    override suspend fun getWeatherByCity(cityName: String): WeatherDataEntity {
        val json = weatherApi.getWeatherByCity(cityName)
        val location = extractLocationFromResponse(json)
        return parseWeatherData(json, location)
    }


    private fun parseWeatherData(json: JsonObject, location: LocationEntity): WeatherDataEntity {

        val main = json.getAsJsonObject("main")
        val wind = json.getAsJsonObject("wind")
        val sys = json.getAsJsonObject("sys")
        val weather = json.getAsJsonArray("weather").get(0).asJsonObject
        val clouds = json.getAsJsonObject("clouds")

        return WeatherDataEntity(
            cityName = json.get("name").asString,
            country = sys.get("country").asString,
            temperature = main.get("temp").asDouble,
            feelsLike = main.get("feels_like").asDouble,
            minTemp = main.get("temp_min").asDouble,
            maxTemp = main.get("temp_max").asDouble,
            humidity = main.get("humidity").asInt,
            pressure = main.get("pressure").asInt,
            windSpeed = wind.get("speed").asDouble,
            windDirection = if (wind.has("deg")) wind.get("deg").asInt else 0,
            description = weather.get("description").asString,
            icon = weather.get("icon").asString,
            cloudiness = clouds.get("all").asInt,
            sunriseTime = sys.get("sunrise").asLong,
            sunsetTime = sys.get("sunset").asLong,
            timezone = json.get("timezone").asInt,
            timestamp = json.get("dt").asLong,
            rawApiData = json.toString(),
            rain = if (json.has("rain") && json.getAsJsonObject("rain").has("1h"))
                json.getAsJsonObject("rain").get("1h").asDouble else null,
            snow = if (json.has("snow") && json.getAsJsonObject("snow").has("1h"))
                json.getAsJsonObject("snow").get("1h").asDouble else null
        )
    }

    private fun extractLocationFromResponse(json: JsonObject): LocationEntity {
        val coord = json.getAsJsonObject("coord")
        val lat = coord.get("lat").asDouble
        val lon = coord.get("lon").asDouble
        val name = json.get("name").asString

        return LocationEntity(lat, lon, name)
    }
}