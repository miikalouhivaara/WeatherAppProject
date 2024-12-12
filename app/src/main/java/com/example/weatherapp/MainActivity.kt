package com.example.weatherapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.weatherapp.models.WeatherResponse
import com.example.weatherapp.network.ApiClient
import com.example.weatherapp.network.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private lateinit var cityName: TextView
    private lateinit var temperature: TextView
    private lateinit var weatherDescription: TextView
    private lateinit var weatherIcon: ImageView
    private lateinit var shareButton: Button
    private lateinit var navigateButton: Button
    private lateinit var backgroundImageView: ImageView

    private val CITY_REQUEST_CODE = 1  // Käytetään tätä tunnistamaan kaupungin valinta

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Yhdistetään käyttöliittymäelementit
        cityName = findViewById(R.id.cityName)
        temperature = findViewById(R.id.temperature)
        weatherDescription = findViewById(R.id.weatherDescription)
        weatherIcon = findViewById(R.id.weatherIcon)
        shareButton = findViewById(R.id.shareButton)
        navigateButton = findViewById(R.id.navigateButton)
        backgroundImageView = findViewById(R.id.backgroundImageView)

        // Asetetaan painikkeet
        navigateButton.setOnClickListener {
            // Siirrytään SecondActivityyn kaupungin valintaan
            val intent = Intent(this, SecondActivity::class.java)
            startActivityForResult(intent, CITY_REQUEST_CODE)
        }

        shareButton.setOnClickListener {
            shareWeatherDetails()
        }

        // Lue tallennettu kaupunki SharedPreferencesista ja hae säätiedot
        val savedCity = getCityFromPreferences()
        if (savedCity.isNotEmpty()) {
            fetchWeather(savedCity) // Haetaan säätiedot tallennetusta kaupungista
        } else {
            showToast("No city selected yet.") // Näytetään viesti, jos kaupunkia ei ole valittu
        }
    }

    // Tämä funktio palauttaa kaupungin valinnan SecondActivitystä
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CITY_REQUEST_CODE && resultCode == RESULT_OK) {
            val selectedCity = data?.getStringExtra("selectedCity") ?: "Helsinki"
            saveCityToPreferences(selectedCity)  // Tallennetaan valittu kaupunki
            fetchWeather(selectedCity)  // Haetaan valitun kaupungin säätiedot
        }
    }

    // Funktio säätiedon hakemiseksi valitulle kaupungille
    private fun fetchWeather(city: String) {
        val apiService = ApiClient.retrofit.create(ApiService::class.java)

        val call = apiService.getCurrentWeather(
            city,
            getString(R.string.weather_api_key) // API-avain
        )

        call.enqueue(object : Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>) {
                if (response.isSuccessful) {
                    val weather = response.body()
                    weather?.let {
                        cityName.text = it.name
                        temperature.text = "${it.main.temp} °C"
                        weatherDescription.text = it.weather[0].description
                        Glide.with(this@MainActivity)
                            .load("https://openweathermap.org/img/wn/${it.weather[0].icon}@2x.png")
                            .into(weatherIcon)

                        // Lataa taustakuva kaupungin mukaan
                        loadBackgroundImage(city)
                    }
                } else {
                    showToast("Error: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                t.printStackTrace()
                showToast("Failed to fetch weather data")
            }
        })
    }

    // Funktio taustakuvan lataamiseksi kaupungin mukaan
    private fun loadBackgroundImage(city: String) {
        val imageUrl = when (city) {
            "Espoo" -> "https://tripsteri.fi/wp-content/uploads/2020/09/Tripsteri-Espoo-innovation-garden-Keilaniemi-Laguuni.jpg"
            "Helsinki" -> "https://images.ctfassets.net/uy1xharimxu3/4wkpGLvz2Og6XxSJugMpn7/b0376d6a351ba97e215f08ee46346fe0/Helsinki_IMG_1372.jpg"
            else -> "https://www.jalkipeli.net/wp-content/uploads/2023/12/nasinneula01.jpg" // Oletuskuva
        }

        Glide.with(this)
            .load(imageUrl)
            .into(backgroundImageView)
    }

    private fun shareWeatherDetails() {
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(
                Intent.EXTRA_TEXT,
                "Sää ${cityName.text}: ${temperature.text}, ${weatherDescription.text}"
            )
        }
        startActivity(Intent.createChooser(shareIntent, "Jaa säätiedot"))
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    // Funktio kaupungin hakemiseen SharedPreferencesista
    private fun getCityFromPreferences(): String {
        val sharedPreferences = getSharedPreferences("WeatherAppPrefs", MODE_PRIVATE)
        return sharedPreferences.getString("selectedCity", "Helsinki") ?: "Helsinki"
    }

    // Funktio kaupungin tallentamiseen SharedPreferencesiin
    private fun saveCityToPreferences(city: String) {
        val sharedPreferences = getSharedPreferences("WeatherAppPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("selectedCity", city)  // Tallennetaan kaupunki
        editor.apply()  // Tallennetaan tiedot
    }
}
