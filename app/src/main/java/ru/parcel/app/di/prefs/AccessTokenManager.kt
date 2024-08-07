package ru.parcel.app.di.prefs

import android.content.SharedPreferences

class AccessTokenManager(
    private val sharedPreferences: SharedPreferences
) {
    private lateinit var token: String

    fun hasAccessToken(): Boolean {
        return sharedPreferences.contains("access_token")
    }

    fun getAccessToken(): String? {
        return if (this::token.isInitialized) {
            token
        } else {
            sharedPreferences.getString("access_token", null)
        }
    }

    fun clearAccountToken() {
        val editor = sharedPreferences.edit()
        editor.remove("access_token")
        editor.apply()
    }

    fun saveAccessToken(accessToken: String) {
        sharedPreferences.edit().putString("access_token", accessToken).apply()
    }
}