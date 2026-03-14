package ru.netology.nework.util

import java.text.SimpleDateFormat
import java.util.*
import java.util.Locale.getDefault

object DateUtils {

    private const val DATE_FORMAT = "dd.MM.yyyy HH:mm"
    private val formatter = SimpleDateFormat(DATE_FORMAT, getDefault())

    fun formatTimestamp(timestamp: Long): String =
        formatter.format(Date(timestamp))
}