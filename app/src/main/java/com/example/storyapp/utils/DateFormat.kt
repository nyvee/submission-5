package com.example.storyapp.utils

import java.text.SimpleDateFormat
import java.util.*

object DateFormat {
    fun format(dateTime: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date = inputFormat.parse(dateTime)

        val outputFormat = SimpleDateFormat("d MMMM yyyy, hh:mm a", Locale.getDefault())
        return outputFormat.format(date)
    }
}