package com.ai.app.move.deskercise.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateTimeUtils {

    const val dd_MM_YYYY_hh_mm_12 = "dd/MM/yyyy hh:mm a"
    fun getDateTimeFormat(date: Date, typeFormat: String): String {
        val simpleDateFormat = SimpleDateFormat(typeFormat, Locale.getDefault())
        return simpleDateFormat.format(date)
    }
}