package fr.ekito.myweatherapp.view.weather

import android.app.AlertDialog
import android.arch.lifecycle.Observer
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v4.app.Fragment
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import fr.ekito.myweatherapp.R
import fr.ekito.myweatherapp.domain.DailyForecastModel
import fr.ekito.myweatherapp.domain.getColorFromCode
import fr.ekito.myweatherapp.view.IntentArguments
import fr.ekito.myweatherapp.view.detail.DetailActivity
import kotlinx.android.synthetic.main.fragment_result_header.*
import org.jetbrains.anko.startActivity
import org.koin.android.architecture.ext.sharedViewModel

class WeatherHeaderFragment : Fragment() {

    private val viewModel by sharedViewModel<WeatherViewModel>()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_result_header, container, false) as ViewGroup
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.states
                .observe(this, Observer { states ->
                    states?.let { state ->
                        when (state) {
                            is WeatherViewModel.WeatherListState ->
                                showWeather(state.location, state.first)
                        }
                    }
                })

        viewModel.events
                .observe(this, Observer { states ->
                    states?.let { state ->
                        when (state) {
                            is WeatherViewModel.LoadingLocationEvent ->
                                showLoadingLocation(state.location)
                            is WeatherViewModel.LoadLocationFailedEvent ->
                                showLocationSearchFailed(state.location, state.error)
                        }
                    }
                })
    }

    fun showWeather(location: String, weather: DailyForecastModel) {
        weatherCity.text = location
        weatherCityCard.setOnClickListener {
            promptLocationDialog()
        }

        weatherIcon.text = weather.icon
        weatherDay.text = weather.day
        weatherTempText.text = weather.temperature.toString()
        weatherText.text = weather.shortText

        val color = context!!.getColorFromCode(weather)
        weatherHeader.background.setTint(color)

        weatherHeader.setOnClickListener {
            activity?.startActivity<DetailActivity>(
                    IntentArguments.ARG_WEATHER_ITEM_ID to weather.id
            )
        }
    }

    private fun promptLocationDialog() {
        val editText = EditText(context)
        editText.hint = getString(R.string.location_hint)
        editText.maxLines = 1
        editText.inputType = InputType.TYPE_TEXT_VARIATION_POSTAL_ADDRESS

        val dialog = AlertDialog.Builder(context)
        dialog.setTitle(getString(R.string.enter_location))
        dialog.setView(editText)
        dialog.setPositiveButton(getString(R.string.search)) { dialogInterface, _ ->
            dialogInterface.dismiss()
            val newLocation = editText.text.trim().toString()
            viewModel.loadNewLocation(newLocation)
            showLoadingLocation(newLocation)
        }
        dialog.setNegativeButton(getString(R.string.cancel)) { dialogInterface, _ ->
            dialogInterface.dismiss()
        }
        dialog.show()
    }

    fun showLoadingLocation(location: String) {
        Snackbar.make(
                weatherHeader,
                getString(R.string.loading_location) + " $location ...",
                Snackbar.LENGTH_LONG
        )
                .show()
    }

    fun showLocationSearchFailed(location: String, error: Throwable) {
        Snackbar.make(weatherHeader, getString(R.string.loading_error), Snackbar.LENGTH_LONG)
                .setAction(R.string.retry, {
                    viewModel.loadNewLocation(location)
                })
                .show()
    }
}