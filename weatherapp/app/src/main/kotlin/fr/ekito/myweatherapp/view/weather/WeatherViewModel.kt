package fr.ekito.myweatherapp.view.weather

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import fr.ekito.myweatherapp.data.repository.WeatherRepository
import fr.ekito.myweatherapp.domain.DailyForecastModel
import fr.ekito.myweatherapp.util.mvvm.RxViewModel
import fr.ekito.myweatherapp.util.mvvm.SingleLiveEvent
import fr.ekito.myweatherapp.util.rx.SchedulerProvider
import fr.ekito.myweatherapp.util.rx.with
import fr.ekito.myweatherapp.view.ErrorState
import fr.ekito.myweatherapp.view.Event
import fr.ekito.myweatherapp.view.LoadingState
import fr.ekito.myweatherapp.view.State


class WeatherViewModel(
        private val weatherRepository: WeatherRepository,
        private val schedulerProvider: SchedulerProvider
) : RxViewModel() {

    private val _states = MutableLiveData<State>()
    val states: LiveData<State>
        get() = _states

    private val _events = SingleLiveEvent<Event>()
    val events: LiveData<Event>
        get() = _events

    fun loadNewLocation(location: String) {
        _events.value = LoadingLocationEvent(location)

        launch {
            weatherRepository.getWeather(location)
                    .with(schedulerProvider)
                    .subscribe(
                            { weather ->
                                _states.value = WeatherListState.from(weather)
                            },
                            { error ->
                                _events.value = LoadLocationFailedEvent(location, error)
                            })
        }
    }

    fun getWeather() {
        _states.value = LoadingState

        launch {
            weatherRepository.getWeather()
                    .with(schedulerProvider)
                    .subscribe(
                            { weather -> _states.value = WeatherListState.from(weather) },
                            { error -> _states.value = ErrorState(error) })
        }
    }

    data class WeatherListState(
            val location: String,
            val first: DailyForecastModel,
            val lasts: List<DailyForecastModel>
    ) : State() {
        companion object {
            fun from(list: List<DailyForecastModel>): WeatherListState {
                return if (list.isEmpty()) error("weather list should not be empty")
                else {
                    val first = list.first()
                    val location = first.location
                    WeatherListState(location, first, list.takeLast(list.size - 1))
                }
            }
        }
    }

    data class LoadingLocationEvent(val location: String) : Event()
    data class LoadLocationFailedEvent(val location: String, val error: Throwable) : Event()
}