package com.haitao.haimei.util

import android.icu.util.ChineseCalendar
import android.icu.util.TimeZone
import android.os.Build
import androidx.annotation.RequiresApi
import java.util.Locale

object LunarUtils {
    private val lunarMonths = arrayOf(
        "正月", "二月", "三月", "四月", "五月", "六月",
        "七月", "八月", "九月", "十月", "冬月", "腊月"
    )

    private val lunarDays = arrayOf(
        "初一", "初二", "初三", "初四", "初五", "初六", "初七", "初八", "初九", "初十",
        "十一", "十二", "十三", "十四", "十五", "十六", "十七", "十八", "十九", "二十",
        "廿一", "廿二", "廿三", "廿四", "廿五", "廿六", "廿七", "廿八", "廿九", "三十"
    )

    data class LunarDate(
        val monthIndex: Int,
        val day: Int,
        val isLeapMonth: Boolean
    )

    private val chinaTimeZone = TimeZone.getTimeZone("Asia/Shanghai")
    @RequiresApi(Build.VERSION_CODES.O)
    private val chinaZoneId = java.time.ZoneId.of("Asia/Shanghai")

    fun lunarDateFromMillis(millis: Long): LunarDate {
        val calendar = ChineseCalendar(Locale.CHINA)
        calendar.timeZone = chinaTimeZone
        calendar.timeInMillis = millis
        val monthIndex = calendar.get(ChineseCalendar.MONTH)
        val day = calendar.get(ChineseCalendar.DAY_OF_MONTH)
        val isLeap = calendar.get(ChineseCalendar.IS_LEAP_MONTH) == 1
        return LunarDate(monthIndex, day, isLeap)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun lunarDateFromLocalDate(date: java.time.LocalDate): LunarDate {
        val instant = date.atTime(12, 0).atZone(chinaZoneId).toInstant()
        return lunarDateFromMillis(instant.toEpochMilli())
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun lunarDateText(millis: Long): String {
        val calendar = java.time.Instant.ofEpochMilli(millis)
            .atZone(chinaZoneId)
            .toLocalDate()
        val lunar = lunarDateFromLocalDate(calendar)
        return lunarDateText(lunar)
    }

    fun lunarDateText(lunar: LunarDate): String {
        val monthText = (if (lunar.isLeapMonth) "闰" else "") + lunarMonths.getOrElse(lunar.monthIndex) { "" }
        val dayText = lunarDays.getOrElse(lunar.day - 1) { "" }
        return "$monthText$dayText"
    }

    fun nextLunarBirthdayMillis(lunar: LunarDate, nowMillis: Long): Long {
        val nowCalendar = ChineseCalendar(Locale.CHINA)
        nowCalendar.timeZone = chinaTimeZone
        nowCalendar.timeInMillis = nowMillis
        val currentLunarYear = nowCalendar.get(ChineseCalendar.EXTENDED_YEAR)

        val candidate = findLunarDateMillis(lunar, currentLunarYear, nowMillis)
        if (candidate > nowMillis) return candidate

        return findLunarDateMillis(lunar, currentLunarYear + 1, nowMillis)
    }

    private fun findLunarDateMillis(lunar: LunarDate, lunarYear: Int, nowMillis: Long): Long {
        var year = lunarYear
        repeat(3) {
            val cal = ChineseCalendar(Locale.CHINA)
            cal.timeZone = chinaTimeZone
            cal.clear()
            cal.set(ChineseCalendar.EXTENDED_YEAR, year)
            cal.set(ChineseCalendar.MONTH, lunar.monthIndex)
            cal.set(ChineseCalendar.DAY_OF_MONTH, lunar.day)
            cal.set(ChineseCalendar.IS_LEAP_MONTH, if (lunar.isLeapMonth) 1 else 0)
            val candidateMillis = cal.timeInMillis

            val check = ChineseCalendar(Locale.CHINA)
            check.timeZone = chinaTimeZone
            check.timeInMillis = candidateMillis
            val matches = check.get(ChineseCalendar.MONTH) == lunar.monthIndex &&
                check.get(ChineseCalendar.DAY_OF_MONTH) == lunar.day &&
                (check.get(ChineseCalendar.IS_LEAP_MONTH) == 1) == lunar.isLeapMonth

            if (matches) return candidateMillis
            year += 1
        }

        return nowMillis
    }
}
