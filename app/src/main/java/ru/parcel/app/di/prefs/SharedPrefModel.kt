package ru.parcel.app.di.prefs

import android.content.Context
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

object SharedPrefModel {
    val prefModule = module {
        single {
            androidContext().getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        }
    }
}