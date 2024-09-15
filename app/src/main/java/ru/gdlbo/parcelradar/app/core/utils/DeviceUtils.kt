package ru.gdlbo.parcelradar.app.core.utils

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import android.provider.Settings
import java.util.Locale

object DeviceUtils {
    private var androidId: String = ""

    fun getAndroidId(): String {
        return androidId
    }

    @SuppressLint("HardwareIds")
    fun setDeviceId(context: Context) {
        try {
            val contentResolver = context.contentResolver
            androidId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getDeviceName(): String {
        val manufacturer = Build.MANUFACTURER
        val model = Build.MODEL
        val cleanedManufacturer = Regex("[^\\x00-\\x7F]").replace(manufacturer, "")
        val cleanedModel = Regex("[^\\x00-\\x7F]").replace(model, "")

        if (cleanedModel.startsWith(cleanedManufacturer, ignoreCase = false)) {
            return if (cleanedModel.isNotEmpty()) {
                val firstChar = cleanedModel[0]
                val titleCasedFirstChar = if (firstChar.isLowerCase()) {
                    Locale.getDefault().let { firstChar.titlecase(it) }
                } else {
                    firstChar.toString()
                }
                titleCasedFirstChar + cleanedModel.substring(1)
            } else {
                cleanedModel
            }
        }

        val manufacturerName = if (cleanedManufacturer.isNotEmpty()) {
            val firstChar = cleanedManufacturer[0]
            val titleCasedFirstChar = if (firstChar.isLowerCase()) {
                Locale.getDefault().let { firstChar.titlecase(it) }
            } else {
                firstChar.toString()
            }
            titleCasedFirstChar + cleanedManufacturer.substring(1)
        } else {
            cleanedManufacturer
        }

        val modelName = if (cleanedModel.isNotEmpty()) {
            val firstChar = cleanedModel[0]
            val titleCasedFirstChar = if (firstChar.isLowerCase()) {
                Locale.getDefault().let { firstChar.titlecase(it) }
            } else {
                firstChar.toString()
            }
            titleCasedFirstChar + cleanedModel.substring(1)
        } else {
            cleanedModel
        }

        return "$manufacturerName $modelName"
    }
}
