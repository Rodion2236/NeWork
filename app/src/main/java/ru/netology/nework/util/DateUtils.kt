package ru.netology.nework.util

import java.text.SimpleDateFormat
import java.util.*

object DateUtils {

    private const val OUTPUT_FORMAT = "dd.MM.yyyy HH:mm"
    fun formatIsoDate(isoString: String): String {
        return try {
            val datePart = isoString.substring(0, 10)
            val timePart = isoString.substring(11, 16)

            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val outputFormat = SimpleDateFormat(OUTPUT_FORMAT, Locale.getDefault())
            val date = inputFormat.parse(datePart)

            outputFormat.format(date) + " " + timePart
        } catch (e: Exception) {
            isoString
        }
    }

    fun formatTimestamp(timestamp: Long): String {
        return try {
            val formatter = SimpleDateFormat(OUTPUT_FORMAT, Locale.getDefault())
            formatter.format(Date(timestamp))
        } catch (e: Exception) {
            timestamp.toString()
        }
    }
}