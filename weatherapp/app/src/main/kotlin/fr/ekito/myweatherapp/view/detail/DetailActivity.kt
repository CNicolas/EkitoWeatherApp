package fr.ekito.myweatherapp.view.detail

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import fr.ekito.myweatherapp.R
import fr.ekito.myweatherapp.domain.DailyForecastModel
import fr.ekito.myweatherapp.domain.getColorFromCode
import fr.ekito.myweatherapp.util.ext.argument
import fr.ekito.myweatherapp.view.ErrorState
import fr.ekito.myweatherapp.view.IntentArguments.ARG_WEATHER_ITEM_ID
import kotlinx.android.synthetic.main.activity_detail.*
import org.koin.android.ext.android.get

/**
 * Weather Detail View
 */
class DetailActivity : AppCompatActivity() {

    // Detail id passed by argument
    private val detailId by argument<String>(ARG_WEATHER_ITEM_ID)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        val viewModel = ViewModelProviders.of(this).get(DetailViewModel::class.java)

        // Apply dependencies "manually"
        viewModel.apply {
            // Use the Koin org.koin.android.ext.android.get() function to retrieve dependencies from an Activity
            weatherRepository = get()
            schedulerProvider = get()
        }

        viewModel.states
                .observe(this, Observer { state ->
                    state?.let {
                        when (state) {
                            is ErrorState -> showError(state.error)
                            is DetailViewModel.WeatherDetailState -> showDetail(state.weather)
                        }
                    }
                })

        viewModel.getDetail(detailId)
    }

    fun showError(error: Throwable) {
        Snackbar.make(weatherItem, getString(R.string.loading_error) + " $error", Snackbar.LENGTH_LONG).show()
    }

    fun showDetail(weather: DailyForecastModel) {
        weatherIcon.text = weather.icon
        weatherDay.text = weather.day
        weatherText.text = weather.fullText
        weatherWindText.text = weather.wind.toString()
        weatherTempText.text = weather.temperature.toString()
        weatherHumidityText.text = weather.humidity.toString()
        weatherItem.background.setTint(getColorFromCode(weather))
        // Set back on background click
        weatherItem.setOnClickListener {
            onBackPressed()
        }
    }
}
