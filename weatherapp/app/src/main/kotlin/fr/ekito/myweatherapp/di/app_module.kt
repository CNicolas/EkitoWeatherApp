package fr.ekito.myweatherapp.di

import fr.ekito.myweatherapp.data.repository.WeatherRepository
import fr.ekito.myweatherapp.data.repository.WeatherRepositoryImpl
import fr.ekito.myweatherapp.util.rx.ApplicationSchedulerProvider
import fr.ekito.myweatherapp.util.rx.SchedulerProvider
import fr.ekito.myweatherapp.view.splash.SplashViewModel
import fr.ekito.myweatherapp.view.weather.WeatherViewModel
import org.koin.android.architecture.ext.viewModel
import org.koin.dsl.module.applicationContext

/**
 * App Components
 */
val weatherAppModule = applicationContext {
    // declare SplashViewModel for Splash View
    viewModel { SplashViewModel(get(), get()) }

    // WeatherViewModel declaration for Weather View components
    viewModel { WeatherViewModel(get(), get()) }

    // Weather Data Repository
    bean { WeatherRepositoryImpl(get()) as WeatherRepository }

    // Rx Schedulers
    bean { ApplicationSchedulerProvider() as SchedulerProvider }
}

// Gather all app modules
val onlineWeatherApp = listOf(weatherAppModule, remoteDatasourceModule)
val offlineWeatherApp = listOf(weatherAppModule, localAndroidDatasourceModule)