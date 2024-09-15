package ru.gdlbo.parcelradar.app.di.prefs

import org.koin.dsl.module

object ATMModel {
    val atmModule = module {
        single {
            AccessTokenManager(get())
        }
    }
}