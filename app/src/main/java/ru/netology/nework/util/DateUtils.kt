package ru.netology.nework.util

import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

object DateUtils {

    private const val OUTPUT_FORMAT = "dd.MM.yyyy HH:mm"
    private const val INPUT_FORMAT_FULL = "yyyy-MM-dd'T'HH:mm:ss"
    private const val INPUT_FORMAT_DATE = "yyyy-MM-dd"
    private val ISO_DATE_PATTERN = Pattern.compile("^(\\d{4})-(\\d{2})-(\\d{2})[T\\s](\\d{2}):(\\d{2})")

    fun formatIsoDate(isoString: String?, errorText: String = "Ошибка даты"): String {
        if (isoString.isNullOrBlank()) return ""

        return try {
            val matcher = ISO_DATE_PATTERN.matcher(isoString)
            if (!matcher.find()) return errorText

            val year = matcher.group(1).toIntOrNull()
            if (year == null || year < 1900 || year > 2100) return errorText

            val inputFormat = SimpleDateFormat(INPUT_FORMAT_FULL, Locale.getDefault()).apply {
                isLenient = false
                timeZone = TimeZone.getTimeZone("UTC")
            }
            val outputFormat = SimpleDateFormat(OUTPUT_FORMAT, Locale.getDefault())

            val date = inputFormat.parse(isoString.substring(0, 19))
            outputFormat.format(date)

        } catch (e: Exception) {
            try {
                val inputFormat = SimpleDateFormat(INPUT_FORMAT_DATE, Locale.getDefault()).apply {
                    isLenient = false
                }
                val outputFormat = SimpleDateFormat(OUTPUT_FORMAT, Locale.getDefault())
                val date = inputFormat.parse(isoString.substring(0, 10))
                outputFormat.format(date)
            } catch (_: Exception) {
                errorText
            }
        }
    }
}