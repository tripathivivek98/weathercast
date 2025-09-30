package com.tripathivivek98.weathercast

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WeatherViewModel : ViewModel() {
    // NOTE: In a production app, the API key should be stored securely, not here.
    private val API: String = "20ad41e36c41240be024bfc5d2f93f46"

    // LiveData to hold the UI state
    private val _weatherData = MutableLiveData<WeatherData>()
    val weatherData: LiveData<WeatherData> = _weatherData

    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _isError = MutableLiveData(false)
    val isError: LiveData<Boolean> = _isError

    init {
        // Fetch weather for the default city on initialization
        // This is the initial hardcoded city.
        fetchWeather("noida,in")
    }

    /**
     * Data class to hold all parsed weather information
     */
    data class WeatherData(
        val address: String = "Fetching...",
        val updatedAtText: String = "--",
        val status: String = "Loading...",
        val temp: String = "--°C",
        val tempMin: String = "Min Temp: --°C",
        val tempMax: String = "Max Temp: --°°C",
        val sunrise: String = "--:-- am",
        val sunset: String = "--:-- pm",
        val windSpeed: String = "--",
        val pressure: String = "--",
        val humidity: String = "--"
    )

    /**
     * Fetches weather data for the specified city.
     * @param city The city name and country code (e.g., "London,uk").
     */
    fun fetchWeather(city: String) {
        _isLoading.value = true
        _isError.value = false

        viewModelScope.launch {
            try {
                // Perform network operation on IO dispatcher
                val result = withContext(Dispatchers.IO) {
                    URL("https://api.openweathermap.org/data/2.5/weather?q=$city&units=metric&appid=$API")
                        .readText(Charsets.UTF_8)
                }

                // Parse the result back on the main thread
                parseWeather(result)

            } catch (e: Exception) {
                Log.e("WeatherViewModel", "Network error for city $city: ${e.message}")
                _isLoading.value = false
                _isError.value = true
            }
        }
    }

    /**
     * Parses the JSON response and updates the LiveData.
     */
    private fun parseWeather(result: String) {
        try {
            val jsonObject = JSONObject(result)
            val main = jsonObject.getJSONObject("main")
            val sys = jsonObject.getJSONObject("sys")
            val wind = jsonObject.getJSONObject("wind")
            val weather = jsonObject.getJSONArray("weather").getJSONObject(0)

            val updatedAt: Long = jsonObject.getLong("dt")
            val sunrise: Long = sys.getLong("sunrise")
            val sunset: Long = sys.getLong("sunset")

            val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.ENGLISH)
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.ENGLISH)

            val data = WeatherData(
                address = jsonObject.getString("name") + ", " + sys.getString("country"),
                updatedAtText = "Updated at: " + simpleDateFormat.format(Date(updatedAt * 1000)),
                status = weather.getString("description").replaceFirstChar { it.uppercase() },
                temp = main.getString("temp") + "°C",
                tempMin = "Min Temp : " + main.getString("temp_min") + "°C",
                tempMax = "Max Temp : " + main.getString("temp_max") + "°C",
                sunrise = timeFormat.format(Date(sunrise * 1000)),
                sunset = timeFormat.format(Date(sunset * 1000)),
                windSpeed = wind.getString("speed"),
                pressure = main.getString("pressure"),
                humidity = main.getString("humidity")
            )

            _weatherData.value = data
            _isLoading.value = false
            _isError.value = false

        } catch (e: Exception) {
            Log.e("WeatherViewModel", "JSON parsing error: ${e.message}")
            _isLoading.value = false
            _isError.value = true
        }
    }
}
