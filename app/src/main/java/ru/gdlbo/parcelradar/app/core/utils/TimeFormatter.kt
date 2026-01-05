package ru.gdlbo.parcelradar.app.core.utils

import android.content.Context
import ru.gdlbo.parcelradar.app.R
import java.text.SimpleDateFormat
import java.util.*

class TimeFormatter {
    fun formatTimeString(timeString: String, context: Context, useLocalTime: Boolean = false): String {
        val localTimeZone = TimeZone.getDefault()
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

        if (useLocalTime) {
            inputFormat.timeZone = TimeZone.getTimeZone("GMT+3")
        } else {
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        }

        val outputFormatToday = SimpleDateFormat("HH:mm", Locale.getDefault())
        val outputFormatYesterday = SimpleDateFormat("HH:mm", Locale.getDefault())
        val outputFormatThisYear = SimpleDateFormat("d MMMM", Locale.getDefault())
        val outputFormatPastYear = SimpleDateFormat("d MMM yyyy", Locale.getDefault())

        if (useLocalTime) {
            outputFormatToday.timeZone = localTimeZone
            outputFormatYesterday.timeZone = localTimeZone
            outputFormatThisYear.timeZone = localTimeZone
            outputFormatPastYear.timeZone = localTimeZone
        } else {
            outputFormatToday.timeZone = TimeZone.getTimeZone("UTC")
            outputFormatYesterday.timeZone = TimeZone.getTimeZone("UTC")
            outputFormatThisYear.timeZone = TimeZone.getTimeZone("UTC")
            outputFormatPastYear.timeZone = TimeZone.getTimeZone("UTC")
        }

        val parsedTime = inputFormat.parse(timeString)
        val parsedCalendar = Calendar.getInstance()
        parsedCalendar.time = parsedTime!!

        val referenceTimeZone = if (useLocalTime) localTimeZone else TimeZone.getTimeZone("UTC")

        val todayStart = Calendar.getInstance(referenceTimeZone)
        todayStart.set(Calendar.HOUR_OF_DAY, 0)
        todayStart.set(Calendar.MINUTE, 0)
        todayStart.set(Calendar.SECOND, 0)
        todayStart.set(Calendar.MILLISECOND, 0)

        val yesterdayStart = Calendar.getInstance(referenceTimeZone)
        yesterdayStart.add(Calendar.DAY_OF_MONTH, -1)
        yesterdayStart.set(Calendar.HOUR_OF_DAY, 0)
        yesterdayStart.set(Calendar.MINUTE, 0)
        yesterdayStart.set(Calendar.SECOND, 0)
        yesterdayStart.set(Calendar.MILLISECOND, 0)

        val thisWeekStart = Calendar.getInstance(referenceTimeZone)
        thisWeekStart.set(Calendar.DAY_OF_WEEK, thisWeekStart.firstDayOfWeek)
        thisWeekStart.set(Calendar.HOUR_OF_DAY, 0)
        thisWeekStart.set(Calendar.MINUTE, 0)
        thisWeekStart.set(Calendar.SECOND, 0)
        thisWeekStart.set(Calendar.MILLISECOND, 0)

        val thisMonthStart = Calendar.getInstance(referenceTimeZone)
        thisMonthStart.set(Calendar.DAY_OF_MONTH, 1)
        thisMonthStart.set(Calendar.HOUR_OF_DAY, 0)
        thisMonthStart.set(Calendar.MINUTE, 0)
        thisMonthStart.set(Calendar.SECOND, 0)
        thisMonthStart.set(Calendar.MILLISECOND, 0)

        val thisYearStart = Calendar.getInstance(referenceTimeZone)
        thisYearStart.set(Calendar.MONTH, Calendar.JANUARY)
        thisYearStart.set(Calendar.DAY_OF_MONTH, 1)
        thisYearStart.set(Calendar.HOUR_OF_DAY, 0)
        thisYearStart.set(Calendar.MINUTE, 0)
        thisYearStart.set(Calendar.SECOND, 0)
        thisYearStart.set(Calendar.MILLISECOND, 0)

        return when {
            parsedCalendar.time.after(todayStart.time) -> { // If the time is today
                outputFormatToday.format(parsedTime)
            }

            parsedCalendar.time.after(yesterdayStart.time) -> { // If the time is yesterday
                context.getString(R.string.yesterday_at) + " " + outputFormatYesterday.format(
                    parsedTime
                )
            }

            parsedCalendar.time.after(thisWeekStart.time) -> { // If the time is within this week
                outputFormatThisYear.format(parsedTime)
            }

            parsedCalendar.time.after(thisMonthStart.time) -> { // If the time is within this month
                outputFormatThisYear.format(parsedTime)
            }

            parsedCalendar.time.after(thisYearStart.time) -> { // If the time is within this year
                outputFormatThisYear.format(parsedTime)
            }

            else -> { // If the time is from a different year
                outputFormatPastYear.format(parsedTime)
            }
        }
    }
}