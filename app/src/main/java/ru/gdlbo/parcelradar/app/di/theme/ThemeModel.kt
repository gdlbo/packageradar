package ru.gdlbo.parcelradar.app.di.theme

import android.content.Context
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

object ThemeModel {
    val themeModule = module {
        single {
            ThemeManager(
                sharedPref = androidContext().getSharedPreferences(
                    "app_preferences",
                    Context.MODE_PRIVATE
                )
            )
        }
    }
}