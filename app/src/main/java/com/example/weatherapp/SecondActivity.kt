package com.example.weatherapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import android.graphics.drawable.Drawable

class SecondActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)

        // Haetaan valittu kaupunki MainActivityltä (tämä on oletuksena null)
        val selectedCity = intent.getStringExtra("selectedCity") ?: "Helsinki"

        // Ladataan taustakuva valitun kaupungin perusteella
        val imageUrl = when (selectedCity) {
            "Helsinki" -> {
                "https://img.freepik.com/free-vector/sky-background-video-conferencing_23-2148623068.jpg"
            }
            "Espoo" -> {
                "https://tripsteri.fi/wp-content/uploads/2020/09/Tripsteri-Espoo-innovation-garden-Keilaniemi-Laguuni.jpg"
            }
            "Tampere" -> {
                "https://img.freepik.com/free-vector/sky-background-video-conferencing_23-2148623068.jpg"
            }
            else -> {
                // Oletuksena Tampereen taustakuva
                "https://img.freepik.com/free-vector/sky-background-video-conferencing_23-2148623068.jpg"
            }
        }

        // Ladataan taustakuva Glide-kirjastolla
        Glide.with(this)
            .load(imageUrl)
            .listener(object : RequestListener<Drawable> {
                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    Log.e("SecondActivity", "Image load failed: $e")  // Logitetaan virhe
                    return false  // Ei estetä virheellistä kuvan lataamista
                }

                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: Target<Drawable>?,
                    dataSource: com.bumptech.glide.load.DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    return false // Kuva latautui onnistuneesti
                }
            })
            .into(findViewById<ImageView>(R.id.backgroundImageView))

        // Asetetaan napit valitsemaan kaupunki
        findViewById<Button>(R.id.buttonHelsinki).setOnClickListener {
            returnCityResult("Helsinki")
        }
        findViewById<Button>(R.id.buttonEspoo).setOnClickListener {
            returnCityResult("Espoo")
        }
        findViewById<Button>(R.id.buttonTampere).setOnClickListener {
            returnCityResult("Tampere")
        }
    }

    // Funktio kaupungin valinnan palauttamiseen MainActivityyn
    private fun returnCityResult(city: String) {
        val resultIntent = Intent()
        resultIntent.putExtra("selectedCity", city)
        setResult(Activity.RESULT_OK, resultIntent)
        finish() // Sulkee SecondActivityn ja palaa MainActivityyn
    }
}
