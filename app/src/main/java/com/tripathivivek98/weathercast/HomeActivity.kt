package com.tripathivivek98.weathercast

import android.os.Bundle
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.tripathivivek98.weathercast.databinding.ActivityHomeBinding

class HomeActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHomeBinding
    // Uses the 'by viewModels()' delegate which requires the activity-ktx dependency
    private val viewModel: WeatherViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set the ViewModel and LifecycleOwner for Data Binding
        binding.viewModel = viewModel
        binding.lifecycleOwner = this

        // Set up the click listener for the search icon (city change)
        // This launches the dialog when the icon next to the city name is tapped.
        binding.searchIcon.setOnClickListener {
            showCityInputDialog()
        }

        // Initial fetch is handled in the ViewModel's init block.
    }

    /**
     * Displays a simple dialog to allow the user to input a new city name.
     */
    private fun showCityInputDialog() {
        // Use an EditText for user input
        val input = EditText(this).apply {
            hint = "City, e.g., Delhi,Noida"
            // Apply padding to the input view for better appearance in the dialog
            setPadding(50, 50, 50, 50)
        }

        AlertDialog.Builder(this)
            .setTitle("Change City")
            .setView(input) // Set the EditText as the dialog's content
            .setPositiveButton("Search") { dialog, _ ->
                val newCity = input.text.toString().trim()
                if (newCity.isNotEmpty()) {
                    // Call the ViewModel function to fetch weather for the new city
                    viewModel.fetchWeather(newCity)
                } else {
                    Toast.makeText(this, "Please enter a city.", Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }
            .show()
    }
}
